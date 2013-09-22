package org.cfr.restlet.ext.shindig.server;

import org.cfr.restlet.ext.shindig.ShindigApplication;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.pmi.restlet.Constants;

public class ShindigServer {

    public static void main(String[] args) throws Exception {
        // Create a new Component.
        Component component = new Component();

        // Add a new HTTP server listening on port 8182.
        Server server = component.getServers().add(Protocol.HTTP, 8182);
        server.getContext().getParameters().add("useForwardedForHeader", "true");

        ShindigApplication application = new ShindigApplication(server.getContext());
        // Attach the sample application.
        component.getDefaultHost().attach("/shindig", application);

        server.getContext().getAttributes().put(Constants.CONTEXT_PATH_KEY, "/shindig");
        server.getContext().getAttributes().put(Constants.BASE_URL_KEY, "http://localhost:8182");

        // Start the component.
        component.start();

    }
}
