package com.pmi.restlet.response.extjs;

/**
 * An item describing the error.
 *
 */
public class FieldMessage implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1866887314074897584L;

    /**
     * Field id.
     */
    private String id;

    /**
     * Field msg.
     */
    private String msg;

    public FieldMessage() {
    }

    public FieldMessage(String id, String msg) {
        super();
        this.id = id;
        this.msg = msg;
    }

    /**
     * Get the id field.
     *
     * @return String
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the msg field.
     *
     * @return String
     */
    public String getMsg() {
        return this.msg;
    }

    /**
     * Set the id field.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set the msg field.
     *
     * @param msg
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

}