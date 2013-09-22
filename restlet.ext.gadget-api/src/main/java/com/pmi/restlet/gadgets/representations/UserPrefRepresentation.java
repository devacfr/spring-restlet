package com.pmi.restlet.gadgets.representations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.EqualsBuilder;

import com.pmi.restlet.gadgets.dashboard.internal.IUserPref;

public final class UserPrefRepresentation {

    public static class EnumValueRepresentation {

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public Boolean isSelected() {
            return selected;
        }

        private final String value;

        private final String label;

        private final Boolean selected;

        public EnumValueRepresentation(String value, String label, boolean selected) {
            this.value = StringEscapeUtils.escapeHtml(value);
            this.label = StringEscapeUtils.escapeHtml(label);
            this.selected = Boolean.valueOf(selected);
        }

        @SuppressWarnings("unused")
        private EnumValueRepresentation() {
            value = null;
            label = null;
            selected = null;
        }
    }

    private final String name;

    private final String value;

    private final String type;

    private final String displayName;

    private final boolean required;

    private final List<EnumValueRepresentation> options;

    public UserPrefRepresentation(IUserPref userPref) {
        name = StringEscapeUtils.escapeHtml(userPref.getName());
        value = StringEscapeUtils.escapeHtml(userPref.getValue() != null ? userPref.getValue() : userPref.getDefaultValue());
        type = userPref.getDataType().toString().toLowerCase();
        displayName = StringEscapeUtils.escapeHtml(userPref.getDisplayName());
        required = userPref.isRequired();
        options = transformEnumValues(userPref.getEnumValues(), value);
    }

    private List<EnumValueRepresentation> transformEnumValues(Map<String, String> enumValues, String selectedValue) {
        List<EnumValueRepresentation> result = new ArrayList<EnumValueRepresentation>();
        Entry<String, String> enumValueEntry;
        boolean isDefaultValue;
        for (Iterator<Entry<String, String>> i$ = enumValues.entrySet().iterator(); i$.hasNext(); result.add(new EnumValueRepresentation(
                enumValueEntry.getKey(), enumValueEntry.getValue(), isDefaultValue))) {
            enumValueEntry = i$.next();
            isDefaultValue = (new EqualsBuilder()).append(selectedValue, enumValueEntry.getKey()).isEquals();
        }

        return result;
    }

    @SuppressWarnings("unused")
    private UserPrefRepresentation() {
        type = null;
        displayName = null;
        name = null;
        value = null;
        required = false;
        options = new ArrayList<EnumValueRepresentation>();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRequired() {
        return required;
    }

    public List<EnumValueRepresentation> getOptions() {
        return new ArrayList<EnumValueRepresentation>(options);
    }

}