package com.pmi.restlet.ext.multicast;

public class ChannelRegisterEvent extends ChannelEvent {

    /**
     * Default class serial version identifier.
     */
    private static final long serialVersionUID = 1L;

    public ChannelRegisterEvent(Object source, Channel channel) {
        super(source, channel);
    }

}