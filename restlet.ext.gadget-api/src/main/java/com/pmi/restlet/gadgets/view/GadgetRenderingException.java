package com.pmi.restlet.gadgets.view;

import com.pmi.restlet.gadgets.GadgetState;

public class GadgetRenderingException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -8150376880407829414L;

    public GadgetRenderingException(String message, GadgetState gadget, Throwable cause) {
        super(message, cause);
        this.gadget = gadget;
    }

    public GadgetRenderingException(String message, GadgetState gadget) {
        super(message);
        this.gadget = gadget;
    }

    public GadgetRenderingException(GadgetState gadget, Throwable cause) {
        super(cause);
        this.gadget = gadget;
    }

    public GadgetState getGadgetState() {
        return gadget;
    }

    private final GadgetState gadget;
}