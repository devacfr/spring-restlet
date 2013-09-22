package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.IGadgetRequestContextFactory;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardPermissionService;
import com.pmi.restlet.security.AnonymousAllowed;

@Resource(path = "/{dashboardId}/numGadgets", strict = true)
public class NumberGadgetsResource extends InjectedResource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private IDashboardPermissionService permissionService;

    private IGadgetRequestContextFactory gadgetRequestContextFactory;

    private IDashboardRepository repository;

    @Inject
    public void setPermissionService(IDashboardPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Inject
    public void setGadgetRequestContextFactory(IGadgetRequestContextFactory gadgetRequestContextFactory) {
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
    }

    @Inject
    public void setRepository(IDashboardRepository repository) {
        this.repository = repository;
    }

    @Get("text")
    @AnonymousAllowed
    public int getNumGadgets() {
        Request request = getRequest();
        DashboardId dashboardId = DashboardId.valueOf(request.getAttributes().get("dashboardId"));
        if (log.isDebugEnabled())
            log.debug("DashboardResource: GET numGadgets received: " + dashboardId);
        final GadgetRequestContext gadgetRequestContext = gadgetRequestContextFactory.get(getRequest());
        if (!permissionService.isReadableBy(dashboardId, gadgetRequestContext.getViewer())) {
            log.warn("DashboardResource: GET numGadgets: prevented getting number of gadgets on dashboard due to insufficient permission");
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return 0;
        }
        final IDashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
        return dashboard.getNumberOfGadgets();
    }

}
