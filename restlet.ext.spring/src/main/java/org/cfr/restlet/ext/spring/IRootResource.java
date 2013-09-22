package org.cfr.restlet.ext.spring;

/**
 * An automatically managed Rest Resource to root path of application
 * @author cfriedri
 *
 */
public interface IRootResource {

    /**
     * Gets indication wether if all characters of resource path must match the template and size be identical.
     * @return Returns <code>true</code> if all characters of path must match the template and size be identical, otherwise <code>false</code>.
     */
    boolean isStrict();
}
