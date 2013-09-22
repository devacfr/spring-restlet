package com.pmi.restlet.ext.xstream.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("customer")
public class AnnotedCustomer implements ICustomer {

    @XStreamAlias("firstName")
    @XStreamAsAttribute
    private String firstName;

    @XStreamAlias("lastName")
    @XStreamAsAttribute
    private String lastName;

    @XStreamImplicit(itemFieldName="city")
    private List<String> addresses;

    @XStreamAlias("modificationDate")
    private Date modificationDate;

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    public Date getModificationDate() {
        return modificationDate;
    }


    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }


    public String getFirstName() {
        return firstName;
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }


    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public List<String> getAddresses() {
        return addresses;
    }

    /* (non-Javadoc)
     * @see com.pmi.restlet.serialization.ICustomer#setAddresses(java.util.List)
     */
    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    @SuppressWarnings("deprecation")
    public static ICustomer createCustomer() {
        ICustomer customer = new AnnotedCustomer();
        customer.setFirstName("Bernard");
        customer.setLastName("Lefevre");
        customer.setModificationDate(new Date(2010,2,10));
        customer.setAddresses(new ArrayList<String>());
        customer.getAddresses().addAll(Arrays.asList("Lyon", "Paris", "Marseille"));
        return customer;
    }

    public static final ICustomer customer = createCustomer();

}
