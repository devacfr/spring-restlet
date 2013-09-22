package com.pmi.restlet.gadgets.representations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cfr.commons.util.Assert;

import com.pmi.restlet.gadgets.dashboard.Layout;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;

public final class DashboardRepresentation {

	public static class Builder {

		public Builder writable(boolean writable) {
			this.writable = writable;
			return this;
		}

		public Builder gadgets(List gadgets) {
			Assert.notNull(gadgets, "gadgets");
			this.gadgets = new ArrayList(gadgets);
			return this;
		}

		public DashboardRepresentation build() {
			return new DashboardRepresentation(this);
		}

		private final String id;

		private final String title;

		private final Layout layout;

		private List gadgets;

		private boolean writable;

		public Builder(IDashboard dashboard) {
			gadgets = Collections.emptyList();
			writable = false;
			id = dashboard.getId().toString();
			title = dashboard.getTitle();
			layout = dashboard.getLayout();
		}
	}

	private final String id;

	private final String title;

	private final boolean writable;

	private final Layout layout;

	private final List gadgets;

	private DashboardRepresentation() {
		id = null;
		title = null;
		layout = null;
		gadgets = new ArrayList();
		writable = false;
	}

	private DashboardRepresentation(Builder builder) {
		id = builder.id;
		title = builder.title;
		layout = builder.layout;
		gadgets = builder.gadgets;
		writable = builder.writable;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Layout getLayout() {
		return layout;
	}

	public List getGadgets() {
		return gadgets;
	}

	public boolean isWritable() {
		return writable;
	}

}
