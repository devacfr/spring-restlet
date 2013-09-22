package org.cfr.restlet.ext.shindig.config;

import org.apache.shindig.config.ContainerConfigException;
import org.apache.shindig.expressions.Expressions;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;

@Singleton
public class ConfigurableJsonConfig extends JsonContainerConfig {

    private String contextBase;

    public ConfigurableJsonConfig(JSONObject json, Expressions expressions) {
        super(json, expressions);
    }

    public ConfigurableJsonConfig(String containers, Expressions expressions) throws ContainerConfigException {
        super(containers, expressions);
    }

    @Inject
    public ConfigurableJsonConfig(@Named("shindig.containers.default") String containers, @Nullable @Named("shindig.host") String host,
            @Nullable @Named("shindig.port") String port, @Named("shindig.contextBase") String contextBase, Expressions expressions)
            throws ContainerConfigException {
        super(containers, host, port, expressions);
        this.contextBase = contextBase;
        JSONObject configJson = loadContainers(containers);
        config = createContainers(configJson);
        init();
    }

    @Override
    protected void doAppendPlaceHolder(JSONObject all) throws JSONException {
        all.getJSONObject(DEFAULT_CONTAINER).put("contextBase", contextBase);
    }

}
