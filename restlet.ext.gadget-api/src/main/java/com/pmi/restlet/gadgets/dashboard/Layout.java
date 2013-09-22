package com.pmi.restlet.gadgets.dashboard;

import com.pmi.restlet.gadgets.dashboard.DashboardState.ColumnIndex;

public enum Layout {

    A(ColumnSpec.FAIR),
    AA(ColumnSpec.FAIR, ColumnSpec.FAIR),
    AB(ColumnSpec.FAIR, ColumnSpec.GREEDY),
    BA(ColumnSpec.GREEDY, ColumnSpec.FAIR),
    AAA(ColumnSpec.FAIR, ColumnSpec.FAIR, ColumnSpec.FAIR),
    ABA(ColumnSpec.FAIR, ColumnSpec.GREEDY, ColumnSpec.FAIR);

    static enum ColumnSpec {
        GREEDY,
        FAIR
    }

    private final ColumnSpec columnSpec[];

    private Layout(ColumnSpec... columnSpec) {
        this.columnSpec = columnSpec;
    }

    public int getNumberOfColumns() {
        return columnSpec.length;
    }

    public boolean contains(DashboardState.ColumnIndex column) {
        return column.index() < columnSpec.length;
    }

    public Iterable<ColumnIndex> getColumnRange() {
        return DashboardState.ColumnIndex.range(DashboardState.ColumnIndex.ZERO, DashboardState.ColumnIndex.from(columnSpec.length - 1));
    }

    public boolean isColumnSizingFair(DashboardState.ColumnIndex column) {
        if (!contains(column))
            throw new IllegalArgumentException((new StringBuilder()).append("Column ").append(column).append(" does not exist in this layout")
                    .toString());
        else
            return columnSpec[column.index()] == ColumnSpec.FAIR;
    }

}