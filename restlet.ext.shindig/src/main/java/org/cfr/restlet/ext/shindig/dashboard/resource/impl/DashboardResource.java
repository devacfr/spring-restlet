package org.cfr.restlet.ext.shindig.dashboard.resource.impl;

import org.cfr.commons.sal.message.I18nResolver;
import org.cfr.restlet.ext.shindig.dashboard.resource.IAddGadgetHandler;
import org.cfr.restlet.ext.shindig.internal.InjectedResource;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pmi.restlet.Resource;
import com.pmi.restlet.gadgets.GadgetParsingException;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetSpecUriNotAllowedException;
import com.pmi.restlet.gadgets.IGadgetRequestContextFactory;
import com.pmi.restlet.gadgets.dashboard.DashboardId;
import com.pmi.restlet.gadgets.dashboard.DashboardState;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboard;
import com.pmi.restlet.gadgets.dashboard.internal.IDashboardRepository;
import com.pmi.restlet.gadgets.dashboard.internal.InconsistentDashboardStateException;
import com.pmi.restlet.gadgets.dashboard.spi.IDashboardPermissionService;
import com.pmi.restlet.gadgets.representations.DashboardRepresentation;
import com.pmi.restlet.gadgets.representations.GadgetRepresentation;
import com.pmi.restlet.gadgets.representations.IRepresentationFactory;
import com.pmi.restlet.security.AnonymousAllowed;

/**
 * Provides REST endpoints for using the dashboard.
 */
@Resource(path = "/{dashboardId}", strict = true)
public class DashboardResource extends InjectedResource {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private IDashboardPermissionService permissionService;

	private IGadgetRequestContextFactory gadgetRequestContextFactory;

	private IAddGadgetHandler addGadgetHandler;

	private I18nResolver i18n;

	private IDashboardRepository repository;

	private IRepresentationFactory representationFactory;

	public DashboardResource() {
		super();
	}

	@Inject
	public void setRepository(IDashboardRepository repository) {
		this.repository = repository;
	}

	@Inject
	public void setRepresentationFactory(IRepresentationFactory representationFactory) {
		this.representationFactory = representationFactory;
	}

	@Inject
	public void setI18n(I18nResolver i18n) {
		this.i18n = i18n;
	}

	@Inject
	public void setAddGadgetHandler(IAddGadgetHandler addGadgetHandler) {
		this.addGadgetHandler = addGadgetHandler;
	}

	@Inject
	public void setPermissionService(IDashboardPermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@Inject
	public void setGadgetRequestContextFactory(IGadgetRequestContextFactory gadgetRequestContextFactory) {
		this.gadgetRequestContextFactory = gadgetRequestContextFactory;
	}

	/**
	 * Constructor.
	 * 
	 * @param permissionService
	 *            the {@code PermissionService} implementation to use
	 * @param gadgetRequestContextFactory
	 *            the {@code GadgetRequestContextFactory} to use
	 * @param addGadgetHandler
	 *            the {@code AddGadgetHandler} to use
	 * @param repository
	 *            the {@code Repository} to use
	 * @param representationFactory
	 *            the {@code RepresentationFactory} to use to construct JAXB
	 *            reps
	 * @param i18n
	 *            the {@code I18nResolver} from SAL
	 */
	public DashboardResource(IDashboardPermissionService permissionService,
			IGadgetRequestContextFactory gadgetRequestContextFactory, IAddGadgetHandler addGadgetHandler,
			IDashboardRepository repository, IRepresentationFactory representationFactory, I18nResolver i18n) {
		this.permissionService = permissionService;
		this.gadgetRequestContextFactory = gadgetRequestContextFactory;
		this.addGadgetHandler = addGadgetHandler;
		this.repository = repository;
		this.representationFactory = representationFactory;
		this.i18n = i18n;
	}

	@Get("json|xml")
	@AnonymousAllowed
	public DashboardRepresentation getDashboard() {

		Request request = getRequest();
		Response response = getResponse();

		DashboardId dashboardId = DashboardId.valueOf(request.getAttributes().get("dashboardId"));
		if (log.isDebugEnabled()) {
			log.debug("DashboardResource: GET received: dashboardId = " + dashboardId);
		}

		final GadgetRequestContext gadgetRequestContext = gadgetRequestContextFactory.get(request);
		if (!permissionService.isReadableBy(dashboardId, gadgetRequestContext.getViewer())) {
			log.warn("DashboardResource: GET: prevented getting dashboard representation due to insufficient permission");
			response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		final IDashboard dashboard = repository.get(dashboardId, gadgetRequestContext);
		final DashboardRepresentation representation = representationFactory.createDashboardRepresentation(dashboard,
				gadgetRequestContext, permissionService.isWritableBy(dashboardId, gadgetRequestContext.getViewer()));
		return representation;
	}

	/**
	 * Adds a gadget to a dashboard, returning the new gadget's entity
	 * representation if successful.
	 * <p>
	 * If the request body contains a gadgetId and sourceDashboardId, then this
	 * will be treated as a 'move' operation, where the gadget will be added to
	 * the dashboard specified and deleted from the sourceDashboard.
	 * </p>
	 * 
	 * @param dashboardId
	 *            the dashboard to add the gadget to
	 * @param request
	 *            the {@code HttpServletRequest} that was routed here
	 * @param jsonContent
	 *            the body of the post in JSON
	 * @return a {@code Response} with the resulting status code and gadget URL
	 *         if applicable
	 */
	@Post("json")
	@AnonymousAllowed
	public GadgetRepresentation addGadget(String jsonContent) {
		Request request = getRequest();

		DashboardId dashboardId = DashboardId.valueOf(request.getAttributes().get("dashboardId"));

		String gadgetUrl = null;
		int columnIndexAsInt = 0;
		DashboardState.ColumnIndex columnIndex = null;
		try {
			JSONObject jsonObject = new JSONObject(jsonContent);
			gadgetUrl = jsonObject.optString("url");
			columnIndexAsInt = jsonObject.optInt("columnIndex");
			columnIndex = DashboardState.ColumnIndex.from(columnIndexAsInt);
		} catch (JSONException jsone) {
			log.warn("DashboardResource: POST: error parsing json", jsone);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("dashboardResource.error.parsing.json", jsone.getMessage()));
		} catch (IllegalArgumentException iae) {
			log.error("DashboardResource: POST: invalid column index " + columnIndexAsInt
					+ ". Valid values for Column are 0-2.");
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("dashboardResource.error.parsing.json", iae.getMessage()));
			return null;
		}

		final GadgetRequestContext requestContext = gadgetRequestContextFactory.get(request);

		if (log.isDebugEnabled()) {
			log.debug("DashboardResource: POST received: dashboardId=" + dashboardId + ", url=" + gadgetUrl);
		}
		if (!permissionService.isWritableBy(dashboardId, requestContext.getViewer())) {
			log.warn("DashboardResource: POST: prevented gadget addition due to insufficient permission");
			getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		try {
			return addGadgetHandler.addGadget(dashboardId, requestContext, gadgetUrl, columnIndex, getResponse());
		} catch (GadgetSpecUriNotAllowedException igsue) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("gadget.spec.not.allowed", igsue.getMessage()));
			return null;
		} catch (GadgetParsingException gpe) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
					i18n.getText("error.parsing.spec", gpe.getMessage()));
			return null;
		} catch (InconsistentDashboardStateException idse) {
			log.error("AddGadgetHandlerImpl: Unexpected error occurred", idse);
			getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, i18n.getText("error.please.reload"));
			return null;
		}
	}
}
