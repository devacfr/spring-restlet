package org.cfr.restlet.ext.shindig.dashboard.resource;

import org.cfr.restlet.ext.shindig.dashboard.resource.impl.UpdateGadgetUserPrefsHandlerImpl;
import org.restlet.Response;
import org.restlet.data.Form;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;

/**
 * Interface for updating the values of user prefs in an active gadget.
 */
@ImplementedBy(UpdateGadgetUserPrefsHandlerImpl.class)
public interface IUpdateGadgetUserPrefsHandler {

    /**
     * Changes the user prefs on the specified gadget and dashboard to the values
     * supplied by the query.
     * @param dashboardId the ID of the dashboard hosting the gadget
     * @param gadgetRequestContext the context of this request
     * @param gadgetId the gadget to change the prefs for
     * @param params the user prefs with their updated values
     */
    void updateUserPrefs(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, GadgetId gadgetId, Form params, Response response);
}
