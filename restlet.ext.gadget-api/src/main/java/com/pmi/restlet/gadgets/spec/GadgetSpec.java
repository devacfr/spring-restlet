package com.pmi.restlet.gadgets.spec;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.cfr.commons.util.Assert;

import com.pmi.restlet.gadgets.view.ViewType;

public final class GadgetSpec {

	public static class Builder {

		public Builder userPrefs(Iterable<UserPrefSpec> userPrefs) {
			Assert.notNull(userPrefs, "userPrefs is required");
			this.userPrefs = userPrefs;
			return this;
		}

		public Builder scrolling(boolean scrolling) {
			this.scrolling = scrolling;
			return this;
		}

		public Builder height(int height) {
			this.height = height;
			return this;
		}

		public Builder width(int width) {
			this.width = width;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder titleUrl(URI titleUrl) {
			this.titleUrl = titleUrl;
			return this;
		}

		public Builder thumbnail(URI thumbnail) {
			this.thumbnail = thumbnail;
			return this;
		}

		public Builder author(String author) {
			this.author = author;
			return this;
		}

		public Builder authorEmail(String authorEmail) {
			this.authorEmail = authorEmail;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder directoryTitle(String directoryTitle) {
			this.directoryTitle = directoryTitle;
			return this;
		}

		public Builder features(Map features) {
			Assert.notNull(features, "features is required");
			this.features = features;
			return this;
		}

		public Builder unsupportedFeatureNames(Iterable<String> unsupportedFeatureNames) {
			Assert.notNull(unsupportedFeatureNames, "unsupportedFeatureNames is required");
			this.unsupportedFeatureNames = unsupportedFeatureNames;
			return this;
		}

		public Builder viewsNames(Set<String> viewsNames) {
			Assert.notNull(viewsNames, "viewsNames is required");
			this.viewsNames = viewsNames;
			return this;
		}

		public GadgetSpec build() {
			return new GadgetSpec(this);
		}

		private final URI specUri;

		private Iterable<UserPrefSpec> userPrefs;

		private boolean scrolling;

		private int height;

		private int width;

		private String title;

		private URI titleUrl;

		private URI thumbnail;

		private String author;

		private String authorEmail;

		private String description;

		private String directoryTitle;

		private Map features;

		private Iterable<String> unsupportedFeatureNames;

		private Set<String> viewsNames;

		private Builder(URI specUri) {
			Assert.notNull(specUri, "specUri is required");
			userPrefs = Collections.emptySet();
			features = Collections.emptyMap();
			unsupportedFeatureNames = Collections.emptySet();
			viewsNames = Collections.emptySet();
			this.specUri = specUri;
		}

		private Builder(GadgetSpec spec) {
			Assert.notNull(spec, "spec is required");
			userPrefs = Collections.emptySet();
			features = Collections.emptyMap();
			unsupportedFeatureNames = Collections.emptySet();
			viewsNames = Collections.emptySet();

			specUri = spec.specUri;
			userPrefs = spec.userPrefs;
			scrolling = spec.scrolling;
			height = spec.height;
			width = spec.width;
			title = spec.title;
			titleUrl = spec.titleUrl;
			thumbnail = spec.thumbnail;
			author = spec.author;
			authorEmail = spec.authorEmail;
			description = spec.description;
			directoryTitle = spec.directoryTitle;
			features = spec.features;
			unsupportedFeatureNames = spec.unsupportedFeatureNames;
			viewsNames = spec.viewsNames;
		}

	}

	private final URI specUri;

	private final Iterable<UserPrefSpec> userPrefs;

	private final boolean scrolling;

	private final int height;

	private final int width;

	private final String title;

	private final URI titleUrl;

	private final URI thumbnail;

	private final String author;

	private final String authorEmail;

	private final String description;

	private final String directoryTitle;

	private final Map features;

	private final Iterable<String> unsupportedFeatureNames;

	private final Set<String> viewsNames;

	private GadgetSpec(Builder builder) {
		specUri = builder.specUri;
		List<UserPrefSpec> userPrefsCopy = new LinkedList<UserPrefSpec>();
		UserPrefSpec userPrefSpec;
		for (Iterator<UserPrefSpec> i = builder.userPrefs.iterator(); i.hasNext(); userPrefsCopy.add(userPrefSpec)) {
			userPrefSpec = i.next();
		}

		userPrefs = Collections.unmodifiableList(userPrefsCopy);
		scrolling = builder.scrolling;
		height = builder.height;
		width = builder.width;
		title = builder.title;
		titleUrl = builder.titleUrl;
		thumbnail = builder.thumbnail;
		author = builder.author;
		authorEmail = builder.authorEmail;
		description = builder.description;
		directoryTitle = builder.directoryTitle;
		features = Collections.unmodifiableMap(new HashMap(builder.features));
		List<String> unsupportedFeatureNamesCopy = new LinkedList<String>();
		String unsupportedFeatureName;
		for (Iterator<String> i = builder.unsupportedFeatureNames.iterator(); i.hasNext(); unsupportedFeatureNamesCopy
				.add(unsupportedFeatureName)) {
			unsupportedFeatureName = i.next();
		}

		unsupportedFeatureNames = Collections.unmodifiableList(unsupportedFeatureNamesCopy);
		viewsNames = Collections.unmodifiableSet(new HashSet<String>(builder.viewsNames));
	}

	public URI getUrl() {
		return specUri;
	}

	public Iterable<UserPrefSpec> getUserPrefs() {
		return userPrefs;
	}

	public boolean supportsViewType(ViewType viewType) {
		if (viewsNames.isEmpty()) {
			return false;
		}
		if (viewsNames.contains(viewType.getCanonicalName())) {
			return true;
		}
		for (Iterator<String> it = viewType.getAliases().iterator(); it.hasNext();) {
			if (viewsNames.contains(it.next())) {
				return true;
			}
		}

		return false;
	}

	public boolean isScrolling() {
		return scrolling;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public String getTitle() {
		return title;
	}

	public URI getTitleUrl() {
		return titleUrl;
	}

	public URI getThumbnail() {
		return thumbnail;
	}

	public String getAuthor() {
		return author;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public String getDescription() {
		return description;
	}

	public String getDirectoryTitle() {
		return directoryTitle;
	}

	public Map getFeatures() {
		return features;
	}

	public Iterable<String> getUnsupportedFeatureNames() {
		return unsupportedFeatureNames;
	}

	public static Builder gadgetSpec(URI specUri) {
		return new Builder(specUri);
	}

	public static Builder gadgetSpec(GadgetSpec gadgetSpec) {
		return new Builder(gadgetSpec);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("title", getTitle()).append("url", getUrl()).toString();
	}

}