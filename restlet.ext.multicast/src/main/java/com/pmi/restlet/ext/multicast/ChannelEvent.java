package com.pmi.restlet.ext.multicast;

import org.cfr.multicastevent.core.multicast.MulticastEvent;

/**
 * Abstract class to create application information event.
 * 
 * @author acochard [Sep 30, 2009]
 * 
 */
public abstract class ChannelEvent extends MulticastEvent {

	/** Default class serial version unique identifier. **/
	private static final long serialVersionUID = 1L;

	/** Store **/
	private final Channel channel;

	/**
	 * Create a new event using provided information
	 * 
	 * @param channel
	 *            channel
	 */
	public ChannelEvent(Object source, Channel channel) {
		super(source);
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}

}
