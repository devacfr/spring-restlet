package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.uri.UriBuilder;
import org.apache.shindig.gadgets.oauth.OAuthCallbackState;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.servlet.OAuthCallbackServlet;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pmi.restlet.Resource;

/**
 * Servlet to act as our OAuth callback URL.  When gadget authors register a consumer key with an
 * OAuth service provider, they can provide a URL pointing to this servlet as their callback URL.
 * 
 * Protocol flow:
 * - gadget discovers it needs approval to access data at OAuth SP.
 * - gadget opens popup window to approval URL, passing URL to this servlet as the oauth_callback
 *   parameter on the approval URL.
 * - user grants approval at service provider
 * - service provider redirects to this servlet
 * - this servlet closes the window
 * - gadget discovers the window has closed and automatically fetches the user's data.
 * Based on {@link OAuthCallbackServlet}
 */
@Resource(path = "/oauth/oauthcallback", strict = true)
public class OAuthCallbackResource extends InjectedResource {

    private static final long serialVersionUID = 7126255229334669172L;

    public static final String CALLBACK_STATE_PARAM = "cs";

    public static final String REAL_DOMAIN_PARAM = "d";

    private static final int ONE_HOUR_IN_SECONDS = 3600;

    // This bit of magic passes the entire callback URL into the opening gadget for later use.
    // gadgets.io.makeRequest (or osapi.oauth) will then pick up the callback URL to complete the
    // oauth dance.
    private static final String RESP_BODY = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" "
            + "\"http://www.w3.org/TR/html4/loose.dtd\">\n" + "<html>\n" + "<head>\n" + "<title>Close this window</title>\n" + "</head>\n"
            + "<body>\n" + "<script type='text/javascript'>\n" + "try {\n"
            + "  window.opener.gadgets.io.oauthReceivedCallbackUrl_ = document.location.href;\n" + "} catch (e) {\n" + "}\n" + "window.close();\n"
            + "</script>\n" + "Close this window.\n" + "</body>\n" + "</html>\n";

    private transient BlobCrypter stateCrypter;

    @Inject
    public void setStateCrypter(@Named(OAuthFetcherConfig.OAUTH_STATE_CRYPTER) BlobCrypter stateCrypter) {
        this.stateCrypter = stateCrypter;
    }

    @Get("html")
    public Representation doGet() throws IOException {
        ContextResource contextResource = getContextResource();
        Request request = contextResource.getRequest();
        OAuthCallbackState callbackState = new OAuthCallbackState(stateCrypter, contextResource.getParameter(CALLBACK_STATE_PARAM));
        if (callbackState.getRealCallbackUrl() != null) {
            // Copy the query parameters from this URL over to the real URL.
            UriBuilder realUri = UriBuilder.parse(callbackState.getRealCallbackUrl());
            Map<String, List<String>> params = UriBuilder.splitParameters(request.getResourceRef().getQuery());
            for (Map.Entry<String, List<String>> entry : params.entrySet()) {
                realUri.putQueryParameter(entry.getKey(), entry.getValue());
            }
            realUri.removeQueryParameter(CALLBACK_STATE_PARAM);
            ResourceUtil.setCachingHeaders(contextResource, ONE_HOUR_IN_SECONDS, true);
            Response response = contextResource.getResponse();
            response.setLocationRef(new Reference(realUri.toString()));
            response.setStatus(Status.REDIRECTION_FOUND);
            return contextResource.consolidate();
        }
        ResourceUtil.setCachingHeaders(contextResource, ONE_HOUR_IN_SECONDS, true);
        contextResource.setCharacterSet(CharacterSet.UTF_8);
        contextResource.setMediaType(MediaType.TEXT_HTML);
        contextResource.append(RESP_BODY);
        return contextResource.consolidate();
    }
}