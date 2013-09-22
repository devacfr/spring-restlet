package com.pmi.restlet.ext.multicast;

import java.util.Set;

/**
 * Provides access to applications informations.
 * 
 * @author acochard [Sep 30, 2009]
 */
public interface IMulticastRegister {

    /**
     * @return availables instances of all KBase applications.
     */
    Set<Channel> getAllAvailableChannels();

    /**
     * Retrieves availables instances of a given application.
     * 
     * @param chanelName name of channel.
     * @return availables instances of the specified application.
     */
    Set<Channel> getAvailableChannels(String channelName);

    /**
     * Retrieves an instance of a given application among all instances of this application.
     * 
     * @param channelName name of channel.
     * @return an instance of the specified application among all instances of this application or <code>null</code> if no instance of the given
     *         application is started.
     */
    Channel getRandomChannel(String channelName);

    boolean isStarted();

    void start() throws Exception;

    void stop() throws Exception;
    
    
    Channel getLocalChannel();

}