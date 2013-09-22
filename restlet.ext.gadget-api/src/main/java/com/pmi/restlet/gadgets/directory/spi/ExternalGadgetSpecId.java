package com.pmi.restlet.gadgets.directory.spi;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class ExternalGadgetSpecId implements Serializable {

    protected ExternalGadgetSpecId(String id) {
        this.id = id;
    }

    public String value() {
        return id;
    }

    public static ExternalGadgetSpecId valueOf(String id) {
        return new ExternalGadgetSpecId(id);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (id == null)
            throw new InvalidObjectException("id cannot be null");
        else
            return;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (other.getClass() != ExternalGadgetSpecId.class) {
            return false;
        } else {
            ExternalGadgetSpecId that = (ExternalGadgetSpecId) other;
            return id.equals(that.id);
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }

    private static final long serialVersionUID = 1L;

    private final String id;
}
