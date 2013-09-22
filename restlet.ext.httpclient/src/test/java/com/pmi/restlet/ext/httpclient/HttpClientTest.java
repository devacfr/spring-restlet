package com.pmi.restlet.ext.httpclient;

import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class HttpClientTest extends Assert {

	protected Proxy chooseProxy(List<Proxy> proxies) {
		if (proxies == null || proxies.isEmpty()) {
			throw new IllegalArgumentException("Proxy list must not be empty.");
		}
		Proxy result = null;
		// check the list for one we can use
		for (int i = 0; result == null && i < proxies.size(); i++) {
			Proxy p = proxies.get(i);
			switch (p.type()) {

			case DIRECT:
			case HTTP:
				result = p;
				break;

			case SOCKS:
				// SOCKS hosts are not handled on the route level.
				// The socket may make use of the SOCKS host though.
				break;
			}
		}
		if (result == null) {
			// @@@ log as warning or info that only a socks proxy is available?
			// result can only be null if all proxies are socks proxies
			// socks proxies are not handled on the route planning level
			result = Proxy.NO_PROXY;
		}

		return result;
	}

	@Test
	public void proxySelector() {
		ProxySelector selector = ProxySelector.getDefault();
		System.setProperty("http.proxyHost", "chlaubc.obs.pmi");
		System.setProperty("http.proxyPort", "8000");
		System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|*.app.pmi");
		{
			List<Proxy> proxies = selector.select(URI.create("http://google.com"));
			Proxy proxy = chooseProxy(proxies);
			Assert.assertEquals(Proxy.Type.HTTP, proxy.type());
		}
		{
			List<Proxy> proxies = selector.select(URI.create("http://integration.app.pmi"));
			Proxy proxy = chooseProxy(proxies);
			Assert.assertEquals(Proxy.Type.DIRECT, proxy.type());
		}
		System.getProperties().remove("http.proxyHost");
		System.getProperties().remove("http.proxyPort");
		System.getProperties().remove("http.nonProxyHosts");
		{
			List<Proxy> proxies = selector.select(URI.create("http://google.com"));
			Proxy proxy = chooseProxy(proxies);
			Assert.assertEquals(Proxy.NO_PROXY, proxy);
		}
		System.setProperty("http.proxyHost", "chlaubc.obs.pmi");
		System.setProperty("http.proxyPort", "8000");
		System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|*.app.pmi");
		{
			List<Proxy> proxies = selector.select(URI.create("http://localhost:8080/kbase-portal/test"));
			Proxy proxy = chooseProxy(proxies);
			Assert.assertEquals(Proxy.Type.DIRECT, proxy.type());
		}
	}
}
