package com.pmi.restlet.gadgets.dashboard.spi.changes;

import org.cfr.commons.util.Assert;

import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.DashboardState.ColumnIndex;

public final class AddGadgetChange implements IDashboardChange {

	private final GadgetState state;

	private final ColumnIndex columnIndex;

	private final int rowIndex;

	public AddGadgetChange(GadgetState state, ColumnIndex columnIndex, int rowIndex) {
		this.state = Assert.notNull(state);
		this.columnIndex = Assert.notNull(columnIndex);
		this.rowIndex = rowIndex;
	}

	@Override
	public void accept(IDashboardChange.Visitor visitor) {
		visitor.visit(this);
	}

	public GadgetState getState() {
		return state;
	}

	public ColumnIndex getColumnIndex() {
		return columnIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

}