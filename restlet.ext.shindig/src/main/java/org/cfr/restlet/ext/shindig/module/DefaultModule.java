package org.cfr.restlet.ext.shindig.module;

import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.conversion.BeanXStreamConverter;
import org.apache.shindig.protocol.conversion.xstream.XStreamConfiguration;
import org.apache.shindig.social.core.util.BeanXStreamAtomConverter;
import org.apache.shindig.social.core.util.xstream.XStream081Configuration;
import org.cfr.restlet.ext.shindig.http.ClientHttpFetcher;
import org.restlet.Context;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Provides bindings for sample-only implementations of social API
 * classes.  This class should never be used in production deployments,
 * but does provide a good overview of the pieces of Shindig that require
 * custom container implementations.
 */
public class DefaultModule extends AbstractModule {

    private Context context = null;

    public DefaultModule(Context context) {
        this.context = context;
    }

    @Override
    protected void configure() {

        bind(Boolean.class).annotatedWith(Names.named(AnonymousAuthenticationHandler.ALLOW_UNAUTHENTICATED)).toInstance(Boolean.TRUE);
        bind(XStreamConfiguration.class).to(XStream081Configuration.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.xml")).to(BeanXStreamConverter.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.atom")).to(BeanXStreamAtomConverter.class);

        bind(Context.class).toInstance(context);

        /*
         * 
         */
        bind(HttpFetcher.class).to(ClientHttpFetcher.class);
        /*
         * 
         */
        //        bind(ContainerConfig.class).to(JsonContainerConfig.class);
        bind(Long.class).annotatedWith(Names.named("org.apache.shindig.serviceExpirationDurationMinutes")).toInstance(60L);
    }

}