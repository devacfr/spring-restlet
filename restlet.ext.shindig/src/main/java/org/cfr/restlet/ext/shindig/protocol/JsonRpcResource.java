package org.cfr.restlet.ext.shindig.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ContentTypes;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.JsonRpcServlet;
import org.apache.shindig.protocol.ResponseItem;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.RpcHandler;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.protocol.multipart.MultipartFormParser;
import org.cfr.restlet.ext.shindig.resource.ContextResource;
import org.cfr.restlet.ext.shindig.util.ShindigJsonConversionUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pmi.restlet.Resource;

/**
 * Based on {@link JsonRpcServlet}
 * 
 * @author cfriedri
 * 
 */
@Resource(path = "/api/rpc", strict = false)
public class JsonRpcResource extends ApiResource {

    public static final Set<String> ALLOWED_CONTENT_TYPES = new ImmutableSet.Builder<String>().addAll(ContentTypes.ALLOWED_JSON_CONTENT_TYPES)
            .addAll(ContentTypes.ALLOWED_MULTIPART_CONTENT_TYPES)
            .build();

    /**
     * In a multipart request, the form item with field name "request" will
     * contain the actual request, per the proposed Opensocial 0.9
     * specification.
     */
    public static final String REQUEST_PARAM = "request";

    private MultipartFormParser formParser;

    @Inject
    void setMultipartFormParser(MultipartFormParser formParser) {
        this.formParser = formParser;
    }

    private String jsonRpcResultField = "result";

    private boolean jsonRpcBothFields = false;

    @Inject
    void setJsonRpcResultField(@Named("shindig.json-rpc.result-field") String jsonRpcResultField) {
        this.jsonRpcResultField = jsonRpcResultField;
        jsonRpcBothFields = "both".equals(jsonRpcResultField);
    }

    @Get()
    public Representation doGet() throws IOException {
        service(getContextResource());
        return getContextResource().consolidate();
    }

