package com.pmi.restlet.gadgets.dashboard.spi;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardNotFoundException;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.internal.impl.TransactionalDashboardStateStoreImpl;
import com.pmi.restlet.gadgets.dashboard.spi.changes.IDashboardChange;

/**
 * 
 * @author cfriedri
 *
 */
@ImplementedBy(TransactionalDashboardStateStoreImpl.class)
public interface IDashboardStateStore {

    /**
     * 
     * @param dashboardid
     * @return
     * @throws DashboardNotFoundException
     * @throws DashboardStateStoreException
     */
    DashboardState retrieve(DashboardId dashboardid) throws DashboardNotFoundException, DashboardStateStoreException;

    /**
     * 
     * @param dashboardstate
     * @param iterable
     * @return
     * @throws DashboardStateStoreException
     */
    DashboardState update(DashboardState dashboardstate, Iterable<IDashboardChange> iterable) throws DashboardStateStoreException;

    /**
     * 
     * @param dashboardid
     * @throws DashboardStateStoreException
     */
    void remove(DashboardId dashboardid) throws DashboardStateStoreException;

    /**
     * 
     * @param gadgetid
     * @return
     * @throws DashboardNotFoundException
     */
    DashboardState findDashboardWithGadget(GadgetId gadgetid) throws DashboardNotFoundException;
}