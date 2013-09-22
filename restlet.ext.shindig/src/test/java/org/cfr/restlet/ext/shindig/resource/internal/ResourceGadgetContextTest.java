/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.cfr.restlet.ext.shindig.resource.internal;

import java.io.IOException;
import java.util.Locale;

import org.apache.shindig.auth.AnonymousSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.gadgets.GadgetContext;
import org.cfr.restlet.ext.shindig.auth.AuthInfo;
import org.cfr.restlet.ext.shindig.internal.ResourceGadgetContext;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.ResourceTestFixture;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;

public class ResourceGadgetContextTest extends ResourceTestFixture {

	ContextResource contextResource;

	@Override
	@Before
	public void setUp() throws IOException {
		contextResource = createContextResource(Method.GET);
	}

	private ContextResource createContextResource(Method method) throws IOException {
		Request request = new Request(method, "http://foo.com/");
		request.getAttributes().put("org.restlet.http.headers", new Form());
		Response response = new Response(request);
		ContextResource contextResource = new ContextResource(request, response);
		return contextResource;
	}

	@Test
	public void testIgnoreCacheParam() {
		contextResource.getParameters().set("nocache", Integer.toString(Integer.MAX_VALUE));
		replay();
		GadgetContext context = new ResourceGadgetContext(contextResource);
		assertTrue(context.getIgnoreCache());
	}

	@Test
	public void testLocale() {
		contextResource.getParameters().set("lang", Locale.CHINA.getLanguage());
		contextResource.getParameters().set("country", Locale.CHINA.getCountry());
		replay();
		GadgetContext context = new ResourceGadgetContext(contextResource);
		assertEquals(Locale.CHINA, context.getLocale());
	}

	@Test
	public void testDebug() {
		contextResource.getParameters().set("debug", "1");
		replay();
		GadgetContext context = new ResourceGadgetContext(contextResource);
		assertTrue(context.getDebug());
	}

	@Test
	public void testGetParameter() {
		contextResource.getParameters().set("foo", "bar");
		replay();
		GadgetContext context = new ResourceGadgetContext(contextResource);
		assertEquals("bar", context.getParameter("foo"));
	}

	@Test
	public void testGetHost() {
		contextResource.getRequest().setHostRef(new Reference("http://foo.org"));
		replay();
		GadgetContext context = new ResourceGadgetContext(contextResource);
		assertEquals("foo.org", context.getHost());
	}

	@Test
	public void testGetUserIp() {
		contextResource.getRequest().getClientInfo().setAddress("2.3.4.5");
		replay();
		GadgetContext context = new ResourceGadgetContext(contextResource);
		assertEquals("2.3.4.5", context.getUserIp());
	}

	@Test
	public void testGetSecurityToken() throws Exception {
		SecurityToken expected = new AnonymousSecurityToken();
		contextResource.getRequest().getAttributes().put(AuthInfo.Attribute.SECURITY_TOKEN.getId(), expected);
		replay();
		GadgetContext context = new ResourceGadgetContext(contextResource);
		assertEquals(expected, context.getToken());
	}
}