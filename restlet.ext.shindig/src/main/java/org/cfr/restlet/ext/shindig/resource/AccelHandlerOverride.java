package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;

import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.RequestPipeline;
import org.apache.shindig.gadgets.rewrite.ResponseRewriterRegistry;
import org.apache.shindig.gadgets.servlet.AccelHandler;
import org.apache.shindig.gadgets.uri.AccelUriManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Override {@link AccelHandler} to put {@link #fetch(HttpRequest)} function visible.
 * @author cfriedri
 *
 */
@Singleton
public class AccelHandlerOverride extends AccelHandler {

    /* copy of AccelHandler class */
    public static final String ERROR_FETCHING_DATA = "Error fetching data";

    @Inject
    public AccelHandlerOverride(RequestPipeline requestPipeline,
            @Named("shindig.accelerate.response.rewriter.registry")
            ResponseRewriterRegistry contentRewriterRegistry,
            AccelUriManager accelUriManager,
            @Named("shindig.accelerate.remapInternalServerError")
            Boolean remapInternalServerError) {
        super(requestPipeline, contentRewriterRegistry, accelUriManager, remapInternalServerError);
    }


    @Override
    public HttpResponse fetch(HttpRequest request) throws IOException, GadgetException {
        return super.fetch(request);
    }
}
