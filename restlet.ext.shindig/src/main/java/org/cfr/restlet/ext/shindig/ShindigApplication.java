package org.cfr.restlet.ext.shindig;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;

public class ShindigApplication extends Application {

    public ShindigApplication(Context context) {
        super(context);
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
        return new ShindigRoute(getContext(), "");
    }

    @Override
    public ShindigRoute getInboundRoot() {
        return (ShindigRoute) super.getInboundRoot();
    }

}
