package org.cfr.restlet.ext.shindig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shindig.gadgets.DefaultGuiceModule;
import org.apache.shindig.gadgets.oauth.OAuthModule;
import org.cfr.restlet.ext.shindig.auth.OAuthResource;
import org.cfr.restlet.ext.shindig.dashboard.resource.impl.ColorResource;
import org.cfr.restlet.ext.shindig.dashboard.resource.impl.DashboardResource;
import org.cfr.restlet.ext.shindig.dashboard.resource.impl.GadgetResource;
import org.cfr.restlet.ext.shindig.dashboard.resource.impl.LayoutResource;
import org.cfr.restlet.ext.shindig.dashboard.resource.impl.NumberGadgetsResource;
import org.cfr.restlet.ext.shindig.dashboard.resource.impl.PrefsResource;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.cfr.restlet.ext.shindig.internal.ServerResourceInjectedFinder;
import org.cfr.restlet.ext.shindig.module.DefaultModule;
import org.cfr.restlet.ext.shindig.module.PropertiesModule;
import org.cfr.restlet.ext.shindig.protocol.ApiResource;
import org.cfr.restlet.ext.shindig.protocol.DataServiceResource;
import org.cfr.restlet.ext.shindig.protocol.JsonRpcResource;
import org.cfr.restlet.ext.shindig.resource.ConcatProxyResource;
import org.cfr.restlet.ext.shindig.resource.GadgetRenderingResource;
import org.cfr.restlet.ext.shindig.resource.HtmlAccelResource;
import org.cfr.restlet.ext.shindig.resource.JsResource;
import org.cfr.restlet.ext.shindig.resource.MakeRequestResource;
import org.cfr.restlet.ext.shindig.resource.OAuthCallbackResource;
import org.cfr.restlet.ext.shindig.resource.ProxyResource;
import org.cfr.restlet.ext.shindig.resource.RpcRelayResource;
import org.cfr.restlet.ext.shindig.resource.RpcResource;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.tools.jmx.Manager;
import com.pmi.restlet.Constants;
import com.pmi.restlet.Resource;

public class ShindigRoute extends Router {

    public static final String SHINDIG_KEY_ATTRIBUTE = "com.pmi.restlet.ext.shindig.ShindigRoute";

    private static final String OPENSOCIAL_ROUTE = "/social";

    private static final String OAUTH_ROUTE = "/oauth";

    private static final String SOCIAL_REST_BASE = OPENSOCIAL_ROUTE + "/rest";

    private static final String SOCIAL_RPC_BASE = OPENSOCIAL_ROUTE + "/rpc";

    private static final String AUTHORIZE_FORM = OAUTH_ROUTE + "/authorize";

    private String gadgetRoute = "";

    protected Injector injector;

