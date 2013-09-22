package org.cfr.restlet.ext.shindig.resource;

import java.util.List;

import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.gadgets.oauth.OAuthCallbackState;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.resource.OAuthCallbackResource;
import org.junit.Assert;
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
import org.restlet.data.Reference;
import org.restlet.data.Status;


/**
 * Tests for OAuth callback servlet.
 */
public class OAuthCallbackResourceTest extends ResourceTestFixture {

    private OAuthCallbackResource resource;

    @Override
    @Before
    public void setUp() throws Exception {
        resource = new OAuthCallbackResource();

        Request request = new Request(Method.GET, "http://foo.com");
        request.getAttributes().put("org.restlet.http.headers", new Form());
        Response response = new Response(request);
        resource.init(Context.getCurrent(), request, response);
    }

    @Test
    public void testServlet() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        replay();
        resource.doGet();
        verify();

        assertEquals(MediaType.TEXT_HTML, contextResource.getMediaType());
        assertEquals(CharacterSet.UTF_8, contextResource.getCharacterSet());
        String body = contextResource.getText();
        Assert.assertNotSame("body is " + body, body.indexOf("window.close()"), -1);
    }

    @Test
    public void testServletWithCallback() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        BlobCrypter crypter = new BasicBlobCrypter("00000000000000000000".getBytes());
        OAuthCallbackState state = new OAuthCallbackState(crypter);
        resource.setStateCrypter(crypter);
        state.setRealCallbackUrl("http://www.example.com/callback");
        contextResource.getParameters().set("cs", state.getEncryptedState());
        contextResource.getRequest().setResourceRef("http://foo.com?cs=foo&bar=baz");
        replay();
        resource.doGet();
        verify();

        assertEquals(Status.REDIRECTION_FOUND, contextResource.getStatus());
        assertEquals(new Reference("http://www.example.com/callback?bar=baz"), contextResource.getResponse().getLocationRef());

        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(2, cacheDirectives.size());
        assertEquals("private", cacheDirectives.get(0).getName());
        assertEquals("max-age", cacheDirectives.get(1).getName());
        assertEquals("3600", cacheDirectives.get(1).getValue());

    }

    @Test
    public void testServletWithCallback_noQueryParams() throws Exception {
        ContextResource contextResource = resource.getContextResource();
        BlobCrypter crypter = new BasicBlobCrypter("00000000000000000000".getBytes());
        OAuthCallbackState state = new OAuthCallbackState(crypter);
        resource.setStateCrypter(crypter);
        state.setRealCallbackUrl("http://www.example.com/callback");
        contextResource.getParameters().set("cs", state.getEncryptedState());
        contextResource.getRequest().setResourceRef("http://foo.com?cs=foo");
        replay();
        resource.doGet();
        verify();

        assertEquals(Status.REDIRECTION_FOUND, contextResource.getStatus());
        assertEquals(new Reference("http://www.example.com/callback"), contextResource.getResponse().getLocationRef());

        List<CacheDirective> cacheDirectives = contextResource.getResponse().getCacheDirectives();
        assertEquals(2, cacheDirectives.size());
        assertEquals("private", cacheDirectives.get(0).getName());
        assertEquals("max-age", cacheDirectives.get(1).getName());
        assertEquals("3600", cacheDirectives.get(1).getValue());
    }
}