package org.cfr.restlet.ext.spring.resource;

import org.cfr.restlet.ext.spring.utils.StaticHeaderUtil;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.local.DirectoryServerResource;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;




public class WebAppDirectory  extends Directory {

    /**
     * Default constructor
     * @param context
     * @param rootUri
     */
    public WebAppDirectory(Context context, String rootUri) {
        super(context, rootUri);
        setTargetClass(DirectoryServerResource.class);
    }

    @Override
    public ServerResource find(Request request, Response response) {
        StaticHeaderUtil.addResponseHeaders(response);
        return super.find(request, response);
    }
}
