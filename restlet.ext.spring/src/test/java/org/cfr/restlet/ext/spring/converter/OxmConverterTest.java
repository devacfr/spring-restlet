package org.cfr.restlet.ext.spring.converter;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.spring.ResourceRegister;
import org.cfr.restlet.ext.spring.converter.OxmConverter;
import org.cfr.restlet.ext.spring.representation.OxmRepresentation;
import org.joda.time.DateTime;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

import com.thoughtworks.xstream.converters.javabean.BeanProvider;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.mapper.Mapper;

public class OxmConverterTest extends EasyMockTestCase {

	private final float delta = 0.001f;

	private static Variant XML_VARIANT = new Variant(MediaType.APPLICATION_ALL_XML);

	private static Variant JSON_VARIANT = new Variant(MediaType.APPLICATION_JSON);

	@Test
	public void scoreJsonMediaType() throws Exception {
		OxmConverter converter = new OxmConverter();
		float score = converter.score(null, JSON_VARIANT, null);
		assertEquals(0.5f, score, delta);
	}

	@Test
	public void scoreXmlMediaType() throws Exception {
		OxmConverter converter = new OxmConverter();
		float score = converter.score(null, XML_VARIANT, null);
		assertEquals(0.8f, score, delta);
	}

	@Test
	public void scoreWithXmlRepresentation() throws Exception {
		OxmConverter converter = new OxmConverter();
		Representation representation = converter.create(MediaType.APPLICATION_JSON, null);
		float score = converter.score(representation, XML_VARIANT, null);
		assertEquals(1.0f, score, delta);
	}

	@Test
	public void scoreTargetClass() throws Exception {
		OxmConverter converter = new OxmConverter();
		Representation representation = converter.create(MediaType.APPLICATION_XML, null);
		float score = converter.score(representation, Object.class, null);
		assertEquals(0.8f, score, delta);
	}

	@Test
	public void scoreNullTargetClass() throws Exception {
		OxmConverter converter = new OxmConverter();
		Representation representation = converter.create(new StringRepresentation("", MediaType.APPLICATION_XML));
		float score = converter.score(representation, (Class<?>) null, null);
		assertEquals(0.5f, score, delta);
	}

	@Test
	public void getObjectClasses() throws Exception {
		OxmConverter converter = new OxmConverter();
		{
			List<Class<?>> l = converter.getObjectClasses(XML_VARIANT);
			assertEquals(2, l.size());
		}
		{
			List<Class<?>> l = converter.getObjectClasses(JSON_VARIANT);
			assertNull(l);
		}
	}

	@Test
	public void getVariants() throws Exception {
		OxmConverter converter = new OxmConverter();
		{
			List<VariantInfo> l = converter.getVariants(Object.class);
			assertEquals(3, l.size());
		}
		{
			List<VariantInfo> l = converter.getVariants(null);
			assertNull(l);
		}
	}

	@Test
	public void toObject() throws Exception {
		OxmConverter converter = new OxmConverter();
		ResourceRegister register = new ResourceRegister();
		register.addClass(Foo.class, "Foo");
		converter.registerObjects(register);
		String text = "<Foo>\n  <firstName>toto</firstName>\n  <lastName>Bernard</lastName>\n  <birthday>2010-10-10 00:00:00.0 CEST</birthday>\n</Foo>";
		Representation source = new StringRepresentation(text, MediaType.APPLICATION_XML);
		Foo foo = converter.toObject(source, Foo.class, null);
		assertNotNull(foo);
		assertEquals(foo, Foo.ok);
	}

	@Test
	public void toRepresentationJSON() throws Exception {
		OxmConverter converter = new OxmConverter();
		ResourceRegister register = new ResourceRegister();
		register.addClass(Foo.class, "Foo");
		converter.registerObjects(register);
		Representation representation = converter.toRepresentation(Foo.ok, XML_VARIANT, null);
		assertEquals(OxmRepresentation.class, representation.getClass());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registrationConverter() throws Exception {
		OxmConverter converter = new OxmConverter();
		converter.registerConverter(ObjectConverter.class);

		converter.registerConverter((Class) null);
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

	public static class ObjectConverter extends JavaBeanConverter {

		static class BeanProviderOverride extends BeanProvider {

			@Override
			protected boolean canStreamProperty(PropertyDescriptor descriptor) {
				return !OMITTED_FIELD.contains(descriptor.getName());
			}
		}

		public final static Collection<String> OMITTED_FIELD = Arrays.asList(new String[] { "objectContext",
				"dataContext", "objEntity", "objectId", "persistenceState", "snapshotVersion" });

		public ObjectConverter(Mapper mapper) {
			super(mapper, new BeanProviderOverride());
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean canConvert(Class type) {
			return false;
		}
	}
}
