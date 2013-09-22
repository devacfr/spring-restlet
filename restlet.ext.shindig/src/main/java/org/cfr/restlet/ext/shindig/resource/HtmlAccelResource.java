package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;

import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.servlet.HtmlAccelServlet;
import org.apache.shindig.gadgets.uri.AccelUriManager;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;

/**
 * Handles requests for accel servlet.
 * The objective is to accelerate web pages.
 */
@Resource(path = "/accel", strict = false)
public class HtmlAccelResource extends InjectedResource {

    private AccelHandlerOverride accelHandler;

    private static Logger logger = LoggerFactory.getLogger(HtmlAccelServlet.class.getName());

    public static final String ACCEL_GADGET_PARAM_NAME = "accelGadget";

    public static final String ACCEL_GADGET_PARAM_VALUE = "true";

    @Inject
    public void setHandler(AccelHandlerOverride accelHandler) {
        this.accelHandler = accelHandler;
    }

    @Get
    public Representation doGet() throws IOException {
        ContextResource contextResource = this.getContextResource();
        logger.info("Accel request = " + contextResource.getReference().toString());
        HttpRequest req = ServletUtil.fromRequest(contextResource);
        req.setContainer(AccelUriManager.CONTAINER);
        HttpResponse response = null;
        try {
            response = accelHandler.fetch(req);
        } catch (GadgetException e) {
            response = ServletUtil.errorResponse(e);
        }

        return ServletUtil.copyResponseToContext(response, contextResource);
    }

    @Post
    protected void doPost() throws IOException {
        doGet();
    }

    public static boolean isAccel(GadgetContext context) {
        return context.getParameter(ACCEL_GADGET_PARAM_NAME) == ACCEL_GADGET_PARAM_VALUE;
    }
}
