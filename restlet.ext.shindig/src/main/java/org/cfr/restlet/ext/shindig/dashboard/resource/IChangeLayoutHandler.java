package org.cfr.restlet.ext.shindig.dashboard.resource;

import org.cfr.restlet.ext.shindig.dashboard.resource.impl.ChangeLayoutHandlerImpl;
import org.restlet.Response;
import org.restlet.data.Form;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;

/**
 * Interface for changing the layout of a dashboard.
 */
@ImplementedBy(ChangeLayoutHandlerImpl.class)
public interface IChangeLayoutHandler {

    /**
     * Attempts to change the layout of the specified dashboard.
     * @param dashboardId the ID of the dashboard to change
     * @param gadgetRequestContext the context of this request
     * @param queryParams parameters sent along with the request  @return a {@code Response} for the client with details on the success
     * or failure
     */
    void changeLayout(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext, Response response, Form queryParams);
}
