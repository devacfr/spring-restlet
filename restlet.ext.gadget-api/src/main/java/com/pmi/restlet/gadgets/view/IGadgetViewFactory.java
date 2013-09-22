package com.pmi.restlet.gadgets.view;

import com.pmi.restlet.gadgets.GadgetParsingException;
import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetState;

public interface IGadgetViewFactory {

    /**
     * @deprecated Method createGadgetView is deprecated
     */

    @Deprecated
    IViewComponent createGadgetView(GadgetState gadgetstate, View view, GadgetRequestContext gadgetrequestcontext) throws GadgetParsingException,
            GadgetRenderingException;

    IViewComponent createGadgetView(GadgetState gadgetstate, ModuleId moduleid, View view, GadgetRequestContext gadgetrequestcontext)
            throws GadgetParsingException, GadgetRenderingException;

    boolean canRenderInViewType(GadgetState gadgetstate, ViewType viewtype, GadgetRequestContext gadgetrequestcontext) throws GadgetParsingException;
}