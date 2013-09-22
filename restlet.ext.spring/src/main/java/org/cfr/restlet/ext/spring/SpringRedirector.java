package org.cfr.restlet.ext.spring;

import org.cfr.restlet.ext.spring.utils.ReferenceUtils;
import org.restlet.Request;
import org.restlet.data.Reference;
import org.restlet.routing.Redirector;



public abstract class SpringRedirector extends Redirector implements IManagedResource {

    /**
     *
     * @param request
     * @param relPart
     * @return
     */
    public static Reference createRootReference(Request request, String relPart) {
        return ReferenceUtils.createRootReference(request, relPart);
    }

    /**
     * Default contructor
     * @param targetTemplate The pattern to build the target URI (using StringTemplate
     *            syntax and the CallModel for variables).
     * @param mode The redirection mode.
     */
    public SpringRedirector(String targetTemplate, int mode) {
        super(null, targetTemplate, mode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attach(IFirstResponder application, boolean isStarted) {
        this.setContext(application.getContext());
    }
}
