package org.cfr.restlet.ext.shindig.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.Pair;
import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.http.HttpRequest;
import org.apache.shindig.gadgets.http.HttpResponse;
import org.apache.shindig.gadgets.http.RequestPipeline;
import org.apache.shindig.gadgets.rewrite.ResponseRewriterRegistry;
import org.apache.shindig.gadgets.rewrite.RewritingException;
import org.apache.shindig.gadgets.servlet.ConcatProxyServlet;
import org.apache.shindig.gadgets.uri.ConcatUriManager;
import org.apache.shindig.gadgets.uri.UriCommon.Param;
import org.cfr.restlet.ext.shindig.common.servlet.ResourceUtil;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.pmi.restlet.Resource;

/**
 * Based on {@link ConcatProxyServlet}
 * 
 * @author cfriedri
 * 
 */
@Resource(path = "/concat", strict = false)
public class ConcatProxyResource extends InjectedResource {

    public static final String JSON_PARAM = Param.JSON.getKey();

    private static final Pattern JSON_PARAM_PATTERN = Pattern.compile("^\\w*$");

    // TODO: parameterize these.
    static final Integer LONG_LIVED_REFRESH = (365 * 24 * 60 * 60); // 1 year

    static final Integer DEFAULT_REFRESH = (60 * 60); // 1 hour

    private static final Logger logger = LoggerFactory.getLogger(ConcatProxyServlet.class.getName());

    private transient RequestPipeline requestPipeline;

    private transient ConcatUriManager concatUriManager;

    private transient ResponseRewriterRegistry contentRewriterRegistry;

    // Sequential version of 'execute' by default.
    private transient ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public void setRequestPipeline(RequestPipeline requestPipeline) {
        this.requestPipeline = requestPipeline;
    }

    @Inject
    public void setConcatUriManager(ConcatUriManager concatUriManager) {
        this.concatUriManager = concatUriManager;
    }

    @Inject
    public void setContentRewriterRegistry(ResponseRewriterRegistry contentRewriterRegistry) {
        this.contentRewriterRegistry = contentRewriterRegistry;
    }

    @Inject
    public void setExecutor(@Named("shindig.concat.executor") ExecutorService executor) {
        // Executor is independently named to allow separate configuration of
        // concat fetch parallelism and other Shindig job execution.
        this.executor = executor;
    }

    @Get
    public Representation doGet() throws IOException {
        ContextResource contextResource = getContextResource();

        if (contextResource.getRequestHeaders().getFirstValue(HeaderConstants.HEADER_IF_MODIFIED_SINCE) != null) {
            contextResource.setStatus(Status.REDIRECTION_NOT_MODIFIED);
            return null;
        }

        Uri uri = ResourceUtil.toUri(contextResource.getReference());
        ConcatUriManager.ConcatUri concatUri = concatUriManager.process(uri);

        ConcatUriManager.Type concatType = concatUri.getType();
        try {
            if (concatType == null) {
                throw new GadgetException(GadgetException.Code.MISSING_PARAMETER, "Missing type", HttpResponse.SC_BAD_REQUEST);
            }
            ResourceUtil.setCachingHeaders(contextResource, concatUri.translateStatusRefresh(LONG_LIVED_REFRESH, DEFAULT_REFRESH), false);
        } catch (GadgetException gex) {
            String err = formatError(gex, uri);
            contextResource.append(err);
            contextResource.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, err);
            return contextResource.consolidate();
        }

        // Throughout this class, wherever output is generated it's done as a UTF8 String.
        // As such, we affirmatively state that UTF8 is being returned here.
        contextResource.getResponseHeaders().add(HeaderConstants.HEADER_CONTENT_TYPE, concatType.getMimeType() + "; charset=UTF8");
        contextResource.getResponseHeaders().add(HeaderConstants.HEADER_CONTENT_DISPOSITION, "attachment;filename=p.txt");

