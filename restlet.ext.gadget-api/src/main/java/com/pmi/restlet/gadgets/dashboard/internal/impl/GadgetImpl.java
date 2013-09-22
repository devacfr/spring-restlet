package com.pmi.restlet.gadgets.dashboard.internal.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.Color;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;
import com.pmi.restlet.gadgets.dashboard.internal.IUserPref;
import com.pmi.restlet.gadgets.spec.DataType;
import com.pmi.restlet.gadgets.spec.GadgetSpec;
import com.pmi.restlet.gadgets.spec.UserPrefSpec;
import com.pmi.restlet.gadgets.view.ViewType;

/**
 * Basic {@code IGadget} implementation.
 */
public final class GadgetImpl implements IGadget {

    private static final class UserPrefSpecToUserPref implements Function<UserPrefSpec, IUserPref> {

        public IUserPref apply(UserPrefSpec userPrefSpec) {
            return new UserPrefImpl(userPrefSpec, userPrefValues.get(userPrefSpec.getName()));
        }

        private final Map<String, String> userPrefValues;

        public UserPrefSpecToUserPref(Map<String, String> userPrefValues) {
            this.userPrefValues = userPrefValues;
        }
    }

    private final GadgetId id;

    private final URI storedSpecUri;

    private final GadgetSpec gadgetSpec;

    private Color color;

    private Iterable<IUserPref> userPrefs;

    private String errorMessage;

    private GadgetState stateOnLoadAttempt;

    public GadgetImpl(GadgetState state, GadgetSpec gadgetSpec) {
        id = state.getId();
        storedSpecUri = state.getGadgetSpecUri();
        this.gadgetSpec = gadgetSpec;
        color = state.getColor();
        errorMessage = null;
        stateOnLoadAttempt = null;
        if (gadgetSpec.getUserPrefs() != null)
            userPrefs = Iterables.transform(gadgetSpec.getUserPrefs(), toUserPrefs(state.getUserPrefs()));
        else
            userPrefs = ImmutableList.of();
    }

    public GadgetImpl(GadgetState stateOnLoadAttempt, String errorMessage) {
        id = stateOnLoadAttempt.getId();
        storedSpecUri = stateOnLoadAttempt.getGadgetSpecUri();
        this.errorMessage = errorMessage;
        this.stateOnLoadAttempt = stateOnLoadAttempt;
        gadgetSpec = null;
    }

    public GadgetId getId() {
        return id;
    }

    public String getTitle() {
        checkLoaded();
        return gadgetSpec.getTitle();
    }

    public URI getTitleUrl() {
        checkLoaded();
        return gadgetSpec.getTitleUrl();
    }

    public String getGadgetSpecUrl() {
        return storedSpecUri.toASCIIString();
    }

    public Integer getHeight() {
        checkLoaded();
        return gadgetSpec.getHeight() == 0 ? null : Integer.valueOf(gadgetSpec.getHeight());
    }

    public Integer getWidth() {
        checkLoaded();
        return gadgetSpec.getWidth() == 0 ? null : Integer.valueOf(gadgetSpec.getWidth());
    }

    public Color getColor() {
        return color;
    }

    public boolean isMaximizable() {
        checkLoaded();
        return gadgetSpec.supportsViewType(ViewType.CANVAS);
    }

    public boolean hasNonHiddenUserPrefs() {
        checkLoaded();
        return Iterables.any(userPrefs, isNotHidden);
    }

    public Iterable<IUserPref> getUserPrefs() {
        checkLoaded();
        return userPrefs;
    }

    public GadgetState getState() {
        if (isLoaded())
            return GadgetState.gadget(id).specUri(storedSpecUri).color(color).userPrefs(createStateFrom(userPrefs)).build();
        else
            return stateOnLoadAttempt;
    }

    private Map<String, String> createStateFrom(Iterable<IUserPref> userPrefs) {
        Map<String, String> userPrefValues = new HashMap<String, String>();
        Iterator<IUserPref> it = userPrefs.iterator();
        do {
            if (!it.hasNext())
                break;
            IUserPref userPref = it.next();
            if (!userPref.isRequired() || StringUtils.isNotBlank(userPref.getValue()))
                userPrefValues.put(userPref.getName(), userPref.getValue());
        } while (true);
        return userPrefValues;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isLoaded() {
        return gadgetSpec != null;
    }

    private void checkLoaded() {
        if (!isLoaded())
            throw new IllegalStateException("gadget could not be loaded");
        else
            return;
    }

    private Function<UserPrefSpec, IUserPref> toUserPrefs(Map<String, String> userPrefValues) {
        return new UserPrefSpecToUserPref(userPrefValues);
    }

    private static final Predicate<IUserPref> isNotHidden = new Predicate<IUserPref>() {

        public boolean apply(IUserPref userPref) {
            return !DataType.HIDDEN.equals(userPref.getDataType());
        }

    };
}