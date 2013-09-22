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

import static junitx.framework.StringAssert.assertStartsWith;

import java.io.IOException;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.AuthType;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.HttpResponseBuilder;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.cfr.restlet.ext.shindig.auth.AuthInfo;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.MakeRequestHandler;
import org.cfr.restlet.ext.shindig.testing.FakeGadgetToken;
import org.easymock.Capture;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;


/**
 * Tests for MakeRequestHandler.
 */
public class MakeRequestHandlerTest extends ResourceTestFixture {

    private static final Uri REQUEST_URL = Uri.parse("http://example.org/file");

    private static final String REQUEST_BODY = "I+am+the+request+body!foo=baz%20la";

    private static final String RESPONSE_BODY = "makeRequest response body";

    private static final SecurityToken DUMMY_TOKEN = new FakeGadgetToken();

    private ContextResource contextResource;

    private final MakeRequestHandler handler = new MakeRequestHandler(pipeline, rewriterRegistry);

    private void expectGetAndReturnBody(String response) throws Exception {
        expectGetAndReturnBody(AuthType.NONE, response);
    }

    private void expectGetAndReturnBody(AuthType authType, String response) throws Exception {
        HttpRequest request = new HttpRequest(REQUEST_URL).setAuthType(authType);
        expect(pipeline.execute(request)).andReturn(new HttpResponse(response));
    }

    private void expectPostAndReturnBody(String postData, String response) throws Exception {
        expectPostAndReturnBody(AuthType.NONE, postData, response);
    }

    private void expectPostAndReturnBody(AuthType authType, String postData, String response) throws Exception {
        HttpRequest req = new HttpRequest(REQUEST_URL).setMethod("POST").setPostBody(REQUEST_BODY.getBytes("UTF-8")).setAuthType(authType).addHeader(
                "Content-Type", "application/x-www-form-urlencoded");
        expect(pipeline.execute(req)).andReturn(new HttpResponse(response));
        contextResource.getParameters().set(MakeRequestHandler.METHOD_PARAM, "POST");
        contextResource.getParameters().set(MakeRequestHandler.POST_DATA_PARAM, REQUEST_BODY);
    }

    private JSONObject extractJsonFromResponse() throws JSONException {
        String body = contextResource.getText();
        assertStartsWith(MakeRequestHandler.UNPARSEABLE_CRUFT, body);
        body = body.substring(MakeRequestHandler.UNPARSEABLE_CRUFT.length());
        return new JSONObject(body).getJSONObject(REQUEST_URL.toString());
    }

    @Override
    @Before
    public void setUp() throws IOException {
        Request request = new Request(Method.POST, REQUEST_URL.toString());
        request.getAttributes().put("org.restlet.http.headers", new Form());
        Response response = new Response(request);
        contextResource = new ContextResource(request, response);

        contextResource.getParameters().set(Param.URL.getKey(), REQUEST_URL.toString());
    }

