package com.pmi.restlet.gadgets.directory.spi;

public class ExternalGadgetSpecStoreException extends RuntimeException {

    public ExternalGadgetSpecStoreException() {
    }

    public ExternalGadgetSpecStoreException(String message) {
        super(message);
    }

    public ExternalGadgetSpecStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternalGadgetSpecStoreException(Throwable cause) {
        super(cause);
    }
}