package org.cfr.restlet.ext.spring.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.Engine;
import org.restlet.engine.io.IoUtils;
import org.restlet.service.Service;
import org.restlet.util.ServiceList;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

public class ServiceHelper {

    public static final String DESCRIPTOR = "META-INF/services";

    public static final String DESCRIPTOR_SERVICE = "org.restlet.service.Service";

    public static final String DESCRIPTOR_SERVICE_PATH = DESCRIPTOR + "/" + DESCRIPTOR_SERVICE;

    /**
     * Registers a new service helper.
     * @return The registered service helper.
     */
    public static synchronized void register(ServiceList serviceList, ApplicationContext applicationContext) {
        Engine engine = Engine.getInstance();
        try {
            registerService(applicationContext, engine.getClassLoader(), DESCRIPTOR_SERVICE_PATH, serviceList, null);
        } catch (IOException e) {
            Context.getCurrentLogger()
                    .log(Level.WARNING, "An error occured while discovering the Services", e);
        }

    }

    public static void registerService(ApplicationContext applicationContext,
                                       ClassLoader classLoader,
                                       String descriptorPath,
                                       ServiceList services,
                                       Class<?> constructorClass) throws IOException {
        Enumeration<java.net.URL> configUrls = classLoader.getResources(descriptorPath);

        if (configUrls != null) {
            for (final Enumeration<java.net.URL> configEnum = configUrls; configEnum.hasMoreElements();) {
                registerServices(applicationContext, classLoader, configEnum.nextElement(), services, constructorClass);
            }
        }
    }

    /**
     * Registers a service.
     * 
     * @param classLoader
     *            The classloader to use.
     * @param configUrl
     *            Configuration URL to parse
     * @param services
     *            The list of services to update.
     * @param constructorClass
     *            The constructor parameter class to look for.
     */
    protected static void registerServices(ApplicationContext applicationContext,
                                           ClassLoader classLoader,
                                           java.net.URL configUrl,
                                           ServiceList services,
                                           Class<?> constructorClass) {
        try {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(configUrl.openStream(), "utf-8"), IoUtils.getBufferSize());
                String line = reader.readLine();

                while (line != null) {
                    registerServiceByProvider(applicationContext, classLoader, getProviderClassName(line), services, constructorClass);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                Context.getCurrentLogger()
                        .log(Level.SEVERE, "Unable to read the provider descriptor: " + configUrl.toString());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ioe) {
            Context.getCurrentLogger()
                    .log(Level.SEVERE, "Exception while detecting the helpers.", ioe);
        }
    }

    @SuppressWarnings("unchecked")
    protected static void registerServiceByProvider(ApplicationContext applicationContext,
                                                    ClassLoader classLoader,
                                                    String provider,
                                                    ServiceList services,
                                                    Class<?> constructorClass) {
        if ((provider != null) && (!provider.equals(""))) {
            // Instantiate the factory
            try {
                Class<Service> providerClass = (Class<Service>) classLoader.loadClass(provider);
                Service service = null;
                if (constructorClass == null) {
                    service = providerClass.newInstance();
                } else {
                    service = providerClass.getConstructor(constructorClass)
                            .newInstance(constructorClass.cast(null));
                }
                if (service != null) {
                    AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
                    beanFactory.autowireBean(service);
                    services.add(service);
                }
            } catch (Throwable t) {
                Context.getCurrentLogger()
                        .log(Level.INFO, "Unable to register the helper " + provider, t);
            }
        }
    }

    /**
     * Parses a line to extract the provider class name.
     * 
     * @param line
     *            The line to parse.
     * @return The provider's class name or an empty string.
     */
    private static String getProviderClassName(String line) {
        final int index = line.indexOf('#');
        if (index != -1) {
            line = line.substring(0, index);
        }
        return line.trim();
    }

}
