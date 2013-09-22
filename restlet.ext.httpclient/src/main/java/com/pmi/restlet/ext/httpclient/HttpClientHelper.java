/**
 * Copyright 2005-2010 Noelios Technologies.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package com.pmi.restlet.ext.httpclient;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.http.ClientCall;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.engine.security.SslUtils;
import org.restlet.util.Series;

import com.pmi.restlet.ext.httpclient.internal.HttpIdleConnectionReaper;
import com.pmi.restlet.ext.httpclient.internal.HttpMethodCall;

/**
 * HTTP client connector using the HttpMethodCall and Apache HTTP Client
 * project. Note that the response must be fully read in all cases in order to
 * surely release the underlying connection. Not doing so may cause future
 * requests to block.<br>
 * <br>
 * Here is the list of parameters that are supported. They should be set in the
 * Client's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>followRedirects</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the protocol will automatically follow redirects. If false, the
 * protocol will not automatically follow redirects.</td>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerHost</td>
 * <td>int</td>
 * <td>2 (uses HttpClient's default)</td>
 * <td>The maximum number of connections that will be created for any particular
 * host.</td>
 * </tr>
 * <tr>
 * <td>maxTotalConnections</td>
 * <td>int</td>
 * <td>20 (uses HttpClient's default)</td>
 * <td>The maximum number of active connections.</td>
 * </tr>
 * <tr>
 * <td>proxyHost</td>
 * <td>String</td>
 * <td>System property "http.proxyHost"</td>
 * <td>The host name of the HTTP proxy.</td>
 * </tr>
 * <tr>
 * <td>proxyPort</td>
 * <td>int</td>
 * <td>System property "http.proxyPort" or "3128"</td>
 * <td>The port of the HTTP proxy.</td>
 * </tr>
 * <tr>
 * <td>stopIdleTimeout</td>
 * <td>int</td>
 * <td>1000</td>
 * <td>The minimum idle time, in milliseconds, for connections to be closed when
 * stopping the connector.</td>
 * </tr>
 * <tr>
 * <td>socketTimeout</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Sets the socket timeout to a specified timeout, in milliseconds. A
 * timeout of zero is interpreted as an infinite timeout.</td>
 * </tr>
 * <tr>
 * <td>retryHandler</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Class name of the retry handler to use instead of HTTP Client default
 * behavior. The given class name must extend the
 * org.apache.http.client.HttpRequestRetryHandler class and have a default
 * constructor</td>
 * </tr>
 * <tr>
 * <td>tcpNoDelay</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicate if Nagle's TCP_NODELAY algorithm should be used.</td>
 * </tr>
 * </table>
 *
 * @see <a href= "http://hc.apache.org/httpcomponents-client/tutorial/html/"
 *      >Apache HTTP Client tutorial</a>
 * @see <a
 *      href="http://java.sun.com/j2se/1.5.0/docs/guide/net/index.html">Networking
 *      Features</a>
 * @author Jerome Louvel
 */
public class HttpClientHelper extends org.restlet.engine.http.HttpClientHelper {

    public interface Parameters {

        /**
         * The host name of the HTTP proxy, if specified.
         */
        public static final String proxyHost = "proxyHost";

        /**
         * The port of the HTTP proxy, if specified, 3128 otherwise.
         */
        public static final String proxyPort = "proxyPort";

        /**
         * Indicates the hosts which should be connected too directly and not through the proxy server.
         * The value can be a list of hosts, each seperated by a |, and in addition a wildcard character (*)
         * can be used for matching.
         * For example: -Dhttp.nonProxyHosts="*.foo.com|localhost".
         */
        public static final String nonProxyHosts = "nonProxyHosts";

        /**
         * The class name of the retry handler to use instead of HTTP Client
         * default behavior. The given class name must implement the
         * org.apache.commons.httpclient.HttpMethodRetryHandler interface and have a
         * default constructor.
         */
        public static final String retryHandler = "retryHandler";

        /**
         * Indicates if the protocol will automatically follow redirects.
         * default value: false.
         */
        public static final String followRedirects = "followRedirects";

