package org.cfr.restlet.ext.shindig.auth;
//package com.pmi.restlet.ext.shindig.auth;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.shindig.auth.AnonymousSecurityToken;
//import org.apache.shindig.auth.AuthenticationMode;
//import org.apache.shindig.auth.SecurityToken;
//
//import com.google.inject.Inject;
//import com.google.inject.name.Named;
//
//
///**
// * Handled Anonymous Authentication, including returning an "anonymous" security token.
// */
//public class AnonymousAuthenticationHandler implements AuthenticationHandler {
//  public static final String ALLOW_UNAUTHENTICATED = "shindig.allowUnauthenticated";
//  private final boolean allowUnauthenticated;
//
//  @Inject
//  public AnonymousAuthenticationHandler(@Named(ALLOW_UNAUTHENTICATED)
//      boolean allowUnauthenticated) {
//    this.allowUnauthenticated = allowUnauthenticated;
//  }
//
//  public String getName() {
//    return AuthenticationMode.UNAUTHENTICATED.name();
//  }
//
//  public SecurityToken getSecurityTokenFromRequest(HttpServletRequest request) {
//    if (allowUnauthenticated) {
//      return new AnonymousSecurityToken();
//    }
//    return null;
//  }
//
//  public String getWWWAuthenticateHeader(String realm) {
//    return null;
//  }
//}
