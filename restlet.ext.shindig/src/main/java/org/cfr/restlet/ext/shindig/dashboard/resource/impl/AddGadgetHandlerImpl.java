package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import java.util.ArrayList;
import java.util.List;

import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.commons.sal.transaction.ITransactionCallback;
import org.cfr.commons.sal.transaction.ITransactionTemplate;
import org.cfr.restlet.ext.shindig.dashboard.resource.IAddGadgetHandler;
import org.restlet.Response;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.IGadgetSpecUrlChecker;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.DashboardState.ColumnIndex;
import com.pmi.restlet.gadgets.dashboard.Layout;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;
import com.pmi.restlet.gadgets.dashboard.internal.IGadgetFactory;
import com.pmi.restlet.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.pmi.restlet.gadgets.dashboard.spi.GadgetLayout;
import com.pmi.restlet.gadgets.representations.GadgetRepresentation;
import com.pmi.restlet.gadgets.representations.IRepresentationFactory;

/**
 * Default implementation that adds a gadget to a live dashboard.
 */
@Singleton
public class AddGadgetHandlerImpl implements IAddGadgetHandler {

	private final Logger log = LoggerFactory.getLogger(AddGadgetHandlerImpl.class);

	private final IGadgetSpecUrlChecker gadgetUrlChecker;

	private final IGadgetFactory gadgetFactory;

	private final IDashboardRepository repository;

	private final IRepresentationFactory representationFactory;

	private final I18nResolver i18n;

	private final ITransactionTemplate<GadgetRepresentation> txTemplate;

	/**
	 * Constructor.
	 * 
	 * @param gadgetUrlChecker
	 *            checks that the gadget can be added to a dashboard
	 * @param gadgetFactory
	 *            used to create the gadget domain object
	 * @param repository
	 *            persists dashboard changes
	 * @param representationFactory
	 *            Factory used to create the JAXB representation of a gadget
	 * @param i18n
	 *            the SAL {@code I18nResolver} to use
	 * @param txTemplate
	 *            transaction template needed for the 'move' operation
	 */
	@Inject
	public AddGadgetHandlerImpl(IGadgetSpecUrlChecker gadgetUrlChecker, IGadgetFactory gadgetFactory,
			IDashboardRepository repository, IRepresentationFactory representationFactory, I18nResolver i18n,
			ITransactionTemplate<GadgetRepresentation> txTemplate) {
		this.gadgetUrlChecker = gadgetUrlChecker;
		this.gadgetFactory = gadgetFactory;
		this.repository = repository;
		this.representationFactory = representationFactory;
		this.i18n = i18n;
		this.txTemplate = txTemplate;
	}

	@Override
	public GadgetRepresentation addGadget(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext,
			String gadgetUrl, Response response) {
		return addGadget(dashboardId, gadgetRequestContext, gadgetUrl, DashboardState.ColumnIndex.ZERO, response);
	}

	@Override
	public GadgetRepresentation addGadget(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext,
			String gadgetUrl, DashboardState.ColumnIndex columnIndex, Response response) {
		gadgetUrlChecker.assertRenderable(gadgetUrl);

		IGadget gadget = gadgetFactory.createGadget(gadgetUrl, gadgetRequestContext);
		IDashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
		dashboard.addGadget(columnIndex, gadget);
		repository.save(dashboard);

		// Assume that writable=true since we've just added the gadget to the
		// dashboard
		final GadgetRepresentation representation = representationFactory.createGadgetRepresentation(dashboard.getId(),
				gadget, gadgetRequestContext, true, columnIndex);
		response.setLocationRef(representation.getGadgetSpecUrl());
		return representation;

	}

	@Override
	public GadgetRepresentation moveGadget(final DashboardId targetDashboardId, final GadgetId gadgetId,
			final DashboardId sourceDashboardId, final ColumnIndex columnIndex, final int rowIndex,
			final GadgetRequestContext gadgetRequestContext, final Response response) {
		try {
			return txTemplate.execute(new ITransactionCallback<GadgetRepresentation>() {

				public GadgetRepresentation doInTransaction() {
					final IDashboard targetDashboard = repository.get(targetDashboardId, gadgetRequestContext);
					final IDashboard sourceDashboard = repository.get(sourceDashboardId, gadgetRequestContext);
					final IGadget gadget = sourceDashboard.findGadget(gadgetId);
					if (gadget == null) {
						throw new InconsistentDashboardStateException("Gadget not found with id '" + gadgetId
								+ "' in dashboard with id '" + sourceDashboardId + "'.");
					}

					// If the source and target dashboards are the same, return
					// 204 (no content)
					if (sourceDashboardId.equals(targetDashboardId)) {
						return null;
					}
					// first remove the old gadget
					sourceDashboard.removeGadget(gadgetId);

					// then add the gadget to the new dashboard
					final GadgetLayout layout = createNewGadgetLayout(targetDashboard, gadgetId, columnIndex, rowIndex);
					targetDashboard.addGadget(gadget);
					targetDashboard.rearrangeGadgets(layout);

					repository.save(sourceDashboard);
					repository.save(targetDashboard);

					// Assume that writable=true since we've just added the
					// gadget to the dashboard
					final GadgetRepresentation representation = representationFactory.createGadgetRepresentation(
							targetDashboardId, gadget, gadgetRequestContext, true, columnIndex);
					response.setLocationRef(representation.getGadgetSpecUrl());
					return representation;
				}
			});
		} catch (InconsistentDashboardStateException idse) {
			log.error("AddGadgetHandlerImpl: Unexpected error occurred", idse);
			response.setStatus(Status.CLIENT_ERROR_CONFLICT, i18n.getText("error.please.reload"));
		}
		return null;
	}

	/**
	 * Given a dashboard, this method will create a {@code GadgetLayout}
	 * instance with the new gadget inserted in the position provided.
	 */
	private GadgetLayout createNewGadgetLayout(IDashboard dashboard, GadgetId movedGadgetId, ColumnIndex column, int row) {
		final Layout layout = dashboard.getLayout();
		final List<Iterable<GadgetId>> columns = new ArrayList<Iterable<GadgetId>>(layout.getNumberOfColumns());
		for (DashboardState.ColumnIndex dashboardColumn : layout.getColumnRange()) {
			// first add all the existing gadgets.
			final List<GadgetId> gadgets = new ArrayList<GadgetId>();
			for (IGadget gadget : dashboard.getGadgetsInColumn(dashboardColumn)) {
				gadgets.add(gadget.getId());
			}

			// then insert the moved gadget at the right position in the right
			// column.
			if (dashboardColumn.equals(column)) {
				if (row < gadgets.size()) {
					gadgets.add(row, movedGadgetId);
				} else {
					gadgets.add(movedGadgetId);
				}
			}
			columns.add(gadgets);
		}
		return new GadgetLayout(columns);
	}
}
