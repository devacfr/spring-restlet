package org.cfr.restlet.ext.shindig.auth;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.OAuth.Parameter;

import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.oauth.OAuthEntry;
import org.apache.shindig.social.sample.oauth.SampleOAuthServlet;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pmi.restlet.Resource;

/**
 * This is a sample class that demonstrates how oauth tokens can be handed out and authorized.
 * This is most certainly not production code. Your server should have clear ui, require user
 * login for creating consumer secrets and authorizing request tokens, do better patch dispatching,
 * and use a non-in memory data store.
 * Base on {@link SampleOAuthServlet}
 */
@Resource(path = "/oauth", strict = false)
public class OAuthResource extends InjectedResource {

    public static final OAuthValidator VALIDATOR = new SimpleOAuthValidator();

    private OAuthDataStore dataStore;

    private String oauthAuthorizeAction;

    @Inject
    public void setDataStore(OAuthDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Inject
    void setAuthorizeAction(@Named("shindig.oauth.authorize-action") String authorizeAction) {
        this.oauthAuthorizeAction = authorizeAction;
    }

    @Post
    public Representation doPost() throws ServletException, IOException {
        return doGet();
    }

    @Get
    public Representation doGet() throws ServletException, IOException {
        ContextResource contextResource = getContextResource();
        ResourceUtil.setNoCache(contextResource);
        String path = contextResource.getReference().getPath();

        try {
            // dispatch
            if (path.endsWith("requestToken")) {
                createRequestToken(contextResource);
            } else if (path.endsWith("authorize")) {
                authorizeRequestToken(contextResource);
            } else if (path.endsWith("accessToken")) {
                createAccessToken(contextResource);
            } else {
                sendError(contextResource, Status.CLIENT_ERROR_NOT_FOUND, "unknown Url");
            }
        } catch (OAuthException e) {
            handleException(e, contextResource, true);
        } catch (URISyntaxException e) {
            handleException(e, contextResource, true);
        }
        return contextResource.consolidate();
    }

    // Hand out a request token if the consumer key and secret are valid
    private void createRequestToken(ContextResource contextResource) throws IOException, OAuthException, URISyntaxException {
        OAuthMessage requestMessage = getOAuthMessage(contextResource.getRequest());

        String consumerKey = requestMessage.getConsumerKey();
        if (consumerKey == null) {
            OAuthProblemException e = new OAuthProblemException(OAuth.Problems.PARAMETER_ABSENT);
            e.setParameter(OAuth.Problems.OAUTH_PARAMETERS_ABSENT, OAuth.OAUTH_CONSUMER_KEY);
            throw e;
        }
        OAuthConsumer consumer = dataStore.getConsumer(consumerKey);

        if (consumer == null) {
            throw new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_UNKNOWN);
        }

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        VALIDATOR.validateMessage(requestMessage, accessor);

        String callback = requestMessage.getParameter(OAuth.OAUTH_CALLBACK);

        if (callback == null) {
            // see if the consumer has a callback
            callback = consumer.callbackURL;
        }
        if (callback == null) {
            callback = "oob";
        }

        // generate request_token and secret
        OAuthEntry entry = dataStore.generateRequestToken(consumerKey, requestMessage.getParameter(OAuth.OAUTH_VERSION), callback);

        List<Parameter> responseParams = OAuth.newList(OAuth.OAUTH_TOKEN, entry.getToken(), OAuth.OAUTH_TOKEN_SECRET, entry.getTokenSecret());
        if (callback != null) {
            responseParams.add(new Parameter(OAuth.OAUTH_CALLBACK_CONFIRMED, "true"));
        }
        sendResponse(contextResource, responseParams);
    }

