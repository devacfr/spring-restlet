package org.cfr.restlet.ext.spring.event;

import org.restlet.Application;

/**
 * A check that is run every time the application is started.
 */
public interface ApplicationEventCheck extends EventCheck {

    void check(Application application);
}