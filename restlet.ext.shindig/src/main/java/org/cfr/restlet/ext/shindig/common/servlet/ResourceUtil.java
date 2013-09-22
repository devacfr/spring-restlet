package org.cfr.restlet.ext.shindig.common.servlet;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.shindig.common.Pair;
import org.apache.shindig.common.servlet.HttpUtil;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.uri.UriBuilder;
import org.apache.shindig.common.util.DateUtil;
import org.apache.shindig.common.util.TimeSource;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.util.Series;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Collection of HTTP utilities Based on {@link HttpUtil}
 */
public class ResourceUtil {

    private ResourceUtil() {
    }

    // 1 year.
    private static int defaultTtl = 60 * 60 * 24 * 365;

    private static TimeSource timeSource;

    static {
        setTimeSource(new TimeSource());
    }

    public static void setTimeSource(TimeSource timeSource) {
        ResourceUtil.timeSource = timeSource;
    }

    public static TimeSource getTimeSource() {
        return timeSource;
    }

    /**
     * Sets HTTP headers that instruct the browser to cache content.
     * Implementations should take care to use cache-busting techniques on the
     * url if caching for a long period of time.
     * 
     * @param response
     *            The HTTP response
     */
    public static void setCachingHeaders(ContextResource contextResource) {
        setCachingHeaders(contextResource, defaultTtl, false);
    }

    /**
     * Sets HTTP headers that instruct the browser to cache content.
     * Implementations should take care to use cache-busting techniques on the
     * url if caching for a long period of time.
     * 
     * @param response
     *            The HTTP response
     * @param noProxy
     *            True if you don't want the response to be cacheable by
     *            proxies.
     */
    public static void setCachingHeaders(ContextResource contextResource, boolean noProxy) {
        setCachingHeaders(contextResource, defaultTtl, noProxy);
    }

    /**
     * Sets HTTP headers that instruct the browser to cache content.
     * Implementations should take care to use cache-busting techniques on the
     * url if caching for a long period of time.
     * 
     * @param response
     *            The HTTP response
     * @param ttl
     *            The time to cache for, in seconds. If 0, then insure that this
     *            object is not cached.
     */
    public static void setCachingHeaders(ContextResource contextResource, int ttl) {
        setCachingHeaders(contextResource, ttl, false);
    }

    public static void setNoCache(ContextResource contextResource) {
        setCachingHeaders(contextResource, 0, false);
    }

    /**
     * Sets HTTP headers that instruct the browser to cache content.
     * Implementations should take care to use cache-busting techniques on the
     * url if caching for a long period of time.
     * 
     * @param response
     *            The HTTP response
     * @param ttl
     *            The time to cache for, in seconds. If 0, then insure that this
     *            object is not cached.
     * @param noProxy
     *            True if you don't want the response to be cacheable by
     *            proxies.
     */
    public static void setCachingHeaders(ContextResource contextResource, int ttl, boolean noProxy) {
        Series<Parameter> responseHeaders = contextResource.getResponseHeaders();
        for (Pair<String, String> header : getCachingHeadersToSet(ttl, noProxy)) {
            responseHeaders.add(header.one, header.two);
        }
    }

    public static List<Pair<String, String>> getCachingHeadersToSet(int ttl, boolean noProxy) {
        List<Pair<String, String>> cachingHeaders = Lists.newArrayListWithExpectedSize(3);
        cachingHeaders.add(Pair.of(HeaderConstants.HEADER_EXPIRES, DateUtil.formatRfc1123Date(timeSource.currentTimeMillis() + (1000L * ttl))));

        if (ttl <= 0) {
            cachingHeaders.add(Pair.of(HeaderConstants.HEADER_PRAGMA, "no-cache"));
            cachingHeaders.add(Pair.of(HeaderConstants.HEADER_CACHE_CONTROL, "no-cache"));
        } else {
            if (noProxy) {
                cachingHeaders.add(Pair.of(HeaderConstants.HEADER_CACHE_CONTROL, "private,max-age=" + Integer.toString(ttl)));
            } else {
                cachingHeaders.add(Pair.of(HeaderConstants.HEADER_CACHE_CONTROL, "public,max-age=" + Integer.toString(ttl)));
            }
        }

        return cachingHeaders;
    }

    public static int getDefaultTtl() {
        return defaultTtl;
    }

    public static void setDefaultTtl(int ttl) {
        defaultTtl = ttl;
    }

    public static Uri toUri(Reference reference) {
        Uri uri = new UriBuilder().setAuthority(reference.getAuthority()).setFragment(reference.getFragment()).setPath(reference.getPath()).setQuery(
                reference.getQuery()).setScheme(reference.getScheme()).toUri();
        return uri;
    }

    static final Pattern GET_REQUEST_CALLBACK_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_\\.]+");

    public static boolean isJSONP(ContextResource contextResource) throws IllegalArgumentException {
        String callback = contextResource.getParameter("callback");

        // Must be a GET
        if (!Method.GET.equals(contextResource.getRequest().getMethod()))
            return false;

        // No callback specified
        if (callback == null)
            return false;

        Preconditions.checkArgument(GET_REQUEST_CALLBACK_PATTERN.matcher(callback).matches(),
                "Wrong format for parameter 'callback' specified. Must match: " + GET_REQUEST_CALLBACK_PATTERN.toString());

        return true;
    }

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";

    /**
     * Set the header for Cross-Site Resource Sharing.
     * @param resp HttpServletResponse to modify
     * @param validOrigins a space separated list of Origins as defined by the html5 spec
     * @see <a href="http://dev.w3.org/html5/spec/browsers.html#origin-0">html 5 spec, section 5.3</a>
     */
    public static void setCORSheader(ContextResource contextResource, Collection<String> validOrigins) {
        if (validOrigins == null) {
            return;
        }
        for (String origin : validOrigins) {
            contextResource.getResponseHeaders().add(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        }
    }

}