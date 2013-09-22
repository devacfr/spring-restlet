package org.cfr.restlet.ext.shindig.internal;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.RenderingContext;
import org.apache.shindig.gadgets.UserPrefs;
import org.apache.shindig.gadgets.servlet.HttpGadgetContext;
import org.cfr.restlet.ext.shindig.auth.AuthInfo;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.restlet.Request;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.util.Series;

import com.google.common.collect.Maps;

/**
 * Based on {@link HttpGadgetContext}
 * 
 * @author cfriedri
 * 
 */
public class ResourceGadgetContext extends GadgetContext {

    private final Request request;

    private final Series<Parameter> requestHeaders;

    private final Series<Parameter> form;

    public static final String USERPREF_PARAM_PREFIX = "up_";

    private String container;

    private Boolean debug;

    private Boolean ignoreCache;

    private Locale locale;

    private Integer moduleId;

    private RenderingContext renderingContext;

    private Uri url;

    private UserPrefs userPrefs;

    private String view;

    public ResourceGadgetContext(ContextResource contextResource) {
        super();
        this.request = contextResource.getRequest();
        this.requestHeaders = contextResource.getRequestHeaders();
        this.form = contextResource.getParameters();
        if (this.form != null) {
            container = getContainer(form);
            debug = getDebug(form);
            ignoreCache = getIgnoreCache(form);
            locale = getLocale(form);
            moduleId = getModuleId(form);
            renderingContext = getRenderingContext(form);
            url = getUrl(form);
            userPrefs = getUserPrefs(form);
            view = getView(form);
        }
    }

    @Override
    public String getParameter(String name) {
        return form.getFirstValue(name);
    }

    @Override
    public String getContainer() {
        if (container == null) {
            return super.getContainer();
        }
        return container;
    }

    @Override
    public String getHost() {
        Reference ref = request.getHostRef();
        String host = ref.getHostDomain();
        if (ref.getHostPort() > 0) {
            host += ':' + String.valueOf(ref.getHostPort());
        }
        if (host == null) {
            return super.getHost();
        }
        return host;
    }

    @Override
    public String getUserIp() {
        String ip = request.getClientInfo().getAddress();
        if (ip == null) {
            return super.getUserIp();
        }
        return ip;
    }

    @Override
    public boolean getDebug() {
        if (debug == null) {
            return super.getDebug();
        }
        return debug;
    }

    @Override
    public boolean getIgnoreCache() {
        if (ignoreCache == null) {
            return super.getIgnoreCache();
        }
        return ignoreCache;
    }

    @Override
    public Locale getLocale() {
        if (locale == null) {
            return super.getLocale();
        }
        return locale;
    }

    @Override
    public int getModuleId() {
        if (moduleId == null) {
            return super.getModuleId();
        }
        return moduleId;
    }

    @Override
    public RenderingContext getRenderingContext() {
        if (renderingContext == null) {
            return super.getRenderingContext();
        }
        return renderingContext;
    }

    @Override
    public SecurityToken getToken() {
        return new AuthInfo(request).getSecurityToken();
    }

    @Override
    public Uri getUrl() {
        if (url == null) {
            return super.getUrl();
        }
        return url;
    }

    @Override
    public UserPrefs getUserPrefs() {
        if (userPrefs == null) {
            return super.getUserPrefs();
        }
        return userPrefs;
    }

    @Override
    public String getView() {
        if (view == null) {
            return super.getView();
        }
        return view;
    }

    @Override
    public String getUserAgent() {
        String userAgent = requestHeaders.getFirstValue(HeaderConstants.HEADER_USER_AGENT);
        if (userAgent == null) {
            return super.getUserAgent();
        }
        return userAgent;
    }

    /**
     * @param req
     * @return The container, if set, or null.
     */
    private static String getContainer(Series<Parameter> req) {
        Parameter container = req.getFirst("container");
        if (container == null) {
            // The parameter used to be called 'synd' FIXME: schedule removal
            return req.getFirstValue("synd");
        }
        return container.getValue();
    }

    /**
     * @param req
     * @return Debug setting, if set, or null.
     */
    private static Boolean getDebug(Series<Parameter> req) {
        Parameter debug = req.getFirst("debug");
        if (debug == null) {
            return null;
        } else if ("0".equals(debug.getValue())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * @param req
     * @return The ignore cache setting, if appropriate params are set, or null.
     */
    private static Boolean getIgnoreCache(Series<Parameter> req) {
        Parameter ignoreCache = req.getFirst("nocache");
        if (ignoreCache == null) {
            return null;
        } else if ("0".equals(ignoreCache.getValue())) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * @param req
     * @return The locale, if appropriate parameters are set, or null.
     */
    private static Locale getLocale(Series<Parameter> req) {
        String language = req.getFirstValue("lang");
        String country = req.getFirstValue("country");
        if (language == null && country == null) {
            return null;
        } else if (language == null) {
            language = "all";
        } else if (country == null) {
            country = "ALL";
        }
        return new Locale(language, country);
    }

    /**
     * @param req
     * @return module id, if specified
     */
    @SuppressWarnings("boxing")
    private static Integer getModuleId(Series<Parameter> req) {
        String mid = req.getFirstValue("mid");
        if (mid == null) {
            return null;
        }

        try {
            return Integer.parseInt(mid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @param req
     * @return The rendering context, if appropriate params are set, or null.
     */
    private static RenderingContext getRenderingContext(Series<Parameter> req) {
        String c = req.getFirstValue("c");
        if (c == null) {
            return null;
        }
        return c.equals("1") ? RenderingContext.CONTAINER : RenderingContext.GADGET;
    }

    /**
     * @param req
     * @return The ignore cache setting, if appropriate params are set, or null.
     */
    private static Uri getUrl(Series<Parameter> req) {
        String url = req.getFirstValue("url");
        if (url == null) {
            return null;
        }
        try {
            return Uri.parse(url);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @param req
     * @return UserPrefs, if any are set for this request.
     */
    private static UserPrefs getUserPrefs(Series<Parameter> req) {
        Map<String, String> prefs = Maps.newHashMap();
        Iterator<String> paramNames = req.getNames().iterator();
        if (paramNames == null) {
            return null;
        }
        while (paramNames.hasNext()) {
            String paramName = paramNames.next();
            if (paramName.startsWith(USERPREF_PARAM_PREFIX)) {
                String prefName = paramName.substring(USERPREF_PARAM_PREFIX.length());
                prefs.put(prefName, req.getFirstValue(paramName));
            }
        }
        return new UserPrefs(prefs);
    }

    /**
     * @param req
     * @return The view, if specified, or null.
     */
    private static String getView(Series<Parameter> req) {
        return req.getFirstValue("view");
    }
}
