package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.servlet.MakeRequestServlet;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;

/**
 * Handles calls to gadgets.io.makeRequest.
 *
 * GET and POST are supported so as to facilitate improved browser caching.
 *
 * Currently this just delegates to MakeRequestHandler, which deals with both
 * makeRequest and open proxy calls.
 * Based on {@link MakeRequestServlet}
 */
@Resource(path = "/makeRequest", strict = true)
public class MakeRequestResource extends InjectedResource {

    private MakeRequestHandler makeRequestHandler;

    @Inject
    public void setMakeRequestHandler(MakeRequestHandler makeRequestHandler) {
        this.makeRequestHandler = makeRequestHandler;
    }

    @Get
    public Representation doGet() throws IOException {
        ContextResource contextResource = getContextResource();
        try {
            makeRequestHandler.fetch(contextResource);
        } catch (GadgetException e) {
            int responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            if (e.getCode() != GadgetException.Code.INTERNAL_SERVER_ERROR) {
                responseCode = HttpServletResponse.SC_BAD_REQUEST;
            }
            contextResource.setStatus(Status.valueOf(responseCode), e.getMessage() != null ? e.getMessage() : "");
        }
        return contextResource.consolidate();
    }

    @Post
    public Representation doPost() throws IOException {
        return doGet();
    }
}