        /**
        * Defines the maximum number of redirects to be followed.
        * The limit on number of redirects is intended to prevent infinite loops.
        * <p>
        * default value: 3.
        * </p>
        */
        public static final String maxRedirects = "maxRedirect";

        /**
         * Time between checks for idle and expired connections. Note that only if
         * this property is set to a value greater than 0 will idle connection
         * reaping occur. Default value:0.
         */
        public static final String idleConnectionCheckIntervalMillis = "idleConnectionCheckIntervalMillis";

        /**
         * Returns the maximum number of connections that will be created for any
         * particular host. Default value: 2.
         */
        public static final String maxConnectionsPerHost = "maxConnectionsPerHost";

        /**
         * Returns the maximum number of active connections.
         * Default value: 20.
         */
        public static final String maxTotalConnections = "maxTotalConnections";

        /**
         * the socket timeout value. A timeout of zero is interpreted as an
         * infinite timeout.
         * Default value: 0
         */
        public static final String socketTimeout = "socketTimeout";

        /**
         * The time in millis beyond which connections idle are eligible for
         * reaping. If no value is specified, 10000L is the interval. In
         * addition, unless the property idleConnectionCheckIntervalMillis
         * has been set to a value greater than 0, this property is of no
         * consequence as reaping will not occur.
         * Default value : 10000.
         */
        public static final String reapConnectionOlderThanMillis = "reapConnectionOlderThanMillis";

        /**
         * Returns the minimum idle time, in milliseconds, for connections to be
         * closed when stopping the connector. Default value: 1000.
         */
        public static final String stopIdleTimeout = "stopIdleTimeout";

        /**
         * Indicates if the protocol will use Nagle's algorithm. Default value: false.
         */
        public static final String tcpNoDelay = "tcpNoDelay";
    }

    private volatile DefaultHttpClient httpClient;

    /** the idle connection reaper. */
    private volatile HttpIdleConnectionReaper idleConnectionReaper;

