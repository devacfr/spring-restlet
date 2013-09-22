package org.cfr.restlet.ext.spring;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.engine.http.HttpServerHelper;
import org.restlet.ext.servlet.ServerServlet;
import org.restlet.ext.servlet.ServletAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.ContextLoader;

@SuppressWarnings("serial")
public class RestletSpringApplicationContextServlet extends ServerServlet {

    public static final String RESTLET_APPLICATION_BEAN_NAME = "resletApplication";

    private Logger logger = null;

    private ContextLoader contextLoader;

    private ServletAdapter serverAdapter;

    @SuppressWarnings("unchecked")
    @Override
    protected Application createApplication(Context parentContext) {
        Application application = super.createApplication(parentContext);
        try {
            registerComponent(application, RESTLET_APPLICATION_BEAN_NAME + "." + getServletConfig().getServletName());

        } catch (Exception e) {
            logger.error("Error while creating Restlet Application", e);
            throw new RuntimeException(e);
        };

        application.setName(getServletConfig().getServletName());

        application.setContext(parentContext.createChildContext());

        // setting logger explicitly, to override the stupid logger put there by
        // ServletContextAdapter
        application.getContext()
                .setLogger(application.getClass()
                        .getName());

        final Context applicationContext = application.getContext();
        String initParam;

        // Copy all the Servlet component initialization parameters
        final javax.servlet.ServletConfig servletConfig = getServletConfig();

        for (final Enumeration<String> enum1 = servletConfig.getInitParameterNames(); enum1.hasMoreElements();) {
            initParam = enum1.nextElement();

            applicationContext.getParameters()
                    .add(initParam, servletConfig.getInitParameter(initParam));
        }

        // Copy all the Servlet application initialization parameters
        for (final Enumeration<String> enum1 = getServletContext().getInitParameterNames(); enum1.hasMoreElements();) {
            initParam = enum1.nextElement();

            applicationContext.getParameters()
                    .add(initParam, getServletContext().getInitParameter(initParam));
        }

        this.serverAdapter = new ServletAdapter(this.getServletContext(), application);
        if (application instanceof RestletSpringApplication) {
            RestletSpringApplication bridge = (RestletSpringApplication) application;
            bridge.doAfterCreateApplication();
        }
        return application;
    }

    @SuppressWarnings("unchecked")
    protected <T> T registerComponent(T bean, String name) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) ContextLoader.getCurrentWebApplicationContext()
                .getAutowireCapableBeanFactory();
        Class<T> clazz = (Class<T>) bean.getClass();
        String className = clazz.getCanonicalName();
        BeanDefinition beanDefinition = null;
        try {
            beanDefinition = BeanDefinitionReaderUtils.createBeanDefinition(null, className, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setAutowireCandidate(true);

        beanFactory.registerBeanDefinition(name, beanDefinition);
        beanFactory.registerSingleton(name, bean);

        beanFactory.configureBean(bean, name);

        return bean;
    }

    @Override
    protected HttpServerHelper createServer(HttpServletRequest request) {
        HttpServerHelper server = super.createServer(request);
        String uriPattern = this.getContextPath(request) + request.getServletPath();

        String serverName = null;
        try {
            serverName = InetAddress.getLocalHost()
                    .getCanonicalHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        if (serverName == null) {
            serverName = request.getLocalName();
        }

        String baseUrl = new StringBuilder().append(request.getScheme())
                .append("://")
                .append(serverName)
                .append(':')
                .append(request.getLocalPort())
                .toString();

        Application application = getApplication();
        if (application instanceof RestletSpringApplication) {
            RestletSpringApplication bridge = (RestletSpringApplication) application;
            bridge.doAfterCreateServer(server, baseUrl, uriPattern);
        }
        return server;
    }

    /**
     * Create the ContextLoader to use. Can be overridden in subclasses.
     *
     * @return the new ContextLoader
     */
    protected ContextLoader createContextLoader() {
        return new ContextLoader();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            SLF4JBridgeHandler.uninstall();
        } catch (Exception e) {
        }
        if (this.contextLoader != null) {
            this.contextLoader.closeWebApplicationContext(getServletContext());
        }
    }

    /**
     * Return the ContextLoader used by this servlet.
     *
     * @return the current ContextLoader
     */
    public ContextLoader getContextLoader() {
        return this.contextLoader;
    }

    @Override
    public String getInitParameter(String name, String defaultValue) {
        String prefixedName = getServletConfig().getServletName() + "." + name;

        String result = getServletConfig().getInitParameter(prefixedName);

        if (result == null) {
            result = getServletConfig().getServletContext()
                    .getInitParameter(prefixedName);
        }

        if (result == null && defaultValue != null) {
            result = getServletConfig().getServletName() + "." + defaultValue;
        }

        return result;
    }

    @Override
    public void init() throws ServletException {
        /*
         * Logging configuration.
         *
         * Routes all incoming JUL (Java Util Logging) records to the SLF4j API. To avoid logging several times the same
         * line, handlers of JUL root logger are removed.
         */
        java.util.logging.Logger rootLogger = LogManager.getLogManager()
                .getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        SLF4JBridgeHandler.install();

        logger = LoggerFactory.getLogger(RestletSpringApplicationContextServlet.class);
        //Log4jWebConfigurer.initLogging(getServletContext());
        if (ContextLoader.getCurrentWebApplicationContext() == null) {
            this.contextLoader = createContextLoader();
            // test context spring exist
            if (ContextLoader.getCurrentWebApplicationContext() != null)
                this.contextLoader.initWebApplicationContext(getServletContext());
        }
        // initialize application before Component
        Application.setCurrent(getApplication());
        super.init();
        Application.setCurrent(null);

    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServer(request);
        this.serverAdapter.service(request, response);
    }

}
