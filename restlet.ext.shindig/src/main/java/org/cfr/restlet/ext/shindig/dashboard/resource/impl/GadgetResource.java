package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import java.util.Map;

import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.shindig.dashboard.resource.IAddGadgetHandler;
import org.cfr.restlet.ext.shindig.dashboard.resource.IDeleteGadgetHandler;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;
import com.pmi.restlet.gadgets.GadgetId;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.IGadgetRequestContextFactory;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardNotFoundException;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardPermissionService;
import com.pmi.restlet.gadgets.representations.GadgetRepresentation;
import com.pmi.restlet.gadgets.representations.IRepresentationFactory;
import com.pmi.restlet.security.AnonymousAllowed;

/**
 * Provides REST endpoints for manipulating a Gadget.
 */
@Resource(path = "/{dashboardId}/gadget/{gadgetId}", strict = false)
public class GadgetResource extends InjectedResource {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private IDashboardPermissionService permissionService;

	private IDashboardRepository repository;

	private IGadgetRequestContextFactory gadgetRequestContextFactory;

	private IAddGadgetHandler addGadgetHandler;

	private IDeleteGadgetHandler deleteGadgetHandler;

	private I18nResolver i18n;

	private IRepresentationFactory representationFactory;

	@Inject
	public void setDeleteGadgetHandler(IDeleteGadgetHandler deleteGadgetHandler) {
		this.deleteGadgetHandler = deleteGadgetHandler;
	}

	@Inject
	public void setPermissionService(IDashboardPermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@Inject
	public void setAddGadgetHandler(IAddGadgetHandler addGadgetHandler) {
		this.addGadgetHandler = addGadgetHandler;
	}

	@Inject
	public void setI18n(I18nResolver i18n) {
		this.i18n = i18n;
	}

	@Inject
	public void setRepository(IDashboardRepository repository) {
		this.repository = repository;
	}

	@Inject
	public void setGadgetRequestContextFactory(IGadgetRequestContextFactory gadgetRequestContextFactory) {
		this.gadgetRequestContextFactory = gadgetRequestContextFactory;
	}

	@Inject
	public void setRepresentationFactory(IRepresentationFactory representationFactory) {
		this.representationFactory = representationFactory;
	}

	/**
	 * Returns a Gadget's JSON or XMl representation.
	 * 
	 * @param dashboardId
	 *            the id of the dashboard which it belongs to
	 * @param gadgetId
	 *            the ID of the gadget to return
	 * @param request
	 *            the {@code HttpServletRequest} that was routed here
	 * @return The gadget representation
	 */
	@Get("json|xml")
	@AnonymousAllowed
	public GadgetRepresentation getRenderedGadget() {
		Request request = getRequest();
		Response response = getResponse();
		Map<String, Object> attrs = request.getAttributes();
		DashboardId dashboardId = DashboardId.valueOf(attrs.get("dashboardId"));
		GadgetId gadgetId = GadgetId.valueOf(attrs.get("gadgetId"));
		if (log.isDebugEnabled()) {
			log.debug("GadgetResource: GET received: dashboardId=" + dashboardId + ", gadgetId = " + gadgetId);
		}
		final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);
		if (!permissionService.isReadableBy(dashboardId, requestContext.getViewer())) {
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		final IDashboard dashboard = repository.get(dashboardId, requestContext);

		IGadget myGadget = null;
		DashboardState.ColumnIndex myColumn = null;
		for (DashboardState.ColumnIndex column : dashboard.getLayout().getColumnRange()) {
			for (IGadget gadget : dashboard.getGadgetsInColumn(column)) {
				if (gadget.getId().equals(gadgetId)) {
					myGadget = gadget;
					myColumn = column;
					log.debug("GadgetResource: GET: Found gadget ID '" + gadgetId + "' in column " + myColumn.index()
							+ "; state=" + myGadget.toString());
					break;
				}
			}
			if (myGadget != null) {
				break;
			}
		}

		if (myGadget == null) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}

		final GadgetRepresentation rep = representationFactory.createGadgetRepresentation(dashboardId, myGadget,
				requestContext, permissionService.isWritableBy(dashboardId, requestContext.getViewer()), myColumn);
		return rep;
	}

