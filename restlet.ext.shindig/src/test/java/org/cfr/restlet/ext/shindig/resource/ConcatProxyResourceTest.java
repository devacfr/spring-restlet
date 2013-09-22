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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.HttpResponseBuilder;
import org.apache.shindig.gadgets.uri.ConcatUriManager;
import org.apache.shindig.gadgets.uri.UriStatus;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.resource.ConcatProxyResource;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ConcatProxyResourceTest extends ResourceTestFixture {

    private static final String REQUEST_DOMAIN = "example.org";

    private static final Uri URL1 = Uri.parse("http://example.org/1.js");

    private static final Uri URL2 = Uri.parse("http://example.org/2.js");

    private static final Uri URL3 = Uri.parse("http://example.org/3.js");

    private static final String SCRT1 = "var v1 = 1;";

    private static final String SCRT2 = "var v2 = { \"a-b\": 1 , c: \"hello!,\" };";

    private static final String SCRT3 = "var v3 = \"world\";";

    private static final String SCRT1_ESCAPED = "var v1 = 1;";

    private static final String SCRT2_ESCAPED = "var v2 = { \\\"a-b\\\": 1 , c: \\\"hello!,\\\" };";

    private static final String SCRT3_ESCAPED = "var v3 = \\\"world\\\";";

    private final ConcatProxyResource resource = new ConcatProxyResource();

    private TestConcatUriManager uriManager;

    private final ExecutorService sequentialExecutor = Executors.newSingleThreadExecutor();

    private final ExecutorService threadedExecutor = Executors.newCachedThreadPool();

    @Override
    @Before
    public void setUp() throws Exception {
        Request request = new Request(Method.GET, new Reference(REQUEST_DOMAIN));
        Response response = new Response(request);
        request.getAttributes().put("org.restlet.http.headers", new Form());

        uriManager = new TestConcatUriManager();
        resource.setRequestPipeline(pipeline);
        resource.setConcatUriManager(uriManager);
        resource.init(Context.getCurrent(), request, response);
        ContextResource contextResource = resource.getContextResource();

        contextResource.getRequestHeaders().set(HeaderConstants.HEADER_HOST, REQUEST_DOMAIN);
        expect(lockedDomainService.isSafeForOpenProxy(REQUEST_DOMAIN)).andReturn(true).anyTimes();

        expectGetAndReturnData(URL1, SCRT1);
        expectGetAndReturnData(URL2, SCRT2);
        expectGetAndReturnData(URL3, SCRT3);
    }

    private void expectGetAndReturnData(Uri url, String data) throws Exception {
        HttpRequest req = new HttpRequest(url);
        HttpResponse resp = new HttpResponseBuilder().setResponse(data.getBytes()).create();
        expect(pipeline.execute(req)).andReturn(resp).anyTimes();
    }

    /**
     * Simulate the added comments by concat
     * @param data - concatenated data
     * @param url - data source url
     * @return data with added comments
     */
    private String addComment(String data, String url) {
        String res = "/* ---- Start " + url + " ---- */\r\n" + data + "/* ---- End " + url + " ---- */\r\n";
        return res;
    }

    private String addErrComment(String url, int code) {
        return "/* ---- Error " + code + " (" + url + ") ---- */\r\n";
    }

    /**
     * Simulate the asJSON result of one script
     * @param url - the script url
     * @param data - the script escaped content
     * @return simulated hash mapping
     */
    private String addVar(String url, String data) {
        return '\"' + url + "\":\"" + data + "\",\r\n";

    }

    /**
     * Run a concat test
     * @param result - expected concat results
     * @param uris - list of uris to concat
     * @throws Exception
     */
    private void runConcat(ExecutorService exec, String result, String tok, Uri... uris) throws Exception {
        expectRequestWithUris(Lists.newArrayList(uris), tok);

        ContextResource contextResource = resource.getContextResource();
        // Run the servlet
        resource.setExecutor(exec);
        resource.doGet();
        verify();
        assertEquals(result, contextResource.getText());
        assertEquals(Status.valueOf(200), contextResource.getStatus());
    }

    @Test
    public void testSimpleConcat() throws Exception {
        String results = addComment(SCRT1, URL1.toString()) + addComment(SCRT2, URL2.toString());
        runConcat(sequentialExecutor, results, null, URL1, URL2);
    }

    @Test
    public void testSimpleConcatThreaded() throws Exception {
        String results = addComment(SCRT1, URL1.toString()) + addComment(SCRT2, URL2.toString());
        runConcat(threadedExecutor, results, null, URL1, URL2);
    }

    @Test
    public void testThreeConcat() throws Exception {
        String results = addComment(SCRT1, URL1.toString()) + addComment(SCRT2, URL2.toString()) + addComment(SCRT3, URL3.toString());
        runConcat(sequentialExecutor, results, null, URL1, URL2, URL3);
    }

    @Test
    public void testThreeConcatThreaded() throws Exception {
        String results = addComment(SCRT1, URL1.toString()) + addComment(SCRT2, URL2.toString()) + addComment(SCRT3, URL3.toString());
        runConcat(threadedExecutor, results, null, URL1, URL2, URL3);
    }

    @Test
    public void testConcatBadException() throws Exception {
        final Uri URL4 = Uri.parse("http://example.org/4.js");

        HttpRequest req = new HttpRequest(URL4);
        expect(pipeline.execute(req)).andThrow(new GadgetException(GadgetException.Code.HTML_PARSE_ERROR)).anyTimes();

        expectRequestWithUris(Lists.newArrayList(URL1, URL4));

        ContextResource contextResource = resource.getContextResource();

        // Run the servlet
        resource.doGet();
        verify();

        String results = addComment(SCRT1, URL1.toString()) + "HTML_PARSE_ERROR concat(http://example.org/4.js) null";
        assertEquals(results, contextResource.getText());
        assertEquals(Status.valueOf(400), contextResource.getStatus());
    }

    @Test
    public void testConcat404() throws Exception {
        String url = "http://nobodyhome.com/";
        HttpRequest req = new HttpRequest(Uri.parse(url));
        HttpResponse resp = new HttpResponseBuilder().setHttpStatusCode(404).create();
        expect(pipeline.execute(req)).andReturn(resp).anyTimes();

        expectRequestWithUris(Lists.newArrayList(URL1, Uri.parse(url)));

        ContextResource contextResource = resource.getContextResource();

        resource.doGet();
        verify();

        String results = addComment(SCRT1, URL1.toString()) + addErrComment(url, 404);
        assertEquals(results, contextResource.getText());
        assertEquals(Status.valueOf(200), contextResource.getStatus());
    }

    @Test
    public void testAsJsonConcat() throws Exception {
        String results = "_js={\r\n" + addVar(URL1.toString(), SCRT1_ESCAPED) + addVar(URL2.toString(), SCRT2_ESCAPED) + "};\r\n";
        runConcat(sequentialExecutor, results, "_js", URL1, URL2);
    }

    @Test
    public void testThreeAsJsonConcat() throws Exception {
        String results = "_js={\r\n" + addVar(URL1.toString(), SCRT1_ESCAPED) + addVar(URL2.toString(), SCRT2_ESCAPED)
                + addVar(URL3.toString(), SCRT3_ESCAPED) + "};\r\n";
        runConcat(sequentialExecutor, results, "_js", URL1, URL2, URL3);
    }

    @Test
    public void testBadJsonVarConcat() throws Exception {
        expectRequestWithUris(Lists.<Uri> newArrayList(), "bad code;");

        ContextResource contextResource = resource.getContextResource();
        resource.doGet();
        verify();
        String results = "/* ---- Error 400, Bad json variable name bad code; ---- */\r\n";

        assertEquals(results, contextResource.getText());
        assertEquals(Status.valueOf(400), contextResource.getStatus());
    }

    @Test
    public void testAsJsonConcat404() throws Exception {
        final Uri URL4 = Uri.parse("http://example.org/4.js");

        HttpRequest req = new HttpRequest(URL4);
        HttpResponse resp = new HttpResponseBuilder().setHttpStatusCode(404).create();
        expect(pipeline.execute(req)).andReturn(resp).anyTimes();

        String results = "_js={\r\n" + addVar(URL1.toString(), SCRT1_ESCAPED) + "/* ---- Error 404 (http://example.org/4.js) ---- */\r\n" + "};\r\n";
        runConcat(sequentialExecutor, results, "_js", URL1, URL4);
    }

    @Test
    public void testAsJsonConcatException() throws Exception {
        final Uri URL4 = Uri.parse("http://example.org/4.js");

        HttpRequest req = new HttpRequest(URL4);
        expect(pipeline.execute(req)).andThrow(new GadgetException(GadgetException.Code.FAILED_TO_RETRIEVE_CONTENT)).anyTimes();

        expectRequestWithUris(Lists.newArrayList(URL1, URL4), "_js");

        ContextResource contextResource = resource.getContextResource();

        resource.doGet();
        verify();
        String results = "_js={\r\n" + addVar(URL1.toString(), SCRT1_ESCAPED)
                + "FAILED_TO_RETRIEVE_CONTENT concat(http://example.org/4.js) null};\r\n";
        assertEquals(results, contextResource.getText());
        assertEquals(Status.valueOf(400), contextResource.getStatus());
    }

    @Test
    public void testAsJsonConcatBadException() throws Exception {
        final Uri URL4 = Uri.parse("http://example.org/4.js");

        HttpRequest req = new HttpRequest(URL4);
        expect(pipeline.execute(req)).andThrow(new GadgetException(GadgetException.Code.HTML_PARSE_ERROR)).anyTimes();

        String results = "_js={\r\n" + addVar(URL1.toString(), SCRT1_ESCAPED) + "HTML_PARSE_ERROR concat(http://example.org/4.js) null};\r\n";

        expectRequestWithUris(Lists.newArrayList(URL1, URL4), "_js");

        ContextResource contextResource = resource.getContextResource();

        // Run the servlet
        resource.doGet();
        verify();
        assertEquals(results, contextResource.getText());
        assertEquals(Status.valueOf(400), contextResource.getStatus());
    }

    private void expectRequestWithUris(List<Uri> uris) {
        expectRequestWithUris(uris, null);
    }

    private void expectRequestWithUris(List<Uri> uris, String tok) {
        Reference reference = new Reference("http://example.com/path");
        resource.getContextResource().getRequest().setResourceRef(reference);

        replay();
        Uri uri = ResourceUtil.toUri(reference);
        uriManager.expect(uri, uris, tok);
    }

    private static class TestConcatUriManager implements ConcatUriManager {

        private final Map<Uri, ConcatUri> uriMap;

        private TestConcatUriManager() {
            this.uriMap = Maps.newHashMap();
        }

        public List<ConcatData> make(List<ConcatUri> resourceUris, boolean isAdjacent) {
            // Not used by ConcatProxyServlet
            throw new UnsupportedOperationException();
        }

        public ConcatUri process(Uri uri) {
            return uriMap.get(uri);
        }

        private void expect(Uri orig, UriStatus status, Type type, List<Uri> uris, String json) {
            uriMap.put(orig, new ConcatUri(status, uris, json, type, null));
        }

        private void expect(Uri orig, List<Uri> uris, String tok) {
            expect(orig, UriStatus.VALID_UNVERSIONED, Type.JS, uris, tok);
        }
    }

}
