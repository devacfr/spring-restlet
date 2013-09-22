package com.pmi.restlet.gadgets.representations;

import java.util.ArrayList;
import java.util.List;

import org.cfr.commons.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.restlet.gadgets.dashboard.internal.IUserPref;

public final class UserPrefsRepresentation {

	private final String action;

	private final List<UserPrefRepresentation> fields;

	@SuppressWarnings("unused")
	private UserPrefsRepresentation() {
		action = null;
		fields = new ArrayList<UserPrefRepresentation>();
	}

	public UserPrefsRepresentation(Iterable<IUserPref> prefs, String actionUrl) {
		Assert.notNull(prefs, "prefs");
		Assert.notNull(actionUrl, "actionUrl");
		action = actionUrl;
		fields = transformCollectionUserPrefsToNameStrings(prefs);
	}

	private List<UserPrefRepresentation> transformCollectionUserPrefsToNameStrings(Iterable<IUserPref> userPrefs) {
		return Lists.newArrayList(Iterables.transform(userPrefs, new Function<IUserPref, UserPrefRepresentation>() {

			@Override
			public UserPrefRepresentation apply(IUserPref userPref) {
				return new UserPrefRepresentation(userPref);
			}

		}));
	}

	public String getAction() {
		return action;
	}

	public List getFields() {
		return fields;
	}

}