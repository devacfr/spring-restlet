package com.pmi.restlet.gadgets.dashboard.spi.changes;

import com.pmi.restlet.gadgets.GadgetId;

public final class RemoveGadgetChange implements IDashboardChange {

    private final GadgetId gadgetId;

    public RemoveGadgetChange(GadgetId gadgetId) {
        this.gadgetId = gadgetId;
    }

    public void accept(IDashboardChange.Visitor visitor) {
        visitor.visit(this);
    }

    public GadgetId getGadgetId() {
        return gadgetId;
    }

}