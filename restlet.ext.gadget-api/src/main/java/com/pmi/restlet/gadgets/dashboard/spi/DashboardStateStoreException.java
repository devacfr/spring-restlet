package com.pmi.restlet.gadgets.dashboard.spi;

public class DashboardStateStoreException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2122055950721935270L;

    public DashboardStateStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public DashboardStateStoreException(String message) {
        super(message);
    }

    public DashboardStateStoreException(Throwable cause) {
        super(cause);
    }
}
