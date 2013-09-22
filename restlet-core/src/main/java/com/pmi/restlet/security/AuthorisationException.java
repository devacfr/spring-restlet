package com.pmi.restlet.security;

/**
 * Exception thrown by the {@link SysadminOnlyResourceFilter} to indicate a user is not a system administrator.
 *
 * @since 1.1
 */
public class AuthorisationException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = 843236177756819133L;

    public AuthorisationException() {
        super("Client must be authenticated as a system administrator to access this resource.");
    }
}
