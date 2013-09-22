package org.cfr.restlet.ext.spring;

import org.restlet.Component;
import org.restlet.data.Protocol;


public class Server {


    public static void main(String[] args) throws Exception {
        // Create a new Component.
        Component component = new Component();

        // Add a new HTTP server listening on port 8182.
        org.restlet.Server server =  component.getServers().add(Protocol.HTTP, 8182);
        server.getContext().getParameters().add("useForwardedForHeader", "true");


        RestletSpringApplication application = new RestletSpringApplication();
        // Attach the sample application.
        component.getDefaultHost().attach( application);

        // Start the component.
        component.start();
    }
}
