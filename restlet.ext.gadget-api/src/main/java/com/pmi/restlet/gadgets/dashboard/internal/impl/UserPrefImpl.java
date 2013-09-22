package com.pmi.restlet.gadgets.dashboard.internal.impl;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.pmi.restlet.gadgets.dashboard.internal.IUserPref;
import com.pmi.restlet.gadgets.spec.DataType;
import com.pmi.restlet.gadgets.spec.UserPrefSpec;

public final class UserPrefImpl implements IUserPref {

    private final UserPrefSpec userPrefSpec;

    private final String value;

    public UserPrefImpl(UserPrefSpec userPrefSpec, String value) {
        this.userPrefSpec = userPrefSpec;
        this.value = (value == null || (userPrefSpec.isRequired() && StringUtils.isBlank(value))) ? userPrefSpec.getDefaultValue() : value;
    }

    public String getName() {
        return userPrefSpec.getName();
    }

    public String getDisplayName() {
        return userPrefSpec.getDisplayName();
    }

    public boolean isRequired() {
        return userPrefSpec.isRequired();
    }

    public DataType getDataType() {
        return userPrefSpec.getDataType();
    }

    public Map<String, String> getEnumValues() {
        return userPrefSpec.getEnumValues();
    }

    public String getDefaultValue() {
        return userPrefSpec.getDefaultValue();
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 35).append(getName()).append(getDisplayName()).append(getValue()).append(isRequired()).append(getDataType())
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (this.getClass() != o.getClass())
            return false;
        IUserPref that = (IUserPref) o;
        return new EqualsBuilder().append(this.getName(), that.getName()).append(this.getDisplayName(), that.getDisplayName()).append(
                this.getValue(), that.getValue()).append(this.isRequired(), that.isRequired()).append(this.getDataType(), that.getDataType())
                .isEquals();
    }
}
