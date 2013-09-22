package com.pmi.restlet.utils;

public final class DataSourceResolver {
	private DataSourceResolver () {}

	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> resolveDataSource (Object o, String data_member) 
	{
		Iterable<T> ds;

		ds = (o instanceof  Iterable ? (Iterable<T>)o : null);
		if (ds != null)
			return ds;


		return null;
	}
}