    /////////////////////
    // deal with authorization request
    private void authorizeRequestToken(ContextResource contextResource) throws ServletException, IOException, OAuthException, URISyntaxException {

        OAuthMessage requestMessage = getOAuthMessage(contextResource.getRequest());

        if (requestMessage.getToken() == null) {
            // MALFORMED REQUEST
            sendError(contextResource, Status.CLIENT_ERROR_BAD_REQUEST, "Authentication token not found");
            return;
        }
        OAuthEntry entry = dataStore.getEntry(requestMessage.getToken());

        if (entry == null) {
            sendError(contextResource, Status.CLIENT_ERROR_NOT_FOUND, "OAuth Entry not found");
            return;
        }

        OAuthConsumer consumer = dataStore.getConsumer(entry.getConsumerKey());

        // Extremely rare case where consumer dissappears
        if (consumer == null) {
            sendError(contextResource, Status.CLIENT_ERROR_NOT_FOUND, "consumer for entry not found");
            return;
        }

        // The token is disabled if you try to convert to an access token prior to authorization
        if (entry.getType() == OAuthEntry.Type.DISABLED) {
            sendError(contextResource, Status.CLIENT_ERROR_FORBIDDEN, "This token is disabled, please reinitate login");
            return;
        }

        String callback = entry.getCallbackUrl();

        // Redirect to a UI flow if the token is not authorized
        if (!entry.isAuthorized()) {
            // TBD -- need to decode encrypted payload somehow..
            if (this.oauthAuthorizeAction.startsWith("http")) {
                // Redirect to authorization page with params
                // Supply standard set of params
                // TBD
            } else {
                // Use internal forward to a jsp page
                getRequest().getAttributes().put("OAUTH_DATASTORE", dataStore);

                getRequest().getAttributes().put("OAUTH_ENTRY", entry);
                getRequest().getAttributes().put("CALLBACK", callback);

                getRequest().getAttributes().put("TOKEN", entry.getToken());
                getRequest().getAttributes().put("CONSUMER", consumer);

                this.getContext().getClientDispatcher().handle(getRequest(), getResponse());
            }
            return;
        }

        // If we're here then the entry has been authorized

        // redirect to callback
        if (callback == null || "oob".equals(callback)) {
            // consumer did not specify a callback
            contextResource.setMediaType(MediaType.TEXT_PLAIN);
            contextResource.append("Token successfully authorized.\n");
            if (entry.getCallbackToken() != null) {
                // Usability fail.
                contextResource.append("Please enter code " + entry.getCallbackToken() + " at the consumer.");
            }
        } else {
            callback = OAuth.addParameters(callback, OAuth.OAUTH_TOKEN, entry.getToken());
            // Add user_id to the callback
            callback = OAuth.addParameters(callback, "user_id", entry.getUserId());
            if (entry.getCallbackToken() != null) {
                if (entry.getCallbackToken() != null) {
                    callback = OAuth.addParameters(callback, OAuth.OAUTH_VERIFIER, entry.getCallbackToken());
                }
            }

            contextResource.setStatus(Status.REDIRECTION_TEMPORARY);
            contextResource.getResponseHeaders().set("Location", callback);
        }
    }

    // Hand out an access token if the consumer key and secret are valid and the user authorized
    // the requestToken
    private void createAccessToken(ContextResource contextResource) throws ServletException, IOException, OAuthException, URISyntaxException {
        OAuthMessage requestMessage = getOAuthMessage(getRequest());

        OAuthEntry entry = getValidatedEntry(requestMessage);
        if (entry == null) {
            throw new OAuthProblemException(OAuth.Problems.TOKEN_REJECTED);
        }

        if (entry.getCallbackToken() != null) {
            // We're using the fixed protocol
            String clientCallbackToken = requestMessage.getParameter(OAuth.OAUTH_VERIFIER);
            if (!entry.getCallbackToken().equals(clientCallbackToken)) {
                dataStore.disableToken(entry);
                sendError(contextResource, Status.CLIENT_ERROR_FORBIDDEN, "This token is not authorized");
                return;
            }
        } else if (!entry.isAuthorized()) {
            // Old protocol.  Catch consumers trying to convert a token to one that's not authorized
            dataStore.disableToken(entry);
            sendError(contextResource, Status.CLIENT_ERROR_FORBIDDEN, "This token is not authorized");
            return;
        }

        // turn request token into access token
        OAuthEntry accessEntry = dataStore.convertToAccessToken(entry);

        sendResponse(contextResource, OAuth.newList(OAuth.OAUTH_TOKEN, accessEntry.getToken(), OAuth.OAUTH_TOKEN_SECRET,
                accessEntry.getTokenSecret(), "user_id", entry.getUserId()));
    }

    private OAuthEntry getValidatedEntry(OAuthMessage requestMessage) throws IOException, ServletException, OAuthException, URISyntaxException {

        OAuthEntry entry = dataStore.getEntry(requestMessage.getToken());
        if (entry == null) {
            throw new OAuthProblemException(OAuth.Problems.TOKEN_REJECTED);
        }

        if (entry.getType() != OAuthEntry.Type.REQUEST) {
            throw new OAuthProblemException(OAuth.Problems.TOKEN_USED);
        }

        if (entry.isExpired()) {
            throw new OAuthProblemException(OAuth.Problems.TOKEN_EXPIRED);
        }

        // find consumer key, compare with supplied value, if present.

        if (requestMessage.getConsumerKey() == null) {
            OAuthProblemException e = new OAuthProblemException(OAuth.Problems.PARAMETER_ABSENT);
            e.setParameter(OAuth.Problems.OAUTH_PARAMETERS_ABSENT, OAuth.OAUTH_CONSUMER_KEY);
            throw e;
        }

        String consumerKey = entry.getConsumerKey();
        if (!consumerKey.equals(requestMessage.getConsumerKey())) {
            throw new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_REFUSED);
        }

