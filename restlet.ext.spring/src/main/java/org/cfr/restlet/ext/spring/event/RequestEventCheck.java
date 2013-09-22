package org.cfr.restlet.ext.spring.event;

import org.restlet.Request;

/**
 * A check that is run every request
 */
public interface RequestEventCheck extends EventCheck {

    void check(EventService eventService, Request request);
}
