package com.pmi.restlet.gadgets;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.cfr.commons.util.Assert;

import com.pmi.restlet.gadgets.dashboard.Color;

public final class GadgetState implements Serializable {

	public static class Builder {

		public Builder color(Color color) {
			this.color = Assert.notNull(color);
			return this;
		}

		public Builder userPrefs(Map<String, String> userPrefs) {
			this.userPrefs = Assert.notNull(userPrefs);
			return this;
		}

		public GadgetState build() {
			return new GadgetState(this);
		}

		private final GadgetId id;

		private final URI specUri;

		private Color color;

		private Map<String, String> userPrefs;

		private Builder(GadgetId id, URI specUri) {
			color = Color.color7;
			userPrefs = Collections.emptyMap();
			this.id = id;
			this.specUri = specUri;
		}

		public Builder(GadgetState state) {
			color = Color.color7;
			userPrefs = Collections.emptyMap();
			Assert.notNull(state);
			id = state.getId();
			specUri = state.getGadgetSpecUri();
			color = state.getColor();
			userPrefs = state.getUserPrefs();
		}

	}

	public static class SpecUriBuilder {

		public Builder specUri(String specUri) throws URISyntaxException {
			return specUri(new URI(Assert.notNull(specUri)));
		}

		public Builder specUri(URI specUri) {
			return new Builder(gadgetId, Assert.notNull(specUri));
		}

		private final GadgetId gadgetId;

		private SpecUriBuilder(GadgetId gadgetId) {
			this.gadgetId = gadgetId;
		}

	}

	private static final long serialVersionUID = 9016360397733397422L;

	private final GadgetId id;

	private final URI specUri;

	private final Color color;

	private Map<String, String> userPrefs;

	private GadgetState(Builder builder) {
		id = builder.id;
		specUri = builder.specUri;
		color = builder.color;
		userPrefs = Collections.unmodifiableMap(new HashMap<String, String>(builder.userPrefs));
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		userPrefs = Collections.unmodifiableMap(new HashMap<String, String>(userPrefs));
		if (id == null) {
			throw new InvalidObjectException("id cannot be null");
		}
		if (specUri == null) {
			throw new InvalidObjectException("specUrl cannot be null");
		}
		if (color == null) {
			throw new InvalidObjectException("color cannot be null");
		} else {
			return;
		}
	}

	public GadgetId getId() {
		return id;
	}

	public URI getGadgetSpecUri() {
		return specUri;
	}

	public Color getColor() {
		return color;
	}

	public Map<String, String> getUserPrefs() {
		return userPrefs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GadgetState)) {
			return false;
		} else {
			GadgetState rhs = (GadgetState) o;
			return new EqualsBuilder().append(getId(), rhs.getId()).append(getGadgetSpecUri(), rhs.getGadgetSpecUri())
					.append(getColor(), rhs.getColor()).append(getUserPrefs(), rhs.getUserPrefs()).isEquals();
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getId()).append(getGadgetSpecUri()).append(getColor())
				.append(getUserPrefs()).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", getId()).append("gadgetSpecUri", getGadgetSpecUri())
				.append("color", getColor()).append("userPrefs", getUserPrefs()).toString();
	}

	public static Builder gadget(GadgetState state) {
		return new Builder(state);
	}

	public static SpecUriBuilder gadget(GadgetId gadgetId) {
		return new SpecUriBuilder(Assert.notNull(gadgetId));
	}

}
