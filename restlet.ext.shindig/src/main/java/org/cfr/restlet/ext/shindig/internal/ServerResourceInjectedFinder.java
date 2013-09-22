package org.cfr.restlet.ext.shindig.internal;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

public class ServerResourceInjectedFinder extends Finder {

    public ServerResourceInjectedFinder() {
        super();
    }

    public ServerResourceInjectedFinder(Context context, Class<?> targetClass) {
        super(context, targetClass);
    }

    public ServerResourceInjectedFinder(Context context) {
        super(context);
    }

    @Override
    public ServerResource create(Request request, Response response) {
        ServerResource resource = super.create(request, response);
        return resource;
    }
}