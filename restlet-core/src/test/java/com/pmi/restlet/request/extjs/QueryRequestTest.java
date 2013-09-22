package com.pmi.restlet.request.extjs;

import org.cfr.commons.testing.EasyMockTestCase;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;

public class QueryRequestTest extends EasyMockTestCase {

	@Test
	public void queryWithFilter() {
		String query = "http://localhost:8080/zurich-app-web/service/local/roles?_dc=1378724368419&page=1&start=0&limit=25&filter=%5B%7B%22property%22%3A%22groupId%22%2C%22value%22%3A200%7D%5D";
		QueryRequest request = new QueryRequest(new Request(Method.GET, new Reference(query)));
		assertEquals(0, request.getStart());
		assertEquals(25, request.getLimit());
		assertEquals(1, request.getFilters().length);
		assertNotNull(request.getFilter("groupId"));
		Filter f = request.getFilters()[0];
		assertEquals("groupId", f.getProperty());
		assertEquals("200", f.getValue());
	}
}
