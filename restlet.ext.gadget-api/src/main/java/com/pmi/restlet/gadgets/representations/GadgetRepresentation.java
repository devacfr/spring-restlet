package com.pmi.restlet.gadgets.representations;

import org.apache.commons.lang.StringUtils;

import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.dashboard.Color;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardUrlBuilder;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;
import com.pmi.restlet.gadgets.view.IRenderedGadgetUriBuilder;
import com.pmi.restlet.gadgets.view.ModuleId;
import com.pmi.restlet.gadgets.view.View;
import com.pmi.restlet.gadgets.view.ViewType;

public class GadgetRepresentation {

    static final class GadgetUrlContainer {

        public String getColorUri() {
            return colorUri;
        }

        public String getGadgetUri() {
            return gadgetUri;
        }

        public String getRenderedGadgetUri() {
            return renderedGadgetUri;
        }

        public String getUserPrefsUri() {
            return userPrefsUri;
        }

        public String getTitleUri() {
            return titleUri;
        }

        private final String colorUri;

        private final String gadgetUri;

        private final String userPrefsUri;

        private final String renderedGadgetUri;

        private final String titleUri;

        GadgetUrlContainer(IRenderedGadgetUriBuilder renderedGadgetUriBuilder, IDashboardUrlBuilder dashboardUrlBuilder, DashboardId dashboardId,
                IGadget gadget, GadgetRequestContext gadgetRequestContext, boolean writable) {
            GadgetId gadgetId = gadget.getId();
            String titleUrlString = gadget.isLoaded() && gadget.getTitleUrl() != null ? gadget.getTitleUrl().toASCIIString() : null;
            if (StringUtils.isNotBlank(titleUrlString))
                titleUri = titleUrlString;
            else
                titleUri = null;
            colorUri = dashboardUrlBuilder.buildGadgetColorUrl(dashboardId, gadgetId);
            gadgetUri = dashboardUrlBuilder.buildGadgetUrl(dashboardId, gadgetId);
            userPrefsUri = dashboardUrlBuilder.buildGadgetUserPrefsUrl(dashboardId, gadgetId);
            View view = (new View.Builder()).viewType(ViewType.DEFAULT).writable(writable).build();
            renderedGadgetUri = renderedGadgetUriBuilder.build(gadget.getState(), ModuleId.valueOf(gadget.getId().value()), view,
                    gadgetRequestContext).toASCIIString();
        }
    }

    private final String id;

    private final String title;

    private final String titleUrl;

    private final String gadgetSpecUrl;

    private final Integer height;

    private final Integer width;

    private final Color color;

    private final Integer column;

    private final String colorUrl;

    private final String gadgetUrl;

    private final Boolean isMaximizable;

    private final String renderedGadgetUrl;

    private final Boolean hasNonHiddenUserPrefs;

    private final UserPrefsRepresentation userPrefs;

    private final Boolean loaded;

    private final String errorMessage;

    @SuppressWarnings("unused")
    private GadgetRepresentation() {
        id = "0";
        title = null;
        titleUrl = null;
        gadgetSpecUrl = null;
        height = null;
        width = null;
        color = null;
        isMaximizable = null;
        userPrefs = null;
        renderedGadgetUrl = null;
        colorUrl = null;
        gadgetUrl = null;
        hasNonHiddenUserPrefs = null;
        column = null;
        loaded = null;
        errorMessage = "";
    }

    GadgetRepresentation(IGadget gadget, GadgetUrlContainer gadgetUrls, DashboardState.ColumnIndex columnIndex) {
        GadgetId gadgetId = gadget.getId();
        id = gadgetId.value();
        loaded = Boolean.valueOf(gadget.isLoaded());
        title = loaded.booleanValue() ? gadget.getTitle() : null;
        height = loaded.booleanValue() ? gadget.getHeight() : null;
        width = loaded.booleanValue() ? gadget.getWidth() : null;
        color = loaded.booleanValue() ? gadget.getColor() : null;
        isMaximizable = loaded.booleanValue() ? Boolean.valueOf(gadget.isMaximizable()) : null;
        userPrefs = loaded.booleanValue() ? new UserPrefsRepresentation(gadget.getUserPrefs(), gadgetUrls.getUserPrefsUri()) : null;
        hasNonHiddenUserPrefs = loaded.booleanValue() ? Boolean.valueOf(gadget.hasNonHiddenUserPrefs()) : null;
        titleUrl = gadgetUrls.getTitleUri();
        gadgetSpecUrl = gadget.getGadgetSpecUrl();
        colorUrl = gadgetUrls.getColorUri();
        gadgetUrl = gadgetUrls.getGadgetUri();
        renderedGadgetUrl = gadgetUrls.getRenderedGadgetUri();
        column = columnIndex != null ? Integer.valueOf(columnIndex.index()) : null;
        errorMessage = gadget.getErrorMessage();
    }

    public Boolean getHasNonHiddenUserPrefs() {
        return hasNonHiddenUserPrefs;
    }

    public Boolean isLoaded() {
        return loaded;
    }

    public Color getColor() {
        return color;
    }

    public String getGadgetSpecUrl() {
        return gadgetSpecUrl;
    }

    public Integer getHeight() {
        return height;
    }

    public String getId() {
        return id;
    }

    public Boolean isMaximizable() {
        return isMaximizable;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleUrl() {
        return titleUrl;
    }

    public UserPrefsRepresentation getUserPrefs() {
        return userPrefs;
    }

    public Integer getWidth() {
        return width;
    }

    public String getRenderedGadgetUrl() {
        return renderedGadgetUrl;
    }

    public String getColorUrl() {
        return colorUrl;
    }

    public String getGadgetUrl() {
        return gadgetUrl;
    }

    public Boolean hasNonHiddenUserPrefs() {
        return hasNonHiddenUserPrefs;
    }

    public Integer getColumn() {
        return column;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}