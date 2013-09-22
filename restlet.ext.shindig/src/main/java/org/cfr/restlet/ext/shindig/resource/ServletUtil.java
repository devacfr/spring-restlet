package org.cfr.restlet.ext.shindig.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.Pair;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.common.uri.UriBuilder;
import org.apache.shindig.common.util.Utf8UrlCoder;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.HttpResponseBuilder;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;
import org.restlet.representation.Representation;


/**
 * Jtility routines for dealing with servlets.
 *
 * @since 2.0.0
 */
public final class ServletUtil {

    public static final String REMOTE_ADDR_KEY = "RemoteAddress";

    public static final String DATA_URI_KEY = "dataUri";

    private ServletUtil() {
    }

    /**
     * Returns an HttpRequest object encapsulating the servlet request.
     * NOTE: Request parameters are not explicitly taken care of, instead we copy
     * the InputStream and query parameters separately.
     *
     * @param restReq The http request.
     * @return An HttpRequest object with all the information provided by the
     *   servlet request.
     * @throws IOException In case of errors.
     */
    public static HttpRequest fromRequest(ContextResource contextResource) throws IOException {
        HttpRequest req = new HttpRequest(ResourceUtil.toUri(contextResource.getReference()));

        Iterator<String> headerNames = contextResource.getRequestHeaders().getNames().iterator();
        while (headerNames.hasNext()) {
            String obj = headerNames.next();
            String headerName = obj;

            String[] headerValues = contextResource.getRequestHeaders().getValuesArray(headerName);
            for (String val : headerValues) {
                req.addHeader(headerName, val);
            }
        }

        req.setMethod(contextResource.getRequest().getMethod().getName());
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            req.setPostBody(contextResource.getRequestEntity().getStream());
        }
        req.setParam(REMOTE_ADDR_KEY, contextResource.getClientInfo().getAddress());
        return req;
    }

    public static void setCachingHeaders(HttpResponseBuilder response, int ttl, boolean noProxy) {
        for (Pair<String, String> header : ResourceUtil.getCachingHeadersToSet(ttl, noProxy)) {
            response.setHeader(header.one, header.two);
        }
    }

    public static Representation copyResponseToContext(HttpResponse response, ContextResource contextResource) throws IOException {
        contextResource.setStatus(Status.valueOf(response.getHttpStatusCode()));
        for (Map.Entry<String, String> header : response.getHeaders().entries()) {
            contextResource.getResponseHeaders().add(header.getKey(), header.getValue());
        }
        ResourceUtil.setCachingHeaders(contextResource, (int) (response.getCacheTtl() / 1000L));
        return contextResource.consolidate(response.getResponse());
    }

    /**
     * Validates and normalizes the given url, ensuring that it is non-null, has
     * scheme http or https, and has a path value of some kind.
     *
     * @return A URI representing a validated form of the url.
     * @throws GadgetException If the url is not valid.
     */
    public static Uri validateUrl(Uri urlToValidate) throws GadgetException {
        if (urlToValidate == null) {
            throw new GadgetException(GadgetException.Code.MISSING_PARAMETER, "Missing url param", HttpResponse.SC_BAD_REQUEST);
        }
        UriBuilder url = new UriBuilder(urlToValidate);
        if (!"http".equals(url.getScheme()) && !"https".equals(url.getScheme())) {
            throw new GadgetException(GadgetException.Code.INVALID_PARAMETER, "Invalid request url scheme in url: "
                    + Utf8UrlCoder.encode(urlToValidate.toString()) + "; only \"http\" and \"https\" supported.", HttpResponse.SC_BAD_REQUEST);
        }
        if (url.getPath() == null || url.getPath().length() == 0) {
            url.setPath("/");
        }
        return url.toUri();
    }

    /**
     * Sets standard forwarding headers on the proxied request.
     * @param inboundRequest
     * @param req
     * @throws GadgetException
     */
    public static void setXForwardedForHeader(HttpRequest inboundRequest, HttpRequest req) throws GadgetException {
        String forwardedFor = getXForwardedForHeader(inboundRequest.getHeader("X-Forwarded-For"), inboundRequest
                .getParam(ServletUtil.REMOTE_ADDR_KEY));
        if (forwardedFor != null) {
            req.setHeader("X-Forwarded-For", forwardedFor);
        }
    }

    public static void setXForwardedForHeader(ContextResource contextResource, HttpRequest req) throws GadgetException {
        ClientInfo clientInfo = contextResource.getClientInfo();
        List<String> forwardedAddresses = clientInfo.getForwardedAddresses();
        String origValue = null;
        if (!forwardedAddresses.isEmpty()) {
            origValue = StringUtils.join(clientInfo.getForwardedAddresses(), ", ");
        }
        String forwardedFor = getXForwardedForHeader(origValue, clientInfo.getAddress());
        if (forwardedFor != null) {
            req.setHeader("X-Forwarded-For", forwardedFor);
        }
    }

    private static String getXForwardedForHeader(String origValue, String remoteAddr) {
        if (!StringUtils.isEmpty(remoteAddr)) {
            if (StringUtils.isEmpty(origValue)) {
                origValue = remoteAddr;
            } else {
                origValue = remoteAddr + ", " + origValue;
            }
        }
        return origValue;
    }

    /**
     * @return An HttpResponse object wrapping the given GadgetException.
     */
    public static HttpResponse errorResponse(GadgetException e) {
        return new HttpResponseBuilder().setHttpStatusCode(e.getHttpStatusCode()).setResponseString(e.getMessage() != null ? e.getMessage() : "")
                .create();
    }

    /**
     * Converts the given {@code HttpResponse} into JSON form, with at least
     * one field, dataUri, containing a Data URI that can be inlined into an HTML page.
     * Any metadata on the given {@code HttpResponse} is also added as fields.
     * 
     * @param response Input HttpResponse to convert to JSON.
     * @return JSON-containing HttpResponse.
     * @throws IOException If there are problems reading from {@code response}.
     */
    public static HttpResponse convertToJsonResponse(HttpResponse response) throws IOException {
        // Pull out charset, if present. If not, this operation simply returns contentType.
        String contentType = response.getHeader("Content-Type");
        if (contentType == null) {
            contentType = "";
        } else if (contentType.contains(";")) {
            contentType = StringUtils.split(contentType, ';')[0].trim();
        }
        // First and most importantly, emit dataUri.
        // Do so in streaming fashion, to avoid needless buffering.
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(os);
        pw.write("{\n  ");
        pw.write(DATA_URI_KEY);
        pw.write(":'data:");
        pw.write(contentType);
        pw.write(";base64;charset=");
        pw.write(response.getEncoding());
        pw.write(",");
        pw.flush();

        // Stream out the base64-encoded data.
        // Ctor args indicate to encode w/o line breaks.
        Base64InputStream b64input = new Base64InputStream(response.getResponse(), true, 0, null);
        byte[] buf = new byte[1024];
        int read = -1;
        try {
            while ((read = b64input.read(buf, 0, 1024)) > 0) {
                os.write(buf, 0, read);
            }
        } finally {
            IOUtils.closeQuietly(b64input);
        }

        // Complete the JSON object.
        pw.write("',\n  ");
        boolean first = true;
        for (Map.Entry<String, String> metaEntry : response.getMetadata().entrySet()) {
            if (DATA_URI_KEY.equals(metaEntry.getKey()))
                continue;
            if (!first) {
                pw.write(",\n  ");
            }
            first = false;
            pw.write("'");
            pw.write(StringEscapeUtils.escapeJavaScript(metaEntry.getKey()).replace("'", "\'"));
            pw.write("':'");
            pw.write(StringEscapeUtils.escapeJavaScript(metaEntry.getValue()).replace("'", "\'"));
            pw.write("'");
        }
        pw.write("\n}");
        pw.flush();

        return new HttpResponseBuilder().setHeader("Content-Type", "application/json").setResponseNoCopy(os.toByteArray()).create();
    }
}