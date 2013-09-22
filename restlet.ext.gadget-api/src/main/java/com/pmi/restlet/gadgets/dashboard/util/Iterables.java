package com.pmi.restlet.gadgets.dashboard.util;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.cfr.commons.util.Assert;

public class Iterables {

	private Iterables() {
		throw new AssertionError("Must not be instantiated");
	}

	public static <T> boolean elementsEqual(Iterable<T> i1, Iterable<T> i2) {
		if (i1 == i2) {
			return true;
		}
		if (i1 == null || i2 == null) {
			return false;
		}
		Iterator<T> iter1 = i1.iterator();
		Iterator<T> iter2;
		for (iter2 = i2.iterator(); iter1.hasNext() && iter2.hasNext();) {
			if (!ObjectUtils.equals(iter1.next(), iter2.next())) {
				return false;
			}
		}

		return !iter1.hasNext() && !iter2.hasNext();
	}

	public static <T> Iterable<T> checkContentsNotNull(Iterable<T> iterable) {
		Object element;
		for (Iterator<T> it = Assert.notNull(iterable).iterator(); it.hasNext(); Assert.notNull(element)) {
			element = it.next();
		}

		return iterable;
	}
}