    @Test
    public void testGetRequest() throws Exception {
        expectGetAndReturnBody(RESPONSE_BODY);
        replay();

        handler.fetch(contextResource);

        JSONObject results = extractJsonFromResponse();
        assertEquals(HttpResponse.SC_OK, results.getInt("rc"));
        assertEquals(RESPONSE_BODY, results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testGetRequestWithUncommonStatusCode() throws Exception {
        HttpRequest req = new HttpRequest(REQUEST_URL);
        HttpResponse response = new HttpResponseBuilder().setHttpStatusCode(HttpResponse.SC_CREATED).setResponseString(RESPONSE_BODY).create();
        expect(pipeline.execute(req)).andReturn(response);
        replay();

        handler.fetch(contextResource);

        JSONObject results = extractJsonFromResponse();
        assertEquals(HttpResponse.SC_CREATED, results.getInt("rc"));
        assertEquals(RESPONSE_BODY, results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testGetRequestWithRefresh() throws Exception {
        contextResource.getParameters().set(Param.REFRESH.getKey(), "120");

        Capture<HttpRequest> requestCapture = new Capture<HttpRequest>();
        expect(pipeline.execute(capture(requestCapture))).andReturn(new HttpResponse(RESPONSE_BODY));

        replay();

        handler.fetch(contextResource);

        HttpRequest httpRequest = requestCapture.getValue();
        assertEquals("public,max-age=120", contextResource.getResponseHeaders().getFirstValue("Cache-Control"));
        assertEquals(120, httpRequest.getCacheTtl());
    }

    @Test
    public void testGetRequestWithBadTtl() throws Exception {
        contextResource.getParameters().set(Param.REFRESH.getKey(), "foo");

        Capture<HttpRequest> requestCapture = new Capture<HttpRequest>();
        expect(pipeline.execute(capture(requestCapture))).andReturn(new HttpResponse(RESPONSE_BODY));

        replay();
        try {
            handler.fetch(contextResource);
            fail();
        } catch (GadgetException e) {
            // Expected - catch now occurs at the MakeRequestServlet level.
        }

        HttpRequest httpRequest = requestCapture.getValue();

        assertEquals(null, contextResource.getResponseHeaders().getFirstValue("Cache-Control"));
        assertEquals(-1, httpRequest.getCacheTtl());
    }

    @Test
    public void testExplicitHeaders() throws Exception {
        String headerString = "X-Foo=bar&X-Bar=baz%20foo";

        HttpRequest expected = new HttpRequest(REQUEST_URL).addHeader("X-Foo", "bar").addHeader("X-Bar", "baz foo");
        expect(pipeline.execute(expected)).andReturn(new HttpResponse(RESPONSE_BODY));
        contextResource.getParameters().set(MakeRequestHandler.HEADERS_PARAM, headerString);
        replay();

        handler.fetch(contextResource);
        verify();

        JSONObject results = extractJsonFromResponse();
        assertEquals(HttpResponse.SC_OK, results.getInt("rc"));
        assertEquals(RESPONSE_BODY, results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testPostRequest() throws Exception {
        contextResource.getParameters().set(MakeRequestHandler.METHOD_PARAM, "POST");
        expectPostAndReturnBody(REQUEST_BODY, RESPONSE_BODY);
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(HttpResponse.SC_OK, results.getInt("rc"));
        assertEquals(RESPONSE_BODY, results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testFetchContentTypeFeed() throws Exception {
        String entryTitle = "Feed title";
        String entryLink = "http://example.org/entry/0/1";
        String entrySummary = "This is the summary";
        String rss = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<rss version=\"2.0\"><channel>" + "<title>dummy</title>"
                + "<link>http://example.org/</link>" + "<item>" + "<title>" + entryTitle + "</title>" + "<link>" + entryLink + "</link>"
                + "<description>" + entrySummary + "</description>" + "</item>" + "</channel></rss>";

        expectGetAndReturnBody(rss);
        contextResource.getParameters().set(MakeRequestHandler.CONTENT_TYPE_PARAM, "FEED");
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        JSONObject feed = new JSONObject(results.getString("body"));
        JSONObject entry = feed.getJSONArray("Entry").getJSONObject(0);

        assertEquals(entryTitle, entry.getString("Title"));
        assertEquals(entryLink, entry.getString("Link"));
        assertNull("getSummaries has the wrong default value (should be false).", entry.optString("Summary", null));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testFetchFeedWithParameters() throws Exception {
        String entryTitle = "Feed title";
        String entryLink = "http://example.org/entry/0/1";
        String entrySummary = "This is the summary";
        String rss = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<rss version=\"2.0\"><channel>" + "<title>dummy</title>"
                + "<link>http://example.org/</link>" + "<item>" + "<title>"
                + entryTitle
                + "</title>"
                + "<link>"
                + entryLink
                + "</link>"
                + "<description>"
                + entrySummary
                + "</description>"
                + "</item>"
                + "<item>"
                + "<title>"
                + entryTitle
                + "</title>"
                + "<link>"
                + entryLink
                + "</link>"
                + "<description>"
                + entrySummary
                + "</description>"
                + "</item>"
                + "<item>"
                + "<title>"
                + entryTitle
                + "</title>"
                + "<link>"
                + entryLink
                + "</link>"
                + "<description>"
                + entrySummary + "</description>" + "</item>" + "</channel></rss>";

        expectGetAndReturnBody(rss);
        contextResource.getParameters().set(MakeRequestHandler.GET_SUMMARIES_PARAM, "true");
        contextResource.getParameters().set(MakeRequestHandler.NUM_ENTRIES_PARAM, "2");
        contextResource.getParameters().set(MakeRequestHandler.CONTENT_TYPE_PARAM, "FEED");
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        JSONObject feed = new JSONObject(results.getString("body"));
        JSONArray feeds = feed.getJSONArray("Entry");

        assertEquals("numEntries not parsed correctly.", 2, feeds.length());

        JSONObject entry = feeds.getJSONObject(1);
        assertEquals(entryTitle, entry.getString("Title"));
        assertEquals(entryLink, entry.getString("Link"));
        assertTrue("getSummaries not parsed correctly.", entry.has("Summary"));
        assertEquals(entrySummary, entry.getString("Summary"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testFetchEmptyDocument() throws Exception {
        expectGetAndReturnBody("");
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(HttpResponse.SC_OK, results.getInt("rc"));
        assertEquals("", results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testSignedGetRequest() throws Exception {

        contextResource.getRequest().getAttributes().put(AuthInfo.Attribute.SECURITY_TOKEN.getId(), DUMMY_TOKEN);
        contextResource.getParameters().set(MakeRequestHandler.AUTHZ_PARAM, AuthType.SIGNED.toString());
        HttpRequest expected = new HttpRequest(REQUEST_URL).setAuthType(AuthType.SIGNED);
        expect(pipeline.execute(expected)).andReturn(new HttpResponse(RESPONSE_BODY));
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(RESPONSE_BODY, results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testSignedPostRequest() throws Exception {
        // Doesn't actually sign since it returns the standard fetcher.
        // Signing tests are in SigningFetcherTest
        expectPostAndReturnBody(AuthType.SIGNED, REQUEST_BODY, RESPONSE_BODY);
        contextResource.getRequest().getAttributes().put(AuthInfo.Attribute.SECURITY_TOKEN.getId(), DUMMY_TOKEN);
        contextResource.getParameters().set(MakeRequestHandler.AUTHZ_PARAM, AuthType.SIGNED.toString());
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(RESPONSE_BODY, results.get("body"));
        assertFalse("A security token was returned when it was not requested.", results.has("st"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testChangeSecurityToken() throws Exception {
        // Doesn't actually sign since it returns the standard fetcher.
        // Signing tests are in SigningFetcherTest
        expectGetAndReturnBody(AuthType.SIGNED, RESPONSE_BODY);
        FakeGadgetToken authToken = new FakeGadgetToken().setUpdatedToken("updated");
        contextResource.getRequest().getAttributes().put(AuthInfo.Attribute.SECURITY_TOKEN.getId(), authToken);
        contextResource.getParameters().set(MakeRequestHandler.AUTHZ_PARAM, AuthType.SIGNED.toString());
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(RESPONSE_BODY, results.get("body"));
        assertEquals("updated", results.getString("st"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testDoOAuthRequest() throws Exception {
        // Doesn't actually do oauth dance since it returns the standard fetcher.
        // OAuth tests are in OAuthRequestTest
        expectGetAndReturnBody(AuthType.OAUTH, RESPONSE_BODY);
        FakeGadgetToken authToken = new FakeGadgetToken().setUpdatedToken("updated");
        contextResource.getRequest().getAttributes().put(AuthInfo.Attribute.SECURITY_TOKEN.getId(), authToken);
        contextResource.getParameters().set(MakeRequestHandler.AUTHZ_PARAM, AuthType.OAUTH.toString());
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(HttpResponse.SC_OK, results.getInt("rc"));
        assertEquals(RESPONSE_BODY, results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testInvalidSigningTypeTreatedAsNone() throws Exception {
        expectGetAndReturnBody(RESPONSE_BODY);
        contextResource.getParameters().set(MakeRequestHandler.AUTHZ_PARAM, "garbage");
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(HttpResponse.SC_OK, results.getInt("rc"));
        assertEquals(RESPONSE_BODY, results.get("body"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testBadHttpResponseIsPropagated() throws Exception {
        HttpRequest internalRequest = new HttpRequest(REQUEST_URL);
        expect(pipeline.execute(internalRequest)).andReturn(HttpResponse.error());
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(HttpResponse.SC_INTERNAL_SERVER_ERROR, results.getInt("rc"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test(expected = GadgetException.class)
    public void testBadSecurityTokenThrows() throws Exception {
        //        contextResource.getParameters().set(AuthInfo.Attribute.SECURITY_TOKEN.getId(), null);
        contextResource.getParameters().set(MakeRequestHandler.AUTHZ_PARAM, AuthType.SIGNED.toString());
        replay();

        handler.fetch(contextResource);
    }

    @Test
    public void testMetadataCopied() throws Exception {
        HttpRequest internalRequest = new HttpRequest(REQUEST_URL);
        HttpResponse response = new HttpResponseBuilder().setResponse("foo".getBytes("UTF-8")).setMetadata("foo", RESPONSE_BODY).create();

        expect(pipeline.execute(internalRequest)).andReturn(response);
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();

        assertEquals(RESPONSE_BODY, results.getString("foo"));
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testSetCookiesReturned() throws Exception {
        HttpRequest internalRequest = new HttpRequest(REQUEST_URL);
        HttpResponse response = new HttpResponseBuilder().setResponse("foo".getBytes("UTF-8")).addHeader("Set-Cookie", "foo=bar; Secure").addHeader(
                "Set-Cookie", "name=value").create();

        expect(pipeline.execute(internalRequest)).andReturn(response);
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();
        JSONObject headers = results.getJSONObject("headers");
        assertNotNull(headers);
        JSONArray cookies = headers.getJSONArray("set-cookie");
        assertNotNull(cookies);
        assertEquals(2, cookies.length());
        assertEquals("foo=bar; Secure", cookies.get(0));
        assertEquals("name=value", cookies.get(1));
    }

    @Test
    public void testLocationReturned() throws Exception {
        HttpRequest internalRequest = new HttpRequest(REQUEST_URL);
        HttpResponse response = new HttpResponseBuilder().setResponse("foo".getBytes("UTF-8")).addHeader("Location", "somewhere else").create();

        expect(pipeline.execute(internalRequest)).andReturn(response);
        replay();

        handler.fetch(contextResource);
        JSONObject results = extractJsonFromResponse();
        JSONObject headers = results.getJSONObject("headers");
        assertNotNull(headers);
        JSONArray locations = headers.getJSONArray("location");
        assertNotNull(locations);
        assertEquals(1, locations.length());
        assertEquals("somewhere else", locations.get(0));
    }
}
