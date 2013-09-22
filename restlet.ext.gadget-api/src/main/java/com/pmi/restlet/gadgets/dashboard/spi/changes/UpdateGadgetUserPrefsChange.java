package com.pmi.restlet.gadgets.dashboard.spi.changes;

import java.util.Map;

import com.pmi.restlet.gadgets.GadgetId;

public final class UpdateGadgetUserPrefsChange implements IDashboardChange {

    private final GadgetId gadgetId;

    private final Map prefValues;

    public UpdateGadgetUserPrefsChange(GadgetId gadgetId, Map prefValues) {
        this.gadgetId = gadgetId;
        this.prefValues = prefValues;
    }

    public void accept(IDashboardChange.Visitor visitor) {
        visitor.visit(this);
    }

    public GadgetId getGadgetId() {
        return gadgetId;
    }

    public Map getPrefValues() {
        return prefValues;
    }

}