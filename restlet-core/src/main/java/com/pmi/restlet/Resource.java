package com.pmi.restlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {

    /**
     * Sets path of resource 
     * @return
     */
    String path();

    /**
     * Sets  wether matching mode to use when parsing a formatted reference is strict
     * @return
     */
    boolean strict() default true;

}