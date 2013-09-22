package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.config.AbstractContainerConfig;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.HttpResponseBuilder;
import org.apache.shindig.gadgets.rewrite.CaptureRewriter;
import org.apache.shindig.gadgets.rewrite.DefaultResponseRewriterRegistry;
import org.apache.shindig.gadgets.rewrite.ResponseRewriter;
import org.apache.shindig.gadgets.uri.AccelUriManager;
import org.apache.shindig.gadgets.uri.DefaultAccelUriManager;
import org.apache.shindig.gadgets.uri.DefaultProxyUriManager;
import org.cfr.restlet.ext.shindig.resource.AccelHandlerOverride;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.HtmlAccelResource;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;

import com.google.common.collect.ImmutableMap;

public class HtmlAccelResourceTest extends ResourceTestFixture {

    private static class FakeContainerConfig extends AbstractContainerConfig {

        protected final Map<String, Object> data = ImmutableMap.<String, Object> builder().put(AccelUriManager.PROXY_HOST_PARAM, "apache.org").put(
                AccelUriManager.PROXY_PATH_PARAM, "/gadgets/accel").build();

        @Override
        public Object getProperty(String container, String name) {
            return data.get(name);
        }
    }

    private class FakeCaptureRewriter extends CaptureRewriter {

        String contentToRewrite;

        public void setContentToRewrite(String s) {
            contentToRewrite = s;
        }

        @Override
        public void rewrite(HttpRequest request, HttpResponseBuilder original) {
            super.rewrite(request, original);
            if (!StringUtils.isEmpty(contentToRewrite)) {
                original.setResponse(contentToRewrite.getBytes());
            }
        }
    }

    private static final String REWRITE_CONTENT = "working rewrite";

    private static final String RESOURCE = "/gadgets/accel";

    private HtmlAccelResource resource;

    @Override
    @Before
    public void setUp() throws Exception {
        resource = new HtmlAccelResource();

        ContainerConfig config = new FakeContainerConfig();
        AccelUriManager accelUriManager = new DefaultAccelUriManager(config, new DefaultProxyUriManager(config, null));
        rewriter = new FakeCaptureRewriter();
        rewriterRegistry = new DefaultResponseRewriterRegistry(Arrays.<ResponseRewriter> asList(rewriter), null);
        resource.setHandler(new AccelHandlerOverride(pipeline, rewriterRegistry, accelUriManager, true));
    }

    private void expectRequest(String extraPath, String url) {
        Request request = new Request(Method.GET, "http://apache.org" + RESOURCE + extraPath);
        request.getAttributes().put(ContextResource.HEADER_ATTRIBUTE_NAME, new Form());
        request.setRootRef(new Reference("http://apache.org" + RESOURCE));
        Response response = new Response(request);
        resource.init(Context.getCurrent(), request, response);

        ContextResource contextResource = resource.getContextResource();

        contextResource.addQueryParameter("url", url);
        contextResource.addQueryParameter("container", "accel");
        contextResource.addQueryParameter("gadget", "test");

    }

    private void expectPostRequest(String extraPath, String url, final String data) throws IOException {

        Request request = new Request(Method.POST, "http://apache.org" + RESOURCE + extraPath);
        request.setEntity(new StringRepresentation(data));
        request.getAttributes().put(ContextResource.HEADER_ATTRIBUTE_NAME, new Form());
        request.setRootRef(new Reference("http://apache.org" + RESOURCE));
        Response response = new Response(request);
        resource.init(Context.getCurrent(), request, response);

        ContextResource contextResource = resource.getContextResource();

        contextResource.addQueryParameter("url", url);
        contextResource.addQueryParameter("container", "accel");
        contextResource.addQueryParameter("gadget", "test");

    }

