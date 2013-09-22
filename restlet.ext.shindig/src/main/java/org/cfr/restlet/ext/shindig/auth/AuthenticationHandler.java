package org.cfr.restlet.ext.shindig.auth;
//package com.pmi.restlet.ext.shindig.auth;
//
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.shindig.auth.SecurityToken;
//
//
///**
// * Implements a specific authentication mechanism and produces a SecurityToken when authentication
// * is successful.
// */
//public interface AuthenticationHandler {
//
//    /**
//     * Some authentication handlers need to read the request body to perform verification. Because
//     * the servlet stream can only be read once, making the content unavailable to the receiving
//     * servlet. An authentication handler that fully reads the body should stash the raw content
//     * byte array using request.setAttribute(STASHED_BODY, <body byte array>)
//     */
//    public static final String STASHED_BODY = "STASHED_BODY";
//
//    /**
//     * @return The name of the authentication handler. This value is bound to the security token
//     * and can be used to determine the authentication mechanism by which the security token was
//     * created. The value is expected to be one of the values in AuthenticationMode but string
//     * is used here to allow containers to have custom authentication modes
//     */
//    String getName();
//
//    /**
//     * Produce a security token extracted from the HTTP request.
//     *
//     * @param request The request to extract a token from.
//     * @return A valid security token for the request, or null if it wasn't possible to authenticate.
//     */
//    SecurityToken getSecurityTokenFromRequest(HttpServletRequest request)
//    throws InvalidAuthenticationException;
//
//    /**
//     * Return a String to be used for a WWW-Authenticate header. This will be called if the
//     * call to getSecurityTokenFromRequest returns null.
//     *
//     * If non-null/non-blank it will be added to the Response.
//     * See Section 6.1.3 of the Portable Contacts Specification
//     *
//     * @param realm the name of the realm to use for the authenticate header
//     * @return Header value for a WWW-Authenticate Header
//     */
//    String getWWWAuthenticateHeader(String realm);
//
//    /**
//     * An exception thrown by an AuthenticationHandler in the situation where
//     * a malformed credential or token is passed. A handler which throws this exception
//     * is required to include the appropriate error state in the servlet response
//     */
//    public static final class InvalidAuthenticationException extends Exception {
//
//        private final Map<String,String> additionalHeaders;
//        private final String redirect;
//
//        /**
//         * @param message Message to output in error response
//         * @param cause Underlying exception
//         */
//        public InvalidAuthenticationException(String message, Throwable cause) {
//            this(message, cause, null, null);
//        }
//
//        /**
//         * @param message Message to output in error response
//         * @param cause Underlying exception
//         * @param additionalHeaders Headers to add to error response
//         * @param redirect URL to redirect to on error
//         */
//        public InvalidAuthenticationException(String message, Throwable cause,
//                Map<String,String> additionalHeaders, String redirect) {
//            super(message, cause);
//            this.additionalHeaders = additionalHeaders;
//            this.redirect = redirect;
//        }
//
//        public Map<String, String> getAdditionalHeaders() {
//            return additionalHeaders;
//        }
//
//        public String getRedirect() {
//            return redirect;
//        }
//    }
//}