    public ShindigRoute(Context context, String gadgetRoute) {
        super(context);
        this.gadgetRoute = gadgetRoute;
        context.getAttributes().put(SHINDIG_KEY_ATTRIBUTE, this);
        doInitializeInboundRoute();
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            Map<String, Object> attrs = getContext().getAttributes();
            Reference baseUrl = new Reference((String) attrs.get(Constants.BASE_URL_KEY));
            String contextPath = (String) attrs.get(Constants.CONTEXT_PATH_KEY);
            startShindig(baseUrl, contextPath);
        }
        super.start();

    }

    private void startShindig(Reference baseUrl, String contextPath) {

        Map<String, String> defaultProperties = new HashMap<String, String>();
        String ctxtPath = contextPath;
        if (gadgetRoute != null)
            ctxtPath += this.gadgetRoute;
        defaultProperties.put(PropertiesModule.CONTEXT_KEY, ctxtPath);
        defaultProperties.put(PropertiesModule.BASE_URL_KEY, baseUrl.toString());

        PropertiesModule propertiesModule = new PropertiesModule(baseUrl);
        propertiesModule.replacePlaceholders(defaultProperties);

        List<Module> modules = new ArrayList<Module>();
        modules.addAll(Arrays.asList(propertiesModule, new DefaultModule(getContext()), new DefaultGuiceModule(), new OAuthModule()/*, new EhCacheModule()*/));
        injector = Guice.createInjector(Stage.PRODUCTION, modules);
        Manager.manage("ShindigGuiceContext", injector);

    }

    protected void doInitializeInboundRoute() {
        Router inboundRouter = this;
        attach(inboundRouter, GadgetRenderingResource.class);
        attach(inboundRouter, ProxyResource.class);
        attach(inboundRouter, MakeRequestResource.class);
        attach(inboundRouter, ConcatProxyResource.class);
        attach(inboundRouter, HtmlAccelResource.class);
        attach(inboundRouter, JsResource.class);
        attach(inboundRouter, RpcResource.class);

        /**
         * OAuth
         */

        attach(inboundRouter, OAuthResource.class);
        attach(inboundRouter, OAuthCallbackResource.class);

        /**
         * protocol
         */
        attach(inboundRouter, JsonRpcResource.class);
        attach(inboundRouter, DataServiceResource.class);
        attach(inboundRouter, RpcRelayResource.class);

        /**
         * Dashboard Api
         */
        attach(inboundRouter, ColorResource.class);
        attach(inboundRouter, DashboardResource.class);
        attach(inboundRouter, GadgetResource.class);
        attach(inboundRouter, LayoutResource.class);
        attach(inboundRouter, NumberGadgetsResource.class);
        attach(inboundRouter, PrefsResource.class);

        /**
         * Internal Resource
         * Note: not directly accessible
         */
        //        attach(component.getInternalRouter(), true, AUTHORIZE_FORM, AuthorizeResource.class);
        //        attach(component.getInternalRouter(), true, "/config/container", ContainerResource.class);
        /**
         * open social
         */
        //        attach(router, false, SOCIAL_RPC_BASE, JsonRpcResource.class);
        //        attach(router, false, SOCIAL_REST_BASE, DataServiceResource.class);
        getContext().getAttributes().put(JsonRpcResource.class.getName() + '.' + ApiResource.HANDLERS_PARAM, "org.apache.shindig.gadgets.handlers");

    }

    /**
     *  Attaches a target Resource class to this router based on a given URI
     * pattern. A new route using the default matching mode (
     * {@link Router#getDefaultMatchingMode()}) will be added routing to the target
     * when calls with a URI matching the pattern will be received.
     * @param router
     * @param strict The matching mode to use when parsing a formatted reference.
     * @param path
     * @param targetClass
     * @return
     */
    protected TemplateRoute attach(Router router, boolean strict, String path, Class<?> targetClass) {
        TemplateRoute route = null;
        if (InjectedResource.class.isAssignableFrom(targetClass)) {
            route = router.attach(path, new ServerResourceInjectedFinder(getContext(), targetClass));
        } else {
            route = router.attach(path, targetClass);
        }
        if (strict) {
            route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
        } else {
            route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
        }
        return route;
    }

    protected TemplateRoute attach(Router router, Class<?> targetClass) {
        TemplateRoute route = null;
        Resource resource = AnnotationUtils.findAnnotation(targetClass, Resource.class);
        if (resource != null) {
            route = router.attach(resource.path(), new ServerResourceInjectedFinder(getContext(), targetClass));
            if (resource.strict()) {
                route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
            } else {
                route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
            }
        } else {
            throw new RuntimeException("Resource " + targetClass + " require @Resouce annotation.");
        }

        return route;
    }

    public void injectMembers(Object inject) {
        Assert.notNull(this.injector, "Guice context is required");
        this.injector.injectMembers(inject);
    }

    public Injector getInjector() {
        return injector;
    }

    public String getGadgetRoute() {
        return gadgetRoute;
    }
}
