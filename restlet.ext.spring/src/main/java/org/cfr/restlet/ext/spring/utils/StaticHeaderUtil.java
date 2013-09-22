package org.cfr.restlet.ext.spring.utils;

import java.util.Arrays;
import java.util.List;

import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;

public class StaticHeaderUtil {

    public final static String HEADER_ATTRIBUTE_NAME = "org.restlet.http.headers";

    private final static CacheDirective maxAge = CacheDirective.maxAge(2592000);

    private static List<CacheDirective> list = Arrays.asList(maxAge);

    public static void addResponseHeaders(Response response) {
        Form responseHeaders = (Form) response.getAttributes().get(HEADER_ATTRIBUTE_NAME);

        if (responseHeaders == null) {
            responseHeaders = new Form();
            response.getAttributes().put(HEADER_ATTRIBUTE_NAME, responseHeaders);
        }

        response.setCacheDirectives(list);
    }
}