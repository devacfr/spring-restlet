package org.cfr.restlet.ext.spring;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

public class ResourceFinder extends Finder {

    private final IResource resource;

    public ResourceFinder(Context context, IResource resource) {
        super(context, RestletResource.class);
        this.resource = resource;
    }


    @Override
    public ServerResource create(Request request, Response response) {
        return new RestletResource( resource);
    }
}