    @Post
    public Representation doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
        service(getContextResource());
        return getContextResource().consolidate();
    }

    protected void service(ContextResource contextResource) throws IOException {
        setCharacterEncodings(contextResource);
        contextResource.setMediaType(MediaType.valueOf(ContentTypes.OUTPUT_JSON_CONTENT_TYPE));

        // only GET/POST
        Method method = contextResource.getRequest().getMethod();

        if (!(Method.GET.equals(method) || Method.POST.equals(method))) {
            sendError(contextResource, new ResponseItem(HttpServletResponse.SC_BAD_REQUEST, "Only POST/GET Allowed"));
            return;
        }

        SecurityToken token = getSecurityToken(contextResource);
        if (token == null) {
            sendSecurityError(contextResource);
            return;
        }

        ResourceUtil.setCORSheader(contextResource, containerConfig.<String> getList(token.getContainer(), "gadgets.parentOrigins"));

        try {
            String content = null;
            String callback = null; // for JSONP
            Map<String, FormDataItem> formData = Maps.newHashMap();

            // Get content or deal with JSON-RPC GET
            if ("POST".equals(method)) {
                content = getPostContent(contextResource, formData);
            } else if (ResourceUtil.isJSONP(contextResource)) {
                content = contextResource.getParameter("request");
                callback = contextResource.getParameter("callback");
            } else {
                // GET request, fromRequest() creates the json objects directly.
                JSONObject request = ShindigJsonConversionUtil.fromRequest(getRequest().getResourceRef().getQueryAsForm());

                if (request != null) {
                    dispatch(request, formData, contextResource, token, null);
                    return;
                }
            }

            if (content == null) {
                sendError(contextResource, new ResponseItem(HttpServletResponse.SC_BAD_REQUEST, "No content specified"));
                return;
            }

            if (isContentJsonBatch(content)) {
                JSONArray batch = new JSONArray(content);
                dispatchBatch(batch, formData, contextResource, token, callback);
            } else {
                JSONObject request = new JSONObject(content);
                dispatch(request, formData, contextResource, token, callback);
            }
            return;
        } catch (JSONException je) {
            sendJsonParseError(je, contextResource);
        } catch (IllegalArgumentException e) {
            // a bad jsonp request..
            sendBadRequest(e, contextResource);
        } catch (ContentTypes.InvalidContentTypeException icte) {
            sendBadRequest(icte, contextResource);
        }
    }

    protected String getPostContent(ContextResource contextResource, Map<String, FormDataItem> formItems)
            throws ContentTypes.InvalidContentTypeException, IOException {
        String content = null;

        ContentTypes.checkContentTypes(ALLOWED_CONTENT_TYPES, contextResource.getMediaType().getName());

        if (formParser.isMultipartContent(contextResource.getRequest())) {
            for (FormDataItem item : formParser.parse(contextResource.getRequest())) {
                if (item.isFormField() && REQUEST_PARAM.equals(item.getFieldName()) && content == null) {
                    // As per spec, in case of a multipart/form-data content, there will be one form field
                    // with field name as "request". It will contain the json request. Any further form
                    // field or file item will not be parsed out, but will be exposed via getFormItem
                    // method of RequestItem.
                    if (!StringUtils.isEmpty(item.getContentType())) {
                        ContentTypes.checkContentTypes(ContentTypes.ALLOWED_JSON_CONTENT_TYPES, item.getContentType());
                    }
                    content = item.getAsString();
                } else {
                    formItems.put(item.getFieldName(), item);
                }
            }
        } else {
            content = contextResource.getText();
        }
        return content;
    }

    protected void dispatchBatch(JSONArray batch, Map<String, FormDataItem> formItems, ContextResource contextResource, SecurityToken token,
            String callback) throws JSONException, IOException {
        // Use linked hash map to preserve order
        List<Future<?>> responses = Lists.newArrayListWithCapacity(batch.length());

        // Gather all Futures. We do this up front so that
        // the first call to get() comes after all futures are created,
        // which allows for implementations that batch multiple Futures
        // into single requests.
        for (int i = 0; i < batch.length(); i++) {
            JSONObject batchObj = batch.getJSONObject(i);
            responses.add(getHandler(batchObj).execute(formItems, token, jsonConverter));
        }

        // Resolve each Future into a response.
        // TODO: should use shared deadline across each request
        List<Object> result = new ArrayList<Object>(batch.length());
        for (int i = 0; i < batch.length(); i++) {
            JSONObject batchObj = batch.getJSONObject(i);
            String key = null;
            if (batchObj.has("id")) {
                key = batchObj.getString("id");
            }
            result.add(getJSONResponse(key, getResponseItem(responses.get(i))));
        }

        // Generate the output
        if (callback != null)
            contextResource.append(callback).append('(');
        jsonConverter.append(contextResource, result);
        if (callback != null)
            contextResource.append(");\n");
    }

    protected void dispatch(JSONObject request, Map<String, FormDataItem> formItems, ContextResource contextResource, SecurityToken token,
            String callback) throws JSONException, IOException {
        String key = null;
        if (request.has("id")) {
            key = request.getString("id");
        }

        // getRpcHandler never returns null
        Future<?> future = getHandler(request).execute(formItems, token, jsonConverter);

        // Resolve each Future into a response.
        // TODO: should use shared deadline across each request
        ResponseItem resp = getResponseItem(future);
        Object result = getJSONResponse(key, resp);

        // Generate the output
        if (callback != null)
            contextResource.append(callback).append('(');
        jsonConverter.append(contextResource, result);
        if (callback != null)
            contextResource.append(");\n");
    }

    /**
     * 
     */
    protected void addResult(Map<String, Object> result, Object data) {
        if (jsonRpcBothFields) {
            result.put("result", data);
            result.put("data", data);
        }
        result.put(jsonRpcResultField, data);
    }

    /**
     * Determine if the content contains a batch request
     *
     * @param content json content or null
     * @return true if content contains is a json array, not a json object or null
     */
    private boolean isContentJsonBatch(String content) {
        if (content == null)
            return false;
        return ((content.indexOf('[') != -1) && content.indexOf('[') < content.indexOf('{'));
    }

    /**
     * Wrap call to dispatcher to allow for implementation specific overrides
     * and servlet-request contextual handling
     */
    protected RpcHandler getHandler(JSONObject rpc) {
        return dispatcher.getRpcHandler(rpc);
    }

    Object getJSONResponse(String key, ResponseItem responseItem) {
        Map<String, Object> result = Maps.newHashMap();
        if (key != null) {
            result.put("id", key);
        }
        if (responseItem.getErrorCode() < 200 || responseItem.getErrorCode() >= 400) {
            result.put("error", getErrorJson(responseItem));
        } else {
            Object response = responseItem.getResponse();
            if (response instanceof DataCollection) {
                addResult(result, ((DataCollection) response).getEntry());
            } else if (response instanceof RestfulCollection) {
                Map<String, Object> map = Maps.newHashMap();
                RestfulCollection<?> collection = (RestfulCollection<?>) response;
                // Return sublist info
                if (collection.getTotalResults() != collection.getEntry().size()) {
                    map.put("startIndex", collection.getStartIndex());
                    map.put("itemsPerPage", collection.getItemsPerPage());
                }

                map.put("totalResults", collection.getTotalResults());

                if (!collection.isFiltered()) {
                    map.put("filtered", collection.isFiltered());
                }

                if (!collection.isUpdatedSince()) {
                    map.put("updatedSince", collection.isUpdatedSince());
                }

                if (!collection.isSorted()) {
                    map.put("sorted", collection.isUpdatedSince());
                }

                map.put("list", collection.getEntry());
                addResult(result, map);
            } else {
                addResult(result, response);
            }

            // TODO: put "code" for != 200?
        }
        return result;
    }

    /** Map of old-style error titles */
    private static final Map<Integer, String> errorTitles = ImmutableMap.<Integer, String> builder().put(HttpServletResponse.SC_NOT_IMPLEMENTED,
            "notImplemented").put(HttpServletResponse.SC_UNAUTHORIZED, "unauthorized").put(HttpServletResponse.SC_FORBIDDEN, "forbidden").put(
            HttpServletResponse.SC_BAD_REQUEST, "badRequest").put(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internalError").put(
            HttpServletResponse.SC_EXPECTATION_FAILED, "limitExceeded").build();

    // TODO(doll): Refactor the responseItem so that the fields on it line up
    // with this format.
    // Then we can use the general converter to output the response to the
    // client and we won't
    // be harcoded to json.
    private Object getErrorJson(ResponseItem responseItem) {
        Map<String, Object> error = new HashMap<String, Object>(2, 1);
        error.put("code", responseItem.getErrorCode());

        String message = errorTitles.get(responseItem.getErrorCode());
        if (message == null) {
            message = responseItem.getErrorMessage();
        } else {
            if (StringUtils.isNotBlank(responseItem.getErrorMessage())) {
                message += ": " + responseItem.getErrorMessage();
            }
        }

        if (StringUtils.isNotBlank(message)) {
            error.put("message", message);
        }

        if (responseItem.getResponse() != null) {
            error.put("data", responseItem.getResponse());
        }

        return error;
    }

    @Override
    protected void sendError(ContextResource contextResource, ResponseItem responseItem) throws IOException {
        jsonConverter.append(contextResource, getErrorJson(responseItem));
        contextResource.setStatus(Status.valueOf(responseItem.getErrorCode()));
    }

    private void sendBadRequest(Throwable t, ContextResource contextResource) throws IOException {
        sendError(contextResource, new ResponseItem(HttpServletResponse.SC_BAD_REQUEST, "Invalid input - " + t.getMessage()));
    }

    private void sendJsonParseError(JSONException e, ContextResource contextResource) throws IOException {
        sendError(contextResource, new ResponseItem(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), "Invalid JSON - " + e.getMessage()));
    }
}
