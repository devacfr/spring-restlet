package org.cfr.restlet.ext.spring;

/**
 * 
 * @author cfriedri
 *
 */
public interface IManagedResource {

    /**
     * 
     * @param application
     * @param isStarted
     */
    public void attach(IFirstResponder application, boolean isStarted);

}
