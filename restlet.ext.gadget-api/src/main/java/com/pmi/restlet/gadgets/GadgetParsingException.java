package com.pmi.restlet.gadgets;

public class GadgetParsingException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 489243550181978354L;

    public GadgetParsingException(String message) {
        super(message);
    }

    public GadgetParsingException(Throwable e) {
        super(e);
    }
}