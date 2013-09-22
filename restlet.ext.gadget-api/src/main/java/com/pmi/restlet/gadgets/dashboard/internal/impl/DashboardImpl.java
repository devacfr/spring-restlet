package com.pmi.restlet.gadgets.dashboard.internal.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetLayoutException;
import com.pmi.restlet.gadgets.GadgetNotFoundException;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.Color;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.Layout;
import com.pmi.restlet.gadgets.dashboard.DashboardState.ColumnIndex;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;
import com.pmi.restlet.gadgets.dashboard.internal.IStateConverter;
import com.pmi.restlet.gadgets.dashboard.internal.IUserPref;
import com.pmi.restlet.gadgets.dashboard.spi.GadgetLayout;
import com.pmi.restlet.gadgets.dashboard.spi.changes.AddGadgetChange;
import com.pmi.restlet.gadgets.dashboard.spi.changes.GadgetColorChange;
import com.pmi.restlet.gadgets.dashboard.spi.changes.IDashboardChange;
import com.pmi.restlet.gadgets.dashboard.spi.changes.RemoveGadgetChange;
import com.pmi.restlet.gadgets.dashboard.spi.changes.UpdateGadgetUserPrefsChange;
import com.pmi.restlet.gadgets.dashboard.spi.changes.UpdateLayoutChange;
import com.pmi.restlet.gadgets.spec.DataType;

/**
 * Implements methods for operating on a backing {@link DashboardState} object.
 */
public class DashboardImpl implements IDashboard {

    private DashboardState state;

    private IStateConverter stateConverter;

    private List<IDashboardChange> changes;

    private final GadgetRequestContext gadgetRequestContext;

    private static final class WithIdPredicate implements Predicate<GadgetState> {

        public boolean apply(GadgetState gadgetState) {
            return gadgetState.getId().equals(gadgetId);
        }

        private final GadgetId gadgetId;

        public WithIdPredicate(GadgetId gadgetId) {
            this.gadgetId = gadgetId;
        }
    }

    private static final class RemoveGadgetFunction implements Function<Iterable<GadgetState>, Iterable<GadgetState>> {

        public Iterable<GadgetState> apply(Iterable<GadgetState> column) {
            return Iterables.filter(column, Predicates.not(predicate));
        }

        private final Predicate<GadgetState> predicate;

        public RemoveGadgetFunction(Predicate<GadgetState> predicate) {
            this.predicate = predicate;
        }
    }

    private static final class InternalGadgetNotFoundException extends RuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = 4944305187885529091L;

