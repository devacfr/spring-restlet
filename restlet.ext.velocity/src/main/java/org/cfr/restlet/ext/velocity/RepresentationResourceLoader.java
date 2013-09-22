package org.cfr.restlet.ext.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.restlet.representation.Representation;

/**
 * Velocity resource loader based on a static map of representations or on a
 * default representation.
 *
 * @author Jerome Louvel
 */
public class RepresentationResourceLoader extends ResourceLoader {

    /** The cache of template representations. */
    private static final Map<String, Representation> store = new ConcurrentHashMap<String, Representation>();

    /**
     * Returns the cache of template representations.
     *
     * @return The cache of template representations.
     */
    public static Map<String, Representation> getStore() {
        return store;
    }

    /** The default representation to load. */
    private final Representation defaultRepresentation;

    /**
     * Constructor.
     *
     * @param defaultRepresentation
     *            The default representation to use.
     */
    public RepresentationResourceLoader(Representation defaultRepresentation) {
        this.defaultRepresentation = defaultRepresentation;
    }

    @Override
    public long getLastModified(Resource resource) {
        final Representation original = getStore().get(resource.getName());
        return (original != null) ? original.getModificationDate().getTime()
                : 0;
    }

    @Override
    public InputStream getResourceStream(String name)
            throws ResourceNotFoundException {
        InputStream result = null;

        try {
            Representation resultRepresentation = getStore().get(name);

            if (resultRepresentation == null) {
                resultRepresentation = this.defaultRepresentation;

                if (resultRepresentation == null) {
                    throw new ResourceNotFoundException(
                            "Could not locate resource '" + name + "'");
                }

                result = resultRepresentation.getStream();
            } else {
                result = resultRepresentation.getStream();
            }
        } catch (IOException ioe) {
            throw new ResourceNotFoundException(ioe);
        }

        return result;
    }

    @Override
    public void init(ExtendedProperties configuration) {

    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return getLastModified(resource) != resource.getLastModified();
    }

}