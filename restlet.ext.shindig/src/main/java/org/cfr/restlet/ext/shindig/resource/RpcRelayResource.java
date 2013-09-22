package org.cfr.restlet.ext.shindig.resource;

import java.io.IOException;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.pmi.restlet.Resource;

@Resource(path = "/container/rpc_relay.html", strict = true)
public class RpcRelayResource extends ServerResource {

    @Get
    public Representation doGet() throws IOException {
        return null;
    }
}
