package com.pmi.restlet.security;

import java.security.Principal;

/**
 * An authentication context to retrieve the principal and authentication status.
 * @since 1.0
 */
public interface IAuthenticationContext {

    /**
     * @return the authenticated principal, {@code null} if none is authenticated.
     */
    Principal getPrincipal();

    /**
     * @return {@code true} if the principal is authenticated, {@code false} otherwise.
     */
    boolean isAuthenticated();
}
