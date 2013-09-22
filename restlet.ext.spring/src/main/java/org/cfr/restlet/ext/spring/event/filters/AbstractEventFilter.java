package org.cfr.restlet.ext.spring.event.filters;

import java.util.Collection;
import java.util.Iterator;

import org.cfr.commons.util.Assert;
import org.cfr.restlet.ext.spring.event.Event;
import org.cfr.restlet.ext.spring.event.EventService;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;


public abstract class AbstractEventFilter extends Filter {

	private final EventService eventService;

	public AbstractEventFilter(final EventService eventService, Context context) {
		super(context);
		this.eventService = Assert.notNull(eventService);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		if (request.getAttributes().containsKey(getFilterkey())) {
			return SKIP;
		}
		return super.beforeHandle(request, response);
	}

	@Override
	protected int doHandle(Request request, Response response) {
		request.getAttributes().put(getFilterkey(), Boolean.TRUE);

		// Run all of the configured request event checks
		this.eventService.checkRequest(request);

		// if there are application consistency events then redirect to the
		// errors page
		if (eventService.hasEvents() && !eventService.isErrorPage(request) && !eventService.ignoreRequest(request)) {
			handleError(eventService, request, response);
		}
		// if application is not setup then send to the Setup Page
		else if (!eventService.ignoreRequest(request) && !eventService.isSetupPage(request) && !eventService.isSetup()) {
			handleNotSetup(eventService, request, response);
		} else {
			return super.doHandle(request, response);
		}

		return CONTINUE;
	}

	protected String getFilterkey() {
		return this.getClass().getName() + "_already_filtered";
	}

	/**
	 * Handles the given request for error cases when there is a Johnson
	 * {@link Event} which stops normal application functioning.
	 * 
	 * @param eventService
	 *            the EventService that contains the events.
	 * @param request
	 *            the request being directed to the error.
	 * @param response
	 *            the response.
	 * @throws ResourceException
	 *             when the error cannot be handled.
	 */
	protected abstract void handleError(EventService eventService, Request request, Response response)
			throws ResourceException;

	/**
	 * Handles the given request for cases when the application is not yet setup
	 * which stops normal application functioning.
	 * 
	 * @param request
	 *            the request being directed to the error.
	 * @param response
	 *            the response.
	 * @throws ResourceException
	 *             when the error cannot be handled.
	 */
	protected abstract void handleNotSetup(EventService eventService, Request request, Response response)
			throws ResourceException;

	/**
	 * 
	 * @param events
	 * @return
	 */
	protected String getStringForEvents(Collection<Event> events) {
		StringBuffer message = new StringBuffer();
		int i = 1;
		for (Iterator<Event> iterator = events.iterator(); iterator.hasNext(); i++) {
			Event event = iterator.next();
			message.append(event.getDesc());
			if (i < events.size()) {
				message.append("\n");
			}
		}
		return message.toString();
	}
}
