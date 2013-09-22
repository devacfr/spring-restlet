package com.pmi.restlet.gadgets;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.cfr.commons.util.Assert;

public final class GadgetId implements Serializable {

	private GadgetId(String id) {
		this.id = id;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		if (id == null) {
			throw new InvalidObjectException("id cannot be null");
		} else {
			return;
		}
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
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		} else {
			GadgetId otherId = (GadgetId) o;
			return id.equals(otherId.id);
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public static GadgetId valueOf(String id) {
		return new GadgetId(id);
	}

	public static GadgetId valueOf(Object id) {
		return new GadgetId(Assert.notNull(id).toString());
	}

	private static final long serialVersionUID = 1L;

	private final String id;
}