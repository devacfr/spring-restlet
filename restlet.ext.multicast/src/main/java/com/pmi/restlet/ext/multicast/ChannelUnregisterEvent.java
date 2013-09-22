package com.pmi.restlet.ext.multicast;

public class ChannelUnregisterEvent extends ChannelEvent {

    /**
     * Default class serial version identifier.
     */
    private static final long serialVersionUID = 1L;

    public ChannelUnregisterEvent(Object source, Channel channel) {
        super(source, channel);
    }

}
