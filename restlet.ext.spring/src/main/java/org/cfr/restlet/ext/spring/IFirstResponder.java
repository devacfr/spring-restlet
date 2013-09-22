package org.cfr.restlet.ext.spring;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;

public interface IFirstResponder {

    /** Key to store used Commons Fileupload FileItemFactory */
    public static final String FILEITEM_FACTORY = "restlet.fileItemFactory";

    /** Key to store the flag should kbase discover resource or no */
    public static final String DISCOVER_RESOURCES = "restlet.discoverResources";

    /**
     * Attaches a target Restlet to this router based on a given URI pattern.
     * @param router
     * @param strict The matching mode to use when parsing a formatted reference.
     * @param resource
     */
    void attach(Router router, boolean strict, IResource resource);

    /**
     * Attaches a target Restlet to this router based on a given URI pattern.
     * @param router
     * @param strict The matching mode to use when parsing a formatted reference.
     * @param uri
     * @param resource
     */
    void attach(Router router, boolean strict, String uri, IResource resource);

    /**
     * Attaches a target Restlet to this router based on a given URI pattern.
     * @param router
     * @param strict The matching mode to use when parsing a formatted reference.
     * @param uriPattern
     * @param target
     * @return
     */
    TemplateRoute attach(Router router, boolean strict, String uriPattern, Restlet target);

    /**
     * Gets the application router Restlet.
     * @return Returns the application router Restlet.
     */
    Router getApplicationRouter();


    /**
     *
     * @return
     */
    ResourceRegister getResourceRegister();

    /**
     * Gets the root inbound Restlet.
     * @return Returns the root inbound Restlet.
     */
    Router getRootRouter();


    /**
     * Gets the context.
     * @return Returns the context.
     */
    Context getContext();

}