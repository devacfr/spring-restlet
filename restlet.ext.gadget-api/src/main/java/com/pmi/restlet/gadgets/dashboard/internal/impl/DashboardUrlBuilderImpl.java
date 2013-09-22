package com.pmi.restlet.gadgets.dashboard.internal.impl;

import org.cfr.commons.plugins.webresource.IWebResourceManager;
import org.cfr.commons.sal.IGlobalApplicationProperties;

import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardUrlBuilder;
import com.pmi.restlet.gadgets.dashboard.util.AbstractUrlBuilder;
import com.pmi.restlet.gadgets.dashboard.util.Uri;

public class DashboardUrlBuilderImpl extends AbstractUrlBuilder implements IDashboardUrlBuilder {

	public DashboardUrlBuilderImpl(IGlobalApplicationProperties applicationProperties,
			IWebResourceManager webResourceManager) {
		super(applicationProperties, webResourceManager, "org.cfr.gadgets.dashboard:dashboard");
	}

	@Override
	public String buildDashboardUrl(DashboardId dashboardId) {
		return new StringBuilder().append(getBaseUrl()).append(dashboardId).toString();
	}

	@Override
	public String buildDashboardLayoutUrl(DashboardId dashboardId) {
		return new StringBuilder().append(buildDashboardUrl(dashboardId)).append("/layout").toString();
	}

	@Override
	public String buildGadgetUrl(DashboardId dashboardId, GadgetId gadgetId) {
		return new StringBuilder().append(buildDashboardUrl(dashboardId)).append("/gadget/").append(gadgetId)
				.toString();
	}

	@Override
	public String buildGadgetColorUrl(DashboardId dashboardId, GadgetId gadgetId) {
		return new StringBuilder().append(buildGadgetUrl(dashboardId, gadgetId)).append("/color").toString();
	}

	@Override
	public String buildGadgetUserPrefsUrl(DashboardId dashboardId, GadgetId gadgetId) {
		return new StringBuilder().append(buildGadgetUrl(dashboardId, gadgetId)).append("/prefs").toString();
	}

	@Override
	public String buildErrorGadgetUrl() {
		return new StringBuilder()
				.append(webResourceManager.getStaticPluginResource("com.pmi.gadgets.dashboard:dashboard", "/files/"))
				.append("errorGadget.html").toString();
	}

	@Override
	public String buildDashboardResourceUrl(DashboardId dashboardId) {
		return new StringBuilder().append(applicationProperties.getBaseUrl()).append("/rest/dashboards/1.0/")
				.append(dashboardId).toString();
	}

	@Override
	public String buildDashboardDirectoryResourceUrl() {
		return new StringBuilder().append(applicationProperties.getBaseUrl()).append("/rest/config/1.0/directory")
				.toString();
	}

	@Override
	public String buildDashboardDirectoryBaseUrl() {
		return Uri.ensureTrailingSlash(applicationProperties.getBaseUrl());
	}

	@Override
	public String buildDashboardDirectoryUrl(DashboardId dashboardId) {
		return new StringBuilder().append(getBaseUrl()).append("/directory/").append(dashboardId).toString();
	}

	public String buildSubscribedGadgetFeedsUrl() {
		return new StringBuilder().append(buildDashboardDirectoryResourceUrl()).append("/subscribed-gadget-feeds")
				.toString();
	}

	@Override
	public String buildDashboardDiagnosticsRelativeUrl() {
		return Uri.create(
				new StringBuilder().append(applicationProperties.getBaseUrl()).append("/gadgets/dashboard-diagnostics")
						.toString()).getPath();
	}

	@Override
	public String buildSecurityTokensUrl() {
		return new StringBuilder().append(applicationProperties.getBaseUrl()).append("/gadgets/security-tokens")
				.toString();
	}
}