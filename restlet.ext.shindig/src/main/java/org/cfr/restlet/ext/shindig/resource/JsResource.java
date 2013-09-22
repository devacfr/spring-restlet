package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.servlet.JsServlet;
import org.apache.shindig.gadgets.uri.JsUriManager;
import org.apache.shindig.gadgets.uri.UriStatus;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;

/**
 * Simple servlet serving up JavaScript files by their registered aliases.
 * Used by type=URL gadgets in loading JavaScript resources.
 * Based on {@link JsServlet}
 */
@Resource(path = "/js", strict = false)
public class JsResource extends InjectedResource {

    static final String ONLOAD_JS_TPL = "(function() {" + "var nm='%s';" + "if (typeof window[nm]==='function') {" + "window[nm]();" + '}' + "})();";

    private static final Pattern ONLOAD_FN_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

    private transient JsHandler jsHandler;

    private transient JsUriManager jsUriManager;

    @Inject
    public void setJsHandler(JsHandler jsHandler) {
        this.jsHandler = jsHandler;
    }

    @Inject
    public void setUrlGenerator(JsUriManager jsUriManager) {
        this.jsUriManager = jsUriManager;
    }

    @Get("javascript")
    public Representation doGet() throws IOException {

        ContextResource contextResource = getContextResource();
        // If an If-Modified-Since header is ever provided, we always say
        // not modified. This is because when there actually is a change,
        // cache busting should occur.
        UriStatus vstatus = null;
        try {
            vstatus = jsUriManager.processExternJsUri(ResourceUtil.toUri(contextResource.getRequest().getResourceRef())).getStatus();
        } catch (GadgetException e) {
            contextResource.setStatus(Status.valueOf(e.getHttpStatusCode()));
            contextResource.append(e.getMessage());
            return contextResource.consolidate();
        }

        if (contextResource.getRequestHeaders().getFirstValue(HeaderConstants.HEADER_IF_MODIFIED_SINCE) != null
                && vstatus == UriStatus.VALID_VERSIONED) {
            contextResource.setStatus(Status.REDIRECTION_NOT_MODIFIED);
            return null;
        }

        // Get JavaScript content from features aliases request.
        JsHandler.JsHandlerResponse handlerResponse = jsHandler.getJsContent(contextResource);
        StringBuilder jsData = handlerResponse.getJsData();
        boolean isProxyCacheable = handlerResponse.isProxyCacheable();

        // Add onload handler to add callback function.
        String onloadStr = contextResource.getParameter("onload");
        if (onloadStr != null) {
            if (!ONLOAD_FN_PATTERN.matcher(onloadStr).matches()) {
                contextResource.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid onload callback specified");
                return contextResource.consolidate();
            }
            jsData.append(String.format(ONLOAD_JS_TPL, StringEscapeUtils.escapeJavaScript(onloadStr)));
        }

        if (jsData.length() == 0) {
            contextResource.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return contextResource.consolidate();
        }

        // post JavaScript content fetching
        postJsContentProcessing(contextResource, vstatus, isProxyCacheable);

        contextResource.setMediaType(MediaType.TEXT_JAVASCRIPT);
        contextResource.setCharacterSet(CharacterSet.UTF_8);
        contextResource.append(jsData.toString());
        return contextResource.consolidate();
    }

    /**
     * Provides post JavaScript content processing. The default behavior will check the UriStatus and
     * update the response header with cache option.
     * 
     * @param contextResource The contextResource object.
     * @param vstatus The UriStatus object.
     * @param isProxyCacheable boolean true if content is cacheable and false otherwise.
     */
    protected void postJsContentProcessing(ContextResource contextResource, UriStatus vstatus, boolean isProxyCacheable) {
        switch (vstatus) {
            case VALID_VERSIONED:
                // Versioned files get cached indefinitely
                ResourceUtil.setCachingHeaders(contextResource, !isProxyCacheable);
                break;
            case VALID_UNVERSIONED:
                // Unversioned files get cached for 1 hour.
                ResourceUtil.setCachingHeaders(contextResource, 60 * 60, !isProxyCacheable);
                break;
            case INVALID_VERSION:
                // URL is invalid in some way, likely version mismatch.
                // Indicate no-cache forcing subsequent requests to regenerate URLs.
                ResourceUtil.setNoCache(contextResource);
                break;
        }
    }
}