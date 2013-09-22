package org.cfr.restlet.ext.shindig.dashboard.resource;

import org.cfr.restlet.ext.shindig.dashboard.resource.impl.ChangeGadgetColorHandlerImpl;
import org.restlet.Response;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.Color;
import com.pmi.restlet.gadgets.dashboard.DashboardId;

/**
 * Interface for changing the color of a gadget on a dashboard.
 */
@ImplementedBy(ChangeGadgetColorHandlerImpl.class)
public interface IChangeGadgetColorHandler {

    /**
     * Sets the color of the specified gadget to the specified color.
     * @param dashboardId the dashboard hosting the gadget
     * @param gadgetRequestContext the context of this request
     * @param gadgetId the ID of the gadget to change
     * @param color the new color of the gadget
     * @return a {@code Response} for the client with details on the success
     * or failure
     */
    void setGadgetColor(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, GadgetId gadgetId, Color color, Response response);
}
