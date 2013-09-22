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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.servlet.HttpUtil;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.HttpResponseBuilder;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.ServletUtil;
import org.cfr.restlet.ext.shindig.testing.FakeTimeSource;
import org.json.JSONObject;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;


public class ServletUtilTest {

    @Test
    public void testValidateUrlNoPath() throws Exception {
        Uri url = ServletUtil.validateUrl(Uri.parse("http://www.example.com"));
        assertEquals("http", url.getScheme());
        assertEquals("www.example.com", url.getAuthority());
        assertEquals("/", url.getPath());
        assertNull(url.getQuery());
        assertNull(url.getFragment());
    }

    @Test
    public void testValidateUrlHttps() throws Exception {
        Uri url = ServletUtil.validateUrl(Uri.parse("https://www.example.com"));
        assertEquals("https", url.getScheme());
        assertEquals("www.example.com", url.getAuthority());
        assertEquals("/", url.getPath());
        assertNull(url.getQuery());
        assertNull(url.getFragment());
    }

    @Test
    public void testValidateUrlWithPath() throws Exception {
        Uri url = ServletUtil.validateUrl(Uri.parse("http://www.example.com/foo"));
        assertEquals("http", url.getScheme());
        assertEquals("www.example.com", url.getAuthority());
        assertEquals("/foo", url.getPath());
        assertNull(url.getQuery());
        assertNull(url.getFragment());
    }

    @Test
    public void testValidateUrlWithPort() throws Exception {
        Uri url = ServletUtil.validateUrl(Uri.parse("http://www.example.com:8080/foo"));
        assertEquals("http", url.getScheme());
        assertEquals("www.example.com:8080", url.getAuthority());
        assertEquals("/foo", url.getPath());
        assertNull(url.getQuery());
        assertNull(url.getFragment());
    }

    @Test
    public void testValidateUrlWithEncodedPath() throws Exception {
        Uri url = ServletUtil.validateUrl(Uri.parse("http://www.example.com/foo%20bar"));
        assertEquals("http", url.getScheme());
        assertEquals("www.example.com", url.getAuthority());
        assertEquals("/foo%20bar", url.getPath());
        assertNull(url.getQuery());
        assertNull(url.getFragment());
    }

    @Test
    public void testValidateUrlWithEncodedQuery() throws Exception {
        Uri url = ServletUtil.validateUrl(Uri.parse("http://www.example.com/foo?q=with%20space"));
        assertEquals("http", url.getScheme());
        assertEquals("www.example.com", url.getAuthority());
        assertEquals("/foo", url.getPath());
        assertEquals("q=with%20space", url.getQuery());
        assertEquals("with space", url.getQueryParameter("q"));
        assertNull(url.getFragment());
    }

    @Test
    public void testValidateUrlWithNoPathAndEncodedQuery() throws Exception {
        Uri url = ServletUtil.validateUrl(Uri.parse("http://www.example.com?q=with%20space"));
        assertEquals("http", url.getScheme());
        assertEquals("www.example.com", url.getAuthority());
        assertEquals("/", url.getPath());
        assertEquals("q=with%20space", url.getQuery());
        assertNull(url.getFragment());
    }

    @Test(expected = GadgetException.class)
    public void testValidateUrlNullInput() throws Exception {
        ServletUtil.validateUrl(null);
    }

    @Test
    public void testOutputDataUri() throws Exception {
        checkOutputDataUri("text/foo", "text/foo", "UTF-8");
    }

    @Test
    public void testOutputDataUriWithCharset() throws Exception {
        checkOutputDataUri("text/bar; charset=ISO-8859-1", "text/bar", "ISO-8859-1");
    }

    @Test
    public void testOutputDataUriWithEmptyCharset() throws Exception {
        checkOutputDataUri("text/bar; charset=", "text/bar", "UTF-8");
    }

