package org.cfr.restlet.ext.shindig.protocol;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ContentTypes;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.DataServiceServlet;
import org.apache.shindig.protocol.ResponseItem;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.pmi.restlet.Resource;

/**
 * Based on {@link DataServiceServlet}
 * @author cfriedri
 *
 */
@Resource(path = "/api/rest", strict = false)
public class DataServiceResource extends ApiResource {

    private static final Logger logger = Logger.getLogger(DataServiceResource.class.getName());

    public static final Set<String> ALLOWED_CONTENT_TYPES = new ImmutableSet.Builder<String>().addAll(ContentTypes.ALLOWED_JSON_CONTENT_TYPES)
            .addAll(ContentTypes.ALLOWED_XML_CONTENT_TYPES)
            .addAll(ContentTypes.ALLOWED_ATOM_CONTENT_TYPES)
            .build();

    protected static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

    @Get
    public Representation doGet() throws IOException {
        return executeRequest();
    }

    @Put
    public Representation doPut() throws IOException {
        try {
            ContentTypes.checkContentTypes(ALLOWED_CONTENT_TYPES, getRequest().getEntity().getMediaType().getName());
            return executeRequest();
        } catch (ContentTypes.InvalidContentTypeException icte) {
            sendError(getContextResource(), new ResponseItem(HttpServletResponse.SC_BAD_REQUEST, icte.getMessage()));
        }
        return null;
    }

    @Delete
    public Representation doDelete() throws IOException {
        return executeRequest();
    }

    @Post
    public Representation doPost() throws IOException {
        try {
            ContentTypes.checkContentTypes(ALLOWED_CONTENT_TYPES, getRequest().getEntity().getMediaType().getName());
            return executeRequest();
        } catch (ContentTypes.InvalidContentTypeException icte) {
            sendError(getContextResource(), new ResponseItem(HttpServletResponse.SC_BAD_REQUEST, icte.getMessage()));
        }
        return null;
    }

    /**
     * Actual dispatch handling for servlet requests
     */
    Representation executeRequest() throws IOException {
        ContextResource contextResource = getContextResource();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Handling restful request for " + contextResource.getReference().getPath());
        }

        setCharacterEncodings(contextResource);

        SecurityToken token = getSecurityToken(contextResource);
        if (token == null) {
            sendSecurityError(contextResource);
            return contextResource.consolidate();
        }

        ResourceUtil.setCORSheader(contextResource, containerConfig.<String> getList(token.getContainer(), "gadgets.parentOrigins"));

        BeanConverter converter = getConverterForRequest(contextResource);

        handleSingleRequest(contextResource, token, converter);
        return contextResource.consolidate();
    }

    @Override
    protected void sendError(ContextResource contextResource, ResponseItem responseItem) throws IOException {
        int errorCode = responseItem.getErrorCode();
        if (errorCode < 0) {
            // Map JSON-RPC error codes into HTTP error codes as best we can
            // TODO: Augment the error message (if missing) with a default
            switch (errorCode) {
                case -32700:
                case -32602:
                case -32600:
                    // Parse error, invalid params, and invalid request
                    errorCode = HttpServletResponse.SC_BAD_REQUEST;
                    break;
                case -32601:
                    // Procedure doesn't exist
                    errorCode = HttpServletResponse.SC_NOT_IMPLEMENTED;
                    break;
                case -32603:
                default:
                    // Internal server error, or any application-defined error
                    errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    break;
            }
        }
        contextResource.setStatus(Status.valueOf(responseItem.getErrorCode()), responseItem.getErrorMessage());
    }

    /**
     * Handler for non-batch requests.
     */
    private void handleSingleRequest(ContextResource contextResource, SecurityToken token, BeanConverter converter) throws IOException {

        // Always returns a non-null handler.
        RestHandler handler = getRestHandler(contextResource);
        Request request = contextResource.getRequest();
        Reader bodyReader = null;
        if (!request.getMethod().equals(Method.GET) && !request.getMethod().equals(Method.HEAD)) {
            bodyReader = request.getEntity().getReader();
        }

        // Execute the request
        Map<String, String[]> parameterMap = contextResource.getParametersMap();
        Future<?> future = handler.execute(parameterMap, bodyReader, token, converter);

        ResponseItem responseItem = getResponseItem(future);

        contextResource.setMediaType(MediaType.valueOf(converter.getContentType()));
        if (responseItem.getErrorCode() >= 200 && responseItem.getErrorCode() < 400) {
            Object response = responseItem.getResponse();
            // TODO: ugliness resulting from not using RestfulItem
            if (!(response instanceof DataCollection) && !(response instanceof RestfulCollection)) {
                response = ImmutableMap.of("entry", response);
            }

            // JSONP style callbacks
            String callback = (ResourceUtil.isJSONP(contextResource) && ContentTypes.OUTPUT_JSON_CONTENT_TYPE.equals(converter.getContentType())) ? contextResource.getParameter("callback")
                    : null;

            if (callback != null)
                contextResource.append(callback + "(");
            contextResource.append(converter.convertToString(response));
            if (callback != null)
                contextResource.append(");\n");
        } else {
            sendError(contextResource, responseItem);
        }
    }

    protected RestHandler getRestHandler(ContextResource contextResource) {
        // TODO Rework to allow sub-services
        String path = contextResource.getReference().getPath();

        // TODO - This shouldnt be on BaseRequestItem
        String method = contextResource.getParameter(X_HTTP_METHOD_OVERRIDE);
        if (method == null) {
            method = contextResource.getRequest().getMethod().getName();
        }

        // Always returns a non-null handler.
        return dispatcher.getRestHandler(path, method.toUpperCase());
    }

    public BeanConverter getConverterForRequest(ContextResource contextResource) {
        String formatString = null;
        BeanConverter converter = null;
        String contentType = null;
        try {
            formatString = contextResource.getParameter(FORMAT_PARAM);
        } catch (Throwable t) {
            // this happens while testing
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Unexpected error : format param is null " + t.toString());
            }
        }
        try {
            contentType = contextResource.getRequestEntity().getMediaType().getName();
        } catch (Throwable t) {
            //this happens while testing
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Unexpected error : content type is null " + t.toString());
            }
        }

        if (contentType != null) {
            if (ContentTypes.ALLOWED_JSON_CONTENT_TYPES.contains(contentType)) {
                converter = jsonConverter;
            } else if (ContentTypes.ALLOWED_ATOM_CONTENT_TYPES.contains(contentType)) {
                converter = atomConverter;
            } else if (ContentTypes.ALLOWED_XML_CONTENT_TYPES.contains(contentType)) {
                converter = xmlConverter;
            } else if (formatString == null) {
                // takes care of cases where content!= null but is ""
                converter = jsonConverter;
            }
        } else if (formatString != null) {
            if (formatString.equals(ATOM_FORMAT)) {
                converter = atomConverter;
            } else if (formatString.equals(XML_FORMAT)) {
                converter = xmlConverter;
            } else {
                converter = jsonConverter;
            }
        } else {
            converter = jsonConverter;
        }
        return converter;
    }
}
