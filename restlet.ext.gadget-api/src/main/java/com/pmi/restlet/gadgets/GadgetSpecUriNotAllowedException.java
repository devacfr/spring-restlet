package com.pmi.restlet.gadgets;

public class GadgetSpecUriNotAllowedException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -8107823358780454356L;

    public GadgetSpecUriNotAllowedException(Throwable e) {
        super(e);
    }

    public GadgetSpecUriNotAllowedException(String message) {
        super(message);
    }
}