package com.pmi.restlet.gadgets.dashboard.internal.impl;

import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;
import com.pmi.restlet.gadgets.dashboard.internal.IGadgetFactory;
import com.pmi.restlet.gadgets.dashboard.internal.IStateConverter;

public class StateConverterImpl implements IStateConverter {

    private final IGadgetFactory gadgetFactory;

    public StateConverterImpl(IGadgetFactory gadgetFactory) {
        this.gadgetFactory = gadgetFactory;
    }

    public IDashboard convertStateToDashboard(DashboardState state, GadgetRequestContext gadgetRequestContext) {
        return new DashboardImpl(state, this, gadgetRequestContext);
    }

    public IGadget convertStateToGadget(GadgetState state, GadgetRequestContext gadgetRequestContext) {
        return gadgetFactory.createGadget(state, gadgetRequestContext);
    }
}
