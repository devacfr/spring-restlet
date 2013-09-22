package org.cfr.restlet.ext.shindig.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.HttpResponseBuilder;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;
import com.google.inject.internal.Preconditions;
import com.google.inject.name.Named;

/**
 * A simple HTTP fetcher implementation based on Apache httpclient. Not recommended for production deployments until
 * the following issues are addressed:
 * <p/>
 * 1. This class potentially allows access to resources behind an organization's firewall.
 * 2. This class does not enforce any limits on what is fetched from remote hosts.
 * Based on {@link org.apache.shindig.gadgets.http.BasicHttpFetcher}
 */
@Singleton
public class ClientHttpFetcher implements HttpFetcher {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;

    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;

    private static final int DEFAULT_MAX_OBJECT_SIZE = 0; // no limit

    private static final long DEFAULT_SLOW_RESPONSE_WARNING = 10000;

    // mutable fields must be volatile
    private volatile int maxObjSize;

    private volatile long slowResponseWarning;

    private static final Logger LOG = Logger.getLogger(ClientHttpFetcher.class.getName());

    private final Set<Class<?>> TIMEOUT_EXCEPTIONS = ImmutableSet.<Class<?>> of(ConnectionPoolTimeoutException.class, SocketTimeoutException.class,
            SocketException.class, HttpHostConnectException.class, NoHttpResponseException.class, InterruptedException.class,
            UnknownHostException.class);

    /**
     * Creates a new fetcher using the default maximum object size and timeout --
     * no limit and 5 seconds.
     * @param basicHttpFetcherProxy The http proxy to use.
     */
    @Inject
    public ClientHttpFetcher(@Nullable @Named("org.apache.shindig.gadgets.http.basicHttpFetcherProxy") String basicHttpFetcherProxy) {
        this(DEFAULT_MAX_OBJECT_SIZE, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS, basicHttpFetcherProxy);
    }

    /**
     * Creates a new fetcher for fetching HTTP objects.  Not really suitable
     * for production use. Use of an HTTP proxy for security is also necessary
     * for production deployment.
     *
     * @param maxObjSize          Maximum size, in bytes, of the object we will fetch, 0 if no limit..
     * @param connectionTimeoutMs timeout, in milliseconds, for connecting to hosts.
     * @param readTimeoutMs       timeout, in millseconds, for unresponsive connections
     * @param basicHttpFetcherProxy The http proxy to use.
     */
    public ClientHttpFetcher(int maxObjSize, int connectionTimeoutMs, int readTimeoutMs, String basicHttpFetcherProxy) {
        // Create and initialize HTTP parameters
        setMaxObjectSizeBytes(maxObjSize);
        setSlowResponseWarning(DEFAULT_SLOW_RESPONSE_WARNING);

    }

    static class GzipDecompressingEntity extends HttpEntityWrapper {

        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

    static class DeflateDecompressingEntity extends HttpEntityWrapper {

        public DeflateDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {

            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new InflaterInputStream(wrappedin, new Inflater(true));
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }

