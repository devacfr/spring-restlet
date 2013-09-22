package com.pmi.restlet.gadgets.dashboard.spi;

import com.pmi.restlet.gadgets.dashboard.DashboardId;

public interface IDashboardPermissionService {

    boolean isReadableBy(DashboardId dashboardid, String view);

    boolean isWritableBy(DashboardId dashboardid, String view);
}