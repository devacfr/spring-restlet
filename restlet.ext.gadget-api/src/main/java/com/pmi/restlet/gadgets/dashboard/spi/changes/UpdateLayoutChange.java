package com.pmi.restlet.gadgets.dashboard.spi.changes;

import com.pmi.restlet.gadgets.dashboard.Layout;
import com.pmi.restlet.gadgets.dashboard.spi.GadgetLayout;

public final class UpdateLayoutChange implements IDashboardChange {

    private final Layout layout;

    private final GadgetLayout gadgetLayout;

    public UpdateLayoutChange(Layout layout, GadgetLayout gadgetLayout) {
        this.layout = layout;
        this.gadgetLayout = gadgetLayout;
    }

    public void accept(IDashboardChange.Visitor visitor) {
        visitor.visit(this);
    }

    public Layout getLayout() {
        return layout;
    }

    public GadgetLayout getGadgetLayout() {
        return gadgetLayout;
    }

}