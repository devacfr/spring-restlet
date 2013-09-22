package org.cfr.restlet.ext.shindig.resource;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.render.Renderer;
import org.apache.shindig.gadgets.render.RenderingResults;
import org.apache.shindig.gadgets.uri.IframeUriManager;
import org.apache.shindig.gadgets.uri.UriStatus;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.GadgetRenderingResource;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;


public class GadgetRenderingResourceTest {

    private static final String NON_ASCII_STRING = "Games, HQ, Mang\u00E1, Anime e tudo que um bom nerd ama";

    private final IMocksControl control = EasyMock.createNiceControl();

    private final Renderer renderer = control.createMock(Renderer.class);

    private final GadgetRenderingResource resource = new GadgetRenderingResource();

    private final IframeUriManager iframeUriManager = control.createMock(IframeUriManager.class);

    @Before
    public void setUpUrlGenerator() {
        Request request = new Request(Method.GET, "http://foo.com?q=a");
        request.getAttributes().put("org.restlet.http.headers", new Form());
        Response response = new Response(request);
        resource.init(new Context(), request, response);

        expect(iframeUriManager.validateRenderingUri(isA(Uri.class))).andReturn(UriStatus.VALID_UNVERSIONED);
        resource.setIframeUriManager(iframeUriManager);
    }

    @Test
    public void dosHeaderRejected() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        contextResource.getRequestHeaders().set(HttpRequest.DOS_PREVENTION_HEADER, "foo");
        control.replay();
        resource.doGet();

        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, contextResource.getResponse().getStatus());
    }

    @Test
    public void renderWithTtl() throws Exception {
        resource.setRenderer(renderer);
        expect(renderer.render(isA(GadgetContext.class))).andReturn(RenderingResults.ok("working"));
        ContextResource contextResource = resource.getContextResource();
        contextResource.getParameters().set(Param.REFRESH.getKey(), "120");
        control.replay();
        resource.doGet();
        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(2, cacheDirectives.size());
        assertEquals("private", cacheDirectives.get(0).getName());
        assertEquals("max-age", cacheDirectives.get(1).getName());
        assertEquals("120", cacheDirectives.get(1).getValue());
    }

    @Test
    public void renderWithBadTtl() throws Exception {
        resource.setRenderer(renderer);
        expect(renderer.render(isA(GadgetContext.class))).andReturn(RenderingResults.ok("working"));

        ContextResource contextResource = resource.getContextResource();
        contextResource.getParameters().set(Param.REFRESH.getKey(), "");
        control.replay();
        resource.doGet();
        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(2, cacheDirectives.size());
        assertEquals("private", cacheDirectives.get(0).getName());
        assertEquals("max-age", cacheDirectives.get(1).getName());
        assertEquals("300", cacheDirectives.get(1).getValue());
    }

    @Test
    public void normalResponse() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        resource.setRenderer(renderer);
        expect(renderer.render(isA(GadgetContext.class))).andReturn(RenderingResults.ok("working"));
        control.replay();

        resource.doGet();

        assertEquals(Status.SUCCESS_OK, contextResource.getStatus());

        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(2, cacheDirectives.size());
        assertEquals("private", cacheDirectives.get(0).getName());
        assertEquals("max-age", cacheDirectives.get(1).getName());
        assertEquals(String.valueOf(GadgetRenderingResource.DEFAULT_CACHE_TTL), cacheDirectives.get(1).getValue());

        assertEquals("working", contextResource.getText());
    }

    @Test
    public void errorsPassedThrough() throws Exception {
        ContextResource contextResource = resource.getContextResource();

        resource.setRenderer(renderer);
        expect(renderer.render(isA(GadgetContext.class))).andReturn(RenderingResults.error("busted", HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
        control.replay();

        resource.doGet();

        assertEquals(Status.SERVER_ERROR_INTERNAL, contextResource.getStatus());

        //        assertNull("Cache-Control header passed where it should not be.",
        //                recorder.getHeader("Cache-Control"));
        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(0, cacheDirectives.size());

        assertEquals("busted", contextResource.getText());

    }

    @Test
    public void errorsAreEscaped() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        resource.setRenderer(renderer);
        expect(renderer.render(isA(GadgetContext.class))).andReturn(
                RenderingResults.error("busted<script>alert(document.domain)</script>", HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
        control.replay();

        resource.doGet();

        assertEquals("busted&lt;script&gt;alert(document.domain)&lt;/script&gt;", contextResource.getText());

        assertEquals(Status.SERVER_ERROR_INTERNAL, contextResource.getStatus());
    }

    @Test
    public void outputEncodingIsUtf8() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        resource.setRenderer(renderer);
        expect(renderer.render(isA(GadgetContext.class))).andReturn(RenderingResults.ok(NON_ASCII_STRING));
        control.replay();

        resource.doGet();

        assertEquals(CharacterSet.UTF_8, contextResource.getCharacterSet());
        assertEquals(MediaType.TEXT_HTML, contextResource.getMediaType());
        assertEquals(NON_ASCII_STRING, contextResource.getText());
    }

    @Test
    public void refreshParameter_specified() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        resource.setRenderer(renderer);
        contextResource.getParameters().set("refresh", "1000");
        expect(renderer.render(isA(GadgetContext.class))).andReturn(RenderingResults.ok("working"));
        control.replay();
        resource.doGet();

        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(2, cacheDirectives.size());
        assertEquals("private", cacheDirectives.get(0).getName());
        assertEquals("max-age", cacheDirectives.get(1).getName());
        assertEquals("1000", cacheDirectives.get(1).getValue());

    }

    @Test
    public void refreshParameter_default() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        resource.setRenderer(renderer);
        expect(renderer.render(isA(GadgetContext.class))).andReturn(RenderingResults.ok("working"));
        control.replay();
        resource.doGet();

        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(2, cacheDirectives.size());
        assertEquals("private", cacheDirectives.get(0).getName());
        assertEquals("max-age", cacheDirectives.get(1).getName());
        assertEquals("300", cacheDirectives.get(1).getValue());
    }
}