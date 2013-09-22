package com.pmi.restlet.gadgets.dashboard.spi;

import java.net.URI;

import com.pmi.restlet.gadgets.GadgetState;

public interface IGadgetStateFactory {

    GadgetState createGadgetState(URI uri);
}