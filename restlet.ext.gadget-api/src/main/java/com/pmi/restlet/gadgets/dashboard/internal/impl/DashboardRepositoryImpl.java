package com.pmi.restlet.gadgets.dashboard.internal.impl;

import static com.google.common.base.Objects.equal;

import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardNotFoundException;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.internal.IStateConverter;
import com.pmi.restlet.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardStateStore;

/**
 * Default implementation of {@code DashboardRepository} which delegates
 * retrieval and save operations to a {@code DashboardStateStore}.
 */
public class DashboardRepositoryImpl implements IDashboardRepository {

    private final IDashboardStateStore stateStore;

    private final IStateConverter converter;

    public DashboardRepositoryImpl(IDashboardStateStore stateStore, IStateConverter converter) {
        this.stateStore = stateStore;
        this.converter = converter;
    }

    public IDashboard get(DashboardId dashboardId, GadgetRequestContext gadgetRequestContext) throws DashboardNotFoundException {
        return converter.convertStateToDashboard(stateStore.retrieve(dashboardId), gadgetRequestContext);
    }

    public void save(IDashboard dashboard) {
        DashboardState state = dashboard.getState();
        if (!equal(stateStore.update(state, dashboard.getChanges()), state)) {
            throw new InconsistentDashboardStateException("Dashboard state after store does not match state provided to store");
        } else {
            dashboard.clearChanges();
            return;
        }
    }

    public DashboardId findDashboardByGadgetId(GadgetId gadgetId) throws DashboardNotFoundException {
        return stateStore.findDashboardWithGadget(gadgetId).getId();
    }

}