    /**
     * Constructor.
     *
     * @param client
     *            The client to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        this.httpClient = null;
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
    }

    /**
     * Configures the HTTP client. By default, it try to set the retry handler.
     *
     * @param httpClient
     *            The HTTP client to configure.
     */
    protected void configure(DefaultHttpClient httpClient) {
        if (getRetryHandler() != null) {
            try {
                HttpRequestRetryHandler retryHandler = (HttpRequestRetryHandler) Engine.loadClass(getRetryHandler()).newInstance();
                this.httpClient.setHttpRequestRetryHandler(retryHandler);
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "An error occurred during the instantiation of the retry handler.", e);
            }
        }
        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(httpClient.getConnectionManager().getSchemeRegistry(), ProxySelector
                .getDefault());
        httpClient.setRoutePlanner(routePlanner);
    }

    /**
     * Configures the various parameters of the connection manager and the HTTP
     * client.
     *
     * @param params
     *            The parameter list to update.
     */
    protected void configure(HttpParams params) {
        ConnManagerParams.setMaxTotalConnections(params, getMaxTotalConnections());
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(getMaxConnectionsPerHost()));

        // Configure other parameters
        HttpClientParams.setAuthenticating(params, false);
        HttpClientParams.setRedirecting(params, isFollowRedirects());
        HttpClientParams.setCookiePolicy(params, CookiePolicy.BROWSER_COMPATIBILITY);
        HttpConnectionParams.setTcpNoDelay(params, getTcpNoDelay());
        HttpConnectionParams.setConnectionTimeout(params, getConnectTimeout());
        HttpConnectionParams.setSoTimeout(params, getSocketTimeout());
        params.setIntParameter(ClientPNames.MAX_REDIRECTS, getMaxRedirects());

        //-Dhttp.proxyHost=chlaubc.obs.pmi -Dhttp.proxyPort=8000 -Dhttp.nonProxyHosts=localhost|127.0.0.1|*.app.pmi
        String httpProxyHost = getProxyHost();
        if (httpProxyHost != null) {
            if (StringUtils.isNotEmpty(getNonProxyHosts())) {
                System.setProperty("http.nonProxyHosts", getNonProxyHosts());
            } else {
                System.getProperties().remove("http.nonProxyHosts");
            }
            System.setProperty("http.proxyPort", String.valueOf(getProxyPort()));
            System.setProperty("http.proxyHost", httpProxyHost);
            HttpHost proxy = new HttpHost(httpProxyHost, getProxyPort());
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

        }
    }

    /**
     * Configures the scheme registry. By default, it registers the HTTP and the
     * HTTPS schemes.
     *
     * @param schemeRegistry
     *            The scheme registry to configure.
     */
    protected void configure(SchemeRegistry schemeRegistry) {
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        SSLSocketFactory sslSocketFactory = null;
        SslContextFactory sslContextFactory = SslUtils.getSslContextFactory(this);

        if (sslContextFactory != null) {
            try {
                SSLContext sslContext = sslContextFactory.createSslContext();
                sslSocketFactory = new SSLSocketFactory(sslContext);
            } catch (Exception e) {
                throw new RuntimeException("Unable to create SSLContext.", e);
            }
        } else {
            sslSocketFactory = SSLSocketFactory.getSocketFactory();
        }

        schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
    }

    /**
     * Creates a low-level HTTP client call from a high-level uniform call.
     *
     * @param request
     *            The high-level request.
     * @return A low-level HTTP client call.
     */
    @Override
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            result = new HttpMethodCall(this, request.getMethod().toString(), request.getResourceRef().toString(), request.isEntityAvailable());
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING, "Unable to create the HTTP client call", ioe);
        }

        return result;
    }

    /**
     * Creates the connection manager. By default, it creates a thread safe
     * connection manager.
     *
     * @param params
     *            The configuration parameters.
     * @param schemeRegistry
     *            The scheme registry to use.
     * @return The created connection manager.
     */
    protected ClientConnectionManager createClientConnectionManager(HttpParams params, SchemeRegistry schemeRegistry) {
        return new ThreadSafeClientConnManager(params, schemeRegistry);
    }

    /**
     * Returns the wrapped Apache HTTP Client.
     *
     * @return The wrapped Apache HTTP Client.
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Time between checks for idle and expired connections. Note that only if
     * this property is set to a value greater than 0 will idle connection
     * reaping occur.
     *
     * @return A value indicating the idle connection check interval or 0 if a
     *         value has not been provided
     */
    public long getIdleConnectionCheckIntervalMillis() {
        return Long.parseLong(getHelpedParameters().getFirstValue(Parameters.idleConnectionCheckIntervalMillis, "0"));
    }

    /**
     * Returns the idle connections reaper.
     *
     * @return The idle connections reaper.
     */
    public HttpIdleConnectionReaper getIdleConnectionReaper() {
        return idleConnectionReaper;
    }

    /**
     * Returns the maximum number of connections that will be created for any
     * particular host.
     *
     * @return The maximum number of connections that will be created for any
     *         particular host.
     */
    public int getMaxConnectionsPerHost() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(Parameters.maxConnectionsPerHost, "2"));
    }

    /**
     * Returns the maximum number of active connections.
     *
     * @return The maximum number of active connections.
     */
    public int getMaxTotalConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(Parameters.maxTotalConnections, "20"));
    }

    /**
     * Indicates the hosts which should be connected too directly and not through the proxy server.
     * The value can be a list of hosts, each seperated by a |, and in addition a wildcard character (*) can be used for matching.
     * For example: -Dhttp.nonProxyHosts="*.foo.com|localhost".
     * @return returns the hosts which should be connected too directly and not through the proxy server
     */
    public String getNonProxyHosts() {
        return getHelpedParameters().getFirstValue(Parameters.nonProxyHosts, System.getProperty("http.nonProxyHosts"));
    }

    /**
     * Returns the host name of the HTTP proxy, if specified.
     *
     * @return the host name of the HTTP proxy, if specified.
     */
    public String getProxyHost() {
        return getHelpedParameters().getFirstValue(Parameters.proxyHost, System.getProperty("http.proxyHost"));
    }

    /**
     * Returns the port of the HTTP proxy, if specified, 3128 otherwise.
     *
     * @return the port of the HTTP proxy.
     */
    public int getProxyPort() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(Parameters.proxyPort, System.getProperty("http.proxyPort", "3128")));
    }

    /**
     * @return The time in millis beyond which connections idle are eligible for
     *         reaping. If no value is specified, 10000L is the interval. In
     *         addition, unless the property idleConnectionCheckIntervalMillis
     *         has been set to a value greater than 0, this property is of no
     *         consequence as reaping will not occur.
     */
    public long getReapConnectionsOlderThanMillis() {
        return Long.parseLong(getHelpedParameters().getFirstValue(Parameters.reapConnectionOlderThanMillis, "10000"));
    }

    /**
     * Returns the class name of the retry handler to use instead of HTTP Client
     * default behavior. The given class name must implement the
     * org.apache.commons.httpclient.HttpMethodRetryHandler interface and have a
     * default constructor.
     *
     * @return The class name of the retry handler.
     */
    public String getRetryHandler() {
        return getHelpedParameters().getFirstValue(Parameters.retryHandler, null);
    }

    /**
     * Returns the socket timeout value. A timeout of zero is interpreted as an
     * infinite timeout.
     *
     * @return The read timeout value.
     */
    public int getSocketTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(Parameters.socketTimeout, "0"));
    }

    /**
     * Returns the minimum idle time, in milliseconds, for connections to be
     * closed when stopping the connector.
     *
     * @return The minimum idle time, in milliseconds, for connections to be
     *         closed when stopping the connector.
     */
    public int getStopIdleTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(Parameters.stopIdleTimeout, "1000"));
    }

    /**
     * Indicates if the protocol will use Nagle's algorithm
     *
     * @return True to enable TCP_NODELAY, false to disable.
     * @see java.net.Socket#setTcpNoDelay(boolean)
     */
    public boolean getTcpNoDelay() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(Parameters.tcpNoDelay, "false"));
    }

    /**
     * Indicates if the protocol will automatically follow redirects.
     *
     * @return True if the protocol will automatically follow redirects.
     */
    public boolean isFollowRedirects() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(Parameters.followRedirects, "false"));
    }

    public int getMaxRedirects() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(Parameters.maxRedirects, "3"));
    }

    /**
     * Sets the idle connections reaper.
     *
     * @param connectionReaper
     *            The idle connections reaper.
     */
    public void setIdleConnectionReaper(HttpIdleConnectionReaper connectionReaper) {
        this.idleConnectionReaper = connectionReaper;
    }

    @Override
    public void start() throws Exception {
        super.start();

        // Define configuration parameters
        HttpParams params = new BasicHttpParams();
        configure(params);

        // Set-up the scheme registry
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        configure(schemeRegistry);

        // Create the connection manager
        ClientConnectionManager connectionManager = createClientConnectionManager(params, schemeRegistry);

        // Create and configure the HTTP client
        this.httpClient = new DefaultHttpClient(connectionManager, params);
        configure(this.httpClient);

        if (this.idleConnectionReaper != null) {
            // If a previous reaper is present, stop it
            this.idleConnectionReaper.stop();
        }

        this.idleConnectionReaper = new HttpIdleConnectionReaper(httpClient, getIdleConnectionCheckIntervalMillis(),
                getReapConnectionsOlderThanMillis());

        getLogger().info("Starting the HTTP client");
    }

    @Override
    public void stop() throws Exception {
        getIdleConnectionReaper().stop();
        getHttpClient().getConnectionManager().closeExpiredConnections();
        getHttpClient().getConnectionManager().closeIdleConnections(getStopIdleTimeout(), TimeUnit.MILLISECONDS);
        getHttpClient().getConnectionManager().shutdown();
        getLogger().info("Stopping the HTTP client");
    }

    @Override
    public Series<Parameter> getHelpedParameters() {
        Series<Parameter> result = null;

        if ((getHelped() != null) && (getHelped().getContext() != null)) {
            if (getHelped().getApplication() != null && getHelped().getContext().getParameters().isEmpty()) {
                result = getHelped().getApplication().getContext().getParameters();
            } else {
                result = getHelped().getContext().getParameters();
            }
        } else {
            result = new Form();
        }

        return result;
    }
}