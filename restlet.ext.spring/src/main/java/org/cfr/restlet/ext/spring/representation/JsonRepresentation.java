package org.cfr.restlet.ext.spring.representation;

import java.io.IOException;
import java.util.logging.Level;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

public class JsonRepresentation extends StringRepresentation {

    private final JsonConfig config;

    private Object object;

    private final Class<?> targetClass;

    public JsonRepresentation(JsonConfig config, MediaType mediaType, Object object) {
        super(null, mediaType);
        if (config == null)
            config = new JsonConfig();
        this.config = config;
        this.object = object;
        this.targetClass = null;
        if (object != null) {
            String buf = null;
            if (JSONUtils.isArray(object)) {
                buf = JSONArray.fromObject(object, this.config).toString();
            } else {
                buf = JSONObject.fromObject(object, this.config).toString();
            }
            if (this.getCharacterSet() == null) {
                this.setCharacterSet(CharacterSet.UTF_8);
            }
            setText(buf);
        }
    }

    public JsonRepresentation(JsonConfig config, Representation representation, Class<?> targetClass) {
        super(null, representation.getMediaType());
        if (config == null)
            config = new JsonConfig();
        this.config = config;
        object = null;
        if (representation != null) {
            try {
                setText(representation.getText());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.targetClass = targetClass;
    }

    public Object getObject() {
        Object result = null;
        if (object != null)
            result = object;
        else if (getText() != null)
            try {
                Object obj = targetClass.newInstance();
                result = JSONObject.toBean(JSONObject.fromObject(getText()), obj, this.config);
            } catch (Exception e) {
                Context.getCurrentLogger().log(Level.WARNING, "Unable to parse the object with Json.", e);
            }
        return result;
    }

}