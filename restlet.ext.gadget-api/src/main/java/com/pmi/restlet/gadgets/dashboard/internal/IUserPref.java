package com.pmi.restlet.gadgets.dashboard.internal;

import java.util.Map;

import com.pmi.restlet.gadgets.spec.DataType;

/**
 * A living breathing user pref with a current value
 */
public interface IUserPref {

    /**
     * Returns the pref name.
     * @return the pref name
     */
    String getName();

    /**
     * Returns the display name of this parameter; if not explicitly
     * set by the gadget, returns the name.
     * @return the name to show the user for this pref
     */
    String getDisplayName();

    /**
     * Returns true if the pref is required.
     * @return true if the pref is required
     */
    boolean isRequired();

    /**
     * Returns the {@link DataType} of this pref.
     * @return this pref's datatype
     */
    DataType getDataType();

    /**
     * Returns the possible values for an enumerated pref, or null if the pref
     * is not an enum.
     * @return possible values for the pref
     */
    Map<String, String> getEnumValues();

    /**
     * Returns the pref's default value.
     * @return the default value of the pref or an empty string if there is no default value
     */
    public String getDefaultValue();

    /**
     * Returns the pref's current value.
     * @return the current value of the pref
     */
    String getValue();
}
