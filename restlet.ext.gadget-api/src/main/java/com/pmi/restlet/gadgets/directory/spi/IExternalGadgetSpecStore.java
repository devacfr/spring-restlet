package com.pmi.restlet.gadgets.directory.spi;

import java.net.URI;

public interface IExternalGadgetSpecStore {

    Iterable<ExternalGadgetSpec> entries();

    ExternalGadgetSpec add(URI uri);

    void remove(ExternalGadgetSpecId externalgadgetspecid);

    boolean contains(URI uri);
}