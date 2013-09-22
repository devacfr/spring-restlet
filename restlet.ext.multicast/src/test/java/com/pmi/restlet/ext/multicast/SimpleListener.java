package com.pmi.restlet.ext.multicast;

import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

public class SimpleListener {

    static {
        try {
            DOMConfigurator.configure(ResourceUtils.getURL("classpath:log4j.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "classpath:com/pmi/restlet/ext/multicast/simple-applicationcontext.xml");

        IMulticastRegister register = applicationContext.getBean(IMulticastRegister.class);
        register.start();
        System.in.read();
        register.stop();
    }

}
