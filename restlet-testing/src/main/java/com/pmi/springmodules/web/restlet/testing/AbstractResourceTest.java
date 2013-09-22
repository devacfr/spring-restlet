package com.pmi.springmodules.web.restlet.testing;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.spring.AbstractResource;
import org.cfr.restlet.ext.spring.IManagedResource;
import org.cfr.restlet.ext.spring.IResource;
import org.cfr.restlet.ext.spring.IRootResource;
import org.cfr.restlet.ext.spring.RestletSpringApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.pmi.restlet.Constants;

public abstract class AbstractResourceTest extends EasyMockTestCase {

	public static int portNumber = 8001;

	protected Reference hostReference = new Reference("http://localhost:" + portNumber);

	private static Component component;

	static {
		try {
			DOMConfigurator.configure(ResourceUtils.getURL("classpath:log4j.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		SLF4JBridgeHandler.install();
	}

	@BeforeClass
	public static void startServer() throws Exception {
		// Creating restlet component
		component = new Component();

		// The status service is disabled by default.
		component.getStatusService().setEnabled(false);

		component.getClients().add(Protocol.HTTP);
		component.getClients().add(Protocol.FILE);
		component.getClients().add(Protocol.WAR);
		component.getClients().add(Protocol.RIAP);

		component.getServers().add(Protocol.HTTP, portNumber);

		component.start();
	}

	@AfterClass
	public static void stopServer() throws Exception {
		try {
			component.stop();
		} catch (Exception ex) {
			// noop
		}
	}

	protected abstract RestletSpringApplication createApplication();

	protected Restlet start(Object resource) throws Exception {

		RestletSpringApplication application = createApplication();

		List<IResource> rsrc = new ArrayList<IResource>();
		if (resource instanceof IResource) {
			rsrc.add((IResource) resource);
		}
		application.setResources(rsrc);

		// Define resource
		List<IManagedResource> resources = new ArrayList<IManagedResource>();
		if (resource instanceof IManagedResource) {
			resources.add((IManagedResource) resource);
		}
		application.setManagedResources(resources);

		application.afterPropertiesSet();

		component.getDefaultHost().attach(application);
		application.getContext().getAttributes().put(Constants.CONTEXT_PATH_KEY, hostReference.toString());
		application.getContext().getAttributes().put(Constants.BASE_URL_KEY, "");
		return application;
	}

	public <T> T execute(IResource resource, ICallBack<T> callBack) throws Exception {
		Restlet rsrc = start(resource);
		try {
			return callBack.execute();
		} finally {
			stop(rsrc);
		}
	}

	public <T> T execute(Restlet resource, ICallBack<T> callBack) throws Exception {
		Restlet rsrc = start(resource);
		try {
			return callBack.execute();
		} finally {
			stop(rsrc);
		}
	}

	private static Reference getResource(AbstractResource resource, Reference hostReference)
			throws MalformedURLException {

		return new Reference(hostReference, hostReference.toString() + resource.getResourceUri());
	}

	public Representation get(final AbstractResource resource) throws Exception {
		return get(resource, null);
	}

	public Representation get(final AbstractResource resource, final String query) throws Exception {

		return execute(resource, new ICallBack<Representation>() {

			@Override
			public Representation execute() throws Exception {
				Reference ref = getResource(resource, hostReference);

				if (StringUtils.hasText(query)) {
					ref.setQuery(query);
				}
				Request request = new Request(Method.GET, ref);
				request.setHostRef(hostReference);
				Response response = component.getContext().getServerDispatcher().handle(request);
				return response.getEntity();
			}

		});
	}

	public Response handle(final Restlet resource, final Request request) throws Exception {
		String path = null;
		Reference ref = request.getResourceRef();
		path = this.hostReference.toString() + ref;

		ref.setPath(path);

		return execute(resource, new ICallBack<Response>() {

			@Override
			public Response execute() throws Exception {
				request.setHostRef(hostReference);
				Response response = component.getContext().getServerDispatcher().handle(request);
				return response;
			}

		});
	}

	public Response handle(final AbstractResource resource, final Request request) throws Exception {
		String path = null;
		Reference ref = request.getResourceRef();
		if (!(resource instanceof IManagedResource) && !(resource instanceof IRootResource)
				&& request.getResourceRef().isRelative()) {
			path = this.hostReference.toString() + ref;
		} else {
			path = hostReference.toString() + resource.getResourceUri();
		}
		ref.setPath(path);

		return execute(resource, new ICallBack<Response>() {

			@Override
			public Response execute() throws Exception {
				request.setHostRef(hostReference);
				Response response = component.getContext().getServerDispatcher().handle(request);
				return response;
			}

		});
	}

	protected void stop(Restlet restlet) throws Exception {
		component.getDefaultHost().detach(restlet);
	}

	public static interface ICallBack<T> {

		T execute() throws Exception;
	}

}
