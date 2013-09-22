package com.pmi.restlet.gadgets.directory.spi;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;

public final class ExternalGadgetSpec implements Serializable {

    private static final long serialVersionUID = 8476725773908350812L;

    private final ExternalGadgetSpecId id;

    private final URI specUri;

    public ExternalGadgetSpec(ExternalGadgetSpecId id, URI specUri) {
        if (id == null)
            throw new IllegalArgumentException((new StringBuilder()).append("id cannot be null. specUri = ").append(specUri).toString());
        if (specUri == null) {
            throw new IllegalArgumentException((new StringBuilder()).append("specUri cannot be null. id = ").append(id).toString());
        } else {
            this.id = id;
            this.specUri = specUri;
            return;
        }
    }

    public ExternalGadgetSpecId getId() {
        return id;
    }

    public URI getSpecUri() {
        return specUri;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (other.getClass() != ExternalGadgetSpec.class) {
            return false;
        } else {
            ExternalGadgetSpec that = (ExternalGadgetSpec) other;
            return id.equals(that.id);
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (id == null)
            throw new InvalidObjectException("id cannot be null");
        if (specUri == null)
            throw new InvalidObjectException("specUri cannot be null");
        else
            return;
    }

}