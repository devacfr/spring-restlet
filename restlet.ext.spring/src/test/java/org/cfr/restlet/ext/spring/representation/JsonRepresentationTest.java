package org.cfr.restlet.ext.spring.representation;

import org.cfr.commons.testing.EasyMockTestCase;
import org.cfr.restlet.ext.spring.representation.JavascriptExecutionRepresentation;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;

public class JsonRepresentationTest extends EasyMockTestCase {

	@Test
	public void createJavascriptCommand() {
		JavascriptExecutionRepresentation representation = new JavascriptExecutionRepresentation("foo", "toto",
				new StringRepresentation("representation"));
		String command = representation.getText();
		assertNotNull(command);
		assertEquals("foo('toto','representation')", command);
	}

	@Test
	public void createJavascriptCommandWithJsonRepresentation() {
		JavascriptExecutionRepresentation representation = new JavascriptExecutionRepresentation("foo", "toto",
				new StringRepresentation("{name:'toto',done:false}", MediaType.APPLICATION_JSON));
		String command = representation.getText();
		assertNotNull(command);
		assertEquals("foo('toto',{name:&quot;toto&quot;,done:false})", command);
	}

	@Test
	public void createJavascriptCommandWithQuote() {
		JavascriptExecutionRepresentation representation = new JavascriptExecutionRepresentation("foo", "toto'\"",
				new StringRepresentation("representation"));
		String command = representation.getText();
		assertNotNull(command);
		assertEquals("foo('toto&quot;\"','representation')", command);
	}

	@Test
	public void createJavascriptCommandWithLineBreak() {
		JavascriptExecutionRepresentation representation = new JavascriptExecutionRepresentation("foo", "toto\n\r",
				new StringRepresentation("representation"));
		String command = representation.getText();
		assertNotNull(command);
		assertEquals("foo('toto','representation')", command);
	}
}
