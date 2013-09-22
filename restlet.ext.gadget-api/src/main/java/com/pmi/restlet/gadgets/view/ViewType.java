package com.pmi.restlet.gadgets.view;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ViewType {

    private static final Map<String, ViewType> allViewTypes = new HashMap<String, ViewType>();

    private static final ReentrantReadWriteLock viewTypeRegistrationLock;

    private static final Lock readLock;

    private static final Lock writeLock;

    public static final ViewType DEFAULT = createViewType("default", new String[] { "DEFAULT", "DASHBOARD", "profile", "home" });

    public static final ViewType CANVAS = createViewType("canvas", new String[0]);

    private final String name;

    private final List<String> aliases;

    static final boolean assertionsDisabled = true;

    static {
        viewTypeRegistrationLock = new ReentrantReadWriteLock();
        readLock = viewTypeRegistrationLock.readLock();
        writeLock = viewTypeRegistrationLock.writeLock();
    }

    private ViewType(String name, String[] aliases) {
        this.name = name;
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }

    public static ViewType createViewType(String name, String aliases[]) {
        writeLock.lock();
        try {
            ViewType viewtype;
            if (allViewTypes.containsKey(name))
                throw new IllegalArgumentException((new StringBuilder()).append("Failed to create ViewType; an existing ViewType with name  ")
                        .append(name).append(" already exists").toString());
            for (int i = 0; i < aliases.length; i++) {
                String alias = aliases[i];
                if (allViewTypes.containsKey(alias))
                    throw new IllegalArgumentException((new StringBuilder()).append("Failed to create ViewType; an existing ViewType with alias  ")
                            .append(alias).append(" already exists").toString());
            }

            ViewType viewType = new ViewType(name, aliases);
            allViewTypes.put(name, viewType);
            for (int i = 0; i < aliases.length; i++) {
                String alias = aliases[i];
                allViewTypes.put(alias, viewType);
            }

            viewtype = viewType;
            return viewtype;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes a ViewType. Its name and all its aliases are free to be used in new ViewTypes
     * @param viewType
     * @return
     */
    public static boolean removeViewType(ViewType viewType) {
        writeLock.lock();
        boolean flag;
        boolean result;
        try {
            label0: {
                result = allViewTypes.remove(viewType.getCanonicalName()) != null;
                if (!result)
                    break label0;
                Iterator<String> it = viewType.getAliases().iterator();
                ViewType aliasedView;
                do {
                    if (!it.hasNext())
                        break label0;
                    String alias = it.next();
                    aliasedView = allViewTypes.remove(alias);
                } while (assertionsDisabled || viewType.equals(aliasedView));
                throw new AssertionError();
            }
            flag = result;
            return flag;
        } finally {
            writeLock.unlock();
        }
    }

    public String getCanonicalName() {
        return name;
    }

    public Collection<String> getAliases() {
        return aliases;
    }

    public static ViewType valueOf(String value) {
        readLock.lock();
        try {
            ViewType viewtype;
            ViewType result = allViewTypes.get(value);
            if (result == null)
                throw new IllegalArgumentException((new StringBuilder()).append("No such ViewType: ").append(value).toString());
            viewtype = result;
            return viewtype;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }

}