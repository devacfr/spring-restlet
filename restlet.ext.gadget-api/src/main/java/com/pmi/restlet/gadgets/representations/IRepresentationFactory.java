package com.pmi.restlet.gadgets.representations;

import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardState.ColumnIndex;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;

public interface IRepresentationFactory {

    DashboardRepresentation createDashboardRepresentation(IDashboard dashboard, GadgetRequestContext gadgetrequestcontext, boolean flag);

    GadgetRepresentation createGadgetRepresentation(DashboardId dashboardid, IGadget gadget, GadgetRequestContext gadgetrequestcontext, boolean flag,
            ColumnIndex columnindex);
}