        OAuthConsumer consumer = dataStore.getConsumer(consumerKey);

        if (consumer == null) {
            throw new OAuthProblemException(OAuth.Problems.CONSUMER_KEY_UNKNOWN);
        }

        OAuthAccessor accessor = new OAuthAccessor(consumer);

        accessor.requestToken = entry.getToken();
        accessor.tokenSecret = entry.getTokenSecret();

        VALIDATOR.validateMessage(requestMessage, accessor);

        return entry;
    }

    private void sendResponse(ContextResource contextResource, List<OAuth.Parameter> parameters) throws IOException {
        contextResource.setMediaType(MediaType.TEXT_PLAIN);
        contextResource.append(OAuth.formEncode(parameters));
    }

    private static void handleException(Exception e, ContextResource contextResource, boolean sendBody) throws IOException, ServletException {
        String realm = contextResource.getReference().getHostIdentifier();
        handleException(contextResource, e, realm, sendBody);
    }

    private static void sendError(ContextResource contextResource, Status status, String message) {
        contextResource.setText(message);
        contextResource.setMediaType(MediaType.TEXT_HTML);
        contextResource.setStatus(status);
    }

    /**
     * Extract the parts of the given request that are relevant to OAuth.
     * Parameters include OAuth Authorization headers and the usual request
     * parameters in the query string and/or form encoded body. The header
     * parameters come first, followed by the rest in the order they came from
     * request.getParameterMap().
     * 
     * @param request
     * @return message
     */
    public static OAuthMessage getOAuthMessage(Request request) {
        final String URL = request.getResourceRef().toString();
        return new OAuthMessage(request.getMethod().getName(), URL, getOAuthParameters(request));
    }

    /**
     * Translate request parameters into OAuth.Parameter objects.
     * 
     * @param request
     * @return parameters
     */
    public static List<OAuth.Parameter> getOAuthParameters(Request request) {
        final Set<OAuth.Parameter> parameters = new HashSet<OAuth.Parameter>();

        // Authorization headers.
        final Form headers = (Form) request.getAttributes().get("org.restlet.http.headers");
        for (final OAuth.Parameter parameter : OAuthMessage.decodeAuthorization(headers.getFirstValue("Authorization", true))) {
            if (!parameter.getKey().equalsIgnoreCase("realm")) {
                parameters.add(parameter);
            }
        }

        // Query parameters.
        for (final org.restlet.data.Parameter p : request.getResourceRef().getQueryAsForm()) {
            parameters.add(new OAuth.Parameter(p.getName(), p.getValue()));
        }

        // POST with x-www-urlencoded data
        if ((request.getMethod() == Method.POST) && (request.getEntity().getMediaType() == MediaType.APPLICATION_WWW_FORM)) {
            for (final org.restlet.data.Parameter p : request.getEntityAsForm()) {
                parameters.add(new OAuth.Parameter(p.getName(), p.getValue()));
            }
        }

        Context.getCurrentLogger().fine("Got OAuth parameters " + parameters);

        return new ArrayList<OAuth.Parameter>(parameters);
    }

    public static void handleException(ContextResource contextResource, Exception e, String realm, boolean sendBody) throws IOException,
            ServletException {
        if (e instanceof OAuthProblemException) {
            OAuthProblemException problem = (OAuthProblemException) e;
            Object httpCode = problem.getParameters().get("HTTP status");
            if (httpCode == null) {
                httpCode = PROBLEM_TO_HTTP_CODE.get(problem.getProblem());
            }
            if (httpCode == null) {
                httpCode = Status.CLIENT_ERROR_FORBIDDEN.getCode();
            }
            contextResource.setText("");
            contextResource.setStatus(Status.valueOf(Integer.valueOf(httpCode.toString())));
            OAuthMessage message = new OAuthMessage(null, null, problem.getParameters().entrySet());
            contextResource.getResponseHeaders().set(HeaderConstants.HEADER_WWW_AUTHENTICATE, message.getAuthorizationHeader(realm));
            if (sendBody) {
                sendForm(contextResource, message.getParameters());
            }
        } else {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (e instanceof ServletException) {
                throw (ServletException) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new ServletException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void sendForm(ContextResource contextResource, Iterable parameters) throws IOException {
        contextResource.setCharacterSet(CharacterSet.UTF_8);
        contextResource.setMediaType(MediaType.APPLICATION_WWW_FORM);
        contextResource.setText(OAuth.formEncode(parameters));
    }

    private static final Map<String, Integer> PROBLEM_TO_HTTP_CODE;

    static {
        PROBLEM_TO_HTTP_CODE = net.oauth.OAuth.Problems.TO_HTTP_CODE;
    }
}