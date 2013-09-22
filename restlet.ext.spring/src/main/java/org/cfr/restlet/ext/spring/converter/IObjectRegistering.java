package org.cfr.restlet.ext.spring.converter;

import org.cfr.restlet.ext.spring.ResourceRegister;


public interface IObjectRegistering {

    void registerObjects(ResourceRegister register) throws Exception;
}
