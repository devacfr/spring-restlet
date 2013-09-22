package org.cfr.restlet.ext.shindig.internal;

import java.io.IOException;

import org.cfr.restlet.ext.shindig.ShindigRoute;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;


public class BaseResource extends ServerResource {

    ContextResource contextResource;

    public ContextResource getContextResource() {
        return contextResource;
    }

    protected ShindigRoute getShindigRoute() {
        if (this.getContext() == null)
            return null;
        return (ShindigRoute) this.getContext().getAttributes().get(ShindigRoute.SHINDIG_KEY_ATTRIBUTE);
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            contextResource = new ContextResource(getRequest(), getResponse());
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }

}
