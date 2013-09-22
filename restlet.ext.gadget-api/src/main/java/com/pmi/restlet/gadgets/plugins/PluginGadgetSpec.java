package com.pmi.restlet.gadgets.plugins;

import static org.cfr.commons.util.Assert.notNull;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.cfr.commons.plugins.core.IPlugin;

import com.atlassian.util.concurrent.Assertions;

public final class PluginGadgetSpec {

	private final IPlugin plugin;

	private final String location;

	private final String moduleKey;

	private final Map params;

	public PluginGadgetSpec(IPlugin plugin, String moduleKey, String location, Map params) {
		this.plugin = notNull(plugin);
		this.moduleKey = notNull(moduleKey);
		this.location = notNull(location);
		this.params = unmodifiableCopy(notNull(params));
	}

	private Map unmodifiableCopy(Map map) {
		return Collections.unmodifiableMap(new HashMap(map));
	}

	public Key getKey() {
		return new Key(plugin.getKey(), location);
	}

	public String getModuleKey() {
		return moduleKey;
	}

	public String getPluginKey() {
		return plugin.getKey();
	}

	public String getLocation() {
		return location;
	}

	public InputStream getInputStream() {
		return plugin.getResourceAsStream(location);
	}

	public boolean isHostedExternally() {
		return location.startsWith("http://") || location.startsWith("https://");
	}

	public boolean hasParameter(String name) {
		return params.containsKey(name);
	}

	public String getParameter(String name) {
		return (String) params.get(name);
	}

	public Date getDateLoaded() {
		return plugin.getDateLoaded();
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = 31 * result + (location != null ? location.hashCode() : 0);
		result = 31 * result + (plugin != null ? plugin.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		} else {
			PluginGadgetSpec other = (PluginGadgetSpec) obj;
			return plugin.equals(other.plugin) && location.equals(other.location);
		}
	}

	@Override
	public String toString() {
		return new StringBuilder().append("PluginGadgetSpec{plugin=").append(plugin).append(", location='")
				.append(location).append('\'').append('}').toString();
	}

	public static final class Key {

		public String getPluginKey() {
			return pluginKey;
		}

		public String getLocation() {
			return location;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			} else {
				Key that = (Key) o;
				return location.equals(that.location) && pluginKey.equals(that.pluginKey);
			}
		}

		@Override
		public int hashCode() {
			return 31 * pluginKey.hashCode() + location.hashCode();
		}

		@Override
		public String toString() {
			return new StringBuilder().append("Key{pluginKey='").append(pluginKey).append('\'').append(", location='")
					.append(location).append('\'').append('}').toString();
		}

		private final String pluginKey;

		private final String location;

		public Key(String pluginKey, String location) {
			this.pluginKey = Assertions.notNull("pluginKey", pluginKey);
			this.location = Assertions.notNull("location", location);
		}
	}

}