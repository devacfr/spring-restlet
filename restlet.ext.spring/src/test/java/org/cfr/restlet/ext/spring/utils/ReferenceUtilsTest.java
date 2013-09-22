package org.cfr.restlet.ext.spring.utils;

import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.spring.utils.ReferenceUtils;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;

public class ReferenceUtilsTest extends EasyMockTestCase {

	@Test(expected = IllegalArgumentException.class)
	public void createRootReferenceWithRootRefNull() {
		Request request = new Request(Method.GET, "http://localhost:8080/");
		request.getResourceRef().setBaseRef(new Reference("http://localhost:8080"));
		ReferenceUtils.createRootReference(request, "/");
	}

	@Test
	public void createRootReferenceWithSlashPath() {
		Request request = new Request(Method.GET, "http:localhost:8080/");
		request.getResourceRef().setBaseRef(new Reference("http:localhost:8080"));
		request.setRootRef(new Reference("http:localhost:8080"));
		Reference ref = ReferenceUtils.createRootReference(request, "/index.html");
		assertEquals("http:localhost:8080/index.html", ref.toString());
	}

	@Test
	public void createRootReferenceWithSimplePath() {
		Request request = new Request(Method.GET, "http://localhost:8080/");
		request.getResourceRef().setBaseRef(new Reference("http://localhost:8080"));
		request.setRootRef(new Reference("http://localhost:8080/site"));
		Reference ref = ReferenceUtils.createRootReference(request, "/test.html");
		assertEquals("http://localhost:8080/site/test.html", ref.toString());
	}

	@Test
	public void getContextPathWithRootRefNull() {
		Request request = new Request(Method.GET, "http://localhost:8080/");
		String path = ReferenceUtils.getContextPath(request);
		assertEquals("", path);
	}

	@Test
	public void getContextPath() {
		Request request = new Request(Method.GET, "http://localhost:8080/index.html");
		request.setRootRef(new Reference("http://localhost:8080"));
		String path = ReferenceUtils.getContextPath(request);
		assertEquals("http://localhost:8080", path);
	}

	@Test
	public void getContextPathWithContext() {
		Request request = new Request(Method.GET, "http://localhost:8080/index.html");
		request.setRootRef(new Reference("http://localhost:8080"));
		Reference ref = ReferenceUtils.getContextPath(null, request);
		assertEquals("http://localhost:8080", ref.toString());
	}

	@Test
	public void isAbsoluteUrl() {
		assertTrue(ReferenceUtils.isAbsoluteUrl("http://localhost/index.html"));
		assertFalse(ReferenceUtils.isAbsoluteUrl("index.html"));
		assertFalse(ReferenceUtils.isAbsoluteUrl(null));
	}

	@Test
	public void isAbsoluteUrlWithInvalidScheme() {
		assertFalse(ReferenceUtils.isAbsoluteUrl("g_tt://localhost/index.html"));
	}

	@Test
	public void resolveUrl() {
		Request request = new Request(Method.GET, "http://localhost:8080");
		request.setRootRef(new Reference("http://localhost:8080"));
		assertEquals("http://localhost:8080/index.html", ReferenceUtils.resolveUrl("/index.html", request));
		assertEquals("index.html", ReferenceUtils.resolveUrl("index.html", request));
	}

	@Test
	public void resolveAbsoluteUrl() {
		Request request = new Request(Method.GET, "http://localhost:8080");
		request.setRootRef(new Reference("http://localhost:8080"));
		assertEquals("http://absolute/index.html", ReferenceUtils.resolveUrl("http://absolute/index.html", request));
	}

	@Test
	public void resolveUrlWithoutRootReference() {
		Request request = new Request(Method.GET, "http://localhost:8080");
		assertEquals("/index.html", ReferenceUtils.resolveUrl("/index.html", request));
		assertEquals("index.html", ReferenceUtils.resolveUrl("index.html", request));
	}
}
