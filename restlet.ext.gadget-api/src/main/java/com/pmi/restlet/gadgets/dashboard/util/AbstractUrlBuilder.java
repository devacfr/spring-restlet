package com.pmi.restlet.gadgets.dashboard.util;

import org.cfr.commons.plugins.webresource.IWebResourceManager;
import org.cfr.commons.sal.IGlobalApplicationProperties;

public abstract class AbstractUrlBuilder implements IUrlBuilder {

	protected final IWebResourceManager webResourceManager;

	protected final IGlobalApplicationProperties applicationProperties;

	private final String pluginModuleKey;

	public AbstractUrlBuilder(IGlobalApplicationProperties applicationProperties,
			IWebResourceManager webResourceManager, String pluginModuleKey) {
		this.applicationProperties = applicationProperties;
		this.webResourceManager = webResourceManager;
		this.pluginModuleKey = pluginModuleKey;
	}

	@Override
	public String buildImageUrl(String path) {
		if (!path.startsWith("/")) {
			path = new StringBuilder().append("/").append(path).toString();
		}
		return new StringBuilder().append(getBaseImageUrl()).append(path).toString();
	}

	@Override
	public String buildRpcJsUrl() {
		return new StringBuilder().append(getBaseUrl()).append("/js/rpc.js?c=1&debug=1").toString();
	}

	protected String getBaseUrl() {
		return new StringBuilder().append(applicationProperties.getBaseUrl()).toString();
	}

	private String getBaseImageUrl() {
		return webResourceManager.getStaticPluginResource(pluginModuleKey, "images/");
	}

}