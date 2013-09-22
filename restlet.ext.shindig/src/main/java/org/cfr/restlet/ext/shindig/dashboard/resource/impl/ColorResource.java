package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import java.util.Arrays;
import java.util.Map;

import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.shindig.dashboard.resource.IChangeGadgetColorHandler;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.IGadgetRequestContextFactory;
import com.pmi.restlet.gadgets.dashboard.Color;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardPermissionService;
import com.pmi.restlet.security.AnonymousAllowed;

@Resource(path = "/{dashboardId}/gadget/{gadgetId}/color", strict = true)
public class ColorResource extends InjectedResource {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private IGadgetRequestContextFactory gadgetRequestContextFactory;

	private IChangeGadgetColorHandler changeGadgetColorHandler;

	private IDashboardPermissionService permissionService;

	private I18nResolver i18n;

	@Inject
	public void setPermissionService(IDashboardPermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@Inject
	public void setGadgetRequestContextFactory(IGadgetRequestContextFactory gadgetRequestContextFactory) {
		this.gadgetRequestContextFactory = gadgetRequestContextFactory;
	}

	@Inject
	public void setChangeGadgetColorHandler(IChangeGadgetColorHandler changeGadgetColorHandler) {
		this.changeGadgetColorHandler = changeGadgetColorHandler;
	}

	@Inject
	public void setI18n(I18nResolver i18n) {
		this.i18n = i18n;
	}

	/**
	 * Forwards POST requests (coming from Ajax or web browsers) to the PUT
	 * handler for color changing.
	 * 
	 * @param method
	 *            the HTTP method to forward to (must be "put")
	 * @param dashboardId
	 *            ID of the dashboard hosting the gadget
	 * @param gadgetId
	 *            ID of the gadget which will change color
	 * @param colorParam
	 *            new color of the gadget
	 * @param request
	 *            the request object (used for providing the locale)
	 * @return a {@code Response} with details on the request's success or
	 *         failure
	 */
	@Post
	@AnonymousAllowed
	public void changeGadgetColorViaPOST() {
		Request request = getRequest();
		Response response = getResponse();
		Form form = request.getEntityAsForm();
		String method = form.getFirstValue("method");
		if (method.equals("put")) {
			log.debug("GadgetResource: POST /color delegated to PUT");
			changeGadgetColor();
		} else {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
	}

	/**
	 * Changes the specified gadget's color in response to a PUT request.
	 * 
	 * @param dashboardId
	 *            ID of the dashboard hosting the gadget
	 * @param gadgetId
	 *            ID of the gadget which will change color
	 * @param colorParam
	 *            new color of the gadget
	 * @param request
	 *            the request object (used for providing the locale)
	 * @return a {@code Response} with details on the request's success or
	 *         failure
	 */
	@Put
	@AnonymousAllowed
	public void changeGadgetColor() {
		Request request = getRequest();
		Response response = getResponse();
		Map<String, Object> attrs = request.getAttributes();
		String colorParam = attrs.get("color").toString();
		DashboardId dashboardId = DashboardId.valueOf(attrs.get("dashboardId"));
		GadgetId gadgetId = GadgetId.valueOf(attrs.get("gadgetId"));

		final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

		if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer())) {
			log.warn("GadgetResource: PUT: prevented gadget color change due to insufficient permission");
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}

		Color color;
		try {
			color = Color.valueOf(colorParam);
		} catch (IllegalArgumentException e) {
			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("gadgetResource.invalid.color", colorParam, Arrays.toString(Color.values())));
			return;
		}
		log.debug("GadgetResource: PUT /color: dashboardId=" + dashboardId + " gadgetId=" + gadgetId + " color="
				+ color);

		changeGadgetColorHandler.setGadgetColor(dashboardId, requestContext, gadgetId, color, response);
	}

}
