package com.pmi.restlet.response.extjs;

import java.util.Collection;

public interface CallbackDataSource<T> {

    Collection<?> populate(Collection<T> source);
}
