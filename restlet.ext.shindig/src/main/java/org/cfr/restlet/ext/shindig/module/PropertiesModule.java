package org.cfr.restlet.ext.shindig.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.config.ContainerConfig;
import org.cfr.restlet.ext.shindig.config.ConfigurableJsonConfig;
import org.restlet.data.Reference;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;

/**
 * Injects everything from the a property file as a Named value
 * Uses the default shindig.properties file if no other is provided
 */
public class PropertiesModule extends AbstractModule {

    public static final String CONTEXT_KEY = "context-path";

    public static final String BASE_URL_KEY = "local-base-url";

    /** Default placeholder prefix: "${" */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    /** Default placeholder suffix: "}" */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /** Default value separator: ":" */
    public static final String DEFAULT_VALUE_SEPARATOR = ":";

    private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

    private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

    private String valueSeparator = DEFAULT_VALUE_SEPARATOR;

    private boolean ignoreUnresolvablePlaceholders = false;

    private final static String DEFAULT_PROPERTIES = "config/kbase-shindig.properties";

    private final Properties properties;

    private final Reference serverReference;

    public PropertiesModule(Reference reference) {
        this(reference, getDefaultPropertiesPath());
    }

    public PropertiesModule(Reference reference, String propertyFile) {
        this.properties = readPropertyFile(propertyFile);
        this.serverReference = reference;
    }

    public PropertiesModule(Reference reference, Properties properties) {
        this.properties = properties;
        this.serverReference = reference;

    }

    /**
     * replace placeholder in properties file with value;
     * @param placeholders placeholder to replace
     */
    public final void replacePlaceholders(Map<String, String> placeholders) {
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator,
                ignoreUnresolvablePlaceholders);
        PlaceholderResolver resolver = new PlaceholderConfigurerResolver(placeholders);
        for (Object key : this.properties.keySet()) {
            Object val = this.properties.get(key);
            this.properties.setProperty(key.toString(), helper.replacePlaceholders(val.toString(), resolver));
        }

    }

    @Override
    protected void configure() {
        Names.bindProperties(this.binder(), getProperties());
        // This could be generalized to inject any system property...
        this.binder().bindConstant().annotatedWith(Names.named("shindig.port")).to(String.valueOf(getServerPort()));
        this.binder().bindConstant().annotatedWith(Names.named("shindig.host")).to(getServerHostname());
        bind(ContainerConfig.class).to(ConfigurableJsonConfig.class);
    }

    /**
     * Should return the port that the current server is running on.  Useful for testing and working out of the box configs.
     * Looks for a port in system properties "shindig.port" then "jetty.port", if not set uses fixed value of "8080"
     * @return an integer port number as a string.
     */
    protected int getServerPort() {
        return serverReference.getHostPort();
    }

    /*
     * Should return the hostname that the current server is running on.  Useful for testing and working out of the box configs.
     * Looks for a hostname in system properties "shindig.host", if not set uses fixed value of "localhost"
     * @return a hostname
     */

    protected String getServerHostname() {
        return serverReference.getHostDomain();
    }

    protected static String getDefaultPropertiesPath() {
        return DEFAULT_PROPERTIES;
    }

    protected Properties getProperties() {
        return properties;
    }

    private Properties readPropertyFile(String propertyFile) {

        Properties properties = new Properties();
        InputStream is = null;
        try {
            is = ResourceLoader.openResource(propertyFile);
            properties.load(is);
        } catch (IOException e) {
            throw new CreationException(Arrays.asList(new Message("Unable to load properties: " + propertyFile)));
        } finally {
            IOUtils.closeQuietly(is);
        }

        return properties;
    }

    private class PlaceholderConfigurerResolver implements PlaceholderResolver {

        private final Map<String, String> props;

        private PlaceholderConfigurerResolver(Map<String, String> props) {
            this.props = props;
        }

        public String resolvePlaceholder(String placeholderName) {
            return props.get(placeholderName);
        }
    }
}