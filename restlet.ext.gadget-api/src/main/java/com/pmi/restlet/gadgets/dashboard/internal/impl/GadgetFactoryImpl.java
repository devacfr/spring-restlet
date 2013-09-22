package com.pmi.restlet.gadgets.dashboard.internal.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import com.pmi.restlet.gadgets.GadgetParsingException;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetSpecUriNotAllowedException;
import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.internal.IGadget;
import com.pmi.restlet.gadgets.dashboard.internal.IGadgetFactory;
import com.pmi.restlet.gadgets.dashboard.spi.IGadgetStateFactory;
import com.pmi.restlet.gadgets.spec.IGadgetSpecFactory;

/**
 * An implementation of {@code IGadgetFactory} that delegates to a {@link IGadgetStateFactory} and a {@link
 * IGadgetSpecFactory} and returns {@link GadgetImpl} instances.
 */
public class GadgetFactoryImpl implements IGadgetFactory {

    private final IGadgetStateFactory stateFactory;

    private final IGadgetSpecFactory specFactory;

    public GadgetFactoryImpl(IGadgetStateFactory stateFactory, IGadgetSpecFactory specFactory) {
        this.stateFactory = stateFactory;
        this.specFactory = specFactory;
    }

    public IGadget createGadget(String gadgetSpecUrl, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException,
            GadgetSpecUriNotAllowedException {
        URI gadgetSpecUri;
        try {
            gadgetSpecUri = new URI(gadgetSpecUrl);
        } catch (URISyntaxException e) {
            throw new GadgetSpecUriNotAllowedException(e);
        }
        return createGadget(stateFactory.createGadgetState(gadgetSpecUri), gadgetRequestContext);
    }

    public IGadget createGadget(GadgetState state, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException {
        try {
            return new GadgetImpl(state, specFactory.getGadgetSpec(state, gadgetRequestContext));
        } catch (Exception ex) {
            String errorMessage = (new StringBuilder()).append("Error loading gadget from ")
                    .append(state.getGadgetSpecUri())
                    .append(" : ")
                    .append(ex.getMessage())
                    .toString();
            if (gadgetRequestContext.isDebuggingEnabled()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw, true);
                ex.printStackTrace(pw);
                StringBuilder extraDebugInfo = new StringBuilder(errorMessage);
                extraDebugInfo.append("\nGadget ID: ").append(state.getId());
                extraDebugInfo.append("\nStack trace: ").append(sw.toString());
                return new GadgetImpl(state, extraDebugInfo.toString());
            } else {
                return new GadgetImpl(state, errorMessage);
            }
        }
    }

}
