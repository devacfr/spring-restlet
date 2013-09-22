package com.pmi.restlet.gadgets.spec;

import java.net.URI;

import com.pmi.restlet.gadgets.GadgetParsingException;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetState;

/**
 * 
 * @author cfriedri
 *
 */
public interface IGadgetSpecFactory {

    /**
     * 
     * @param gadgetstate
     * @param gadgetrequestcontext
     * @return
     * @throws GadgetParsingException
     */
    GadgetSpec getGadgetSpec(GadgetState gadgetstate, GadgetRequestContext gadgetrequestcontext) throws GadgetParsingException;

    /**
     * 
     * @param uri
     * @param gadgetrequestcontext
     * @return
     * @throws GadgetParsingException
     */
    GadgetSpec getGadgetSpec(URI uri, GadgetRequestContext gadgetrequestcontext) throws GadgetParsingException;
}