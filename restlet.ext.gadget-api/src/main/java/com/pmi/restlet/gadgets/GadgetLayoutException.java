package com.pmi.restlet.gadgets;

/**
 * An exception that is thrown if the gadgets are not layed out in accordance with the currently set dashboard 
 * {@link Layout}.
 */
public class GadgetLayoutException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -4101395489051483522L;

    public GadgetLayoutException(String message) {
        super(message);
    }
}
