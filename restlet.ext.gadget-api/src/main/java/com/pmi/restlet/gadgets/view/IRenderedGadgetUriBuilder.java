package com.pmi.restlet.gadgets.view;

import java.net.URI;

import com.pmi.restlet.gadgets.GadgetRequestContext;
import com.pmi.restlet.gadgets.GadgetState;

public interface IRenderedGadgetUriBuilder {

    /**
     * @deprecated Method build is deprecated
     */

    @Deprecated
    public abstract URI build(GadgetState gadgetstate, View view, GadgetRequestContext gadgetrequestcontext);

    public abstract URI build(GadgetState gadgetstate, ModuleId moduleid, View view, GadgetRequestContext gadgetrequestcontext);
}