        private InternalGadgetNotFoundException() {
        }

    }

    private final class UpdatePrefValues implements Function<GadgetState, GadgetState> {

        public GadgetState apply(GadgetState gadget) {
            return GadgetState.gadget(gadget).userPrefs(
                    updatedUserPrefs(stateConverter.convertStateToGadget(gadget, gadgetRequestContext), newPrefValues)).build();
        }

        private final Map<String, String> newPrefValues;

        public UpdatePrefValues(Map<String, String> newPrefValues) {
            this.newPrefValues = newPrefValues;
        }
    }

    private static final class ChangeColorTo implements Function<GadgetState, GadgetState> {

        public GadgetState apply(GadgetState gadget) {
            return GadgetState.gadget(gadget).color(color).build();
        }

        private final Color color;

        public ChangeColorTo(Color color) {
            this.color = color;
        }
    }

    private final class GadgetStateConverter implements Function<GadgetState, IGadget> {

        public IGadget apply(GadgetState gadget) {
            return stateConverter.convertStateToGadget(gadget, gadgetRequestContext);
        }

    }

    public DashboardImpl(DashboardState state, IStateConverter stateConverter, GadgetRequestContext gadgetRequestContext) {
        changes = Lists.newArrayList();
        this.state = Preconditions.checkNotNull(state, "state");
        this.stateConverter = Preconditions.checkNotNull(stateConverter, "stateConverter");
        this.gadgetRequestContext = Preconditions.checkNotNull(gadgetRequestContext, "gadgetRequestContext");
    }

    public DashboardId getId() {
        return state.getId();
    }

    public String getTitle() {
        return state.getTitle();
    }

    public Layout getLayout() {
        return state.getLayout();
    }

    public Iterable<IGadget> getGadgetsInColumn(ColumnIndex column) {
        if (!getLayout().contains(column))
            throw new IllegalArgumentException((new StringBuilder()).append("Invalid column index for layout ").append(getLayout()).toString());
        else
            return Iterables.transform(state.getGadgetsInColumn(column), toGadgets());
    }

    public void appendGadget(IGadget gadget) {
        appendGadget(ColumnIndex.ZERO, gadget);
    }

    public void appendGadget(ColumnIndex columnIndex, IGadget gadget) {
        state = state.appendGadgetToColumn(gadget.getState(), columnIndex);
        changes.add(new AddGadgetChange(gadget.getState(), columnIndex, Iterables.size(state.getGadgetsInColumn(columnIndex)) - 1));
    }

    public void addGadget(IGadget gadget) {
        addGadget(ColumnIndex.ZERO, gadget);
    }

    public void addGadget(ColumnIndex column, IGadget gadget) {
        state = state.prependGadgetToColumn(gadget.getState(), column);
        changes.add(new AddGadgetChange(gadget.getState(), column, 0));
    }

    public void changeLayout(Layout layout, GadgetLayout gadgetLayout) throws GadgetLayoutException {
        assertGadgetLayoutIsValid(layout, gadgetLayout);
        assertAllGadgetsPresent(gadgetLayout);
        state = DashboardState.dashboard(state).layout(layout).columns(getRearrangedColumns(layout, gadgetLayout)).build();
        changes.add(new UpdateLayoutChange(layout, gadgetLayout));
    }

    public void rearrangeGadgets(GadgetLayout gadgetLayout) throws GadgetLayoutException {
        assertGadgetLayoutIsValid(getLayout(), gadgetLayout);
        assertAllGadgetsPresent(gadgetLayout);
        state = DashboardState.dashboard(state).columns(getRearrangedColumns(getLayout(), gadgetLayout)).build();
        changes.add(new UpdateLayoutChange(getLayout(), gadgetLayout));
    }

    public void changeGadgetColor(GadgetId gadgetId, Color color) {
        Preconditions.checkNotNull(gadgetId, "gadgetId");
        Preconditions.checkNotNull(color, "color");
        try {
            state = DashboardState.dashboard(state).columns(updateGadget(withId(gadgetId), changeColorTo(color))).build();
            changes.add(new GadgetColorChange(gadgetId, color));
        } catch (InternalGadgetNotFoundException e) {
            throw new GadgetNotFoundException(gadgetId);
        }
    }

    public void updateGadgetUserPrefs(GadgetId gadgetId, Map<String, String> prefValues) {
        Preconditions.checkNotNull(gadgetId, "gadgetId");
        Preconditions.checkNotNull(prefValues, "prefValues");
        try {
            state = DashboardState.dashboard(state).columns(updateGadget(withId(gadgetId), updateUserPrefs(prefValues))).build();
            changes.add(new UpdateGadgetUserPrefsChange(gadgetId, prefValues));
        } catch (InternalGadgetNotFoundException e) {
            throw new GadgetNotFoundException(gadgetId);
        }
    }

    public void removeGadget(GadgetId gadgetId) {
        state = DashboardState.dashboard(state).columns(Iterables.transform(state.getColumns(), removeGadget(withId(gadgetId)))).build();
        changes.add(new RemoveGadgetChange(gadgetId));
    }

    public DashboardState getState() {
        return state;
    }

    public IGadget findGadget(GadgetId gadgetId) {
        Iterator<ColumnIndex> it = getLayout().getColumnRange().iterator();
        GadgetState gadget;
        label0: do
            if (it.hasNext()) {
                ColumnIndex columnIndex = it.next();
                Iterator<GadgetState> i = state.getGadgetsInColumn(columnIndex).iterator();
                do {
                    if (!i.hasNext())
                        continue label0;
                    gadget = i.next();
                } while (!withId(gadgetId).apply(gadget));
                break;
            } else {
                return null;
            }
        while (true);
        return toGadgets().apply(gadget);
    }

    public List<IDashboardChange> getChanges() {
        return ImmutableList.copyOf(changes);
    }

    public void clearChanges() {
        changes = Lists.newArrayList();
    }

    private Function<GadgetState, IGadget> toGadgets() {
        return new GadgetStateConverter();
    }

    private Function<GadgetState, GadgetState> changeColorTo(Color color) {
        return new ChangeColorTo(color);
    }

    private Function<GadgetState, GadgetState> updateUserPrefs(Map<String, String> prefValues) {
        return new UpdatePrefValues(prefValues);
    }

    private Iterable<Iterable<GadgetState>> updateGadget(Predicate<GadgetState> predicate, Function<GadgetState, GadgetState> function) {
        Preconditions.checkNotNull(predicate, "predicate");
        Preconditions.checkNotNull(function, "function");
        boolean foundGadget = false;
        Builder<Iterable<GadgetState>> columnsBuilder = ImmutableList.builder();
        Builder<GadgetState> columnBuilder;
        for (Iterator<Iterable<GadgetState>> it = state.getColumns().iterator(); it.hasNext(); columnsBuilder.add(columnBuilder.build())) {
            Iterable<GadgetState> column = it.next();
            columnBuilder = ImmutableList.builder();
            for (Iterator<GadgetState> i = column.iterator(); i.hasNext();) {
                GadgetState gadget = i.next();
                if (predicate.apply(gadget)) {
                    foundGadget = true;
                    columnBuilder.add(function.apply(gadget));
                } else {
                    columnBuilder.add(gadget);
                }
            }

        }

        if (!foundGadget)
            throw new InternalGadgetNotFoundException();
        else
            return columnsBuilder.build();
    }

    private Map<String, String> updatedUserPrefs(IGadget gadget, Map<String, String> newPrefValues) {
        com.google.common.collect.ImmutableMap.Builder<String, String> newUserPrefs = ImmutableMap.builder();
        for (Iterator<IUserPref> it = gadget.getUserPrefs().iterator(); it.hasNext();) {
            IUserPref userPref = it.next();
            if (newPrefValues.containsKey(userPref.getName())) {
                String newValue = newPrefValues.get(userPref.getName());
                if (DataType.BOOL.equals(userPref.getDataType()) && StringUtils.isBlank(newValue))
                    newValue = Boolean.FALSE.toString();
                if (userPref.isRequired() && StringUtils.isBlank(newValue))
                    throw new IllegalArgumentException((new StringBuilder()).append("pref '").append(userPref.getName()).append("' is required ")
                            .append("and must have a non-null, non-empty value").toString());
                newUserPrefs.put(userPref.getName(), newValue);
            } else {
                newUserPrefs.put(userPref.getName(), userPref.getValue());
            }
        }

        return newUserPrefs.build();
    }

    private Function<Iterable<GadgetState>, Iterable<GadgetState>> removeGadget(Predicate<GadgetState> predicate) {
        return new RemoveGadgetFunction(predicate);
    }

    private Predicate<GadgetState> withId(GadgetId gadgetId) {
        return new WithIdPredicate(gadgetId);
    }

    private Iterable<Iterable<GadgetState>> getRearrangedColumns(Layout layout, GadgetLayout gadgetLayout) {
        Map<GadgetId, GadgetState> gadgets = getAllGadgets();
        Builder<Iterable<GadgetState>> columnsBuilder = ImmutableList.builder();
        for (int i = 0; i < layout.getNumberOfColumns(); i++) {
            Builder<GadgetState> columnBuilder = ImmutableList.builder();
            Iterator<GadgetId> it = gadgetLayout.getGadgetsInColumn(i).iterator();
            do {
                if (!it.hasNext())
                    break;
                GadgetId gadgetId = it.next();
                if (gadgets.containsKey(gadgetId)) {
                    columnBuilder.add(gadgets.get(gadgetId));
                    gadgets.remove(gadgetId);
                }
            } while (true);
            columnsBuilder.add(columnBuilder.build());
        }

        return columnsBuilder.build();
    }

    private Map<GadgetId, GadgetState> getAllGadgets() {
        Map<GadgetId, GadgetState> gadgets = new HashMap<GadgetId, GadgetState>();
        for (Iterator<Iterable<GadgetState>> it = state.getColumns().iterator(); it.hasNext();) {
            Iterable<GadgetState> column = it.next();
            Iterator<GadgetState> i = column.iterator();
            while (i.hasNext()) {
                GadgetState gadgetState = i.next();
                gadgets.put(gadgetState.getId(), gadgetState);
            }
        }

        return gadgets;
    }

    public int getNumberOfGadgets() {
        int numberOfGadgets = 0;
        for (Iterator<Iterable<GadgetState>> it = state.getColumns().iterator(); it.hasNext();) {
            Iterable<GadgetState> column = it.next();
            numberOfGadgets += Iterables.size(column);
        }

        return numberOfGadgets;
    }

    private void assertAllGadgetsPresent(GadgetLayout gadgetLayout) throws GadgetLayoutException {
        Map<GadgetId, GadgetState> gadgets = getAllGadgets();
        for (int i = 0; i < gadgetLayout.getNumberOfColumns(); i++) {
            GadgetId gadgetId;
            for (Iterator<GadgetId> it = gadgetLayout.getGadgetsInColumn(i).iterator(); it.hasNext(); gadgets.remove(gadgetId))
                gadgetId = it.next();

        }

        if (!gadgets.isEmpty())
            throw new GadgetLayoutException("Gadgets cannot be removed by changing the layout, they need to be removed explicitly");
        else
            return;
    }

    public void assertGadgetLayoutIsValid(Layout layout, GadgetLayout gadgetLayout) throws GadgetLayoutException {
        if (gadgetLayout.getNumberOfColumns() > layout.getNumberOfColumns())
            throw new GadgetLayoutException((new StringBuilder()).append("New layout has ").append(gadgetLayout.getNumberOfColumns()).append(
                    " but the current layout only allows ").append(layout.getNumberOfColumns()).append(" columns.").toString());
        else
            return;
    }

}