    private void checkOutputDataUri(String contentType, String expectedType, String expectedEncoding) throws Exception {
        String theData = "this is the data";
        String mk1 = "meta1", mv1 = "val1";
        String mk2 = "'\"}key", mv2 = "val{\"'";
        HttpResponse response = new HttpResponseBuilder().setResponseString(theData).addHeader("Content-Type", contentType).setMetadata(mk1, mv1)
                .setMetadata(mk2, mv2).setMetadata(ServletUtil.DATA_URI_KEY, "foo") // Should be ignored.
                .create();

        HttpResponse jsonified = ServletUtil.convertToJsonResponse(response);

        assertEquals("application/json; charset=UTF-8", jsonified.getHeader("Content-Type"));

        String emitted = jsonified.getResponseAsString();
        JSONObject js = new JSONObject(emitted);
        assertEquals(mv1, js.getString(mk1));
        assertEquals(mv2, js.getString(mk2));
        String content64 = getBase64(theData);
        assertEquals("data:" + expectedType + ";base64;charset=" + expectedEncoding + "," + content64, js.getString(ServletUtil.DATA_URI_KEY));
    }

    private String getBase64(String input) throws Exception {
        return new String(Base64.encodeBase64(input.getBytes("UTF8")), "UTF8");
    }

    @Test
    public void testFromRequest() throws Exception {
        Request request = new Request(Method.POST, "https://www.example.org:444/path/foo?one=two&three=four");
        request.getAttributes().put(ContextResource.HEADER_ATTRIBUTE_NAME, new Form());
        Response response = new Response(request);
        ContextResource contextResource = new ContextResource(request, response);

        contextResource.getRequestHeaders().add("Header1", "HVal1");
        contextResource.getRequestHeaders().add("Header1", "HVal3");
        contextResource.getRequestHeaders().add("Header2", "HVal2");
        contextResource.getRequestHeaders().add("Header2", "HVal4");
        contextResource.getRequest().setEntity(new StringRepresentation("post body"));
        contextResource.getRequest().getClientInfo().setAddress("1.2.3.4");

        HttpRequest req = ServletUtil.fromRequest(contextResource);

        assertEquals(Uri.parse("https://www.example.org:444/path/foo?one=two&three=four"), req.getUri());
        assertEquals(3, req.getHeaders().size());
        assertEquals("HVal1", req.getHeaders("Header1").get(0));
        assertEquals("HVal3", req.getHeaders("Header1").get(1));
        assertEquals("HVal2", req.getHeaders("Header2").get(0));
        assertEquals("HVal4", req.getHeaders("Header2").get(1));
        assertEquals(Method.POST.getName(), req.getMethod());
        assertEquals("post body", req.getPostBodyAsString());
        assertEquals("1.2.3.4", req.getParam(ServletUtil.REMOTE_ADDR_KEY));
    }

    @Test
    public void testCopyResponseToContext() throws Exception {
        HttpResponse response = new HttpResponseBuilder().setResponseString("response string").setHttpStatusCode(200).addHeader("h1", "v1")
                .addHeader("h2", "v2").setCacheTtl(1000).create();

        Request request = new Request(Method.GET, "https://www.example.org/");
        Response resp = new Response(request);
        ContextResource contextResource = new ContextResource(request, resp);

        ServletUtil.copyResponseToContext(response, contextResource);

        assertEquals(Status.SUCCESS_OK, contextResource.getStatus());
        assertEquals("response string", contextResource.getText());
        assertEquals("v1", contextResource.getResponseHeaders().getFirstValue("h1"));
        assertEquals("v2", contextResource.getResponseHeaders().getFirstValue("h2"));
    }

    @Test
    public void testCopyResponseToContextNoCache() throws Exception {
        HttpResponse response = new HttpResponseBuilder().setResponseString("response string").setHttpStatusCode(200).addHeader("h1", "v1")
                .addHeader("h2", "v2").setStrictNoCache().create();

        Request request = new Request(Method.GET, "https://www.example.org/");
        Response resp = new Response(request);
        ContextResource contextResource = new ContextResource(request, resp);

        FakeTimeSource fakeTime = new FakeTimeSource();
        HttpUtil.setTimeSource(fakeTime);
        ServletUtil.copyResponseToContext(response, contextResource);

        assertEquals(Status.SUCCESS_OK, contextResource.getStatus());
        assertEquals("response string", contextResource.getText());
        assertEquals("v1", contextResource.getResponseHeaders().getFirstValue("h1"));
        assertEquals("v2", contextResource.getResponseHeaders().getFirstValue("h2"));
        assertEquals("no-cache", contextResource.getResponseHeaders().getFirstValue("Pragma"));
        assertEquals("no-cache", contextResource.getResponseHeaders().getFirstValue("Cache-Control"));
    }

    String mergeParamters(String... args) {
        return StringUtils.join(args, ',');
    }
}
