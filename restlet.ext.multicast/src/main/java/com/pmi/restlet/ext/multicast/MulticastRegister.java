package com.pmi.restlet.ext.multicast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.cfr.multicastevent.core.MemberJoinedEvent;
import org.cfr.multicastevent.core.MulticastClosingEvent;
import org.cfr.multicastevent.core.MulticastOpenEvent;
import org.cfr.multicastevent.core.multicast.IMulticast;
import org.cfr.multicastevent.core.multicast.IMulticastProvider;
import org.cfr.multicastevent.core.multicast.impl.Multicast;
import org.cfr.multicastevent.jgroups.JGroupsMulticastProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.Assert;

/**
 * Applications registry using multicast to register/unregister application.
 * 
 * @author acochard [Sep 30, 2009]
 */
public class MulticastRegister implements ApplicationListener<ApplicationEvent>, ApplicationContextAware,
		InitializingBean, ApplicationEventPublisherAware, IMulticastRegister {

	/** Class logger **/
	private static final Logger logger = LoggerFactory.getLogger(MulticastRegister.class);

	/** Local application information **/
	private Channel localChannel;

	private String clusterName;

	/** Event publisher **/
	private ApplicationEventPublisher applicationEventPublisher;

	/** Store applications informations **/
	private final Set<Channel> allAvailableChannels = new HashSet<Channel>();

	private ApplicationContext applicationContext;

	/** Randomizer **/
	private final Random randomizer;

	private IMulticastProvider multicastProvider;

	private IMulticast multicast;

	private String channelName;

	private String channelVersion;

	public MulticastRegister() {
		randomizer = new Random();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(channelName, "channelName is required");
		Assert.hasText(channelVersion, "channelVersion is required");
		localChannel = new Channel();
		localChannel.setName(channelName);
		localChannel.setVersion(channelVersion);
		// Retrieve host address
		try {

			localChannel.setHostAddress(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			throw new Exception("Unable to resolve localhost address", e);
		}
		DefaultListableBeanFactory autowireCapableBeanFactory = (DefaultListableBeanFactory) this.applicationContext
				.getAutowireCapableBeanFactory();

		ApplicationEventMulticaster eventMulticaster = this.applicationContext.getBean(
				AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
		Assert.notNull(eventMulticaster, "Spring multicaster is required");
		if (multicastProvider == null) {
			Assert.hasText(clusterName, "clusterName is required");
			JGroupsMulticastProvider provider = new JGroupsMulticastProvider();
			provider.setClusterName(getClusterName());
			provider.setStartOnContext(false);
			autowireCapableBeanFactory.autowireBeanProperties(provider, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
			autowireCapableBeanFactory.initializeBean(provider, "jGroupsMulticastProvider");
			this.multicastProvider = provider;
			eventMulticaster.addApplicationListener(provider);

		}
		if (multicastProvider.isStartOnContext()) {
			throw new RuntimeException(
					"multicastProvider hasn't to start automatically, set 'startOnContext' property to false");
		}

		multicast = new Multicast();
		multicast.setProvider(multicastProvider);
		autowireCapableBeanFactory.autowireBeanProperties(multicast, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
		autowireCapableBeanFactory.initializeBean(multicast, "multicast");
		eventMulticaster.addApplicationListener(this.multicast);

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public boolean isStarted() {
		return multicastProvider.isStarted();
	}

	@Override
	public synchronized void start() throws Exception {
		multicastProvider.start();

	}

	@Override
	public synchronized void stop() throws Exception {

		multicastProvider.stop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Channel> getAllAvailableChannels() {
		return Collections.unmodifiableSet(allAvailableChannels);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Channel> getAvailableChannels(String channelName) {
		Set<Channel> channels = new HashSet<Channel>();
		for (Channel instance : this.allAvailableChannels) {
			if (instance.getName().equals(channelName)) {
				channels.add(instance);
			}
		}
		return Collections.unmodifiableSet(channels);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Channel getRandomChannel(String channelName) {
		Set<Channel> instances = getAvailableChannels(channelName);

		if (instances.isEmpty()) {
			return null;
		}

		int instanceToUse = randomizer.nextInt(instances.size());

		int i = 0;
		for (Channel applicationInformation : instances) {
			if (i == instanceToUse) {
				return applicationInformation;
			}
			i++;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event == null) {
			return;
		}
		if (event.getSource() == this) {
			return;
		}
		if (event instanceof MemberJoinedEvent || event instanceof MulticastOpenEvent) {
			// Register me
			applicationEventPublisher.publishEvent(new ChannelRegisterEvent(this, localChannel));
		} else if (event instanceof MulticastClosingEvent) {
			// Unregister me
			applicationEventPublisher.publishEvent(new ChannelUnregisterEvent(this, localChannel));
		} else if (event instanceof ChannelUnregisterEvent) {
			// Unregister application
			ChannelUnregisterEvent applicationEvent = (ChannelUnregisterEvent) event;
			allAvailableChannels.remove(applicationEvent.getChannel());
			if (logger.isDebugEnabled()) {
				logger.debug("Unregistering application: " + applicationEvent.getChannel());
			}
		} else if (event instanceof ChannelRegisterEvent) {
			// Register application if not already in registry
			ChannelRegisterEvent applicationEvent = (ChannelRegisterEvent) event;
			if (!allAvailableChannels.contains(applicationEvent.getChannel())) {
				allAvailableChannels.add(applicationEvent.getChannel());
				if (logger.isDebugEnabled()) {
					logger.debug("Registering application: " + applicationEvent.getChannel());
				}
			}
		}

	}

	public IMulticastProvider getMulticastProvider() {
		return multicastProvider;
	}

	@Autowired
	public void setMulticastProvider(IMulticastProvider multicastProvider) {
		this.multicastProvider = multicastProvider;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public Channel getLocalChannel() {
		return localChannel;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelVersion() {
		return channelVersion;
	}

	public void setChannelVersion(String channelVersion) {
		this.channelVersion = channelVersion;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
}
