package com.pmi.restlet.response.extjs;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class ErrorResponse.
 *
 */
@XmlRootElement()
public class ErrorFormResponse implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean success = false;

    /**
     * Field errors.
     */
    private Set<FieldMessage> fields;

    /**
     * Method addError.
     *
     * @param errorMessage
     */
    public void addFieldError(FieldMessage errorMessage) {
        getErrors().add(errorMessage);
    }

    public void addFieldError(String id, String msg) {
        getErrors().add(new FieldMessage(id, msg));
    }

    /**
     * Method getErrors.
     *
     * @return java.util.List
     */
    public Set<FieldMessage> getErrors() {
        if (this.fields == null) {
            this.fields = new HashSet<FieldMessage>();
        }

        return this.fields;
    }

    /**
     * Method removeError.
     *
     * @param errorMessage
     */
    public void removeFieldError(FieldMessage errorMessage) {
        getErrors().remove(errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}