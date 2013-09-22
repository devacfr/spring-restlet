package org.cfr.restlet.ext.spring.plugin;

import org.cfr.commons.plugins.core.IPlugin;
import org.cfr.commons.plugins.core.PluginParseException;
import org.cfr.commons.plugins.core.descriptors.AbstractModuleDescriptor;
import org.cfr.commons.plugins.core.descriptors.RequiresRestart;
import org.cfr.commons.plugins.core.module.IModuleFactory;
import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.spring.RestletResource;
import org.dom4j.Element;


/**
 * The portlet plugin allows end users to write plugins.
 */
@RequiresRestart
public class RestletResourceModuleDescriptor extends AbstractModuleDescriptor<RestletResource> {

	private I18nResolver resolver;

	public RestletResourceModuleDescriptor(final IModuleFactory moduleFactory) {
		super(moduleFactory);
	}

	@Override
	public void init(final IPlugin plugin, final Element element) throws PluginParseException {
		super.init(plugin, element);

		// final Element labelEl = element.element("label");
		// if (labelEl != null) {
		// if (labelEl.attribute("key") != null) {
		// labelKey = labelEl.attributeValue("key");
		// } else {
		// label = labelEl.getTextTrim();
		// }
		// }
		//
		// final Element thumbnailEl = element.element("thumbnail");
		// final List<ResourceDescriptor> descriptors = new
		// ArrayList<ResourceDescriptor>(resources.getResourceDescriptors());
		// descriptors.addAll(createResourceDescriptorsForThumbnail(thumbnailEl));
		// resources = new Resources(descriptors);
		// thumbnail = (thumbnailEl == null ? "" : thumbnailEl.getTextTrim());
		//
		// if (element.attribute("lazy") != null) {
		// lazyLoad = "true".equalsIgnoreCase(element.attribute("lazy")
		// .getText());
		// }

	}

	@Override
	public RestletResource getModule() {
		throw new UnsupportedOperationException("There is no module for Restlet Resources");
	}

	public I18nResolver getI18nBean() {
		return resolver;
	}

}