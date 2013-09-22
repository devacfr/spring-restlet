package org.cfr.restlet.ext.spring;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.UniformResource;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * Restlet resource that return (on GET) the content of a Spring resource.
 * @author acochard
 */

public abstract class AbstractStaticResource extends AbstractResource {

    private PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper(PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX,
            PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX);

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getPayloadClass() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Variant> getVariants() {
        return Arrays.asList(new Variant(getMediaType()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(UniformResource resource, Variant variant) throws ResourceException {
        try {
            return resolve(IOUtils.toString(getResource().getInputStream()));
        } catch (IOException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
        }
    }

    /**
     * Return the properties to be used when resolving placeholder inside resource stream.
     * @return the properties or null if no placeholder resolving is needed
     */
    protected abstract Properties getProperties();

    /**
     * Return the spring resource's media type.
     * @return the media type
     */
    protected abstract MediaType getMediaType();

    /**
     * Return the spring resource.
     * @return the spring resource
     */
    protected abstract Resource getResource();

    /**
     * Resolve properties placeholder.
     * @param source the string where placeholder must be replaced
     * @return the string with placeholder replaced
     */
    private String resolve(String source) {
        Properties properties = getProperties();
        if (properties != null) {
            return placeholderHelper.replacePlaceholders(source, properties);
        } else {
            return source;
        }

    }
}
