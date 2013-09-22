package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.shindig.dashboard.resource.IDeleteGadgetHandler;
import org.restlet.Response;
import org.restlet.data.Status;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.internal.InconsistentDashboardStateException;

/**
 * Default implementation that removes a gadget from a live dashboard.
 */
@Singleton
public class DeleteGadgetHandlerImpl implements IDeleteGadgetHandler {

	private final Log log = LogFactory.getLog(DeleteGadgetHandlerImpl.class);

	private final IDashboardRepository repository;

	private final I18nResolver i18n;

	/**
	 * Constructor.
	 * 
	 * @param repository
	 *            the {@code DashboardRepository} for getting/saving dashboards
	 * @param i18n
	 *            the {@code I18nResolver} implementation to use
	 */
	@Inject
	public DeleteGadgetHandlerImpl(IDashboardRepository repository, I18nResolver i18n) {
		this.repository = repository;
		this.i18n = i18n;
	}

	@Override
	public void deleteGadget(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, GadgetId gadgetId,
			Response response) {
		IDashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
		dashboard.removeGadget(gadgetId);

		try {
			repository.save(dashboard);
		} catch (InconsistentDashboardStateException idse) {
			log.error("DeleteGadgetHandlerImpl: Unexpected error occurred", idse);
			response.setStatus(Status.CLIENT_ERROR_CONFLICT, i18n.getText("error.please.reload"));
		}

	}
}
