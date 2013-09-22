package com.pmi.restlet.gadgets.dashboard;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.dashboard.internal.impl.DashboardServiceImpl;

@ImplementedBy(DashboardServiceImpl.class)
public interface IDashboardService {

    public abstract DashboardState get(DashboardId dashboardid, String s) throws PermissionException;

    public abstract DashboardState save(DashboardState dashboardstate, String s) throws PermissionException;
}