package com.pmi.restlet.gadgets.dashboard;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public final class DashboardId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    private DashboardId(String id) {
        this.id = id;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (id == null)
            throw new InvalidObjectException("id cannot be null");
        else
            return;
    }

    public String value() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            DashboardId otherId = (DashboardId) o;
            return id.equals(otherId.id);
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static DashboardId valueOf(String id) {
        return new DashboardId(id);
    }

    public static DashboardId valueOf(Object id) {
        return new DashboardId(id.toString());
    }

}
