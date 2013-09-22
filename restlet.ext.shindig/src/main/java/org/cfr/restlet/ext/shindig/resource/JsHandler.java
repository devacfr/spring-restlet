package org.cfr.restlet.ext.shindig.resource;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.JsonSerializer;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.RenderingContext;
import org.apache.shindig.gadgets.config.ConfigContributor;
import org.apache.shindig.gadgets.features.FeatureRegistry;
import org.apache.shindig.gadgets.features.FeatureResource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JsHandler {

    protected final FeatureRegistry registry;

    protected final ContainerConfig containerConfig;

    protected final Map<String, ConfigContributor> configContributors;

    @Inject
    public JsHandler(FeatureRegistry registry, ContainerConfig containerConfig, Map<String, ConfigContributor> configContributors) {
        this.registry = registry;
        this.containerConfig = containerConfig;
        this.configContributors = configContributors;
    }

    /**
     * Get the JavaScript content from the feature name aliases.
     *
     * @param req the HttpRequest object.
     * @return JsHandlerResponse object that contains JavaScript data and cacheable flag.
     */
    public JsHandlerResponse getJsContent(final ContextResource contextResource) {
        // get the Set of features needed from request
        Set<String> needed = getFeaturesNeeded(contextResource);

        // get the GadgetContext instance
        GadgetContext ctx = getGadgetContext(contextResource);

        // get js data from feature resoources.
        return getFeatureResourcesContent(contextResource, ctx, needed);
    }

    /**
     * Get the Set of feature names from the request.
     *
     * @param req the HttpServletRequest object.
     * @return Set of names of needed JavaScript as feature aliases from the request.
     */
    protected Set<String> getFeaturesNeeded(final ContextResource contextResource) {
        // Use the last component as filename; prefix is ignored
        String uri = contextResource.getReference().getPath();
        // We only want the file name part. There will always be at least 1 slash
        // (the server root), so this is always safe.
        String resourceName = uri.substring(uri.lastIndexOf('/') + 1);
        if (resourceName.endsWith(".js")) {
            // Lop off the suffix for lookup purposes
            resourceName = resourceName.substring(0, resourceName.length() - ".js".length());
        }

        Set<String> needed = ImmutableSet.of(StringUtils.split(resourceName, ':'));
        return needed;
    }

    /**
     * Get the GadgetContext to be used when calling FeatureRegistry.getFeatureResources.
     * 
     * @param req the HttpServletRequest object.
     * @return GadgetContext instance.
     */
    protected GadgetContext getGadgetContext(final ContextResource contextResource) {
        String containerParam = contextResource.getParameter("container");
        String containerStr = contextResource.getParameter("c");

        final RenderingContext context = "1".equals(containerStr) ? RenderingContext.CONTAINER : RenderingContext.GADGET;
        final String container = containerParam != null ? containerParam : ContainerConfig.DEFAULT_CONTAINER;

        return new JsGadgetContext(context, container);
    }

    /**
     * Get the content of the feature resources and push it to jsData.
     * 
     * @param req The HttpServletRequest object.
     * @param ctx GadgetContext object.
     * @param needed Set of requested feature names.
     * @return JsHandlerResponse object that contains JavaScript data and cacheable flag.
     */
    protected JsHandlerResponse getFeatureResourcesContent(final ContextResource contextResource, final GadgetContext ctx, Set<String> needed) {
        StringBuilder jsData = new StringBuilder();
        Collection<? extends FeatureResource> resources = registry.getFeatureResources(ctx, needed, null);
        String debugStr = contextResource.getParameter("debug");
        boolean debug = "1".equals(debugStr);
        String container = ctx.getContainer();
        boolean isProxyCacheable = true;

        for (FeatureResource featureResource : resources) {
            String content = debug ? featureResource.getDebugContent() : featureResource.getContent();
            if (!featureResource.isExternal()) {
                jsData.append(content);
            } else {
                // Support external/type=url feature serving through document.write()
                jsData.append("document.write('<script src=\"").append(content).append("\"></script>')");
            }
            isProxyCacheable = isProxyCacheable && featureResource.isProxyCacheable();
            jsData.append(";\n");
        }

        if (ctx.getRenderingContext() == RenderingContext.CONTAINER) {
            // Append some container specific things
            Map<String, Object> features = containerConfig.getMap(container, "gadgets.features");
            Map<String, Object> config = Maps.newHashMapWithExpectedSize(features == null ? 2 : features.size() + 2);

            if (features != null) {
                // Discard what we don't care about.
                for (String name : registry.getFeatures(needed)) {
                    Object conf = features.get(name);
                    // Add from containerConfig
                    if (conf != null) {
                        config.put(name, conf);
                    }
                    ConfigContributor contributor = configContributors.get(name);
                    if (contributor != null) {
                        contributor.contribute(config, container, contextResource.getRequestHeaders().getFirstValue("Host"));
                    }
                }
                jsData.append("gadgets.config.init(").append(JsonSerializer.serialize(config)).append(");\n");
            }
        }
        return new JsHandlerResponse(jsData, isProxyCacheable);
    }

    /**
     * Define the response data from JsHandler.
     */
    public static class JsHandlerResponse {

        private final boolean isProxyCacheable;

        private final StringBuilder jsData;

        public JsHandlerResponse(StringBuilder jsData, boolean isProxyCacheable) {
            this.jsData = jsData;
            this.isProxyCacheable = isProxyCacheable;
        }

        public boolean isProxyCacheable() {
            return isProxyCacheable;
        }

        public StringBuilder getJsData() {
            return jsData;
        }
    }

    /**
     * GadgetContext for JsHandler called by FeatureRegistry when fetching the resources.
     */
    protected static class JsGadgetContext extends GadgetContext {

        private final RenderingContext renderingContext;

        private final String container;

        public JsGadgetContext(RenderingContext renderingContext, String container) {
            this.renderingContext = renderingContext;
            this.container = container;
        }

        @Override
        public RenderingContext getRenderingContext() {
            return renderingContext;
        }

        @Override
        public String getContainer() {
            return container;
        }
    }
}
