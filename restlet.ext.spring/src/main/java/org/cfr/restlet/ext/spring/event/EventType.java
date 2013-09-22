package org.cfr.restlet.ext.spring.event;

public enum EventType {
    InitialCheck("Initial Check"),
    Upgrade("Upgrade"),
    Database("Database"),
    Setup("Setup"),
    Export("Export"),
    Import("import"),
    LicenseInvalid("Invalid License");

    private final String description;

    private EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
