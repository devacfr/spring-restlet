package com.pmi.restlet.gadgets.dashboard.internal;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.internal.impl.StateConverterImpl;

/**
 * Converts stored states into usable objects
 */
@ImplementedBy(StateConverterImpl.class)
public interface IStateConverter {

    /**
     * Creates a {@code Dashboard} from a {@code DashboardState}
     * @param state the state to convert
     * @param gadgetRequestContext the context of this request
     * @return the created {@code Dashboard}
     */
    IDashboard convertStateToDashboard(DashboardState state, GadgetRequestContext gadgetRequestContext);

    /**
     * Creates a {@code Gadget} from a {@code GadgetState}
     * @param state the state to convert
     * @param gadgetRequestContext the context of this request
     * @return the created {@code Gadget}
     */
    IGadget convertStateToGadget(GadgetState state, GadgetRequestContext gadgetRequestContext);
}
