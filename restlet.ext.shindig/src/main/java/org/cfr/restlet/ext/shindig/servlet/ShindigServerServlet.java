package org.cfr.restlet.ext.shindig.servlet;

import javax.servlet.http.HttpServletRequest;

import org.cfr.restlet.ext.shindig.ShindigApplication;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.engine.http.HttpServerHelper;
import org.restlet.ext.servlet.ServerServlet;

import com.pmi.restlet.Constants;

public class ShindigServerServlet extends ServerServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 2146101779270651145L;

    /**
     * Creates the single Application used by this Servlet.
     * 
     * @param parentContext
     *            The parent component context.
     * 
     * @return The newly created Application or null if unable to create
     */
    @Override
    protected Application createApplication(Context parentContext) {
        ShindigApplication application = new ShindigApplication(parentContext.createChildContext());
        return application;
    }

    @Override
    public ShindigApplication getApplication() {
        return (ShindigApplication) super.getApplication();
    }

    @Override
    protected HttpServerHelper createServer(HttpServletRequest request) {
        HttpServerHelper helper = super.createServer(request);
        String uriPattern = this.getContextPath(request) + request.getServletPath();
        String baseUrl = new StringBuilder().append(request.getScheme()).append("://").append(request.getLocalName()).append(':').append(
                request.getLocalPort()).toString();
        getComponent().getContext().getAttributes().put(Constants.CONTEXT_PATH_KEY, uriPattern);
        getComponent().getContext().getAttributes().put(Constants.BASE_URL_KEY, baseUrl);
        return helper;
    }

}
