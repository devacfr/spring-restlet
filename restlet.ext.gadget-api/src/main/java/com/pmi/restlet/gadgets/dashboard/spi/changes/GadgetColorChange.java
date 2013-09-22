package com.pmi.restlet.gadgets.dashboard.spi.changes;

import org.cfr.commons.util.Assert;

import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.dashboard.Color;

public final class GadgetColorChange implements IDashboardChange {

	private final GadgetId gadgetId;

	private final Color color;

	public GadgetColorChange(GadgetId gadgetId, Color color) {
		this.gadgetId = Assert.notNull(gadgetId);
		this.color = Assert.notNull(color);
	}

	@Override
	public void accept(IDashboardChange.Visitor visitor) {
		visitor.visit(this);
	}

	public Color getColor() {
		return color;
	}

	public GadgetId getGadgetId() {
		return gadgetId;
	}

}