package com.pmi.restlet.gadgets.dashboard.internal;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.internal.impl.DashboardUrlBuilderImpl;
import com.pmi.restlet.gadgets.dashboard.util.IUrlBuilder;

@ImplementedBy(DashboardUrlBuilderImpl.class)
public interface IDashboardUrlBuilder extends IUrlBuilder {

    /**
     * Returns the URL to the dashboard container.
     *
     * @param dashboardId the dashboard ID to return the URL for
     * @return the URL to the dashboard container.
     */
    String buildDashboardUrl(DashboardId dashboardId);

    /**
     * Returns the URL to the RPC javascript file for the dashboard container.
     * 
     * @return the URL to the RPC javascript file for the dashboard container.
     */
    String buildRpcJsUrl();

    /**
     * Returns the URL to the layout resource of a dashboard.
     * 
     * @param dashboardId {@code DashboardId} of the dashboard that we want the layout resource URL of 
     * @return the URL to the layout resource of the dashboard
     */
    String buildDashboardLayoutUrl(DashboardId dashboardId);

    /**
     * Returns the URL to a gadget resource on a dashboard.
     * 
     * @param dashboardId {@code DashboardId} to find the {@code GadgetId} on
     * @param gadgetId {@code GadgetId} of the gadget we want the resource URL of 
     * @return the URL to a gadget resource on a dashboard
     */
    String buildGadgetUrl(DashboardId dashboardId, GadgetId gadgetId);

    /**
     * Returns the URL to the color resource of a gadget.
     * 
     * @param dashboardId {@code DashboardId} to find the {@code GadgetId} on
     * @param gadgetId {@code GadgetId} of the gadget we want the color resource URL of 
     * @return the URL to the color resource of a gadget
     */
    String buildGadgetColorUrl(DashboardId dashboardId, GadgetId gadgetId);

    /**
     * Returns the URL to the user prefs resource of a gadget.
     * 
     * @param dashboardId {@code DashboardId} to find the {@code GadgetId} on
     * @param gadgetId {@code GadgetId} of the gadget we want the user prefs resource URL of 
     * @return the URL to the user prefs resource of a gadget
     */
    String buildGadgetUserPrefsUrl(DashboardId dashboardId, GadgetId gadgetId);

    /**
     * Returns a URL that can be used to display an error message when there is a problem loading a gadget.
     * 
     * @return URL that can be used to display an error message when there is a problem loading a gadget.
     */
    String buildErrorGadgetUrl();

    /**
     * Returns a URL that can be used to retrieve the list of available gadgets in the directory
     * or to post a new gadget spec URL to the directory.
     *
     * @return URL that can be used to retrieve the list of available gadgets in the directory
     * or to post a new gadget spec URL to the directory.
     */
    public String buildDashboardDirectoryResourceUrl();

    /**
     * Returns a URL that can be used to access the Dashboard resource, to post new gadgets.
     *
     * @param dashboardId the ID of the dashboard to post gadgets to
     * @return URL that can be used to access the Dashboard resource, to post new gadgets.
     */
    public String buildDashboardResourceUrl(DashboardId dashboardId);

    /**
     * Returns a URL that can be used to post a new gadget to the directory.
     *
     * @param dashboardId the ID of the dashboard to post gadgets to
     * @return URL that can be used to post a new gadget to the directory.
     */
    public String buildDashboardDirectoryUrl(DashboardId dashboardId);

    /**
     * Returns the base URL of the directory
     *
     * @return the base URL of the directory
     */
    public String buildDashboardDirectoryBaseUrl();

    /**
     * Returns a relative URL that can be used to access the Dashboard Diagnostics Servlet.
     * @return relative URL that can be used to access the Dashboard Diagnostics Servlet.
     */
    public String buildDashboardDiagnosticsRelativeUrl();

    /**
     * Returns the URL for getting new security tokens.
     * 
     * @return URL for getting new security tokens.
     */
    public String buildSecurityTokensUrl();
}