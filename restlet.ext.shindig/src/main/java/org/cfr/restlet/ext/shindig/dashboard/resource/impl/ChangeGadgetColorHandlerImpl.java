package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.shindig.dashboard.resource.IChangeGadgetColorHandler;
import org.restlet.Response;
import org.restlet.data.Status;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.Color;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.internal.InconsistentDashboardStateException;

/**
 * Default implementation that changes the color of a live gadget on a
 * dashboard.
 */
@Singleton
public class ChangeGadgetColorHandlerImpl implements IChangeGadgetColorHandler {

	private final Log log = LogFactory.getLog(ChangeGadgetColorHandlerImpl.class);

	private final IDashboardRepository repository;

	private final I18nResolver i18n;

	/**
	 * Constructor.
	 * 
	 * @param repository
	 *            the repository to retrieve dashboards
	 * @param i18n
	 *            the lookup to resolve i18n messages
	 */
	@Inject
	public ChangeGadgetColorHandlerImpl(IDashboardRepository repository, I18nResolver i18n) {
		this.repository = repository;
		this.i18n = i18n;
	}

	@Override
	public void setGadgetColor(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, GadgetId gadgetId,
			Color color, Response response) {
		IDashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
		dashboard.changeGadgetColor(gadgetId, color);
		try {
			repository.save(dashboard);
		} catch (InconsistentDashboardStateException idse) {
			log.error("ChangeGadgetColorHandlerImpl: Unexpected error occurred: ", idse);
			response.setStatus(Status.CLIENT_ERROR_CONFLICT, i18n.getText("error.please.reload"));
		}
	}
}
