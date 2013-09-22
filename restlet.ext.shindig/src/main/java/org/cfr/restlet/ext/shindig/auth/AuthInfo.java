package org.cfr.restlet.ext.shindig.auth;

import org.apache.shindig.auth.SecurityToken;
import org.restlet.Request;

import com.google.inject.Inject;


/**
 * Class to get authorization information on a servlet request.
 *
 * Information is set by adding an AuthentiationServletFilter, and there
 * is no way to set in a public API. This can be added in the future for testing
 * purposes.
 */
public class AuthInfo {

    private final Request req;

    /**
     * Create AuthInfo from a given HttpServletRequest
     * @param req
     */
    @Inject
    public AuthInfo(Request req) {
        this.req = req;
    }

    /**
     * Constants for request attribute keys
     *
     * This is only public for testing.
     */
    public enum Attribute {
        /** The security token */
        SECURITY_TOKEN,
        /** The named auth type */
        AUTH_TYPE;

        public String getId() {
            return Attribute.class.getName() + '.' + this.name();
        }
    }

    /**
     * Get the security token for this request.
     *
     * @return The security token
     */
    public SecurityToken getSecurityToken() {
        return getRequestAttribute(req, Attribute.SECURITY_TOKEN);
    }

    /**
     * Get the hosted domain for this request.
     *
     * @return The domain, or {@code null} if no domain was found
     */
    public String getAuthType() {
        return getRequestAttribute(req, Attribute.AUTH_TYPE);
    }

    /**
     * Set the security token for the request.
     *
     * @param token The security token
     * @return This object
     */
    AuthInfo setSecurityToken(SecurityToken token) {
        setRequestAttribute(req, Attribute.SECURITY_TOKEN, token);
        return this;
    }

    /**
     * Set the auth type for the request.
     *
     * @param authType The named auth type
     * @return This object
     */
    AuthInfo setAuthType(String authType) {
        setRequestAttribute(req, Attribute.AUTH_TYPE, authType);
        return this;
    }

    /**
     * Set a standard request attribute.
     *
     * @param req The request
     * @param att The attribute
     * @param value The value
     */
    private static <T> void setRequestAttribute(Request req, Attribute att, T value) {
        req.getAttributes().put(att.getId(), value);
    }

    /**
     * Get a standard attribute
     *
     * @param req The request
     * @param att The attribute
     * @return The value
     */
    @SuppressWarnings("unchecked")
    private static <T> T getRequestAttribute(Request req, Attribute att) {
        return (T) req.getAttributes().get(att.getId());
    }
}
