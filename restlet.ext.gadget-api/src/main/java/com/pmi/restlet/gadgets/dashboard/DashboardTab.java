package com.pmi.restlet.gadgets.dashboard;

import java.net.URI;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public final class DashboardTab {

    private final DashboardId dashboardId;

    private final String title;

    private final URI tabUri;

    public DashboardTab(DashboardId dashboardId, String title, URI tabUri) {
        this.dashboardId = dashboardId;
        this.title = title;
        this.tabUri = tabUri;
    }

    public DashboardId getDashboardId() {
        return dashboardId;
    }

    public URI getTabUri() {
        return tabUri;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DashboardTab)) {
            return false;
        } else {
            DashboardTab rhs = (DashboardTab) o;
            return (new EqualsBuilder()).append(getDashboardId(), rhs.getDashboardId()).append(getTitle(), rhs.getTitle()).append(getTabUri(),
                    rhs.getTabUri()).isEquals();
        }
    }

    @Override
    public int hashCode() {
        return (new HashCodeBuilder()).append(getDashboardId()).append(getTitle()).append(getTabUri()).toHashCode();
    }

    @Override
    public String toString() {
        return (new ToStringBuilder(this)).append("dashboardid", getDashboardId()).append("title", getTitle()).append("tabUri", getTabUri())
                .toString();
    }

}