	/**
	 * Deletes or moves the specified gadget from the specified dashboard when
	 * invoked as a POST request.
	 * 
	 * @param method
	 *            the HTTP method to forward to ("delete" does a delete, "put"
	 *            does a move)
	 * @param dashboardId
	 *            ID of the dashboard hosting the gadget
	 * @param gadgetId
	 *            ID of the gadget
	 * @param request
	 *            the request object (used for providing the locale)
	 * @return a {@code Response} with details on the request's success or
	 *         failure
	 */
	@Post
	@AnonymousAllowed
	public GadgetRepresentation deleteOrMoveGadgetViaPost() {
		Request request = getRequest();
		Response response = getResponse();
		Form form = request.getEntityAsForm();
		String method = form.getFirstValue("method");
		if (method.equalsIgnoreCase("delete")) {
			log.debug("GadgetResource: POST redirected to DELETE");
			deleteGadget();
		} else if (method.equalsIgnoreCase("put")) {
			log.debug("GadgetResource: POST redirected to PUT");
			return moveGadget();
		} else {
			response.setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
		}
		return null;
	}

	/**
	 * Deletes the specified gadget from the specified dashboard when invoked as
	 * a DELETE request.
	 * 
	 * @param dashboardId
	 *            ID of the dashboard hosting the gadget
	 * @param gadgetId
	 *            ID of the gadget to remove
	 * @param request
	 *            the request object (used for providing the locale)
	 * @return a {@code Response} with details on the request's success or
	 *         failure
	 */
	@Delete
	@AnonymousAllowed
	public void deleteGadget() {
		Request request = getRequest();
		Map<String, Object> attrs = request.getAttributes();
		DashboardId dashboardId = DashboardId.valueOf(attrs.get("dashboardId"));
		GadgetId gadgetId = GadgetId.valueOf(attrs.get("gadgetId"));

		final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

		if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer())) {
			log.warn("GadgetResource: DELETE: prevented gadget delete due to insufficient permission");
			getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return;
		}

		deleteGadgetHandler.deleteGadget(dashboardId, requestContext, gadgetId, getResponse());
	}

	/**
	 * Moves the specified gadget to the specified dashboard. The gadget is
	 * assumed to exist on some other (unspecified) dashboard. The gadget is
	 * safely removed from that dashboard and added to the target dashboard.
	 * 
	 * @param targetDashboardId
	 *            the dashboard id for the dashboard to which this gadget should
	 *            be added
	 * @param gadgetId
	 *            the id of the gadget to move
	 * @param request
	 *            the request object
	 * @return a {@code Response} with details on the request's success or
	 *         failure
	 */
	@Put
	@AnonymousAllowed
	public GadgetRepresentation moveGadget() {
		Request request = getRequest();
		Response response = getResponse();
		Map<String, Object> attrs = request.getAttributes();
		DashboardId targetDashboardId = DashboardId.valueOf(attrs.get("dashboardId"));
		GadgetId gadgetId = GadgetId.valueOf(attrs.get("gadgetId"));

		final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

		if (!permissionService.isWritableBy(targetDashboardId, requestContext.getViewer())) {
			log.warn("GadgetResource: PUT: prevented gadget move due to insufficient permissions on target dashboard");
			getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		try {
			DashboardId sourceDashboardId = repository.findDashboardByGadgetId(gadgetId);
			if (!permissionService.isWritableBy(sourceDashboardId, requestContext.getViewer())) {
				log.warn("GadgetResource: PUT: prevented gadget move due to insufficient permissions on source dashboard");
				getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
				return null;
			}
			return addGadgetHandler.moveGadget(targetDashboardId, gadgetId, sourceDashboardId,
					DashboardState.ColumnIndex.ZERO, 0, requestContext, response);
		} catch (DashboardNotFoundException dnfe) {
			log.error("DashboardResource: PUT: could not find a dashboard containing gadget " + gadgetId);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("gadgetResource.error.moving.gadget", dnfe.getMessage()));
			return null;
		}
	}

}
