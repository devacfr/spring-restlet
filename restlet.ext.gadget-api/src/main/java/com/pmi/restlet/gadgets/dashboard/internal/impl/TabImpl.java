package com.pmi.restlet.gadgets.dashboard.internal.impl;

import java.net.URI;

import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardTab;
import com.pmi.restlet.gadgets.dashboard.internal.ITab;

public class TabImpl implements ITab {

    private final DashboardTab tab;

    private final boolean writable;

    public TabImpl(DashboardTab tab, boolean writable) {
        this.tab = tab;
        this.writable = writable;
    }

    public DashboardId getDashboardId() {
        return tab.getDashboardId();
    }

    public String getTitle() {
        return tab.getTitle();
    }

    public URI getTabUri() {
        return tab.getTabUri();
    }

    public boolean isWritable() {
        return writable;
    }
}
