package com.pmi.restlet.mapping;

import java.util.Arrays;

import org.cfr.commons.testing.EasyMockTestCase;
import org.hamcrest.core.IsNot;
import org.junit.Test;

public class ClassDescriptorTest extends EasyMockTestCase {

	@Test(expected = NullPointerException.class)
	public void defaultConstructorWithNullParameter() {
		new ClassDescriptor(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void defaultConstructorWithNullClassParameter() {
		new ClassDescriptor(null, "");
	}

	@Test()
	public void defaultConstructor() {
		new ClassDescriptor(Object.class, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addExcludeFieldWithNullFieldName() {
		ClassDescriptor classDescriptor = new ClassDescriptor(Object.class);
		classDescriptor.addExcludeField(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addExcludeFieldWithEmptyFieldName() {
		ClassDescriptor classDescriptor = new ClassDescriptor(Object.class);
		classDescriptor.addExcludeField("");
	}

	@Test()
	public void addExcludeField() {
		ClassDescriptor classDescriptor = new ClassDescriptor(Object.class);
		classDescriptor.addExcludeField("name");
		assertEquals(1, classDescriptor.getExcludesFields().size());
		classDescriptor.addExcludeFields(Arrays.asList("name", "method"));
		assertEquals(2, classDescriptor.getExcludesFields().size());
		classDescriptor.addExcludeFields("name", "method", "equalsTo");
		assertEquals(3, classDescriptor.getExcludesFields().size());
	}

	@Test
	public void equalsTo() {
		ClassDescriptor classObj = new ClassDescriptor(Object.class);

		ClassDescriptor classString = new ClassDescriptor(String.class);

		assertThat(classObj, IsNot.not(classString));

		assertFalse(classObj.equals(new Object()));

		assertEquals(classObj, new ClassDescriptor(Object.class));
	}
}
