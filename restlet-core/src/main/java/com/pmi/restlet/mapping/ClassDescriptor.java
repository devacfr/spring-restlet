package com.pmi.restlet.mapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.pmi.restlet.utils.Assert;

/**
 * This class allows to Describe mapping class for marshaller and Unmarshaller.
 * @author cfriedri
 *
 */
public class ClassDescriptor {

    private final Class<?> clazz;

    private final String name;

    private final Set<String> excludesFields = new HashSet<String>();

    /**
     * Default construtor
     * @param clazz class associated to this instance
     */
    public ClassDescriptor(Class<?> clazz) {
        this(clazz, clazz.getSimpleName());
    }

    public ClassDescriptor(Class<?> clazz, String name) {
        Assert.notNull(clazz, "clazz parameter is required");
        this.clazz = clazz;
        this.name = (name == null ? this.clazz.getSimpleName() : name);
    }

    /**
     * Prevents a field from being serialized.
     * @param fieldName
     * @return Return the instance of this instance.
     */
    public ClassDescriptor addExcludeField(String fieldName) {
        Assert.hasText(fieldName, "fieldName is required");
        excludesFields.add(fieldName);
        return this;
    }

    /**
     * Prevents collection of fields from being serialized.
     * @param fieldNames collection of field name.
     * @return Return the instance of this instance.
     */
    public ClassDescriptor addExcludeFields(Collection<String> fieldNames) {
        excludesFields.addAll(fieldNames);
        return this;
    }

    /**
     * Prevents collection of fields from being serialized.
     * @param fieldNames array of field name.
     * @return Return the instance of this instance.
     */
    public ClassDescriptor addExcludeFields(String... fieldNames) {
        excludesFields.addAll(Arrays.asList(fieldNames));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassDescriptor)) {
            return false;
        }
        ClassDescriptor cl = (ClassDescriptor) obj;
        return clazz.equals(cl.clazz);
    }

    /**
     * Gets omitted collection of fields from being serialized
     * @return
     */
    public Collection<String> getExcludesFields() {
        return Collections.unmodifiableCollection(excludesFields);
    }

    /**
     * Gets alias ths class to a shorter name to be used in XML elements.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the class associated to this instance
     * @return
     */
    public Class<?> getTargetClass() {
        return clazz;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }
}
