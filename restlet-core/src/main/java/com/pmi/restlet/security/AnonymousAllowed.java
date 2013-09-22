package com.pmi.restlet.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>An annotation to tell that resources are accessible by anonymous users (i.e. not authenticated user).</p>
 * <p>The default for all resources is to require authentication, those resources that don't require authentication
 * should use this annotation.</p>
 */
@Target( { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AnonymousAllowed {
}