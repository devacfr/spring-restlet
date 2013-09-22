package org.cfr.restlet.ext.spring.event.filters;

import org.cfr.commons.web.ui.WebUtils;
import org.cfr.restlet.ext.spring.event.EventService;
import org.cfr.restlet.ext.spring.utils.ReferenceUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A filter that handles cases where the application is unable to handle a
 * normal request and redirects to the configured error path so that a nice
 * error page can be provided.
 */
public class EventFilter extends AbstractEventFilter {

	public static final Logger log = LoggerFactory.getLogger(EventFilter.class);

	public EventFilter(EventService eventService, Context context) {
		super(eventService, context);
	}

	@Override
	protected void handleError(EventService eventService, Request request, Response response) throws ResourceException {
		log.info("The application is still starting up, or there are errors.");
		response.redirectSeeOther(new Reference(WebUtils.CleanupPath(ReferenceUtils.getContextPath(request))
				+ eventService.getErrorPath().toString()));
	}

	@Override
	protected void handleNotSetup(EventService eventService, Request request, Response response)
			throws ResourceException {
		log.info("The application is not yet setup.");
		response.redirectSeeOther(new Reference(WebUtils.CleanupPath(ReferenceUtils.getContextPath(request))
				+ eventService.getSetupPath().toString()));
	}

}
