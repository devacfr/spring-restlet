package org.cfr.restlet.ext.spring;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/**
 * A simple restlet that is returned as root, while allowing to recreate roots in applications per application request.
 *
 * @author cstamas
 */
public class RetargetableRestlet extends Filter {

    public RetargetableRestlet(Context context) {
        super(context);
    }

    @Override
    protected int doHandle(Request request, Response response) {
        if (getNext() != null) {
            return super.doHandle(request, response);
        } else {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);

            return CONTINUE;
        }

    }
}