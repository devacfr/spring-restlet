package com.pmi.restlet.gadgets;

import org.restlet.Request;

public interface IGadgetRequestContextFactory {

    GadgetRequestContext get(Request request);
}