    public HttpResponse fetch(org.apache.shindig.gadgets.http.HttpRequest request) throws GadgetException {

        Preconditions.checkNotNull(request);
        final String methodType = request.getMethod();

        final Response response;
        final long started = System.currentTimeMillis();

        // Break the request Uri to its components:
        Uri uri = request.getUri();
        if (StringUtils.isEmpty(uri.getAuthority())) {
            throw new GadgetException(GadgetException.Code.INVALID_USER_DATA, "Missing domain name for request: " + uri,
                    HttpServletResponse.SC_BAD_REQUEST);
        }
        if (StringUtils.isEmpty(uri.getScheme())) {
            throw new GadgetException(GadgetException.Code.INVALID_USER_DATA, "Missing schema for request: " + uri,
                    HttpServletResponse.SC_BAD_REQUEST);
        }
        String[] hostparts = uri.getAuthority().split(":");
        int port = -1; // default port
        if (hostparts.length > 2) {
            throw new GadgetException(GadgetException.Code.INVALID_USER_DATA, "Bad host name in request: " + uri.getAuthority(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
        if (hostparts.length == 2) {
            try {
                port = Integer.parseInt(hostparts[1]);
            } catch (NumberFormatException e) {
                throw new GadgetException(GadgetException.Code.INVALID_USER_DATA, "Bad port number in request: " + uri.getAuthority(),
                        HttpServletResponse.SC_BAD_REQUEST);
            }
        }

        try {
            Method method = null;
            Representation representation = null;
            Series<Parameter> header = new Form();
            if ("POST".equals(methodType) || "PUT".equals(methodType)) {
                if (request.getPostBodyLength() > 0) {
                    representation = new InputRepresentation(request.getPostBody());
                }
                method = Method.valueOf(methodType);
            } else if ("GET".equals(methodType)) {
                method = Method.GET;
            } else if ("HEAD".equals(methodType)) {
                method = Method.HEAD;
            } else if ("DELETE".equals(methodType)) {
                method = Method.DELETE;
            }
            for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
                header.add(entry.getKey(), StringUtils.join(entry.getValue(), ','));
            }

            Request req = new Request(method, uri.toString());
            // TODO to verify
            req.getAttributes().put("org.restlet.http.headers", header);
            if (representation != null)
                req.setEntity(representation);

            ClientResource clientResource = new ClientResource(req, new Response(req));
            // HttpClient doesn't handle all cases when breaking url (specifically '_' in domain)
            // So lets pass it the url parsed:
            clientResource.handle();
            response = clientResource.getResponse();

            if (response == null) {
                throw new IOException("Unknown problem with request");
            }

            long now = System.currentTimeMillis();
            if (now - started > slowResponseWarning) {
                slowResponseWarning(request, started, now);
            }

            return makeResponse(response);

        } catch (Exception e) {
            long now = System.currentTimeMillis();

            // Find timeout exceptions, respond accordingly
            if (TIMEOUT_EXCEPTIONS.contains(e.getClass())) {
                LOG.info("Timeout for " + request.getUri() + " Exception: " + e.getClass().getName() + " - " + e.getMessage() + " - "
                        + (now - started) + "ms");
                return HttpResponse.timeout();
            }

            LOG.log(Level.INFO, "Got Exception fetching " + request.getUri() + " - " + (now - started) + "ms", e);

            // Separate shindig error from external error
            throw new GadgetException(GadgetException.Code.INTERNAL_SERVER_ERROR, e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Called when a request takes too long.   Consider subclassing this if you want to do something other than logging
     * a warning .
     *
     * @param request the request that generated the slowrequest
     * @param started  the time the request started, in milliseconds.
     * @param finished the time the request finished, in milliseconds.
     */
    protected void slowResponseWarning(HttpRequest request, long started, long finished) {
        LOG.warning("Slow response from " + request.getUri() + ' ' + (finished - started) + "ms");
    }

    /**
     * Change the global maximum fetch size (in bytes) for all fetches.
     *
     * @param maxObjectSizeBytes value for maximum number of bytes, or 0 for no limit
     */
    @Inject(optional = true)
    public void setMaxObjectSizeBytes(@Named("shindig.http.client.max-object-size-bytes") int maxObjectSizeBytes) {
        this.maxObjSize = maxObjectSizeBytes;
    }

    /**
     * Change the global threshold for warning about slow responses
     *
     * @param slowResponseWarning time in milliseconds after we issue a warning
     */

    @Inject(optional = true)
    public void setSlowResponseWarning(@Named("shindig.http.client.slow-response-warning") long slowResponseWarning) {
        this.slowResponseWarning = slowResponseWarning;
    }

    /**
     * Change the global connection timeout for all new fetchs.
     *
     * @param connectionTimeoutMs new connection timeout in milliseconds
     */
    @Inject(optional = true)
    public void setConnectionTimeoutMs(@Named("shindig.http.client.connection-timeout-ms") int connectionTimeoutMs) {
    }

    /**
     * Change the global read timeout for all new fetchs.
     *
     * @param connectionTimeoutMs new connection timeout in milliseconds
     */
    @Inject(optional = true)
    public void setReadTimeoutMs(@Named("shindig.http.client.read-timeout-ms") int connectionTimeoutMs) {
    }

    /**
     * @param response The response to parse
     * @return A HttpResponse object made by consuming the response of the
     *         given HttpMethod.
     * @throws IOException when problems occur processing the body content
     */
    @SuppressWarnings("unchecked")
    private HttpResponse makeResponse(Response response) throws IOException {
        HttpResponseBuilder builder = new HttpResponseBuilder();

        Series<Parameter> headers = (Series<Parameter>) response.getAttributes().get("org.restlet.http.headers");
        if (headers != null) {
            for (Parameter param : headers) {
                if (param.getName() != null) {
                    builder.addHeader(param.getName(), param.getValue());
                }
            }
        }

        Representation entity = Preconditions.checkNotNull(response.getEntity());

        if (maxObjSize > 0 && entity != null && entity.getSize() > maxObjSize) {
            return HttpResponse.badrequest("Exceeded maximum number of bytes - " + maxObjSize);
        }

        return builder.setHttpStatusCode(response.getStatus().getCode()).setResponse(toByteArraySafe(entity)).create();
    }

    /**
     * This method is Safe replica version of org.apache.http.util.EntityUtils.toByteArray.
     * The try block embedding 'instream.read' has a corresponding catch block for 'EOFException'
     * (that's Ignored) and all other IOExceptions are let pass.
     *
     * @param entity
     * @return byte array containing the entity content. May be empty/null.
     * @throws IOException if an error occurs reading the input stream
     */
    public byte[] toByteArraySafe(final Representation entity) throws IOException {
        if (entity == null) {
            return null;
        }

        InputStream instream = entity.getStream();
        if (instream == null) {
            return new byte[] {};
        }
        Preconditions.checkArgument(entity.getSize() < Integer.MAX_VALUE, "HTTP entity too large to be buffered in memory");

        // The raw data stream (inside JDK) is read in a buffer of size '512'. The original code
        // org.apache.http.util.EntityUtils.toByteArray reads the unzipped data in a buffer of
        // 4096 byte. For any data stream that has a compression ratio lesser than 1/8, this may
        // result in the buffer/array overflow. Increasing the buffer size to '16384'. It's highly
        // unlikely to get data compression ratios lesser than 1/32 (3%).
        final int bufferLength = 16384;
        int i = (int) entity.getSize();
        if (i < 0) {
            i = bufferLength;
        }
        ByteArrayBuffer buffer = new ByteArrayBuffer(i);
        try {
            byte[] tmp = new byte[bufferLength];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
        } catch (EOFException eofe) {
            /**
             * Ref: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4040920
             * Due to a bug in JDK ZLIB (InflaterInputStream), unexpected EOF error can occur.
             * In such cases, even if the input stream is finished reading, the
             * 'Inflater.finished()' call erroneously returns 'false' and
             * 'java.util.zip.InflaterInputStream.fill' throws the 'EOFException'.
             * So for such case, ignore the Exception in case Exception Cause is
             * 'Unexpected end of ZLIB input stream'.
             *
             * Also, ignore this exception in case the exception has no message
             * body as this is the case where {@link GZIPInputStream#readUByte}
             * throws EOFException with empty message. A bug has been filed with Sun
             * and will be mentioned here once it is accepted.
             */
            if (instream.available() == 0 && (eofe.getMessage() == null || eofe.getMessage().equals("Unexpected end of ZLIB input stream"))) {
                LOG.log(Level.FINE, "EOFException: ", eofe);
            } else {
                throw eofe;
            }
        } finally {
            instream.close();
        }
        return buffer.toByteArray();
    }
}