package org.cfr.restlet.ext.spring;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.Protocol;


public class SpringComponent extends Component {

    /**
     * Adds a client to the list of connectors. The value can be either a
     * protocol name, a Protocol instance or a Client instance.
     *
     * @param clientInfo
     *            The client info.
     */
    public void setClient(Object clientInfo) {
        final List<Object> clients = new ArrayList<Object>();
        clients.add(clientInfo);
        setClientsList(clients);
    }

    /**
     * Sets the list of clients, either as protocol names, Protocol instances or
     * Client instances.
     *
     * @param clients
     *            The list of clients.
     */
    public synchronized void setClientsList(List<Object> clients) {
        for (final Object client : clients) {
            if (client instanceof String) {
                getClients().add(Protocol.valueOf((String) client));
            } else if (client instanceof Protocol) {
                getClients().add((Protocol) client);
            } else if (client instanceof Client) {
                getClients().add((Client) client);
            } else {
                getLogger()
                        .warning(
                                "Unknown object found in the clients list. Only instances of String, org.restlet.data.Protocol and org.restlet.Client are allowed.");
            }
        }
    }


}
