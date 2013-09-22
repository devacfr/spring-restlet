package com.pmi.restlet.gadgets.dashboard.internal;

import java.net.URI;

import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.Color;

public interface IGadget {

    GadgetId getId();

    String getTitle();

    /**
     * Returns an optional URL that the gagdget's title will link to
     * @returns a URL for the gadget title link, or {@code null} if the title has no link
     */
    URI getTitleUrl();

    String getGadgetSpecUrl();

    Integer getHeight();

    Integer getWidth();

    Color getColor();

    boolean isMaximizable();

    /**
     * Returns true if the gadget contains at least one pref that isn't hidden.
     * If the gadget has no prefs of any sort, this will return false.
     * @return true if the gadget contains at least one non-hidden pref
     */
    boolean hasNonHiddenUserPrefs();

    Iterable<IUserPref> getUserPrefs();

    GadgetState getState();

    /**
     * Returns an error message if the gadget was constructed with one, which should be the case when {@code isLoaded()} returns false
     * @returns an error message if the gadget was constructed with one (currently only possible with one of two constructors)
     */
    String getErrorMessage();

    /**
     * Tells if the gadget has been loaded successfully
     * @returns true if the gadget is loaded, i.e. has been constructed with a gadgetSpec and no error (currently only
     * possible with one of two constructors)
     */
    boolean isLoaded();
}
