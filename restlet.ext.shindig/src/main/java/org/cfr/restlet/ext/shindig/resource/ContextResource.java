package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;
import org.springframework.util.Assert;

public class ContextResource implements Appendable {

    public static final String HEADER_ATTRIBUTE_NAME = "org.restlet.http.headers";

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    private final Request request;

    private final Response response;

    private AppendableRepresentation representation;

    private Series<Parameter> parameters;

    private Series<Parameter> responseHeaders;

    public void setResponseHeaders(Series<Parameter> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public ContextResource(Request request, Response response) throws IOException {
        Assert.notNull(request, "request parameter is required");
        Assert.notNull(response, "response parameter is required");
        this.request = request;
        this.response = response;
        this.init();
    }

    public ContextResource(Request request, Response response, Series<Parameter> parameters) throws IOException {
        Assert.notNull(request, "request parameter is required");
        Assert.notNull(response, "response parameter is required");
        this.request = request;
        this.response = response;
        this.parameters = parameters;
        this.init();
    }

    protected void init() throws IOException {
        if (this.response.getEntity() != null) {
            this.representation = new AppendableRepresentation(this.response.getEntity().getText());
        } else {
            this.representation = new AppendableRepresentation();
        }
        this.response.setEntity(representation);

        if (this.parameters == null) {
            if (Method.GET.equals(request.getMethod())) {
                this.parameters = request.getResourceRef().getQueryAsForm();
            } else if (Method.POST.equals(request.getMethod())) {
                this.parameters = new Form(request.getEntity());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Series<Parameter> getRequestHeaders() {
        if (request.getAttributes() == null || request.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
            return new Form();
        }
        return (Series<Parameter>) request.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
    }

    public Series<Parameter> getResponseHeaders() {
        Series<Parameter> c = this.responseHeaders;
        if (c == null) {
            synchronized (this) {
                c = this.responseHeaders;
                if (c == null) {
                    this.responseHeaders = c = new Form();
                }
            }
        }
        return c;
    }

    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Series<Parameter> getParameters() {
        return parameters;
    }

    public String getParameter(String name) {
        return this.parameters.getFirstValue(name);
    }

    public ClientInfo getClientInfo() {
        return this.request.getClientInfo();
    }

    public AppendableRepresentation getAppendableRepresentation() {
        Representation representation = getResponseEntity();
        if (representation instanceof AppendableRepresentation)
            return (AppendableRepresentation) representation;
        throw new RuntimeException("Entity of response is not appendable representation");
    }

    public CharacterSet getCharacterSet() {
        return getResponseEntity().getCharacterSet();
    }

    public void setCharacterSet(CharacterSet characterSet) {
        getResponseEntity().setCharacterSet(characterSet);
    }

    public Representation consolidate() {
        if (responseHeaders != null) {
            HeaderUtils.extractEntityHeaders(responseHeaders, response.getEntity());
            HeaderUtils.copyResponseTransportHeaders(responseHeaders, response);
        }
        return representation;
    }

    public Representation consolidate(InputStream inputStream) {
        if (responseHeaders != null) {
            HeaderUtils.extractEntityHeaders(responseHeaders, response.getEntity());
            HeaderUtils.copyResponseTransportHeaders(responseHeaders, response);
        }
        Representation entity = new InputRepresentation(inputStream);
        response.setEntity(entity);
        return entity;
    }

    public void setStatus(Status status) {
        this.response.setStatus(status);
    }

    public void setStatus(Status status, String message) {
        this.response.setStatus(status, message);
    }

    public Status getStatus() {
        return this.response.getStatus();
    }

    public void setText(CharSequence text) {
        getAppendableRepresentation().setText(text);
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        return getAppendableRepresentation().append(csq);
    }

    @Override
    public Appendable append(char c) throws IOException {
        return getAppendableRepresentation().append(c);
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return getAppendableRepresentation().append(csq, start, end);
    }

    public String getText() {
        try {
            return getResponseEntity().getText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMediaType(MediaType mediaType) {
        getResponseEntity().setMediaType(mediaType);
    }

    public MediaType getMediaType() {
        return getResponseEntity().getMediaType();
    }

    public Disposition getDisposition() {
        return getResponseEntity().getDisposition();
    }

    public void setDisposition(Disposition disposition) {
        getResponseEntity().setDisposition(disposition);
    }

    public Representation getRequestEntity() {
        return this.request.getEntity();
    }

    public Representation getResponseEntity() {
        return this.response.getEntity();
    }

    public Reference getReference() {
        return getRequest() == null ? null : getRequest().getResourceRef();
    }

    public Map<String, String[]> getParametersMap() {
        Map<String, String[]> map = new HashMap<String, String[]>();
        Series<Parameter> params = this.getParameters();
        for (Parameter parameter : params) {
            map.put(parameter.getName(), params.getValuesArray(parameter.getName()));
        }
        return map;
    }

    public void addQueryParameter(String name, String value) {
        getReference().addQueryParameter(name, value);
        this.parameters.add(name, value);
    }
}
