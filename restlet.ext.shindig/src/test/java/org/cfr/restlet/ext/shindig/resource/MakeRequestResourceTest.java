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
import static junitx.framework.StringAssert.assertStartsWith;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.MakeRequestHandler;
import org.cfr.restlet.ext.shindig.resource.MakeRequestResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;


/**
 * Tests for MakeRequestServlet.
 *
 * Tests are trivial; real tests are in MakeRequestHandlerTest.
 */
public class MakeRequestResourceTest extends ResourceTestFixture {

    private static final Uri REQUEST_URL = Uri.parse("http://example.org/file");

    private static final String RESPONSE_BODY = "Hello, world!";

    private static final String ERROR_MESSAGE = "Broken!";

    private final MakeRequestResource resource = new MakeRequestResource();

    private final MakeRequestHandler handler = new MakeRequestHandler(pipeline, null);

    private final HttpRequest internalRequest = new HttpRequest(REQUEST_URL);

    private final HttpResponse internalResponse = new HttpResponse(RESPONSE_BODY);

    @Override
    @Before
    public void setUp() throws Exception {
        Request request = new Request(Method.GET, REQUEST_URL.toString());
        request.getAttributes().put(ContextResource.HEADER_ATTRIBUTE_NAME, new Form());
        Response response = new Response(request);
        resource.init(Context.getCurrent(), request, response);

        ContextResource contextResource = resource.getContextResource();
        resource.setMakeRequestHandler(handler);
        //        expect(request.getHeaderNames()).andReturn(EMPTY_ENUM).anyTimes();
        contextResource.getParameters().set(MakeRequestHandler.METHOD_PARAM, "GET");
        contextResource.getParameters().set(Param.URL.getKey(), REQUEST_URL.toString());
    }

    private void assertResponseOk(int expectedStatus, String expectedBody) throws JSONException {
        ContextResource contextResource = resource.getContextResource();

        if (Status.SUCCESS_OK.equals(contextResource.getStatus())) {
            String body = contextResource.getText();
            assertStartsWith(MakeRequestHandler.UNPARSEABLE_CRUFT, body);
            body = body.substring(MakeRequestHandler.UNPARSEABLE_CRUFT.length());
            JSONObject object = new JSONObject(body);
            object = object.getJSONObject(REQUEST_URL.toString());
            assertEquals(expectedStatus, object.getInt("rc"));
            assertEquals(expectedBody, object.getString("body"));
        } else {
            fail("Invalid response for request.");
        }
    }

    @Test
    public void testDoGetNormal() throws Exception {

        expect(pipeline.execute(internalRequest)).andReturn(internalResponse);
        replay();

        resource.doGet();

        assertResponseOk(HttpResponse.SC_OK, RESPONSE_BODY);
    }

    @Test
    public void testDoGetHttpError() throws Exception {
        expect(pipeline.execute(internalRequest)).andReturn(HttpResponse.notFound());
        replay();

        resource.doGet();

        assertResponseOk(HttpResponse.SC_NOT_FOUND, "");
    }

    @Test
    public void testDoGetException() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        expect(pipeline.execute(internalRequest)).andThrow(new GadgetException(GadgetException.Code.FAILED_TO_RETRIEVE_CONTENT, ERROR_MESSAGE));
        replay();

        resource.doGet();

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, contextResource.getStatus());
        assertContains(ERROR_MESSAGE, contextResource.getStatus().getDescription());
    }

    @Test
    public void testDoPostNormal() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        contextResource.getRequest().setMethod(Method.POST);
        expect(pipeline.execute(internalRequest)).andReturn(internalResponse);
        replay();

        resource.doPost();

        assertResponseOk(HttpResponse.SC_OK, RESPONSE_BODY);
    }

    @Test
    public void testDoPostHttpError() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        contextResource.getRequest().setMethod(Method.POST);
        expect(pipeline.execute(internalRequest)).andReturn(HttpResponse.notFound());
        replay();

        resource.doGet();

        assertResponseOk(HttpResponse.SC_NOT_FOUND, "");
    }

    @Test
    public void testDoPostException() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        contextResource.getRequest().setMethod(Method.POST);
        expect(pipeline.execute(internalRequest)).andThrow(new GadgetException(GadgetException.Code.FAILED_TO_RETRIEVE_CONTENT, ERROR_MESSAGE));
        replay();

        resource.doPost();

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, contextResource.getStatus());
        assertContains(ERROR_MESSAGE, contextResource.getStatus().getDescription());
    }
}
