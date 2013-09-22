package com.pmi.restlet.gadgets.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class View {

    public static class Builder {

        public Builder viewType(ViewType viewType) {
            this.viewType = viewType;
            return this;
        }

        public Builder writable(boolean writable) {
            paramMap.put(WRITABLE_PARAM_NAME, Boolean.toString(writable));
            return this;
        }

        public Builder addViewParam(String name, String value) {
            paramMap.put(name, value);
            return this;
        }

        public Builder addViewParams(Map<String, String> params) {
            paramMap.putAll(params);
            return this;
        }

        public View build() {
            return new View(this);
        }

        private ViewType viewType;

        private Map<String, String> paramMap;

        public Builder() {
            paramMap = new HashMap<String, String>();
        }
    }

    private View(Builder builder) {
        viewType = builder.viewType;
        String writableParam = builder.paramMap.get(WRITABLE_PARAM_NAME);
        writable = writableParam != null ? Boolean.valueOf(writableParam).booleanValue() : false;
        params = Collections.unmodifiableMap(new HashMap<String, String>(builder.paramMap));
    }

    public ViewType getViewType() {
        return viewType;
    }

    public boolean isWritable() {
        return writable;
    }

    public Map<String, String> paramsAsMap() {
        return params;
    }

    public static final View DEFAULT;

    private static final String WRITABLE_PARAM_NAME = "writable";

    private final ViewType viewType;

    private final boolean writable;

    private final Map<String, String> params;

    static {
        DEFAULT = (new Builder()).viewType(ViewType.DEFAULT).writable(true).build();
    }
}
