package org.cfr.restlet.ext.spring.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.cfr.commons.util.Assert;
import org.cfr.restlet.ext.spring.event.filters.EventFilter;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Reference;
import org.restlet.routing.Filter;
import org.restlet.service.Service;
import org.springframework.util.AntPathMatcher;


/**
 * 
 * @author cfriedri
 * 
 */
public class EventService extends Service {

	private List<RequestEventCheck> eventChecks;

	private final AntPathMatcher matcher = new AntPathMatcher();

	private final List<String> ignorePatterns = new ArrayList<String>();

	/*
	 * a WeakHashMap so that the garbage collector will destroy it if there are
	 * no links left to it. This may be needed if the user presses the Stop
	 * button while an action is performing
	 */
	private final static Map<Event, Event> events = new WeakHashMap<Event, Event>();

	private ISetupConfig setupConfig;

	/**
	 * Determine if there are Application Error Events
	 * 
	 * @return true if there are application errors otherwise false
	 */
	public synchronized boolean hasEvents() {
		return !events.isEmpty();
	}

	/**
	 * public an application event to the events Map
	 * 
	 * @param event
	 *            New Event
	 */
	public static synchronized void publishEvent(Event event) {
		events.put(event, event);
	}

	/**
	 * Discards an application event from the events Map
	 * 
	 * @param event
	 *            Event to be removed
	 */
	public static synchronized void discardEvent(Event event) {
		events.remove(event);
	}

	/**
	 * Removes All event from the events Map
	 * 
	 * @param event
	 *            Event to be removed
	 */
	public static synchronized void removeAllEvent() {
		events.clear();
	}

	/**
	 * Gets a collection of the Event objects
	 * 
	 * @return an unmodifiable connection
	 */
	public synchronized Collection<Event> getEvents() {
		// prevent concurrent modification - clone the values before returning
		// them.
		return Collections.<Event> unmodifiableCollection(new ArrayList<Event>(events.values()));
	}

	@Override
	public Filter createInboundFilter(Context context) {
		return new EventFilter(this, context);
	}

	public void checkRequest(Request req) {
		if (eventChecks == null) {
			return;
		}
		// run all of the configured request event checks
		for (RequestEventCheck requestEventCheck : this.eventChecks) {
			requestEventCheck.check(this, req);
		}
	}

	public boolean ignoreRequest(Request request) {
		String path = request.getResourceRef().toString();
		for (String pattern : this.ignorePatterns) {
			if (matcher.match(pattern, path)) {
				return true;
			}
		}
		return false;
	}

	public EventService addIgnorePattern(String pattern) {
		this.ignorePatterns.add(Assert.hasText(pattern));
		return this;
	}

	public boolean isSetupPage(Request request) {
		if (this.setupConfig == null) {
			return false;
		}
		return this.setupConfig.isSetupPage(request);
	}

	public boolean isErrorPage(Request request) {
		if (this.setupConfig == null) {
			return false;
		}
		return this.setupConfig.isErrorPage(request);
	}

	public Reference getSetupPath() {
		if (this.setupConfig == null) {
			return null;
		}
		return setupConfig.getSetupPage();
	}

	public Reference getErrorPath() {
		if (this.setupConfig == null) {
			return null;
		}
		return setupConfig.getErrorPage();
	}

	public void setSetupConfig(ISetupConfig setupConfig) {
		this.setupConfig = setupConfig;
	}

	public boolean isSetup() {
		if (this.setupConfig == null) {
			return false;
		}
		return setupConfig.isSetup();
	}

}