    @Test
    public void testHtmlAccelNoData() throws Exception {
        String url = "http://example.org/data.html";

        HttpRequest req = new HttpRequest(Uri.parse(url));
        expect(pipeline.execute(req)).andReturn(null).once();
        expectRequest("", url);
        replay();

        resource.doGet();
        verify();
        ContextResource contextResource = resource.getContextResource();
        assertEquals(AccelHandlerOverride.ERROR_FETCHING_DATA, contextResource.getText());
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, contextResource.getStatus());
    }

    @Test
    public void testHtmlAccelRewriteSimple() throws Exception {

        String url = "http://example.org/data.html";
        String data = "<html><body>Hello World</body></html>";

        ((FakeCaptureRewriter) rewriter).setContentToRewrite(REWRITE_CONTENT);
        HttpRequest req = new HttpRequest(Uri.parse(url));
        HttpResponse resp = new HttpResponseBuilder().setResponse(data.getBytes()).setHeader("Content-Type", "text/html").setHttpStatusCode(200)
        .create();
        expect(pipeline.execute(req)).andReturn(resp).once();
        expectRequest("", url);
        replay();

        resource.doGet();
        verify();
        ContextResource contextResource = resource.getContextResource();
        assertEquals(REWRITE_CONTENT, contextResource.getText());
        assertEquals(Status.SUCCESS_OK, contextResource.getStatus());
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testHtmlAccelRewriteDoesNotFollowRedirects() throws Exception {
        String url = "http://example.org/data.html";
        String data = "<html><body>Moved to new page</body></html>";
        String redirectLocation = "http://example-redirected.org/data.html";

        ((FakeCaptureRewriter) rewriter).setContentToRewrite(data);
        HttpRequest req = new HttpRequest(Uri.parse(url));
        HttpResponse resp = new HttpResponseBuilder().setResponse(data.getBytes()).setHeader("Content-Type", "text/html").setHeader("Location",
                redirectLocation).setHttpStatusCode(302).create();
        expect(pipeline.execute(req)).andReturn(resp).once();
        expectRequest("", url);
        replay();

        resource.doGet();
        verify();
        ContextResource contextResource = resource.getContextResource();

        assertEquals(data, contextResource.getText());
        assertEquals(redirectLocation, contextResource.getResponseHeaders().getFirstValue("Location"));
        assertEquals(Status.REDIRECTION_FOUND, contextResource.getStatus());
        assertTrue(rewriter.responseWasRewritten());
    }

    @Test
    public void testHtmlAccelReturnsOriginal404MessageAndCode() throws Exception {
        String url = "http://example.org/data.html";
        String data = "<html><body>This is error page</body></html>";

        ((FakeCaptureRewriter) rewriter).setContentToRewrite(REWRITE_CONTENT);
        HttpRequest req = new HttpRequest(Uri.parse(url));
        HttpResponse resp = new HttpResponseBuilder().setResponse(data.getBytes()).setHeader("Content-Type", "text/html").setHttpStatusCode(404)
        .create();
        expect(pipeline.execute(req)).andReturn(resp).once();
        expectRequest("", url);
        replay();

        resource.doGet();
        verify();
        ContextResource contextResource = resource.getContextResource();
        assertEquals(data, contextResource.getText());
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, contextResource.getStatus());
        assertFalse(rewriter.responseWasRewritten());
    }

    @Test
    public void testHtmlAccelRewriteInternalError() throws Exception {
        String url = "http://example.org/data.html";
        String data = "<html><body>This is error page</body></html>";

        ((FakeCaptureRewriter) rewriter).setContentToRewrite(data);
        HttpRequest req = new HttpRequest(Uri.parse(url));
        HttpResponse resp = new HttpResponseBuilder().setResponse(data.getBytes()).setHeader("Content-Type", "text/html").setHttpStatusCode(500)
        .create();
        expect(pipeline.execute(req)).andReturn(resp).once();
        expectRequest("", url);
        replay();

        resource.doGet();
        verify();
        ContextResource contextResource = resource.getContextResource();
        assertEquals(data, contextResource.getText());
        assertEquals(Status.SERVER_ERROR_BAD_GATEWAY, contextResource.getStatus());
        assertFalse(rewriter.responseWasRewritten());
    }

    @Test
    public void testHtmlAccelHandlesPost() throws Exception {
        String url = "http://example.org/data.html";
        String data = "<html><body>This is error page</body></html>";

        ((FakeCaptureRewriter) rewriter).setContentToRewrite(data);
        Capture<HttpRequest> req = new Capture<HttpRequest>();
        HttpResponse resp = new HttpResponseBuilder().setResponse(data.getBytes()).setHeader("Content-Type", "text/html").create();
        expect(pipeline.execute(capture(req))).andReturn(resp).once();
        expectPostRequest("", url, "hello=world");
        replay();

        resource.doGet();
        verify();
        ContextResource contextResource = resource.getContextResource();
        assertEquals(data, contextResource.getText());
        assertEquals(Status.SUCCESS_OK, contextResource.getStatus());
        assertTrue(rewriter.responseWasRewritten());
        assertEquals("POST", req.getValue().getMethod());
        assertEquals("hello=world", req.getValue().getPostBodyAsString());
    }

    @Test
    public void testHtmlAccelReturnsSameStatusCodeAndMessageWhenError() throws Exception {
        String url = "http://example.org/data.html";
        String data = "<html><body>This is error page</body></html>";

        ((FakeCaptureRewriter) rewriter).setContentToRewrite(data);
        HttpRequest req = new HttpRequest(Uri.parse(url));
        HttpResponse resp = new HttpResponseBuilder().setResponse(data.getBytes()).setHeader("Content-Type", "text/html").setHttpStatusCode(5001)
        .create();
        expect(pipeline.execute(req)).andReturn(resp).once();
        expectRequest("", url);
        replay();

        resource.doGet();
        verify();
        ContextResource contextResource = resource.getContextResource();
        assertEquals(data, contextResource.getText());
        assertEquals(Status.valueOf(5001), contextResource.getStatus());
        assertFalse(rewriter.responseWasRewritten());
    }
}