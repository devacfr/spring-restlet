package com.pmi.restlet.gadgets.dashboard.spi;

import java.util.Collections;
import java.util.List;

import com.pmi.restlet.gadgets.GadgetId;

public final class GadgetLayout {

    private final List<Iterable<GadgetId>> columnLayout;

    public GadgetLayout(List<Iterable<GadgetId>> columnLayout) {
        this.columnLayout = Collections.unmodifiableList(columnLayout);
    }

    public int getNumberOfColumns() {
        return columnLayout.size();
    }

    public Iterable<GadgetId> getGadgetsInColumn(int column) {
        return columnLayout.get(column);
    }

}
