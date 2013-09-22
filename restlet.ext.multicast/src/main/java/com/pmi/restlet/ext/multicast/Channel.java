package com.pmi.restlet.ext.multicast;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;

/**
 * Default {@link IChannel} implementation.
 * @author acochard [Sep 30, 2009]
 *
 */
public class Channel implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 7796907206410010216L;

    /** Application name **/
    private String name;

    private String version;

    private int hostPort;

    private String hostAddress;

    private String contextPath;

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getHostPort() {
        return hostPort;
    }

    public String getName() {
        return name;
    }

    public String getContextPath() {
        return contextPath;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    void setName(String name) {
        this.name = name;
    }

    void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name + "-" + version + "@" + hostAddress + ":" + hostPort;
    }

    public Reference getRootRef() {
        if (hostAddress == null || hostPort == 0 || contextPath == null)
            return null;
        return new Reference(Protocol.HTTP.getName(), hostAddress, hostPort, contextPath, null, null);
    }
}
