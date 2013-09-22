package com.pmi.restlet.request.extjs;

/**
 * POJO Class used mainly to populate Ext.ComboxBox  
 * @author cfriedri
 *
 */
public class NamedValue {

    private String code;

    private String name;

    public NamedValue(String code, String name) {
        super();
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
