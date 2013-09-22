package org.cfr.restlet.ext.shindig.module;

import java.util.List;

import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.UrlParameterAuthenticationHandler;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;


/**
 * Binds auth types used by gadget rendering. This should be used when running a stand-alone gadget
 * renderer.
 */
public class AuthenticationModule extends AbstractModule {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(new TypeLiteral<List<AuthenticationHandler>>(){}).toProvider(AuthProvider.class);
    }

    private static class AuthProvider implements Provider<List<AuthenticationHandler>> {
        private final List<AuthenticationHandler> handlers;

        @Inject
        public AuthProvider(UrlParameterAuthenticationHandler urlParameterAuthHandler,
                AnonymousAuthenticationHandler anonymoustAuthHandler) {
            handlers = Lists.newArrayList(urlParameterAuthHandler, anonymoustAuthHandler);
        }

        public List<AuthenticationHandler> get() {
            return handlers;
        }
    }

}