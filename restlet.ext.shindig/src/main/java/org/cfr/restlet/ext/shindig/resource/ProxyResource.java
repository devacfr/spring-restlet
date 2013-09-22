package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.LockedDomainService;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.servlet.ProxyHandler;
import org.apache.shindig.gadgets.uri.ProxyUriManager;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;

/**
 * Handles open proxy requests (used in rewriting and for URLs returned by
 * gadgets.io.getProxyUrl).
 */
@Resource(path = "/proxy", strict = true)
public class ProxyResource extends InjectedResource {

    private static final long serialVersionUID = 9085050443492307723L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyResource.class.getName());

    private transient ProxyUriManager proxyUriManager;

    private transient LockedDomainService lockedDomainService;

    private transient ProxyHandler proxyHandler;

    @Inject
    public void setProxyHandler(ProxyHandler proxyHandler) {
        this.proxyHandler = proxyHandler;
    }

    @Inject
    public void setProxyUriManager(ProxyUriManager proxyUriManager) {
        this.proxyUriManager = proxyUriManager;
    }

    @Inject
    public void setLockedDomainService(LockedDomainService lockedDomainService) {
        this.lockedDomainService = lockedDomainService;
    }

    @Get
    public Representation doGet() throws IOException {
        ContextResource contextResource = getContextResource();
        if (contextResource.getRequestHeaders().getFirstValue("If-Modified-Since") != null) {
            contextResource.setStatus(Status.REDIRECTION_NOT_MODIFIED);
            return contextResource.consolidate();
        }

        Uri reqUri = ResourceUtil.toUri(contextResource.getReference());
        HttpResponse response = null;
        try {
            // Parse request uri:
            ProxyUriManager.ProxyUri proxyUri = proxyUriManager.process(reqUri);

            // TODO: Consider removing due to redundant logic.
            String host = contextResource.getRequestHeaders().getFirstValue("Host");
            if (!lockedDomainService.isSafeForOpenProxy(host)) {
                // Force embedded images and the like to their own domain to avoid XSS
                // in gadget domains.
                Uri resourceUri = proxyUri.getResource();
                String msg = "Embed request for url " + (resourceUri != null ? resourceUri.toString() : "n/a") + " made to wrong domain " + host;
                LOGGER.info(msg);
                throw new GadgetException(GadgetException.Code.INVALID_PARAMETER, msg, HttpResponse.SC_BAD_REQUEST);
            }

            response = proxyHandler.fetch(proxyUri);
        } catch (GadgetException e) {
            response = ServletUtil.errorResponse(new GadgetException(e.getCode(), e.getMessage(), HttpServletResponse.SC_BAD_REQUEST));
        }

        return ServletUtil.copyResponseToContext(response, contextResource);
    }
}