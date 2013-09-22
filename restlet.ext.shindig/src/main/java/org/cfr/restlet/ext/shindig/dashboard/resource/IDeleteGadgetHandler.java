package org.cfr.restlet.ext.shindig.dashboard.resource;

import org.cfr.restlet.ext.shindig.dashboard.resource.impl.DeleteGadgetHandlerImpl;
import org.restlet.Response;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;

/**
 * Interface for removing a gadget from a live dashboard.
 */
@ImplementedBy(DeleteGadgetHandlerImpl.class)
public interface IDeleteGadgetHandler {

    /**
     * Removes the specified gadget from the specified dashboard.
     * @param dashboardId the dashboard hosting the gadget
     * @param gadgetRequestContext the context of this request
     * @param gadgetId the gadget to remove
     * @return a {@code Response} for the client with details on success or
     * failure
     */
    void deleteGadget(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, GadgetId gadgetId, Response response);
}
