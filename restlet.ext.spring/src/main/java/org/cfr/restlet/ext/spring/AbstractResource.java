package org.cfr.restlet.ext.spring;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.cfr.restlet.ext.spring.utils.ReferenceUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.UniformResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author cfriedri
 *
 */
public abstract class AbstractResource implements IResource {

    public static Reference getContextPath(Context context, Request request) {
        return ReferenceUtils.getContextPath(context, request);
    }

    public static String getContextPath(Request request) {
        return ReferenceUtils.getContextPath(request);
    }

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean available = true;

    private boolean negotiateContent = true;

    private boolean modified = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsUpload() {
        // since this property will not change during the lifetime of a
        // resource, it is needed to be overrided
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(UniformResource resource) throws ResourceException {
        throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(UniformResource resource, Variant variant) throws ResourceException {
        throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Class<?> getPayloadClass();

    /**
     * Allows to give security protection according to this resource.
     */
    @Override
    public abstract PathProtectionDescriptor getResourceProtection();

    /**
     * {@inheritDoc}
     */
    // to be implemented subclasses
    public abstract String getResourceUri();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract List<Variant> getVariants();

    /**
     * {@inheritDoc}
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModified() {
        return modified;
    }

    // public void configureXStream(XStream xstream) {
    // // a dummy implementation to be overridden if needed
    // }

    public boolean isNegotiated() {
        return negotiateContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object post(UniformResource resource, Variant variant, Object payload) throws ResourceException {
        throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(UniformResource resource, Variant variant, Object payload) throws ResourceException {
        throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    }

    /**
     * {@inheritDoc}
     */
    public void register(ResourceRegister register) {

    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void setNegotiateContent(boolean negotiateContent) {
        this.negotiateContent = negotiateContent;
    }

    @Override
    public Object upload(UniformResource resource, List<FileItem> files) throws ResourceException {
        throw new ResourceException(Status.SERVER_ERROR_NOT_IMPLEMENTED);
    }

    @Override
    public boolean isConditional() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Returns the parent application if it exists, or instantiates a new one if
     * needed.
     * 
     * @return The parent application if it exists, or a new one.
     */
    public org.restlet.Application getApplication(UniformResource resource) {
        org.restlet.Application result = org.restlet.Application.getCurrent();
        return (result == null) ? new org.restlet.Application(resource.getContext()) : result;
    }

    /**
     * Returns the application's converter service or create a new one.
     * 
     * @return The converter service.
     */
    public org.restlet.service.ConverterService getConverterService(UniformResource resource) {
        org.restlet.service.ConverterService result = null;

        result = getApplication(resource).getConverterService();

        if (result == null) {
            result = new org.restlet.service.ConverterService();
        }

        return result;
    }

    /**
     * Converts a representation into a Java object. Leverages the
     * {@link org.restlet.service.ConverterService}.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param source
     *            The source representation to convert.
     * @param target
     *            The target class of the Java object.
     * @return The converted Java object.
     * @throws ResourceException
     */
    protected <T> T toObject(Representation source, Class<T> target, UniformResource resource) throws ResourceException {
        T result = null;

        if (source != null) {
            try {
                org.restlet.service.ConverterService cs = getConverterService(resource);
                result = cs.toObject(source, target, resource);
            } catch (Exception e) {
                throw new ResourceException(e);
            }
        }

        return result;
    }

    /**
     * Converts an object into a representation based on client preferences.
     * 
     * @param source
     *            The object to convert.
     * @param target
     *            The target representation variant.
     * @return The wrapper representation.
     */
    protected Representation toRepresentation(Object source, Variant target, UniformResource resource) {
        Representation result = null;

        if (source != null) {
            org.restlet.service.ConverterService cs = getConverterService(resource);
            result = cs.toRepresentation(source, target, resource);
        }

        return result;
    }
}
