package org.cfr.restlet.ext.spring.event;

public enum EventLevel {

    Warning("Warning"),
    Error("Error"),
    Fatal("Fatal");

    private final String description;

    private EventLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
