package org.cfr.restlet.ext.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.service.ConverterService;

public class RestletResource extends ServerResource {

    private class FakeFileItem implements FileItem {

        private static final long serialVersionUID = 414885488690939983L;

        private final String name;

        private final Representation representation;

        public FakeFileItem(String name, Representation representation) {
            this.name = name;

            this.representation = representation;
        }

        public void delete() {
        }

        public byte[] get() {
            return null;
        }

        public String getContentType() {
            return representation.getMediaType().getName();
        }

        public String getFieldName() {
            return getName();
        }

        // == ignored methods

        public InputStream getInputStream() throws IOException {
            return representation.getStream();
        }

        public String getName() {
            return name;
        }

        public OutputStream getOutputStream() throws IOException {
            return null;
        }

        public long getSize() {
            return 0;
        }

        public String getString() {
            return null;
        }

        public String getString(String encoding) throws UnsupportedEncodingException {
            return null;
        }

        public boolean isFormField() {
            return false;
        }

        public boolean isInMemory() {
            return false;
        }

        public void setFieldName(String name) {
        }

        public void setFormField(boolean state) {
        }

        public void write(File file) throws Exception {
        }

    }

    private final IResource delegate;

    public RestletResource(IResource delegate) {

        this.delegate = delegate;
        // set variants
        getVariants().clear();
        getVariants().addAll(delegate.getVariants());

        setNegotiated(delegate.isNegotiated());
    }

    @Override
    protected Representation delete(Variant variant) throws ResourceException {
        delegate.delete(this);

        // if we have an Entity set, then return a 200 (default)
        // if not return a 204
        if (!getResponse().isEntityAvailable()) {
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        }

        if (getResponse().getStatus().isSuccess() && getResponseEntity() != null) {
            updateModificationDate(false);
            if (delegate.isModified()) {
                getResponseEntity().setModificationDate(getModificationDate());
            }
            updateModificationDate(true);
        }
        return getResponseEntity();
    }

    protected Representation doRepresent(Object payload, Variant variant) throws ResourceException {
        ConverterService cs = getConverterService();
        return cs.toRepresentation(payload, variant, this);
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        //        if (getLogger().isLoggable(Level.FINE)) {
        getLogger().log(Level.INFO, "Variant accepted:" + variant);
        if (getRequest().getEntity() != null) {
            getLogger().log(Level.INFO, "Variant request:" + getRequest().getEntity().getMediaType());
        }
        //        }
        Object result = delegate.get(this, variant);

        Representation representation = (result != null) ? doRepresent(result, variant) : null;
        if (representation != null && delegate.isModified()) {
            updateModificationDate(false);
            representation.setModificationDate(getModificationDate());
            updateModificationDate(true);
        }
        return representation;
    }

    /**
     * For file uploads we are using commons-fileupload integration with
     * restlet.org. We are storing one FileItemFactory instance in context. This
     * method simply encapsulates gettting it from Resource context.
     *
     * @return
     */
    protected FileItemFactory getFileItemFactory() {
        return (FileItemFactory) getContext().getAttributes().get(IFirstResponder.FILEITEM_FACTORY);
    }

    protected Date getModificationDate() {
        Date result = (Date) getContext().getAttributes().get(getModificationDateKey(false));

        if (result == null) {
            // get parent's date
            result = (Date) getContext().getAttributes().get(getModificationDateKey(true));

            if (result == null) {
                // get app date
                RestletSpringApplication application = (RestletSpringApplication) getApplication();

                result = application.getCreatedOn();
            }

            getContext().getAttributes().put(getModificationDateKey(false), result);
        }

        return result;
    }

    private String getModificationDateKey(boolean parent) {
        if (parent) {
            return getRequest().getResourceRef().getParentRef().getPath() + "#modified";
        } else {
            return getRequest().getResourceRef().getPath() + "#modified";
        }
    }

