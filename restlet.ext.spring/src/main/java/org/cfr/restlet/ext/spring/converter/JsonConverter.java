package org.cfr.restlet.ext.spring.converter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ezmorph.MorpherRegistry;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.EnumMorpher;
import net.sf.json.util.JSONUtils;

import org.apache.commons.lang.time.DateFormatUtils;
import org.cfr.restlet.ext.spring.ResourceRegister;
import org.cfr.restlet.ext.spring.morpher.SimpleDateMorpher;
import org.cfr.restlet.ext.spring.representation.JsonRepresentation;
import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

import com.pmi.restlet.mapping.ClassDescriptor;

public class JsonConverter extends ConverterHelper implements IObjectRegistering {

    private final JsonConfig config;

    public JsonConfig getConfig() {
        return config;
    }

    public JsonConverter() {
        this.config = new JsonConfig();
        this.config.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {

            @Override
            public Object processArrayValue(Object value, JsonConfig config) {
                return value;
            }

            @Override
            public Object processObjectValue(String key, Object value, JsonConfig config) {
                if (value != null && value instanceof Date) {
                    return DateFormatUtils.ISO_DATETIME_FORMAT.format((Date) value);
                }
                return value;
            }
        });
        // override Date converter because ezMorph doesn't accept ISO8601 format
        // date
        MorpherRegistry morpherRegistry = JSONUtils.getMorpherRegistry();
        morpherRegistry.registerMorpher(new SimpleDateMorpher(DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()), true);
    }

    protected JsonRepresentation create(MediaType mediaType, Object source) {
        return new JsonRepresentation(this.config, mediaType, source);
    }

    protected JsonRepresentation create(Representation source, Class<?> objectClass) {
        return new JsonRepresentation(this.config, source, objectClass);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;
        if (VARIANT_JSON.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, JsonRepresentation.class);
        }
        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;
        if (source != null)
            result = addVariant(result, VARIANT_JSON);
        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1F;
        if (source instanceof JsonRepresentation)
            result = 1.0F;
        else if (VARIANT_JSON.isCompatible(target))
            result = 0.8F;
        else
            result = 0.5F;
        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target, UniformResource resource) {
        float result = -1F;
        if (target != null && VARIANT_JSON.isCompatible(source))
            result = 0.8F;
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target, UniformResource resource) throws IOException {
        if (target == null) {
            return (T) source;
        }
        Object result = null;
        if (source instanceof JsonRepresentation)
            result = ((JsonRepresentation) source).getObject();
        else if (VARIANT_JSON.isCompatible(source))
            result = create(source, target).getObject();
        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target, UniformResource resource) {
        Representation result = null;
        if (source instanceof JsonRepresentation) {
            result = (JsonRepresentation) source;
        } else {
            if (target.getMediaType() == null)
                target.setMediaType(MediaType.APPLICATION_JSON);
            if (VARIANT_JSON.isCompatible(target)) {
                JsonRepresentation representation = create(target.getMediaType(), source);
                result = representation;
            }
        }
        return result;
    }

    private static final VariantInfo VARIANT_JSON;

    static {
        VARIANT_JSON = new VariantInfo(MediaType.APPLICATION_JSON);
    }

    @Override
    public void registerObjects(ResourceRegister register) {
        this.config.clearPropertyExclusions();
        if (!register.isEmpty()) {
            Set<Class<? extends Enum<?>>> enums = new HashSet<Class<? extends Enum<?>>>();
            Collection<ClassDescriptor> cls = register.getClassDescriptors();
            Map<String, Class<?>> classMap = new Hashtable<String, Class<?>>(cls.size());
            for (ClassDescriptor cl : cls) {
                Class<?> clazz = cl.getTargetClass();
                if (Enum.class.isAssignableFrom(clazz)) {
                    enums.add((Class<? extends Enum<?>>) clazz);
                } else {
                    classMap.put(cl.getName(), clazz);
                    Collection<String> fields = cl.getExcludesFields();
                    for (String fieldName : fields) {
                        this.config.registerPropertyExclusion(clazz, fieldName);
                    }
                }
            }
            MorpherRegistry morpherRegistry = JSONUtils.getMorpherRegistry();
            for (Class<? extends Enum> e : enums) {
                morpherRegistry.registerMorpher(new EnumMorpher(e));
            }
            this.config.setClassMap(classMap);
        }
    }
}
