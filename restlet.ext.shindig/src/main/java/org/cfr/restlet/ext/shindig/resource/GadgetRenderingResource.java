package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.render.Renderer;
import org.apache.shindig.gadgets.render.RenderingResults;
import org.apache.shindig.gadgets.servlet.GadgetRenderingServlet;
import org.apache.shindig.gadgets.uri.IframeUriManager;
import org.apache.shindig.gadgets.uri.UriStatus;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.cfr.restlet.ext.shindig.internal.ResourceGadgetContext;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;

/**
 * Based on {@link GadgetRenderingServlet}
 * 
 * @author cfriedri
 * 
 */
@Resource(path = "/ifr", strict = true)
public class GadgetRenderingResource extends InjectedResource {

    private static final long serialVersionUID = -5634040113214794888L;

    static final int DEFAULT_CACHE_TTL = 60 * 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(GadgetRenderingResource.class.getName());

    private transient Renderer renderer;

    private transient IframeUriManager iframeUriManager;

    @Inject
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    @Inject
    public void setIframeUriManager(IframeUriManager iframeUriManager) {
        this.iframeUriManager = iframeUriManager;
    }

    @Get("html")
    public Representation doGet() throws IOException {
        // If an If-Modified-Since header is ever provided, we always say
        // not modified. This is because when there actually is a change,
        // cache busting should occur.
        UriStatus urlstatus = getUrlStatus();
        if (getContextResource().getRequestHeaders().getFirst(HeaderConstants.HEADER_IF_MODIFIED_SINCE) != null
                && !"1".equals(getContextResource().getParameter("nocache")) && urlstatus == UriStatus.VALID_VERSIONED) {
            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
            return null;
        }
        return render(urlstatus);
    }

    @Post("html")
    public Representation doPost() throws IOException {
        return render(getUrlStatus());
    }

    protected Representation render(UriStatus urlstatus) throws IOException {

        ContextResource contextResource = getContextResource();

        if (contextResource.getRequestHeaders().getFirst(HttpRequest.DOS_PREVENTION_HEADER) != null) {
            // Refuse to render for any request that came from us.
            // TODO: Is this necessary for any other type of request? Rendering
            // seems to be the only one
            // that can potentially result in an infinite loop.
            contextResource.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return null;
        }

        contextResource.setMediaType(MediaType.TEXT_HTML);
        GadgetContext context = new ResourceGadgetContext(contextResource);
        RenderingResults results = renderer.render(context);

        // process the rendering results
        postGadgetRendering(new PostGadgetRenderingParams(contextResource, urlstatus, context, results));
        return contextResource.consolidate();
    }

    /**
     * Implementations that extend this class are strongly discouraged from overriding this method.
     * To customize the behavior please override the hook methods for each of the
     * RenderingResults.Status enum values instead. 
     */
    protected void postGadgetRendering(PostGadgetRenderingParams params) throws IOException {
        switch (params.getResults().getStatus()) {
            case OK:
                onOkRenderingResultsStatus(params);
                break;
            case ERROR:
                onErrorRenderingResultsStatus(params);
                break;
            case MUST_REDIRECT:
                onMustRedirectRenderingResultsStatus(params);
                break;
        }
    }

    protected void onOkRenderingResultsStatus(PostGadgetRenderingParams params) throws IOException {
        UriStatus urlStatus = params.getUrlStatus();
        ContextResource contextResource = params.getContextResource();
        if (params.getContext().getIgnoreCache() || urlStatus == UriStatus.INVALID_VERSION) {
            ResourceUtil.setCachingHeaders(contextResource, 0);
        } else if (urlStatus == UriStatus.VALID_VERSIONED) {
            // Versioned files get cached indefinitely
            ResourceUtil.setCachingHeaders(contextResource, true);
        } else {
            // Unversioned files get cached for 5 minutes by default, but this can be overridden
            // with a query parameter.
            int ttl = DEFAULT_CACHE_TTL;
            String ttlStr = contextResource.getParameter(Param.REFRESH.getKey());
            if (!StringUtils.isEmpty(ttlStr)) {
                try {
                    ttl = Integer.parseInt(ttlStr);
                } catch (NumberFormatException e) {
                    // Ignore malformed TTL value
                    LOGGER.info("Bad TTL value '" + ttlStr + "' was ignored");
                }
            }
            ResourceUtil.setCachingHeaders(contextResource, ttl, true);
        }
        contextResource.setText(params.getResults().getContent());
    }

    protected void onErrorRenderingResultsStatus(PostGadgetRenderingParams params) throws IOException {
        ContextResource contextResource = params.getContextResource();
        contextResource.setStatus(Status.valueOf(params.getResults().getHttpStatusCode()));
        contextResource.setText(StringEscapeUtils.escapeHtml(params.getResults().getErrorMessage()));
    }

    protected void onMustRedirectRenderingResultsStatus(PostGadgetRenderingParams params) throws IOException {
        params.getContextResource().getResponse().redirectTemporary(params.getResults().getRedirect().toString());
    }

    private UriStatus getUrlStatus() {
        return iframeUriManager.validateRenderingUri(ResourceUtil.toUri(getRequest().getResourceRef()));
    }

    /**
     * Contains the input parameters for post rendering methods.
     */
    protected static class PostGadgetRenderingParams {

        private ContextResource contextResource;

        private UriStatus urlStatus;

        private GadgetContext context;

        private RenderingResults results;

        public PostGadgetRenderingParams(ContextResource contextResource, UriStatus urlStatus, GadgetContext context, RenderingResults results) {
            this.contextResource = contextResource;
            this.urlStatus = urlStatus;
            this.context = context;
            this.results = results;
        }

        public ContextResource getContextResource() {
            return contextResource;
        }

        public UriStatus getUrlStatus() {
            return urlStatus;
        }

        public GadgetContext getContext() {
            return context;
        }

        public RenderingResults getResults() {
            return results;
        }
    }

}
