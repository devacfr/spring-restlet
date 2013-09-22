package org.cfr.restlet.ext.spring.utils;

import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.spring.utils.StaticHeaderUtil;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;

public class StaticHeaderUtilTest extends EasyMockTestCase {

	@Test
	public void addResponseHeadersWithNullHeaders() {
		Request request = new Request(Method.GET, "http://localhost/index.html");
		Response response = new Response(request);
		StaticHeaderUtil.addResponseHeaders(response);
		Form form = (Form) response.getAttributes().get(StaticHeaderUtil.HEADER_ATTRIBUTE_NAME);
		assertNotNull(form);
		assertEquals(1, response.getCacheDirectives().size());
	}
}
