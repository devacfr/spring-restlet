package com.pmi.restlet.gadgets;

public class GadgetNotFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 4423871151011974486L;

    public GadgetNotFoundException(GadgetId gadgetId) {
        super((new StringBuilder()).append("No such gadget with id ").append(gadgetId).append(" exists on this dashboard").toString());
        this.gadgetId = gadgetId;
    }

    public GadgetId getGadgetId() {
        return gadgetId;
    }

    private final GadgetId gadgetId;
}