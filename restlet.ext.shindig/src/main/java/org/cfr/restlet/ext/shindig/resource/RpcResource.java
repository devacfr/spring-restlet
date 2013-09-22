package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.shindig.gadgets.servlet.JsonRpcHandler;
import org.apache.shindig.gadgets.servlet.RpcException;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CharacterSet;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.pmi.restlet.Resource;

/**
 * Handles RPC metadata requests.
 */
@Resource(path = "/metadata", strict = false)
public class RpcResource extends InjectedResource {

    private static final long serialVersionUID = 1382573217773582182L;

    static final String GET_REQUEST_REQ_PARAM = "req";

    static final String GET_REQUEST_CALLBACK_PARAM = "callback";

    private static final Logger logger = LoggerFactory.getLogger("org.apache.shindig.gadgets");

    private transient JsonRpcHandler jsonHandler;

    @Inject
    public void setJsonRpcHandler(JsonRpcHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
    }

    @Get
    public Representation doGet() throws IOException {
        String reqValue;
        String callbackValue;

        ContextResource contextResource = getContextResource();

        try {
            ResourceUtil.isJSONP(contextResource);
            reqValue = validateParameterValue(contextResource, GET_REQUEST_REQ_PARAM);
            callbackValue = validateParameterValue(contextResource, GET_REQUEST_CALLBACK_PARAM);

        } catch (IllegalArgumentException e) {
            contextResource.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            logger.info(e.getMessage(), e);
            return null;
        }

        Result result = process(contextResource, reqValue);
        contextResource.setText(result.isSuccess() ? callbackValue + '(' + result.getOutput() + ')' : result.getOutput());
        return contextResource.consolidate();
    }

    @Post
    public Representation doPost() throws IOException {
        ContextResource contextResource = getContextResource();
        try {
            String body = contextResource.getRequest().getEntityAsText();
            Result result = process(contextResource, body);
            contextResource.append(result.getOutput());
        } catch (UnsupportedEncodingException e) {
            contextResource.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            logger.info(e.getMessage(), e);
            contextResource.setText("Unsupported input character set");
        }
        return contextResource.consolidate();
    }

    private String validateParameterValue(ContextResource contextResource, String parameter) throws IllegalArgumentException {
        String result = contextResource.getParameter(parameter);
        Preconditions.checkArgument(result != null, "No parameter '%s' specified", parameter);
        return result;
    }

    private Result process(ContextResource contextResource, String body) {
        try {
            JSONObject req = new JSONObject(body);
            JSONObject resp = jsonHandler.process(req);
            contextResource.setStatus(Status.SUCCESS_OK);
            contextResource.setMediaType(MediaType.APPLICATION_JSON);
            contextResource.setCharacterSet(CharacterSet.UTF_8);
            Form disposition = new Form();
            disposition.add(Disposition.NAME_FILENAME, "rpc.text");
            contextResource.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT, disposition));
            return new Result(resp.toString(), true);
        } catch (JSONException e) {
            contextResource.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new Result("Malformed JSON request.", false);
        } catch (RpcException e) {
            contextResource.setStatus(Status.SERVER_ERROR_INTERNAL);
            logger.info(e.getMessage(), e);
            return new Result(e.getMessage(), false);
        }
    }

    private static class Result {

        private final String output;

        private final boolean success;

        public Result(String output, boolean success) {
            this.output = output;
            this.success = success;
        }

        public String getOutput() {
            return output;
        }

        public boolean isSuccess() {
            return success;
        }
    }

}