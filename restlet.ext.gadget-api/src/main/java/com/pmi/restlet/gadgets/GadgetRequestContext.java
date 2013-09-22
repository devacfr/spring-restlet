package com.pmi.restlet.gadgets;

import java.util.Locale;

public final class GadgetRequestContext {

    public static class Builder {

        public static Builder gadgetRequestContext() {
            return new Builder();
        }

        public GadgetRequestContext build() {
            return new GadgetRequestContext(this);
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder ignoreCache(boolean ignoreCache) {
            this.ignoreCache = ignoreCache;
            return this;
        }

        public Builder viewer(String viewer) {
            this.viewer = viewer;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        private Locale locale;

        private boolean ignoreCache;

        private String viewer;

        private boolean debug;

        public Builder() {
            locale = Locale.US;
            ignoreCache = false;
            viewer = null;
            debug = false;
        }
    }

    private GadgetRequestContext(Builder builder) {
        locale = builder.locale;
        ignoreCache = builder.ignoreCache;
        viewer = builder.viewer;
        debug = builder.debug;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean getIgnoreCache() {
        return ignoreCache;
    }

    public String getViewer() {
        return viewer;
    }

    public boolean isDebuggingEnabled() {
        return debug;
    }

    public static final GadgetRequestContext NO_CURRENT_REQUEST = Builder.gadgetRequestContext().locale(new Locale("")).ignoreCache(false).debug(
            false).build();

    private final Locale locale;

    private final boolean ignoreCache;

    private final String viewer;

    private final boolean debug;

}