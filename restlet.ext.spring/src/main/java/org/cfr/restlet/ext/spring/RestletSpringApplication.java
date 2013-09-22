package org.cfr.restlet.ext.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.cfr.commons.plugins.webresource.rest.FileServerResource;
import org.cfr.restlet.ext.spring.converter.IObjectRegistering;
import org.cfr.restlet.ext.spring.event.EventService;
import org.cfr.restlet.ext.spring.resource.WebAppDirectory;
import org.cfr.restlet.ext.spring.security.SpringSecurityGuard;
import org.cfr.restlet.ext.spring.service.ServiceHelper;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.application.Encoder;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.http.HttpServerHelper;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;

import com.pmi.restlet.Constants;
import com.pmi.restlet.response.extjs.DataSourceResponse;
import com.pmi.restlet.response.extjs.ErrorFormResponse;
import com.pmi.restlet.response.extjs.FieldMessage;
import com.pmi.restlet.response.extjs.FormResponse;

public class RestletSpringApplication extends Application implements InitializingBean, IFirstResponder,
		ApplicationContextAware {

	private final Logger logger = LoggerFactory.getLogger(RestletSpringApplication.class);

	private static final String ENABLE_ENCODER_KEY = "enable-restlet-encoder";

	private ApplicationContext applicationContext;

	private String baseUrl;

	private String contextPath;

	@Autowired(required = false)
	private List<IResource> resources;

	@Autowired(required = false)
	private List<IManagedResource> managedResources;

	/** Date of creation of this application */
	private final Date createdOn;

	/** The root that is changeable as-needed basis */
	private RetargetableRestlet root;

	/** The root */
	private Router rootRouter;

	/** The applicationRouter */
	private Router applicationRouter;

	private final ResourceRegister resourceRegister = new ResourceRegister();

	private Guard securityGuard;

	public Guard getSecurityGuard() {
		return securityGuard;
	}

	/**
	 * Constructor.
	 */
	public RestletSpringApplication() {
		this(null);
	}

	public String getContextPath() {
		return contextPath;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 */
	public RestletSpringApplication(Context context) {
		super(context);
		this.createdOn = new Date();
	}

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public EventService getEventService() {
		return this.getServices().get(EventService.class);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (resources != null) {
			// remove resource managed of simple resource
			for (Iterator<IResource> i = resources.iterator(); i.hasNext();) {
				IResource resource = i.next();
				if (resource instanceof IManagedResource) {
					i.remove();
				}

			}
		}
		ServiceHelper.register(this.getServices(), this.getApplicationContext());
	}

	/**
	 * 
	 * @param router
	 * @param strict
	 *            The matching mode to use when parsing a formatted reference.
	 * @param resource
	 */
	@Override
	public void attach(Router router, boolean strict, IResource resource) {
		attach(router, strict, resource.getResourceUri(), resource);
	}

	/**
	 * 
	 * @param router
	 * @param strict
	 *            The matching mode to use when parsing a formatted reference.
	 * @param uri
	 * @param resource
	 */
	@Override
	public void attach(Router router, boolean strict, String uri, IResource resource) {
		attach(router, strict, uri, new ResourceFinder(getContext(), resource));

		handleResourceSecurity(resource);
	}

	/**
	 * 
	 * @param router
	 * @param strict
	 *            The matching mode to use when parsing a formatted reference.
	 * @param uriPattern
	 * @param target
	 * @return
	 */
	@Override
	public TemplateRoute attach(Router router, boolean strict, String uriPattern, Restlet target) {
		if (logger.isDebugEnabled()) {
			logger.debug("Attaching Restlet of class '" + target.getClass().getName() + "' to URI='" + uriPattern
					+ "' (strict='" + strict + "')");
		}

		TemplateRoute route = router.attach(uriPattern, target);

		if (strict) {
			route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
		} else {
			route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
		}
		return route;
	}

	/**
	 * Creating all sort of shared tools and putting them into context, to make
	 * them usable by per-request instantaniated Resource implementors.
	 */
	protected final void configure() {
		doConfigure();
	}

	@Override
	public Restlet createInboundRoot() {
		if (root == null) {
			root = new RetargetableRestlet(getContext());
		}
		configure();

		recreateRoot(true);

		return root;
	}

	/**
	 * Method to be overridden by subclasses. It will be called only once in the
	 * lifetime of this Application. This is the place when you need to create
	 * and add to context some stuff.
	 */
	protected void doConfigure() {
		// empty implementation, left for subclasses to do something meaningful
	}

	protected void doAfterCreateApplication() {
		this.securityGuard = createSecurityGuard();
	}

	protected void doAfterCreateServer(HttpServerHelper server, String baseUrl, String contextPath) {
		this.contextPath = contextPath;
		this.baseUrl = baseUrl;
		getContext().getAttributes().put(Constants.CONTEXT_PATH_KEY, contextPath);
		getContext().getAttributes().put(Constants.BASE_URL_KEY, baseUrl);
	}

	/**
	 * Called when the app root needs to be created. Override it if you need
	 * "old way" to attach resources, or need to use the isStarted flag.
	 * 
	 * @param root
	 * @param isStarted
	 */
	protected void doCreateRoot(Router root, boolean isStarted) {
		if (!isStarted) {
			return;
		}
		// publish the WAR contents
		Directory rootDir = new WebAppDirectory(getContext(), "war:///");
		rootDir.setListingAllowed(false);
		rootDir.setModifiable(false);
		rootDir.setNegotiatingContent(true);

		attach(root, false, "/", rootDir);
		root.attach(FileServerResource.RESOURCE_PATH, FileServerResource.class, Template.MODE_STARTS_WITH);

		this.getConnectorService().setClientProtocols(
				Arrays.asList(Protocol.HTTP, Protocol.FILE, Protocol.WAR, Protocol.RIAP));
	}

	@Override
	public Router getApplicationRouter() {
		return applicationRouter;
	}

	/**
	 * Returns the timestamp of instantaniation of this object. This is used as
	 * timestamp for transient objects when they are still unchanged (not
	 * modified).
	 * 
	 * @return date
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	public List<IManagedResource> getManagedResources() {
		return managedResources;
	}

	@Override
	public ResourceRegister getResourceRegister() {
		return resourceRegister;
	}

	public List<IResource> getResources() {
		return resources;
	}

	@Override
	public Router getRootRouter() {
		return rootRouter;
	}

	protected void handleResourceSecurity(IResource resource) {
		PathProtectionDescriptor descriptor = resource.getResourceProtection();

		if (descriptor == null) {
			return;
		}

		// if ( PlexusMutableWebConfiguration.class.isAssignableFrom(
		// plexusWebConfiguration.getClass() ) )
		// {
		// try
		// {
		// ( (PlexusMutableWebConfiguration) plexusWebConfiguration
		// ).addProtectedResource( "/service/*"
		// + descriptor.getPathPattern(), descriptor.getFilterExpression() );
		// }
		// catch ( SecurityConfigurationException e )
		// {
		// throw new IllegalStateException(
		// "Could not configure JSecurity to protect resource mounted to "
		// + resource.getResourceUri() + " of class " +
		// resource.getClass().getName(), e );
		// }
		// }
	}

	/**
	 * Left for subclass to inject a "prefix" path. The automatically managed
	 * Resources will be attached under the router returned by this method.
	 * 
	 * @param root
	 * @return
	 */
	protected Router initializeRouter(Router root, boolean isStarted) {
		return root;
	}

	protected final void recreateRoot(boolean isStarted) {
		// reboot?
		if (root != null) {
			resourceRegister.clear();
			// create a new root router
			rootRouter = new Router(getContext());

			applicationRouter = initializeRouter(rootRouter, isStarted);

			// attach all Resources
			if (isStarted) {
				if (resources != null) {
					for (IResource resource : resources) {
						resource.register(resourceRegister);
						if (resource instanceof IRootResource) {
							attach(rootRouter, resource.isStrict(), resource);
						} else {
							attach(applicationRouter, resource.isStrict(), resource);
						}
					}
				}
			}

			if (isStarted) {
				if (managedResources != null) {
					for (IManagedResource resource : managedResources) {
						if (resource instanceof IResource) {
							((IResource) resource).register(resourceRegister);
							resource.attach(this, false);
						} else {
							resource.attach(this, false);
						}

					}
				}
			}

			doCreateRoot(rootRouter, isStarted);

			// check if we want to compress stuff
			boolean enableCompression = false;

			if (this.getContext().getAttributes().containsKey(ENABLE_ENCODER_KEY)
					&& Boolean.parseBoolean(this.getContext().getAttributes().get(ENABLE_ENCODER_KEY).toString())
					|| this.getMetadataService().getDefaultEncoding() != null) {
				enableCompression = true;
				getLogger().fine("Restlet Encoder will compress output.");
			}

			// encoding support
			ArrayList<MediaType> ignoredMediaTypes = new ArrayList<MediaType>(Encoder.getDefaultIgnoredMediaTypes());
			ignoredMediaTypes.add(MediaType.APPLICATION_COMPRESS); // anything
																	// compressed
			ignoredMediaTypes.add(new MediaType("application/x-bzip2"));
			ignoredMediaTypes.add(new MediaType("application/x-bzip"));
			ignoredMediaTypes.add(new MediaType("application/x-compressed"));
			ignoredMediaTypes.add(new MediaType("application/x-shockwave-flash"));

			Encoder encoder = new Encoder(getContext(), false, enableCompression, Encoder.ENCODE_ALL_SIZES,
					Encoder.getDefaultAcceptedMediaTypes(), ignoredMediaTypes);

			encoder.setNext(rootRouter);

			if (getSecurityGuard() != null) {
				root.setNext(getSecurityGuard());
				getSecurityGuard().setNext(encoder);
			} else {
				root.setNext(encoder);
			}

			doConfigureConverter();

		}
	}

	public void setManagedResources(List<IManagedResource> managedResources) {
		this.managedResources = managedResources;
	}

	public void setResources(List<IResource> resources) {
		this.resources = resources;
	}

	protected void doConfigureConverter() {
		resourceRegister.addClass(DataSourceResponse.class, "datasourceResponse");
		resourceRegister.addClass(FormResponse.class, "formResponse");
		resourceRegister.addClass(ErrorFormResponse.class, "message");
		resourceRegister.addClass(FieldMessage.class, "field");

		Engine engine = Engine.getInstance();
		List<ConverterHelper> converters = engine.getRegisteredConverters();
		for (ConverterHelper converterHelper : converters) {
			if (converterHelper instanceof IObjectRegistering) {
				IObjectRegistering registring = (IObjectRegistering) converterHelper;
				try {
					registring.registerObjects(resourceRegister);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	protected Guard createSecurityGuard() {
		ApplicationContext context = getApplicationContext();
		AccessDecisionManager accessDecisionManager = context.getBean(AccessDecisionManager.class);
		WebInvocationPrivilegeEvaluator invocationPrivilegeEvaluator = context
				.getBean(WebInvocationPrivilegeEvaluator.class);
		if (accessDecisionManager == null || invocationPrivilegeEvaluator == null) {
			return null;
		}
		SpringSecurityGuard springSecurityGuard = new SpringSecurityGuard();
		springSecurityGuard.setApplicationContext(context);
		springSecurityGuard.setAccessDecisionManager(accessDecisionManager);
		springSecurityGuard.setInvocationPrivilegeEvaluator(invocationPrivilegeEvaluator);
		try {
			springSecurityGuard.afterPropertiesSet();
		} catch (Exception e) {
			throw new RuntimeException("Error initialization security Guard", e);
		}
		return springSecurityGuard;

	}

}
