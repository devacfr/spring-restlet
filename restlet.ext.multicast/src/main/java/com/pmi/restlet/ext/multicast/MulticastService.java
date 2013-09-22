package com.pmi.restlet.ext.multicast;

import org.restlet.Context;
import org.restlet.routing.Filter;
import org.restlet.service.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Applications registry using multicast to register/unregister application.
 * 
 * @author acochard [Sep 30, 2009]
 */
public class MulticastService extends Service {

    private IMulticastRegister register;

    public MulticastService() {
        super();
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new MulticastFilter(context, this);
    }

    @Override
    public boolean isStarted() {
        return register.isStarted();
    }

    @Override
    public synchronized void start() throws Exception {
        // noop -> wait filter start the service
    }

    synchronized void internalStart() throws Exception {
        if (isEnabled() && register != null && !register.isStarted()) {
            register.start();
            super.start();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        if (register != null)
            register.stop();
        super.stop();
    }

    public IMulticastRegister getRegister() {
        return register;
    }

    @Autowired
    public void setRegister(IMulticastRegister register) {
        this.register = register;
    }

    public Channel getLocalChannel() {
        return this.register.getLocalChannel();
    }

}
