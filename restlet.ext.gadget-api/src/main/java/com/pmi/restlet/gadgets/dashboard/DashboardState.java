package com.pmi.restlet.gadgets.dashboard;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.cfr.commons.util.Assert;
import org.cfr.commons.util.collection.CollectionUtil;

import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.util.Iterables;

public final class DashboardState implements Serializable {

	public enum ColumnIndex {

		ZERO(0), ONE(1), TWO(2);

		private final int index;

		private ColumnIndex(int index) {
			this.index = index;
		}

		public int index() {
			return index;
		}

		public boolean hasNext() {
			return this != TWO;
		}

		public ColumnIndex next() {
			if (!hasNext()) {
				throw new IllegalStateException("No next column index, already at the max");
			} else {
				return from(index + 1);
			}
		}

		public static ColumnIndex from(int index) {
			switch (index) {
			case 0: // '\0'
				return ZERO;

			case 1: // '\001'
				return ONE;

			case 2: // '\002'
				return TWO;
			}
			throw new IllegalArgumentException("Valid values for Column are 0-2");
		}

		public static Iterable<ColumnIndex> range(final ColumnIndex start, final ColumnIndex end) {
			return new Iterable<ColumnIndex>() {

				@Override
				public Iterator<ColumnIndex> iterator() {
					return new Iterator<ColumnIndex>() {

						@Override
						public boolean hasNext() {
							return nextIndex != null;
						}

						@Override
						public ColumnIndex next() {
							if (!hasNext()) {
								throw new NoSuchElementException();
							}
							ColumnIndex currentIndex = nextIndex;
							if (currentIndex.hasNext() && currentIndex != end) {
								nextIndex = currentIndex.next();
							} else {
								nextIndex = null;
							}
							return currentIndex;
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException("Cannot remove elements from this iterator");
						}

						private ColumnIndex nextIndex = start;

					};
				}
			};
		}

	}

	public static class Builder {

		public Builder layout(Layout layout) {
			Assert.notNull(layout, "layout is required");
			this.layout = layout;
			return this;
		}

		public Builder title(String title) {
			this.title = Assert.notNull(title, "title is required");
			return this;
		}

		public Builder columns(Iterable<? extends Iterable<GadgetState>> columns) {
			this.columns = Assert.notNull(columns, "columns is required");
			return this;
		}

		public Builder version(long version) {
			this.version = version;
			return this;
		}

		public DashboardState build() {
			return new DashboardState(this);
		}

		private final DashboardId id;

		private String title;

		private Layout layout;

		private Iterable<? extends Iterable<GadgetState>> columns;

		private long version;

		private Builder(DashboardId id, String title) {
			layout = Layout.AA;
			columns = Collections.emptyList();
			version = 0L;
			this.id = id;
			this.title = title;
		}

		private Builder(DashboardState state) {
			layout = Layout.AA;
			this.columns = Collections.emptyList();
			version = 0L;
			id = state.getId();
			title = state.getTitle();
			layout = state.getLayout();
			List<Iterable<GadgetState>> columns = new ArrayList<Iterable<GadgetState>>();
			ColumnIndex columnIndex;
			for (Iterator<ColumnIndex> i = layout.getColumnRange().iterator(); i.hasNext(); columns.add(state
					.getGadgetsInColumn(columnIndex))) {
				columnIndex = i.next();
			}

			this.columns = columns;
			version = state.getVersion();
		}

	}

	public static class TitleBuilder {

		public Builder title(String title) {
			Assert.notNull(title);
			return new Builder(id, title);
		}

		private final DashboardId id;

		private TitleBuilder(DashboardId id) {
			this.id = id;
		}

	}

	private static final long serialVersionUID = 4862870053224734927L;

	private final DashboardId id;

	private final String title;

	private final Layout layout;

	private List<Iterable<GadgetState>> columns;

	private final long version;

	private DashboardState(Builder builder) {
		id = builder.id;
		title = builder.title;
		layout = builder.layout;
		version = builder.version;
		columns = copy(builder.columns, layout.getNumberOfColumns());
	}

	private List<Iterable<GadgetState>> copy(Iterable<? extends Iterable<GadgetState>> columns, int size) {
		List<Iterable<GadgetState>> copy = new LinkedList<Iterable<GadgetState>>();
		if (columns != null) {
			Iterable<GadgetState> column;
			for (Iterator<? extends Iterable<GadgetState>> it = columns.iterator(); it.hasNext(); copy.add(Collections
					.unmodifiableList(CollectionUtil.toList(column)))) {
				column = it.next();
			}

		}
		pad(copy, size, Collections.emptyList());
		return Collections.unmodifiableList(copy);
	}

