package com.pmi.restlet.gadgets.dashboard.internal;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardNotFoundException;
import com.pmi.restlet.gadgets.dashboard.internal.impl.DashboardRepositoryImpl;

@ImplementedBy(DashboardRepositoryImpl.class)
public interface IDashboardRepository {

    IDashboard get(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext) throws DashboardNotFoundException;

    void save(IDashboard dashboard);

    DashboardId findDashboardByGadgetId(GadgetId gadgetId) throws DashboardNotFoundException;
}
