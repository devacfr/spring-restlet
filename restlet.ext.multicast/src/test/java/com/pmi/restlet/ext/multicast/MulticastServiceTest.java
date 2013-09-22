package com.pmi.restlet.ext.multicast;

import org.cfr.commons.testing.EasyMockTestCase;
import org.junit.Test;
import org.restlet.Context;

public class MulticastServiceTest extends EasyMockTestCase {

	private MulticastService multicastService;

	private IMulticastRegister multicastRegister;

	private Context context;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		multicastService = new MulticastService();

		multicastRegister = mock(IMulticastRegister.class);
		multicastService.setRegister(multicastRegister);
		multicastService.setContext(context);
	}

	@Test
	public void startDisabledMulticastService() throws Exception {

	}
}
