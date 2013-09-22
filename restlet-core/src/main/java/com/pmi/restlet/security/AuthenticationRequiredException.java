package com.pmi.restlet.security;

/**
 * Exception thrown when a client tries to access a resources that requires authentication and the client is not authenticated.
 * @see AnonymousAllowed
 */
public class AuthenticationRequiredException extends SecurityException {

    /**
     * 
     */
    private static final long serialVersionUID = -850582593725750489L;

    public AuthenticationRequiredException() {
        super("Client must be authenticated to access this resource.");
    }
}
