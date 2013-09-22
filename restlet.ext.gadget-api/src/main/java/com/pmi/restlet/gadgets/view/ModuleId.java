package com.pmi.restlet.gadgets.view;

import org.cfr.commons.util.Assert;

public final class ModuleId {

	private ModuleId(long id) {
		this.id = id;
	}

	public long value() {
		return id;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		} else {
			ModuleId otherId = (ModuleId) obj;
			return id == otherId.id;
		}
	}

	@Override
	public int hashCode() {
		return (int) (id ^ id >>> 32);
	}

	public static ModuleId valueOf(long id) {
		return new ModuleId(id);
	}

	public static ModuleId valueOf(String id) throws NumberFormatException {
		return new ModuleId(Long.parseLong(Assert.notNull(id, "id")));
	}

	private final long id;
}