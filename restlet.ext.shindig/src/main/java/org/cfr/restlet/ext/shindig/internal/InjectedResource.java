package org.cfr.restlet.ext.shindig.internal;

import org.apache.shindig.common.servlet.InjectedServlet;
import org.restlet.resource.ResourceException;

import com.google.inject.Injector;

/**
 * Based on {@link InjectedServlet}
 * @author cfriedri
 *
 */
public class InjectedResource extends BaseResource {

    public void registerInject() {
        if (getShindigRoute() != null)
            getShindigRoute().injectMembers(this);
    }

    protected Injector getInjector() {
        if (getShindigRoute() != null)
            return getShindigRoute().getInjector();
        return null;
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        registerInject();
    }

}
