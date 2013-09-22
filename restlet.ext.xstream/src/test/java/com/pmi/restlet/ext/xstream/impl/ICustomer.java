package com.pmi.restlet.ext.xstream.impl;

import java.util.Date;
import java.util.List;

public interface ICustomer {

    public abstract Date getModificationDate();

    public abstract void setModificationDate(Date modificationDate);

    public abstract String getFirstName();

    public abstract void setFirstName(String firstName);

    public abstract String getLastName();

    public abstract void setLastName(String lastName);

    public abstract List<String> getAddresses();

    public abstract void setAddresses(List<String> addresses);

}