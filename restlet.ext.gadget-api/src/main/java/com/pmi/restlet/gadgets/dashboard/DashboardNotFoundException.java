package com.pmi.restlet.gadgets.dashboard;

public class DashboardNotFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -1674419665277136998L;

    public DashboardNotFoundException(DashboardId id) {
        dashboardId = id;
    }

    public DashboardId getDashboardId() {
        return dashboardId;
    }

    private final DashboardId dashboardId;
}