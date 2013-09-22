package com.pmi.restlet.gadgets.dashboard.internal;

import com.google.inject.ImplementedBy;
import com.pmi.restlet.gadgets.GadgetParsingException;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetSpecUriNotAllowedException;
import com.pmi.restlet.gadgets.GadgetState;
import com.pmi.restlet.gadgets.dashboard.internal.impl.GadgetFactoryImpl;

@ImplementedBy(GadgetFactoryImpl.class)
public interface IGadgetFactory {

    IGadget createGadget(String gadgetSpecUrl, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException,
            GadgetSpecUriNotAllowedException;

    IGadget createGadget(GadgetState state, GadgetRequestContext gadgetRequestContext) throws GadgetParsingException,
            GadgetSpecUriNotAllowedException;
}