	@SuppressWarnings("unchecked")
	private void pad(Collection list, int toSize, Object obj) {
		list.addAll(Collections.nCopies(toSize - list.size(), obj));
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		columns = copy(columns, layout == null ? Layout.AA.getNumberOfColumns() : layout.getNumberOfColumns());
		if (id == null) {
			throw new InvalidObjectException("id cannot be null");
		}
		if (title == null) {
			throw new InvalidObjectException("title cannot be null");
		}
		if (layout == null) {
			throw new InvalidObjectException("layout cannot be null");
		}
		if (columns.size() != layout.getNumberOfColumns()) {
			throw new InvalidObjectException(new StringBuilder().append("columns size must be ")
					.append(layout.getNumberOfColumns()).toString());
		} else {
			return;
		}
	}

	public DashboardId getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Layout getLayout() {
		return layout;
	}

	public Iterable<GadgetState> getGadgetsInColumn(ColumnIndex column) {
		return columns.get(column.index());
	}

	public Iterable<Iterable<GadgetState>> getColumns() {
		return columns;
	}

	public long getVersion() {
		return version;
	}

	private DashboardState add(GadgetState gadgetState, ColumnIndex index, boolean prepend) {
		boolean foundRequestedColumn = false;
		List<Iterable<GadgetState>> modifiedColumns = new LinkedList<Iterable<GadgetState>>();
		for (Iterator<ColumnIndex> it = layout.getColumnRange().iterator(); it.hasNext();) {
			ColumnIndex i = it.next();
			Iterable<GadgetState> column = getGadgetsInColumn(i);
			if (i.equals(index)) {
				foundRequestedColumn = true;
				List<GadgetState> newColumn = new LinkedList<GadgetState>();
				if (prepend) {
					newColumn.add(gadgetState);
					addExistingGadgetsToColumn(column, newColumn);
				} else {
					addExistingGadgetsToColumn(column, newColumn);
					newColumn.add(gadgetState);
				}
				modifiedColumns.add(Collections.unmodifiableList(CollectionUtil.toList(newColumn)));
			} else {
				modifiedColumns.add(column);
			}
		}

		if (!foundRequestedColumn) {
			throw new IllegalArgumentException("index is out of this dashboard's columns range");
		} else {
			return dashboard(id).title(title).layout(layout).columns(modifiedColumns).version(version).build();
		}
	}

	private void addExistingGadgetsToColumn(Iterable<GadgetState> column, List<GadgetState> newColumn) {
		GadgetState gadget;
		for (Iterator<GadgetState> it = column.iterator(); it.hasNext(); newColumn.add(gadget)) {
			gadget = it.next();
		}

	}

	public DashboardState prependGadgetToColumn(GadgetState gadgetState, ColumnIndex index) {
		return add(gadgetState, index, true);
	}

	public DashboardState appendGadgetToColumn(GadgetState gadgetState, ColumnIndex index) {
		return add(gadgetState, index, false);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DashboardState)) {
			return false;
		}
		DashboardState rhs = (DashboardState) o;
		boolean equals = new EqualsBuilder().append(getId(), rhs.getId()).append(getTitle(), rhs.getTitle())
				.append(getLayout(), rhs.getLayout()).isEquals();
		if (!equals) {
			return false;
		}
		Iterator<ColumnIndex> i = getLayout().getColumnRange().iterator();
		do {
			if (!i.hasNext()) {
				break;
			}
			ColumnIndex columnIndex = i.next();
			equals = Iterables.elementsEqual(getGadgetsInColumn(columnIndex), rhs.getGadgetsInColumn(columnIndex));
		} while (equals);
		return equals;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder().append(getId()).append(getTitle()).append(getLayout());
		ColumnIndex columnIndex;
		for (Iterator<ColumnIndex> i = getLayout().getColumnRange().iterator(); i.hasNext(); builder
				.append(getGadgetsInColumn(columnIndex))) {
			columnIndex = i.next();
		}

		return builder.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", getId()).append("title", getTitle())
				.append("layout", getLayout()).append("columns", columns).toString();
	}

	public static Builder dashboard(DashboardState state) {
		return new Builder(Assert.notNull(state));
	}

	public static TitleBuilder dashboard(DashboardId id) {
		return new TitleBuilder(Assert.notNull(id));
	}

}