        if (doFetchConcatResources(contextResource, concatUri)) {
            contextResource.setStatus(Status.SUCCESS_OK);
        } else {
            contextResource.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return contextResource.consolidate();
    }

    /**
     * @param response HttpservletResponse.
     * @param concatUri URI representing the concatenated list of resources requested.
     * @return false for cases where concat resources could not be fetched, true for success cases.
     * @throws IOException
     */
    private boolean doFetchConcatResources(ContextResource contextResource, ConcatUriManager.ConcatUri concatUri) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // Check for json concat and set output stream.
        ConcatOutputStream cos = null;

        String jsonVar = concatUri.getSplitParam();
        if (jsonVar != null) {
            // JSON-concat mode.
            if (JSON_PARAM_PATTERN.matcher(jsonVar).matches()) {
                cos = new JsonConcatOutputStream(outputStream, jsonVar);
            } else {
                contextResource.append(outputStream.toString());
                contextResource.append(formatHttpError(Status.CLIENT_ERROR_BAD_REQUEST, "Bad json variable name " + jsonVar, null) + "\r\n");
                contextResource.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return false;
            }
        } else {
            // Standard concat output mode.
            cos = new VerbatimConcatOutputStream(outputStream);
        }

        List<Pair<Uri, FutureTask<RequestContext>>> futureTasks = new ArrayList<Pair<Uri, FutureTask<RequestContext>>>();

