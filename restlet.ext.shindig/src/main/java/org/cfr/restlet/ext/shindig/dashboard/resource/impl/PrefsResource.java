package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import java.util.Map;

import org.cfr.restlet.ext.shindig.dashboard.resource.IUpdateGadgetUserPrefsHandler;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.IGadgetRequestContextFactory;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardPermissionService;
import com.pmi.restlet.security.AnonymousAllowed;

@Resource(path = "/{dashboardId}/gadget/{gadgetId}/prefs", strict = true)
public class PrefsResource extends InjectedResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private IDashboardPermissionService permissionService;

    private IUpdateGadgetUserPrefsHandler updateGadgetUserPrefsHandler;

    private IGadgetRequestContextFactory gadgetRequestContextFactory;

    @Inject
    public void setGadgetRequestContextFactory(IGadgetRequestContextFactory gadgetRequestContextFactory) {
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
    }

    @Inject
    public void setUpdateGadgetUserPrefsHandler(IUpdateGadgetUserPrefsHandler updateGadgetUserPrefsHandler) {
        this.updateGadgetUserPrefsHandler = updateGadgetUserPrefsHandler;
    }

    @Inject
    public void setPermissionService(IDashboardPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * <p>Forwards POST requests (coming from Ajax or web browsers) to the PUT
     * handler for user pref changes.<p>
     * @param method the HTTP method to forward to (must be "put")
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget to update the prefs for
     * @param request the request object (used for providing the locale)
     * @param formParams the container for the form parameters
     * @return a {@code Response} with details on the request's success or
     * failure
     */
    @Post()
    @AnonymousAllowed
    public void updateUserPrefsViaPOST() {
        Request request = getRequest();
        Map<String, Object> attrs = getRequestAttributes();
        DashboardId dashboardId = DashboardId.valueOf(attrs.get("dashboardId"));
        GadgetId gadgetId = GadgetId.valueOf(attrs.get("gadgetId"));
        Form form = request.getEntityAsForm();
        String method = form.getFirstValue("method");
        if (method.equals("put")) {
            updateUserPrefs(dashboardId, gadgetId, form);
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }
    }

    /**
     * Updates the user prefs of the specified gadget.
     * @param dashboardId ID of the dashboard hosting the gadget
     * @param gadgetId ID of the gadget to update the prefs for
     * @param request the request object (used for providing the locale)
     * @param uriInfo provides the query parameters for the user pref
     * values
     * @return a {@code Response} with details on the request's success or
     * failure
     */
    @Put
    @AnonymousAllowed
    public void updateUserPrefsViaPUT() {
        Map<String, Object> attrs = getRequestAttributes();
        DashboardId dashboardId = DashboardId.valueOf(attrs.get("dashboardId"));
        GadgetId gadgetId = GadgetId.valueOf(attrs.get("gadgetId"));
        updateUserPrefs(dashboardId, gadgetId, getQuery());
    }

    private void updateUserPrefs(DashboardId dashboardId, GadgetId gadgetId, Form params) {
        Request request = getRequest();
        Response response = getResponse();
        final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

        if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer())) {
            log.warn("GadgetResource: prevented gadget prefs change due to insufficient permission");
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return;
        }
        log.debug("GadgetResource: update /prefs: dashboardId=" + dashboardId + " gadgetId=" + gadgetId);

        updateGadgetUserPrefsHandler.updateUserPrefs(dashboardId, requestContext, gadgetId, params, response);
    }
}
