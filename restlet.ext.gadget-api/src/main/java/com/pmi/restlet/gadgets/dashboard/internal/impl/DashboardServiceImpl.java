package com.pmi.restlet.gadgets.dashboard.internal.impl;

import com.google.common.collect.ImmutableList;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.IDashboardService;
import com.pmi.restlet.gadgets.dashboard.PermissionException;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardPermissionService;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardStateStore;
import com.pmi.restlet.gadgets.dashboard.spi.changes.IDashboardChange;

/**
 * Default implementation of {@code DashboardService}.
 */
public class DashboardServiceImpl implements IDashboardService {

    private final IDashboardStateStore stateStore;

    private final IDashboardPermissionService permissionService;

    public DashboardServiceImpl(IDashboardStateStore stateStore, IDashboardPermissionService permissionService) {
        this.stateStore = stateStore;
        this.permissionService = permissionService;
    }

    public DashboardState get(DashboardId id, String username) throws PermissionException {
        if (!permissionService.isReadableBy(id, username))
            throw new PermissionException();
        else
            return stateStore.retrieve(id);
    }

    public DashboardState save(DashboardState state, String username) throws PermissionException {
        if (!permissionService.isWritableBy(state.getId(), username))
            throw new PermissionException();
        else
            return stateStore.update(state, ImmutableList.<IDashboardChange> of());
    }
}
