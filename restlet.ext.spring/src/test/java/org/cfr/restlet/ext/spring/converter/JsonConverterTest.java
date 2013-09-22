package org.cfr.restlet.ext.spring.converter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateFormatUtils;
import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.spring.ResourceRegister;
import org.cfr.restlet.ext.spring.converter.JsonConverter;
import org.cfr.restlet.ext.spring.representation.JsonRepresentation;
import org.joda.time.DateTime;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;


public class JsonConverterTest extends EasyMockTestCase {

	private final float delta = 0.001f;

	private static Variant XML_VARIANT = new Variant(MediaType.APPLICATION_ALL_XML);

	private static Variant JSON_VARIANT = new Variant(MediaType.APPLICATION_JSON);

	@Test
	public void scoreJsonMediaType() {
		JsonConverter converter = new JsonConverter();
		float score = converter.score(null, JSON_VARIANT, null);
		assertEquals(0.8f, score, delta);
	}

	@Test
	public void scoreXmlMediaType() {
		JsonConverter converter = new JsonConverter();
		float score = converter.score(null, XML_VARIANT, null);
		assertEquals(0.5f, score, delta);
	}

	@Test
	public void scoreWithJsonRepresentation() {
		JsonConverter converter = new JsonConverter();
		Representation representation = converter.create(MediaType.APPLICATION_JSON, null);
		float score = converter.score(representation, XML_VARIANT, null);
		assertEquals(1.0f, score, delta);
	}

	@Test
	public void scoreTargetClass() {
		JsonConverter converter = new JsonConverter();
		Representation representation = converter.create(MediaType.APPLICATION_JSON, null);
		float score = converter.score(representation, Object.class, null);
		assertEquals(0.8f, score, delta);
	}

	@Test
	public void scoreNullTargetClass() {
		JsonConverter converter = new JsonConverter();
		Representation representation = converter.create(new StringRepresentation("", MediaType.APPLICATION_JSON),
				(Class<?>) null);
		float score = converter.score(representation, (Class<?>) null, null);
		assertEquals(-1.0f, score, delta);
	}

	@Test
	public void getObjectClasses() {
		JsonConverter converter = new JsonConverter();
		{
			List<Class<?>> l = converter.getObjectClasses(JSON_VARIANT);
			assertEquals(2, l.size());
		}
		{
			List<Class<?>> l = converter.getObjectClasses(XML_VARIANT);
			assertNull(l);
		}
	}

	@Test
	public void getVariants() {
		JsonConverter converter = new JsonConverter();
		{
			List<VariantInfo> l = converter.getVariants(Object.class);
			assertEquals(1, l.size());
		}
		{
			List<VariantInfo> l = converter.getVariants(null);
			assertNull(l);
		}
	}

	@Test
	public void toObject() throws IOException {
		JsonConverter converter = new JsonConverter();
		ResourceRegister register = new ResourceRegister();
		register.addClass(Foo.class, "Foo");
		converter.registerObjects(register);
		String birthday = DateFormatUtils.ISO_DATETIME_FORMAT.format(Foo.ok.getBirthday());
		String text = "{\"birthday\":\"" + birthday + "\",\"firstName\":\"toto\",\"lastName\":\"Bernard\"}";
		Representation source = new StringRepresentation(text, MediaType.APPLICATION_JSON);
		Foo foo = converter.toObject(source, Foo.class, null);
		assertNotNull(foo);
		assertEquals(foo, Foo.ok);
	}

	@Test
	public void toRepresentationJSON() {
		JsonConverter converter = new JsonConverter();
		Representation representation = converter.toRepresentation(Foo.ok, JSON_VARIANT, null);
		assertEquals(JsonRepresentation.class, representation.getClass());
	}

	@Test
	public void toRepresentationJSONWithExclusionField() throws IOException {
		JsonConverter converter = new JsonConverter();
		ResourceRegister register = new ResourceRegister();
		register.addClass(Foo.class, "Foo").addExcludeFields("firstName");

		converter.registerObjects(register);
		String birthday = DateFormatUtils.ISO_DATETIME_FORMAT.format(Foo.ok.getBirthday());
		String text = "{\"birthday\":\"" + birthday + "\",\"lastName\":\"Bernard\"}";

		Representation representation = converter.toRepresentation(Foo.ok, JSON_VARIANT, null);
		assertEquals(JsonRepresentation.class, representation.getClass());
		assertEquals(text, representation.getText());
	}

	public static class Foo {

		private String firstName;

		private String lastName;

		private Date birthday;

		public static Foo ok = new Foo("toto", "Bernard", new DateTime(2010, 10, 10, 0, 0, 0, 0).toDate());

		public Foo() {
		}

		public Foo(String firstName, String lastName, Date birthday) {
			super();
			this.firstName = firstName;
			this.lastName = lastName;
			this.birthday = birthday;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public Date getBirthday() {
			return birthday;
		}

		public void setBirthday(Date birthday) {
			this.birthday = birthday;
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

	}
}
