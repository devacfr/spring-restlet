/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.cfr.restlet.ext.shindig.resource;

import static junitx.framework.StringAssert.assertContains;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.LockedDomainService;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.servlet.ProxyHandler;
import org.apache.shindig.gadgets.uri.ProxyUriManager;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.ProxyResource;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;


/**
 * Tests for ProxyServlet.
 *
 * Tests are trivial; real tests are in ProxyHandlerTest.
 */
public class ProxyResourceTest extends ResourceTestFixture {

    private static final Uri REQUEST_URL = Uri.parse("http://example.org/file");

    private static final String BASIC_SYNTAX_URL = "http://opensocial.org/proxy?foo=bar&url=" + REQUEST_URL;

    private static final String RESPONSE_BODY = "Hello, world!";

    private static final String ERROR_MESSAGE = "Broken!";

    private final ProxyUriManager proxyUriManager = mock(ProxyUriManager.class);

    private final LockedDomainService lockedDomainService = mock(LockedDomainService.class);

    private final ProxyHandler proxyHandler = mock(ProxyHandler.class);

    private final ProxyResource resource = new ProxyResource();

    private final ProxyUriManager.ProxyUri proxyUri = mock(ProxyUriManager.ProxyUri.class);

    @Override
    @Before
    public void setUp() throws Exception {

        Request request = new Request(Method.GET, REQUEST_URL.toString());
        request.getAttributes().put("org.restlet.http.headers", new Form());
        Response response = new Response(request);
        resource.init(Context.getCurrent(), request, response);

        resource.setProxyHandler(proxyHandler);
        resource.setProxyUriManager(proxyUriManager);
        resource.setLockedDomainService(lockedDomainService);

    }

    private void setupRequest(String str) throws Exception {
        setupRequest(str, true);
    }

    private void setupRequest(String str, boolean ldSafe) throws Exception {
        Uri uri = Uri.parse(str);
        ContextResource contextResource = resource.getContextResource();
        contextResource.getRequest().setResourceRef(new Reference(str));
        contextResource.getRequestHeaders().add(HeaderConstants.HEADER_HOST, uri.getAuthority());
        expect(proxyUriManager.process(uri)).andReturn(proxyUri);
        expect(lockedDomainService.isSafeForOpenProxy(uri.getAuthority())).andReturn(ldSafe);
    }

    private void assertResponseOk(Status expectedStatus, String expectedBody) {
        ContextResource contextResource = resource.getContextResource();
        assertEquals(expectedStatus, contextResource.getStatus());
        assertEquals(expectedBody, contextResource.getText());
    }

    @Test
    public void testIfModifiedSinceAlwaysReturnsEarly() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        contextResource.getRequestHeaders().set("If-Modified-Since", "Yes, this is an invalid header");

        replay();
        resource.doGet();
        verify();

        assertEquals(Status.REDIRECTION_NOT_MODIFIED, contextResource.getStatus());
        assertFalse(rewriter.responseWasRewritten());
    }

    @Test
    public void testDoGetNormal() throws Exception {
        setupRequest(BASIC_SYNTAX_URL);
        expect(proxyHandler.fetch(proxyUri)).andReturn(new HttpResponse(RESPONSE_BODY));
        replay();

        resource.doGet();
        verify();
        assertResponseOk(Status.SUCCESS_OK, RESPONSE_BODY);
    }

    @Test
    public void testDoGetHttpError() throws Exception {
        setupRequest(BASIC_SYNTAX_URL);
        expect(proxyHandler.fetch(proxyUri)).andReturn(HttpResponse.notFound());
        replay();

        resource.doGet();
        verify();

        assertResponseOk(Status.CLIENT_ERROR_NOT_FOUND, "");
    }

    @Test
    public void testDoGetException() throws Exception {
        setupRequest(BASIC_SYNTAX_URL);
        expect(proxyHandler.fetch(proxyUri)).andThrow(new GadgetException(GadgetException.Code.FAILED_TO_RETRIEVE_CONTENT, ERROR_MESSAGE));
        replay();

        resource.doGet();
        verify();

        ContextResource contextResource = resource.getContextResource();

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, contextResource.getStatus());
        assertContains(ERROR_MESSAGE, contextResource.getText());
    }

    @Test
    public void testDoGetNormalWithLockedDomainUnsafe() throws Exception {
        setupRequest(BASIC_SYNTAX_URL, false);

        replay();
        resource.doGet();
        verify();

        ContextResource contextResource = resource.getContextResource();

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, contextResource.getStatus());
        assertContains("wrong domain", contextResource.getText());
    }

}
