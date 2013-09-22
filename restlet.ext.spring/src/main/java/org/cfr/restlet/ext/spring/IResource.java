package org.cfr.restlet.ext.spring;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.UniformResource;

/**
 * An automatically managed Rest Resource.
 *
 */
public interface IResource {

    /**
     * Gets indication wether if all characters of resource path must match the template and size be identical.
     * @return Returns <code>true</code> if all characters of path must match the template and size be identical, otherwise <code>false</code>.
     */
    boolean isStrict();

    /**
     * If true, will redirect POST and PUT to as many upload() method calls, as many files are in request.
     *
     * @return
     */
    boolean acceptsUpload();

    /**
     * Method invoked on incoming DELETE request.
     *
     * @param UniformResource the cross-request resource
     * @throws ResourceException
     */
    void delete(UniformResource resource) throws ResourceException;

    /**
     * Method invoked on incoming GET request. The method may return: Representation (will be passed unchanged to
     * restlet engine), InputStream (will be wrapped into InputStreamRepresentation), String (will be wrapped into
     * StringRepresentation) and Object. If Object is none of those previously listed, an XStream serialization is
     * applied to it (into variant originally negotiated with client).
     *
     * @param UniformResource the cross-request resource
     * @param variant - the result of the content negotiation (for use by PlexusResources that want's to cruft manually
     *        some Representation).
     * @return Object to be returned to the client. Object may be: InputStream, restlet.org Representation, String or
     *         any object. The "any" object will be serialized by XStream to a proper mediaType if possible.
     * @throws ResourceException
     */
    Object get(UniformResource resource, Variant variant) throws ResourceException;

    /**
     * A permission prefix to be applied when securing the resource.
     *
     * @return
     */
    //    PathProtectionDescriptor getResourceProtection();
    /**
     * A factory method to create an instance of DTO.
     *
     * @return
     */
    Class<?> getPayloadClass();

    /**
     * The location to attach this resource to.
     *
     * @return
     */
    String getResourceUri();

    /**
     * Presents a modifiable list of available variants.
     *
     * @return
     */
    List<Variant> getVariants();

    /**
     * If true, Restlet will try to negotiate the "best" content.
     *
     * @return
     */
    boolean isNegotiated();

    /**
     * Indicates if conditional handling is enabled. The default value is true.
     */
    boolean isConditional();

    /**
     * Gets indicating whether this resource is modified.
     * @return returns <b>true</b> whether this resource is modified, otherwise <b>false</b>.
     */
    boolean isModified();

    /**
     * Method invoked on incoming POST request. For return Object, see GET method.
     *
     * @param UniformResource the cross-request resource
     * @param payload - the deserialized payload (if it was possible to deserialize). Otherwise, the Representation is
     *        accessible thru request. If deserialization was not possible it is null.
     * @return
     * @throws ResourceException
     */
    Object post(UniformResource resource, Variant variant, Object payload) throws ResourceException;

    /**
     * Method invoked on incoming PUT request. For return Object, see GET method.
     *
     * @param UniformResource the cross-request resource
     * @param payload - the deserialized payload (if it was possible to deserialize). Otherwise, the Representation is
     *        accessible thru request. If deserialization was not possible it is null.
     * @return
     * @throws ResourceException
     */
    Object put(UniformResource resource, Variant variant, Object payload) throws ResourceException;

    /**
     *
     * @param register
     */
    void register(ResourceRegister register);

    /**
     * Sets the indicating wether this resource has changed
     * @param modified
     */
    void setModified(boolean modified);

    /**
     * "Catch all" method if this method accepts uploads (acceptsUpload() returns true). In this case, the PUT and POST
     * requests will be redirected to this method. For return Object, see GET method.
     *
     * @param UniformResource the cross-request resource
     * @param request - the request
     * @param response = the response
     * @param files
     * @return
     * @throws ResourceException
     */
    Object upload(UniformResource resource, List<FileItem> files) throws ResourceException;

    /**
     *
     * @return
     */
    PathProtectionDescriptor getResourceProtection();
}