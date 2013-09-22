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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

import java.io.IOException;

import org.apache.shindig.gadgets.servlet.JsonRpcHandler;
import org.apache.shindig.gadgets.servlet.RpcException;
import org.cfr.restlet.ext.shindig.resource.RpcResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;


/**
 * Tests for RpcServlet.
 */
public class RpcResourceTest extends Assert {

    private RpcResource resource;

    private JsonRpcHandler handler;

    @Before
    public void setUp() throws Exception {
        resource = new RpcResource();

        handler = createMock(JsonRpcHandler.class);
        resource.setJsonRpcHandler(handler);
    }

    @Test
    public void testDoGetNormal() throws Exception {

        Request request = createGetRequest("{\"gadgets\":[]}", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._");;
        Response response = createResponse(request, MediaType.APPLICATION_JSON,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz._({\"GADGETS\":[]})", Status.SUCCESS_OK);

        Form params = new Form();
        params.add(Disposition.NAME_FILENAME, "rpc.txt");
        Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT, params);
        response.getEntity().setDisposition(disposition);
        resource.init(Context.getCurrent(), request, response);

        JSONObject handlerResponse = new JSONObject("{\"GADGETS\":[]}");
        expect(handler.process(isA(JSONObject.class))).andReturn(handlerResponse);
        replay(handler);
        resource.doGet();
        //        verify(response);
    }

    @Test
    public void testDoGetWithHandlerRpcException() throws Exception {
        Request request = createGetRequest("{\"gadgets\":[]}", "function");
        Response response = createResponse(request, "rpcExceptionMessage", Status.SERVER_ERROR_INTERNAL);
        expect(handler.process(isA(JSONObject.class))).andThrow(new RpcException("rpcExceptionMessage"));
        replay(handler);
        resource.init(Context.getCurrent(), request, response);
        resource.doGet();
        //        verify(response);
    }

    @Test
    public void testDoGetWithHandlerJsonException() throws Exception {
        Request request = createGetRequest("{\"gadgets\":[]}", "function");
        Response response = createResponse(request, "Malformed JSON request.", Status.CLIENT_ERROR_BAD_REQUEST);
        expect(handler.process(isA(JSONObject.class))).andThrow(new JSONException("json"));
        replay(handler);
        resource.init(Context.getCurrent(), request, response);
        resource.doGet();
        //        verify(response);
    }

    @Test
    public void testDoGetWithMissingReqParam() throws Exception {
        Request request = createGetRequest(null, "function");
        Response response = createResponse(request, null, Status.CLIENT_ERROR_BAD_REQUEST);
        resource.init(Context.getCurrent(), request, response);
        resource.doGet();
        //        verify(response);
    }

    @Test
    public void testDoGetWithMissingCallbackParam() throws Exception {
        Request request = createGetRequest("{\"gadgets\":[]}", null);
        Response response = createResponse(request, null, Status.CLIENT_ERROR_BAD_REQUEST);
        resource.init(Context.getCurrent(), request, response);

        resource.doGet();
        //        verify(response);
    }

    @Test
    public void testDoGetWithBadCallbackParamValue() throws Exception {
        Request request = createGetRequest("{\"gadgets\":[]}", "/'!=");
        Response response = createResponse(request, null, Status.CLIENT_ERROR_BAD_REQUEST);

        resource.init(Context.getCurrent(), request, response);
        resource.doGet();
        //        verify(response);
    }

    private Request createGetRequest(String reqParamValue, String callbackParamValue) {
        Reference ref = new Reference("http://foo.com");
        if (reqParamValue != null) {
            ref.addQueryParameter(RpcResource.GET_REQUEST_REQ_PARAM, reqParamValue);
        }
        if (callbackParamValue != null) {
            ref.addQueryParameter(RpcResource.GET_REQUEST_CALLBACK_PARAM, callbackParamValue);
        }
        Request result = new Request(Method.GET, ref);
        result.setEntity(new StringRepresentation(""));
        result.getAttributes().put("org.restlet.http.headers", new Form());
        result.getEntity().setCharacterSet(CharacterSet.UTF_8);
        return result;
    }

    private Response createResponse(Request request, String response, Status httpStatusCode) throws IOException {
        return createResponse(request, null, response, httpStatusCode);
    }

    private Response createResponse(Request request, MediaType contentType, String response, Status httpStatusCode) throws IOException {
        Response result = new Response(request);
        if (response != null) {
            Representation representation = new StringRepresentation(response);
            result.setEntity(representation);
            if (contentType != null) {
                representation.setMediaType(contentType);
            }
        }
        result.setStatus(httpStatusCode);
        return result;
    }
}
