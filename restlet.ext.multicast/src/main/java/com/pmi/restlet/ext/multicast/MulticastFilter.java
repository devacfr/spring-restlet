package com.pmi.restlet.ext.multicast;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.routing.Filter;

public class MulticastFilter extends Filter {

    private MulticastService register;

    public MulticastFilter(Context context, MulticastService register) {
        super(context);
        this.register = register;
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
        if (!register.isStarted()) {
            Channel localChannel = this.register.getLocalChannel();
            localChannel.setContextPath(request.getRootRef().getPath());
            localChannel.setHostPort(request.getRootRef().getHostPort());
            try {
                register.internalStart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return super.beforeHandle(request, response);
    }

}
