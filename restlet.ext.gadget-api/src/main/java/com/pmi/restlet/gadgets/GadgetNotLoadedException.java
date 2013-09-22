package com.pmi.restlet.gadgets;

public class GadgetNotLoadedException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2014287114710848052L;

    private final GadgetId gadgetId;

    public GadgetNotLoadedException(GadgetId gadgetId) {
        super("Gadget with id " + gadgetId + " could not be loaded, so some operations (e.g. change color, user prefs) cannot be performed on it");
        this.gadgetId = gadgetId;
    }

    public GadgetId getGadgetId() {
        return gadgetId;
    }
}