    /**
     * Returns the preferred variant according to the client preferences
     * specified in the request.
     *
     * @return The preferred variant.
     */
    public Variant getPreferredVariant() {
        Variant result = null;
        final List<Variant> variants = getVariants();

        if ((variants != null) && (!variants.isEmpty())) {
            // Compute the preferred variant. Get the default language
            // preference from the Application (if any).
            result = getRequest().getClientInfo().getPreferredVariant(variants,
                    (getApplication() == null) ? null : getApplication().getMetadataService());
        }

        return result;
    }

    @Override
    protected Representation post(Representation representation, Variant variant) throws ResourceException {
        if (delegate.acceptsUpload()) {
            upload(representation);
        } else {
            Object payload = null;
            if (delegate.getPayloadClass() != null) {
                ConverterService cs = getConverterService();
                try {
                    payload = cs.toObject(getRequestEntity(), delegate.getPayloadClass(), this);
                } catch (IOException e1) {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "error on desialization", e1);
                }
            }

            Object result = null;

            try {
                result = delegate.post(this, variant, payload);

                // This is a post, so set the status correctly
                // but only if the status was not changed to be something else,
                // like a 202
                if (getResponse().getStatus() == Status.SUCCESS_OK) {
                    getResponse().setStatus(Status.SUCCESS_CREATED);
                }
            } catch (RestletResourceException e) {
                // set the status
                getResponse().setStatus(e.getStatus());
                // try to get the responseObject
                result = e.getResultObject();
            }

            if (result != null) {
                getResponse().setEntity(doRepresent(result, variant));
            }
        }

        if (getResponse().getStatus().isSuccess()) {
            updateModificationDate(false);
        }
        return getResponseEntity();
    }

    @Override
    public Representation put(Representation representation, Variant variant) throws ResourceException {
        if (delegate.acceptsUpload()) {
            upload(representation);
        } else {

            Object payload = null;
            if (delegate.getPayloadClass() != null) {
                ConverterService cs = getConverterService();
                try {
                    payload = cs.toObject(getRequestEntity(), delegate.getPayloadClass(), this);
                } catch (IOException e1) {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "error on desialization", e1);
                }
            }

            Object result = null;
            try {
                result = delegate.put(this, variant, payload);

            } catch (RestletResourceException e) {
                // set the status
                getResponse().setStatus(e.getStatus());
                // try to get the responseObject
                result = e.getResultObject();
            }

            if (result != null) {
                getResponse().setEntity(doRepresent(result, representation));
            }
        }

        if (getResponse().getStatus().isSuccess()) {
            updateModificationDate(false);
            if (delegate.isModified()) {
                representation.setModificationDate(getModificationDate());
            }
            updateModificationDate(true);
        }
        return getResponseEntity();
    }

    // ==

    protected void updateModificationDate(boolean parent) {
        getContext().getAttributes().put(getModificationDateKey(parent), new Date());
    }

    public void upload(Representation representation) throws ResourceException {
        Object result = null;

        List<FileItem> files = null;

        try {
            RestletFileUpload uploadRequest = new RestletFileUpload(getFileItemFactory());

            files = uploadRequest.parseRepresentation(representation);

            result = delegate.upload(this, files);
        } catch (FileUploadException e) {
            // try to take simply the body as stream
            String name = getRequest().getResourceRef().getPath();

            if (name.contains("/")) {
                name = name.substring(name.lastIndexOf("/") + 1, name.length());
            }

            FileItem file = new FakeFileItem(name, representation);

            files = new ArrayList<FileItem>();

            files.add(file);

            result = delegate.upload(this, files);
        }

        // only if the status was not changed to be something else, like a 202
        if (getResponse().getStatus() == Status.SUCCESS_OK) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        }

        if (result != null) {
            // TODO: representation cannot be returned as multipart!
            // (representation above is possibly multipart)
            getResponse().setEntity(doRepresent(result, getPreferredVariant()));
        }

    }

}