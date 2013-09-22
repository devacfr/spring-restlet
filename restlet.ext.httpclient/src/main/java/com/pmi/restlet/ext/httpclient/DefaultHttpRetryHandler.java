package com.pmi.restlet.ext.httpclient;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;


public class DefaultHttpRetryHandler extends DefaultHttpRequestRetryHandler {

    public DefaultHttpRetryHandler() {
        super(3, true);
    }
}