package org.cfr.restlet.ext.spring.representation;

import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.spring.representation.JsonRepresentation;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

public class JavascriptExecutionRepresentationTest extends EasyMockTestCase {

	@Test
	public void convertObjectToJson() {
		Foo foo = new Foo();
		foo.name = "foo";
		foo.code = "person";
		foo.value = 10;
		JsonRepresentation representation = new JsonRepresentation(null, MediaType.APPLICATION_JSON, foo);
		String text = representation.getText();
		assertNotNull(text);
		assertEquals("{\"code\":\"person\",\"name\":\"foo\",\"value\":10}", text);
		assertEquals(foo, representation.getObject());
	}

	@Test
	public void convertJsonToObject() {
		String text = "{\"code\":\"person\",\"name\":\"foo\",\"value\":10}";
		JsonRepresentation representation = new JsonRepresentation(null, new StringRepresentation(text), Foo.class);
		Foo foo = (Foo) representation.getObject();
		assertNotNull(foo);
		assertEquals(foo.name, "foo");
		assertEquals(foo.code, "person");
		assertEquals(foo.value, 10);
		assertEquals("{\"code\":\"person\",\"name\":\"foo\",\"value\":10}", representation.getText());

	}

	public static class Foo {

		public String name;

		public String code;

		public int value;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

	}
}
