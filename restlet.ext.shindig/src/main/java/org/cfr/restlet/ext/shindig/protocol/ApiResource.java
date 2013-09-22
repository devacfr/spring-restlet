package org.cfr.restlet.ext.shindig.protocol;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.ApiServlet;
import org.apache.shindig.protocol.HandlerRegistry;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.ResponseItem;
import org.apache.shindig.protocol.SystemHandler;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.cfr.restlet.ext.shindig.auth.AuthInfo;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.restlet.data.CharacterSet;
import org.restlet.resource.ResourceException;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Based of {@link ApiServlet}
 * 
 * @author cfriedri
 * 
 */
public abstract class ApiResource extends InjectedResource {

    private static final Logger logger = Logger.getLogger(ApiResource.class.getName());

    protected static final String FORMAT_PARAM = "format";

    protected static final String JSON_FORMAT = "json";

    protected static final String ATOM_FORMAT = "atom";

    protected static final String XML_FORMAT = "xml";

    protected static final CharacterSet DEFAULT_ENCODING = CharacterSet.UTF_8;

    /**
     * ServletConfig parameter set to provide an explicit named binding for
     * handlers
     */
    public static final String HANDLERS_PARAM = "handlers";

    /**
     * The default key used to look up handlers if the servlet config parameter
     * is not available
     */
    public static final Key<Set<Object>> DEFAULT_HANDLER_KEY = Key.get(new TypeLiteral<Set<Object>>() {}, Names
            .named("org.apache.shindig.protocol.handlers"));

    protected HandlerRegistry dispatcher;

    protected BeanJsonConverter jsonConverter;

    protected BeanConverter xmlConverter;

    protected BeanConverter atomConverter;

    protected ContainerConfig containerConfig;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        // Lookup the set of handlers to bind to this api endpoint and
        // populate the registry with them
        String handlers = (String) getContext().getAttributes().get(this.getClass().getName() + '.' + HANDLERS_PARAM);
        Key<Set<Object>> handlerKey;
        if (handlers == null || "".equals(handlers)) {
            handlerKey = DEFAULT_HANDLER_KEY;
        } else {
            handlerKey = Key.get(new TypeLiteral<Set<Object>>() {}, Names.named(handlers));
        }
        this.dispatcher.addHandlers(getInjector().getInstance(handlerKey));
        this.dispatcher.addHandlers(Collections.<Object> singleton(new SystemHandler(dispatcher)));

    }

    @Inject
    public void setHandlerRegistry(HandlerRegistry dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Inject
    public void setContainerConfig(ContainerConfig containerConfig) {
        this.containerConfig = containerConfig;
    }

    @Inject
    public void setBeanConverters(@Named("shindig.bean.converter.json") BeanConverter jsonConverter,
            @Named("shindig.bean.converter.xml") BeanConverter xmlConverter, @Named("shindig.bean.converter.atom") BeanConverter atomConverter) {
        // fix this
        this.jsonConverter = (BeanJsonConverter) jsonConverter;
        this.xmlConverter = xmlConverter;
        this.atomConverter = atomConverter;
    }

    protected SecurityToken getSecurityToken(ContextResource contextResource) {
        return new AuthInfo(contextResource.getRequest()).getSecurityToken();
    }

    protected abstract void sendError(ContextResource contextResource, ResponseItem responseItem) throws IOException;

    protected void sendSecurityError(ContextResource contextResource) throws IOException {
        sendError(contextResource, new ResponseItem(HttpServletResponse.SC_UNAUTHORIZED,
                "The request did not have a proper security token nor oauth message and unauthenticated " + "requests are not allowed"));
    }

    protected ResponseItem getResponseItem(Future<?> future) {
        try {
            // TODO: use timeout methods?
            Object result = future != null ? future.get() : null;
            // TODO: null is now a supported return value for post/delete, but
            // is bad for get().
            return new ResponseItem(result != null ? result : Collections.emptyMap());
        } catch (InterruptedException ie) {
            return responseItemFromException(ie);
        } catch (ExecutionException ee) {
            return responseItemFromException(ee.getCause());
        }
    }

    protected ResponseItem responseItemFromException(Throwable t) {
        if (t instanceof ProtocolException) {
            ProtocolException pe = (ProtocolException) t;
            logger.log(Level.INFO, "Returning a response error as result of a protocol exception", pe);
            return new ResponseItem(pe.getCode(), pe.getMessage(), pe.getResponse());
        }
        logger.log(Level.WARNING, "Returning a response error as result of an exception", t);
        return new ResponseItem(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
    }

    protected void setCharacterEncodings(ContextResource contextResource) throws IOException {
        if (contextResource.getCharacterSet() == null) {
            contextResource.setCharacterSet(DEFAULT_ENCODING);
        }
    }

}
