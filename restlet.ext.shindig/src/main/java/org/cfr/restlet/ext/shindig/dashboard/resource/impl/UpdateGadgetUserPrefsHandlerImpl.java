package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.shindig.dashboard.resource.IUpdateGadgetUserPrefsHandler;
import org.restlet.Response;
import org.restlet.data.Form;
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
 * Default implementation that updates the prefs in a live gadget.
 */
@Singleton
public class UpdateGadgetUserPrefsHandlerImpl implements IUpdateGadgetUserPrefsHandler {

	private final Log log = LogFactory.getLog(UpdateGadgetUserPrefsHandlerImpl.class);

	private static final String USER_PREF_PREFIX = "up_";

	private final IDashboardRepository repository;

	private final I18nResolver i18n;

	/**
	 * Constructor.
	 * 
	 * @param repository
	 *            the {@code DashboardRepository} for getting/saving dashboards
	 * @param i18n
	 *            the {@code I18nResolver} to use for looking up i18n messages
	 */
	@Inject
	public UpdateGadgetUserPrefsHandlerImpl(IDashboardRepository repository, I18nResolver i18n) {
		this.repository = repository;
		this.i18n = i18n;
	}

	@Override
	public void updateUserPrefs(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, GadgetId gadgetId,
			Form queryParams, Response response) {
		IDashboard dashboard = repository.get(dashboardId, gadgetRequestContext);

		try {
			dashboard.updateGadgetUserPrefs(gadgetId, adaptParameterMapToUserPrefValues(queryParams));
			repository.save(dashboard);
		} catch (IllegalArgumentException iae) {
			// thrown if a required pref was set to an illegal value
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, i18n.getText("invalid.value.for.required.pref"));
		} catch (InconsistentDashboardStateException idse) {
			log.error("UpdateGadgetUserPrefsHandlerImpl: Unexpected error occurred: ", idse);
			response.setStatus(Status.CLIENT_ERROR_CONFLICT, i18n.getText("error.please.reload"));
		}
	}

	private Map<String, String> adaptParameterMapToUserPrefValues(Form params) {
		Map<String, String> values = new HashMap<String, String>();
		for (String param : params.getNames()) {
			if (param.startsWith(USER_PREF_PREFIX) && params.getFirst(param) != null) {
				values.put(param.substring(USER_PREF_PREFIX.length()), params.getFirstValue(param));
			}
		}
		return values;
	}
}
