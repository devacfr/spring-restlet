package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import org.cfr.restlet.ext.shindig.dashboard.resource.IChangeLayoutHandler;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.IGadgetRequestContextFactory;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardPermissionService;
import com.pmi.restlet.security.AnonymousAllowed;

@Resource(path = "/{dashboardId}/layout", strict = true)
public class LayoutResource extends InjectedResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private IChangeLayoutHandler changeLayoutHandler;

    private IDashboardPermissionService permissionService;

    private IGadgetRequestContextFactory gadgetRequestContextFactory;

    @Inject
    public void setChangeLayoutHandler(IChangeLayoutHandler changeLayoutHandler) {
        this.changeLayoutHandler = changeLayoutHandler;
    }

    @Inject
    public void setPermissionService(IDashboardPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Inject
    public void setGadgetRequestContextFactory(IGadgetRequestContextFactory gadgetRequestContextFactory) {
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
    }

    /**
     * Changes the existing layout of the specified dashboard in response to a
     * POST request.
     * @param dashboardId ID of the dashboard on which to change the
     * layout
     * @param request the {@code HttpServletRequest} that was routed here
     * @param formParams the query parameters passed in by the form
     * @return a {@code Response} for the client with details on the request's
     * success or failure
     */
    @Post
    @AnonymousAllowed
    public void changeLayoutViaPOST() {
        Form param = getRequest().getResourceRef().getQueryAsForm();
        Request request = getRequest();
        DashboardId dashboardId = DashboardId.valueOf(request.getAttributes().get("dashboardId"));
        changeLayout(dashboardId, param);
    }

    /**
     * Changes the existing layout of the specified dashboard in response to a
     * PUT request.
     * @param dashboardId ID of the dashboard on which to change the
     * layout
     * @param uriInfo provides the query parameters indicating the new layout
     * @param request the {@code HttpServletRequest} that was routed here
     * @return a {@code Response} for the client with details on the request's
     * success or failure
     */
    @Put
    @AnonymousAllowed
    public void changeLayoutViaPUT() {
        Form param = getRequest().getResourceRef().getQueryAsForm();
        Request request = getRequest();
        DashboardId dashboardId = DashboardId.valueOf(request.getAttributes().get("dashboardId"));
        changeLayout(dashboardId, param);
    }

    private void changeLayout(DashboardId dashboardId, Form queryParams) {
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(getRequest());
        log.debug("DashboardResource: changeLayout: dashboardId=" + dashboardId + " viewer=" + requestContext.getViewer());

        if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer())) {
            log.warn("DashboardResource: changeLayout: prevented layout change due to insufficient permission");
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return;
        }

        changeLayoutHandler.changeLayout(dashboardId, requestContext, getResponse(), queryParams);
    }
}
