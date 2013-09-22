package com.pmi.restlet.ext.xstream;

import java.util.List;

import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.xstream.XstreamConverter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import com.pmi.restlet.ext.xstream.impl.AnnotedCustomer;
import com.pmi.restlet.ext.xstream.impl.ICustomer;
import com.pmi.restlet.ext.xstream.impl.SimpleCustomer;

public class XStreamSerializationTest extends EasyMockTestCase {

	public static int portNumber = 8001;

	public static ICustomer persistence;

	private static final String uri = "http://localhost:" + portNumber + "/customers";

	private Component component;

	private XstreamConverter xstreamConverter = null;

	@Before
	public void startServer() throws Exception {
		persistence = null;
		component = new Component();

		// The status service is disabled by default.
		component.getStatusService().setEnabled(false);

		component.getServers().add(Protocol.HTTP, portNumber);
		Application application = new Application() {

			@Override
			public Restlet createInboundRoot() {
				Router router = new Router(getContext());

				// Defines a route for the resource "list of customers"
				router.attach("/customers", CustomerResource.class);
				// Defines a route for the resource customer
				router.attach("/customers/{customerId}", CustomerResource.class);
				return router;
			}
		};
		// Attach the sample application.
		component.getDefaultHost().attach(application);
		component.start();

		Engine engine = Engine.getInstance();
		List<ConverterHelper> converters = engine.getRegisteredConverters();
		for (ConverterHelper converterHelper : converters) {
			if (converterHelper instanceof XstreamConverter) {
				xstreamConverter = (XstreamConverter) converterHelper;
				xstreamConverter.registerObjects(AnnotedCustomer.class);
				return;
			}
		}
		throw new RuntimeException("Xstream extension restlet module is required.");
	}

	@After
	public void stopServer() throws Exception {
		component.stop();
	}

	@Test
	public void simpleRequestGetWrapper() throws Exception {
		ClientResource clientResource = new ClientResource(uri);
		TestResource testResource = clientResource.wrap(TestResource.class);

		testResource.store(SimpleCustomer.customer);
		assertEquals(SimpleCustomer.customer, testResource.retrieve());

	}

	@Test
	public void simpleRequestGet() throws Exception {

		ClientResource clientResource = new ClientResource(uri);
		clientResource.post(SimpleCustomer.customer);
		assertEquals(SimpleCustomer.customer, clientResource.get(SimpleCustomer.class));

	}

	@Test
	public void simpleRequestGetJsonRepresentation() throws Exception {

		ClientResource clientResource = new ClientResource(uri);
		clientResource.post(SimpleCustomer.customer);
		Representation representation = clientResource.get(MediaType.APPLICATION_JSON);
		String text = representation.getText();
		assertEquals(
				"{\"com.pmi.restlet.ext.xstream.impl.SimpleCustomer\":{\"firstName\":\"Bernard\",\"lastName\":\"Lefevre\",\"addresses\":[{\"string\":[\"Lyon\",\"Paris\",\"Marseille\"]}],\"modificationDate\":\"3910-11-10 00:00:00.0 CET\"}}",
				text);

	}

	@Test
	public void simpleRequestGetXmlRepresentation() throws Exception {

		ClientResource clientResource = new ClientResource(uri);
		clientResource.post(SimpleCustomer.customer);

		Representation representation = clientResource.get(MediaType.APPLICATION_XML);
		String text = representation.getText();
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<com.pmi.restlet.ext.xstream.impl.SimpleCustomer>\n  <firstName>Bernard</firstName>\n"
						+ "  <lastName>Lefevre</lastName>\n  <addresses>\n    <string>Lyon</string>\n    <string>Paris</string>\n"
						+ "    <string>Marseille</string>\n  </addresses>\n  <modificationDate>3910-11-10 00:00:00.0 CET</modificationDate>\n</com.pmi.restlet.ext.xstream.impl.SimpleCustomer>",
				text);

	}

	@Test
	public void annotateRequestGetWrapper() throws Exception {

		ClientResource clientResource = new ClientResource(uri);
		TestResource testResource = clientResource.wrap(TestResource.class);

		testResource.store(AnnotedCustomer.customer);
		ICustomer result = testResource.retrieve();
		assertEquals(AnnotedCustomer.customer, result);

	}

	@Test
	public void annotateRequestGet() throws Exception {

		ClientResource clientResource = new ClientResource(uri);
		clientResource.post(AnnotedCustomer.customer);
		assertEquals(AnnotedCustomer.customer, clientResource.get(SimpleCustomer.class));

	}

	@Test
	public void annotateRequestGetJsonRepresentation() throws Exception {

		ClientResource clientResource = new ClientResource(uri);
		clientResource.post(AnnotedCustomer.customer);
		Representation representation = clientResource.get(MediaType.APPLICATION_JSON);
		String text = representation.getText();
		assertEquals(
				"{\"customer\":{\"@firstName\":\"Bernard\",\"@lastName\":\"Lefevre\",\"city\":[\"Lyon\",\"Paris\",\"Marseille\"],\"modificationDate\":\"3910-03-10 00:00:00.0 CET\"}}",
				text);

	}

	@Test
	public void annotateRequestGetXmlRepresentation() throws Exception {

		ClientResource clientResource = new ClientResource(uri);
		clientResource.post(AnnotedCustomer.customer);
		Representation representation = clientResource.get(MediaType.APPLICATION_XML);
		String text = representation.getText();
		assertEquals(
				"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<customer firstName=\"Bernard\" lastName=\"Lefevre\">\n  <city>Lyon</city>\n  <city>Paris</city>\n  <city>Marseille</city>\n  <modificationDate>3910-03-10 00:00:00.0 CET</modificationDate>\n</customer>",
				text);

	}

	public void getSomeObjectClasses() {
		{
			// json
			List<Class<?>> list = xstreamConverter.getObjectClasses(new Variant(MediaType.APPLICATION_JSON));
			Assert.assertNotNull(list);
		}
		{
			// Xml
			List<Class<?>> list = xstreamConverter.getObjectClasses(new Variant(MediaType.TEXT_XML));
			Assert.assertNotNull(list);
		}
		{
			// otherthing
			List<Class<?>> list = xstreamConverter.getObjectClasses(new Variant(MediaType.APPLICATION_FLASH));
			Assert.assertNull(list);
		}
	}

	public interface TestResource {

		@Get
		public ICustomer retrieve();

		@Put
		public void store(ICustomer customer);

		@Post
		public void post(ICustomer customer) throws Exception;

		@Delete
		public void remove() throws Exception;

	}

	public static class CustomerResource extends ServerResource implements TestResource {

		public CustomerResource() {
		}

		@Override
		public ICustomer retrieve() {
			System.out.println("GET request received");
			return persistence;
		}

		@Override
		public void store(ICustomer customer) {
			System.out.println("PUT request received");
			persistence = customer;
		}

		@Override
		public void post(ICustomer customer) throws Exception {
			System.out.println("POST request received");
			persistence = customer;
		}

		@Override
		public void remove() throws Exception {
			System.out.println("DELETE request received");
			persistence = null;
		}

	}
}
