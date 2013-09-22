package org.cfr.restlet.ext.spring.event;

import org.restlet.Request;
import org.restlet.data.Reference;

/**
 * Interface allows to configure the event service.
 * @author cfriedri
 *
 */
public interface ISetupConfig {

    /**
     * Gets a value indicating whether the application server has already completed the setup.
     * @return Returns <code>true</code> if the application server has already completed the setup, <code>false</code> otherwise.
     */
    boolean isSetup();

    /**
     * Gets a value indicating whether the request match to the setup page.
     * @param request the request to check
     * @return Returns <code>true</code> if the request match to the setup page, <code>false</code> otherwise.
     */
    boolean isSetupPage(Request request);

    /**
     * Gets a value indicating whether the request match to the error page.
     * @param request the request to check
     * @return Returns <code>true</code> if the request match to the error page, <code>false</code> otherwise.
     */
    boolean isErrorPage(Request request);

    /**
     * Gets the reference of setup page.
     * @return Returns the reference of setup page.
     */
    Reference getSetupPage();

    /**
     * Gets the reference of error page.
     * @return Returns the reference of error page.
     */
    Reference getErrorPage();
}
