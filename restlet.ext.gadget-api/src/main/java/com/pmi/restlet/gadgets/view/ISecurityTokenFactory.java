package com.pmi.restlet.gadgets.view;

import com.pmi.restlet.gadgets.GadgetState;

public interface ISecurityTokenFactory {

    String newSecurityToken(GadgetState gadgetstate, String s);
}