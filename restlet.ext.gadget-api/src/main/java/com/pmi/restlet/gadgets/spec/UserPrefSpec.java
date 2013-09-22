package com.pmi.restlet.gadgets.spec;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.cfr.commons.util.Assert;

public final class UserPrefSpec {

	public static class Builder {

		public Builder displayName(String displayName) {
			Assert.notNull(displayName, "displayName is required");
			this.displayName = displayName;
			return this;
		}

		public Builder required(boolean required) {
			this.required = required;
			return this;
		}

		public Builder dataType(DataType dataType) {
			Assert.notNull(dataType, "dataType is required");
			this.dataType = dataType;
			return this;
		}

		public Builder enumValues(Map<String, String> enumValues) {
			Assert.notNull(enumValues, "enumValues is required");
			this.enumValues = enumValues;
			return this;
		}

		public Builder defaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public UserPrefSpec build() {
			return new UserPrefSpec(this);
		}

		private final String name;

		private String displayName;

		private boolean required;

		private DataType dataType;

		private Map<String, String> enumValues;

		private String defaultValue;

		private Builder(String name) {
			Assert.notNull(name, "name is required");
			enumValues = Collections.emptyMap();
			this.name = name;
		}

		private Builder(UserPrefSpec spec) {
			Assert.notNull(spec, "spec is required");
			enumValues = Collections.emptyMap();
			name = spec.name;
			displayName = spec.displayName;
			required = spec.required;
			dataType = spec.dataType;
			enumValues = spec.enumValues;
			defaultValue = spec.defaultValue;
		}

	}

	private UserPrefSpec(Builder builder) {
		name = builder.name;
		displayName = builder.displayName;
		required = builder.required;
		dataType = builder.dataType;
		Map<String, String> enumValuesCopy = new LinkedHashMap<String, String>();
		String key;
		for (Iterator<String> i = builder.enumValues.keySet().iterator(); i.hasNext(); enumValuesCopy.put(key,
				builder.enumValues.get(key))) {
			key = i.next();
		}

		enumValues = Collections.unmodifiableMap(enumValuesCopy);
		defaultValue = builder.defaultValue;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isRequired() {
		return required;
	}

	public DataType getDataType() {
		return dataType;
	}

	public Map<String, String> getEnumValues() {
		return enumValues;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public static Builder userPrefSpec(String name) {
		return new Builder(name);
	}

	public static Builder userPrefSpec(UserPrefSpec userPrefSpec) {
		return new Builder(userPrefSpec);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", getName()).append("dataType", getDataType()).toString();
	}

	private final String name;

	private final String displayName;

	private final boolean required;

	private final DataType dataType;

	private final Map<String, String> enumValues;

	private final String defaultValue;

}