        try {
            for (Uri resourceUri : concatUri.getBatch()) {
                try {
                    HttpRequest httpReq = concatUri.makeHttpRequest(resourceUri);
                    FutureTask<RequestContext> httpFetcher = new FutureTask<RequestContext>(new HttpFetchCallable(httpReq));
                    futureTasks.add(Pair.of(httpReq.getUri(), httpFetcher));
                    executor.execute(httpFetcher);
                } catch (GadgetException ge) {
                    if (cos.outputError(resourceUri, ge)) {
                        // True returned from outputError indicates a terminal error.
                        return false;
                    }
                }
            }

            for (Pair<Uri, FutureTask<RequestContext>> futureTask : futureTasks) {
                RequestContext requestCxt = null;
                try {
                    try {
                        requestCxt = futureTask.two.get();
                    } catch (InterruptedException ie) {
                        throw new GadgetException(GadgetException.Code.INTERNAL_SERVER_ERROR, ie);
                    } catch (ExecutionException ee) {
                        throw new GadgetException(GadgetException.Code.INTERNAL_SERVER_ERROR, ee);
                    }
                    if (requestCxt.getGadgetException() != null) {
                        throw requestCxt.getGadgetException();
                    }
                    HttpResponse httpResp = requestCxt.getHttpResp();
                    if (httpResp != null) {
                        if (contentRewriterRegistry != null) {
                            try {
                                httpResp = contentRewriterRegistry.rewriteHttpResponse(requestCxt.getHttpReq(), httpResp);
                            } catch (RewritingException e) {
                                throw new GadgetException(GadgetException.Code.INTERNAL_SERVER_ERROR, e, e.getHttpStatusCode());
                            }
                        }
                        cos.output(futureTask.one, httpResp);
                    } else {
                        return false;
                    }
                } catch (GadgetException ge) {
                    if (cos.outputError(futureTask.one, ge)) {
                        return false;
                    }
                }
            }
        } finally {
            if (cos != null) {
                try {
                    cos.close();
                    contextResource.append(outputStream.toString());
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }

        return true;
    }

    private static String formatHttpError(Status status, String errorMessage, Uri uri) {
        StringBuilder err = new StringBuilder();
        err.append("/* ---- Error ");
        err.append(status.getCode());
        if (!StringUtils.isEmpty(errorMessage)) {
            err.append(", ");
            err.append(errorMessage);
        }
        if (uri != null) {
            err.append(" (").append(uri.toString()).append(')');
        }

        err.append(" ---- */");
        return err.toString();
    }

    private static String formatError(GadgetException excep, Uri uri) throws IOException {
        StringBuilder err = new StringBuilder();
        err.append(excep.getCode().toString());
        err.append(" concat(");
        err.append(uri.toString());
        err.append(") ");
        err.append(excep.getMessage());

        // Log the errors here for now. We might want different severity levels
        // for different error codes.
        logger.info("Concat proxy request failed", err);
        return err.toString();
    }

    private static abstract class ConcatOutputStream extends ServletOutputStream {

        private final OutputStream wrapped;

        protected ConcatOutputStream(OutputStream wrapped) {
            this.wrapped = wrapped;
        }

        protected abstract void outputJs(Uri uri, String data) throws IOException;

        public void output(Uri uri, HttpResponse resp) throws IOException {
            if (resp.getHttpStatusCode() != HttpServletResponse.SC_OK) {
                println(formatHttpError(Status.valueOf(resp.getHttpStatusCode()), resp.getResponseAsString(), uri));
            } else {
                outputJs(uri, resp.getResponseAsString());
            }
        }

        public boolean outputError(Uri uri, GadgetException e) throws IOException {
            print(formatError(e, uri));
            return e.getHttpStatusCode() == HttpResponse.SC_INTERNAL_SERVER_ERROR;
        }

        @Override
        public void write(int b) throws IOException {
            wrapped.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            wrapped.write(b, off, len);
        }

        @Override
        public void write(byte b[]) throws IOException {
            wrapped.write(b);
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }

        @Override
        public void print(String data) throws IOException {
            write(data.getBytes("UTF8"));
        }

        @Override
        public void println(String data) throws IOException {
            print(data);
            write("\r\n".getBytes("UTF8"));
        }
    }

    private static class VerbatimConcatOutputStream extends ConcatOutputStream {

        public VerbatimConcatOutputStream(OutputStream wrapped) {
            super(wrapped);
        }

        @Override
        protected void outputJs(Uri uri, String data) throws IOException {
            println("/* ---- Start " + uri.toString() + " ---- */");
            print(data);
            println("/* ---- End " + uri.toString() + " ---- */");
        }
    }

    private static class JsonConcatOutputStream extends ConcatOutputStream {

        public JsonConcatOutputStream(OutputStream wrapped, String tok) throws IOException {
            super(wrapped);
            this.println(tok + "={");
        }

        @Override
        protected void outputJs(Uri uri, String data) throws IOException {
            print("\"");
            print(uri.toString());
            print("\":\"");
            print(StringEscapeUtils.escapeJavaScript(data));
            println("\",");
        }

        @Override
        public void close() throws IOException {
            println("};");
            super.close();
        }

    }

    // Encapsulates the response context of a single resource fetch.
    private static class RequestContext {

        private HttpRequest httpReq;

        private HttpResponse httpResp;

        private GadgetException gadgetException;

        public HttpRequest getHttpReq() {
            return httpReq;
        }

        public HttpResponse getHttpResp() {
            return httpResp;
        }

        public GadgetException getGadgetException() {
            return gadgetException;
        }

        public RequestContext(HttpRequest httpReq, HttpResponse httpResp, GadgetException ge) {
            this.httpReq = httpReq;
            this.httpResp = httpResp;
            this.gadgetException = ge;
        }
    }

    // Worker class responsible for fetching a single resource.
    public class HttpFetchCallable implements Callable<RequestContext> {

        private HttpRequest httpReq;

        public HttpFetchCallable(HttpRequest httpReq) {
            this.httpReq = httpReq;
        }

        public RequestContext call() {
            HttpResponse httpResp = null;
            GadgetException gEx = null;
            try {
                httpResp = requestPipeline.execute(httpReq);
            } catch (GadgetException ge) {
                gEx = ge;
            }
            return new RequestContext(httpReq, httpResp, gEx);
        }
    }

}
