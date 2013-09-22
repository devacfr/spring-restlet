package org.cfr.restlet.ext.spring.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.cfr.restlet.ext.spring.ResourceRegister;
import org.cfr.restlet.ext.spring.representation.OxmRepresentation;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
import org.springframework.oxm.support.AbstractMarshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.util.StringUtils;

import com.pmi.restlet.mapping.ClassDescriptor;
import com.thoughtworks.xstream.converters.ConverterMatcher;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converter between the XML/JSON and Representation classes based on XStream.
 *
 */
public class OxmConverter extends ConverterHelper implements IObjectRegistering {

    private static final VariantInfo VARIANT_APPLICATION_ALL_XML = new VariantInfo(MediaType.APPLICATION_ALL_XML);

    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(MediaType.APPLICATION_XML);

    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(MediaType.TEXT_XML);

    /** The XStream XML driver class. */
    private Class<? extends HierarchicalStreamDriver> xmlDriverClass;

    /** The modifiable XStream object. */
    private AbstractMarshaller xstreamXml;

    public OxmConverter() throws Exception {
        this.xmlDriverClass = DomDriver.class;
    }

    public AbstractMarshaller getMarshaller() {
        if (this.xstreamXml == null) {
            this.xstreamXml = createMarshaller();
        }
        return this.xstreamXml;
    }

    /**
     * Creates an XStream object based on a media type. By default, it creates a
     * {@link HierarchicalStreamDriver} or a {@link DomDriver}.
     * @return The XStream object.
     */
    protected AbstractMarshaller createMarshaller() {
        AbstractMarshaller result = null;

        try {
            XStreamMarshaller marshaller = new XStreamMarshaller();
            marshaller.setStreamDriver(getXmlDriverClass().newInstance());
            result = marshaller;
        } catch (Exception e) {
            Context.getCurrentLogger().log(Level.WARNING, "Unable to create the XStream driver.", e);
        }

        return result;
    }

    /**
     * Returns the XStream XML driver class.
     *
     * @return The XStream XML driver class.
     */
    public Class<? extends HierarchicalStreamDriver> getXmlDriverClass() {
        return xmlDriverClass;
    }

    /**
     * Sets the XStream XML driver class.
     *
     * @param xmlDriverClass
     *            The XStream XML driver class.
     */
    public void setXmlDriverClass(Class<? extends HierarchicalStreamDriver> xmlDriverClass) {
        this.xmlDriverClass = xmlDriverClass;
    }

    /**
     * Creates the marshaling {@link OxmRepresentation}.
     *
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link OxmRepresentation}.
     */
    protected <T> OxmRepresentation<T> create(MediaType mediaType, T source) {
        return new OxmRepresentation<T>(this.getMarshaller(), mediaType, source);
    }

    /**
     * Creates the unmarshaling {@link OxmRepresentation}.
     *
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @return The unmarshaling {@link OxmRepresentation}.
     */
    protected <T> OxmRepresentation<T> create(Representation source) {
        return new OxmRepresentation<T>(getMarshaller(), source);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_APPLICATION_ALL_XML.isCompatible(source) || VARIANT_APPLICATION_XML.isCompatible(source) || VARIANT_TEXT_XML.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, OxmRepresentation.class);
        }
        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            result = addVariant(result, VARIANT_APPLICATION_ALL_XML);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_TEXT_XML);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof OxmRepresentation<?>) {
            result = 1.0F;
        } else {
            if (VARIANT_APPLICATION_ALL_XML.isCompatible(target) || VARIANT_APPLICATION_XML.isCompatible(target)
                    || VARIANT_TEXT_XML.isCompatible(target)) {
                result = 0.8F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target, UniformResource resource) {
        float result = -1.0F;

        if (target != null) {
            if (VARIANT_APPLICATION_ALL_XML.isCompatible(source) || VARIANT_APPLICATION_XML.isCompatible(source)
                    || VARIANT_TEXT_XML.isCompatible(source)) {
                result = 0.8F;
            }
        } else {
            result = 0.5F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target, UniformResource resource) throws IOException {
        Object result = null;

        if (source instanceof OxmRepresentation) {
            result = ((OxmRepresentation) source).getObject();
        } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(source) || VARIANT_APPLICATION_XML.isCompatible(source)
                || VARIANT_TEXT_XML.isCompatible(source)) {
            if (target != null) {
                result = create(source).getObject();
            } else {
                return (T) source;
            }
        }

        return (T) result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Representation toRepresentation(Object source, Variant target, UniformResource resource) {
        Representation result = null;

        if (source instanceof OxmRepresentation) {
            result = (OxmRepresentation) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.TEXT_XML);
            }

            if (VARIANT_APPLICATION_ALL_XML.isCompatible(target) || VARIANT_APPLICATION_XML.isCompatible(target)
                    || VARIANT_TEXT_XML.isCompatible(target)) {
                result = create(target.getMediaType(), source);
            }
        }

        return result;
    }

    @Override
    public void registerObjects(ResourceRegister register) throws Exception {
        registerClassDescriptor(getMarshaller(), register);
    }

    public void registerClassDescriptor(AbstractMarshaller marshaller, ResourceRegister register) throws Exception {
        if (marshaller instanceof XStreamMarshaller) {
            registerXStreamMarshaller((XStreamMarshaller) marshaller, register);
        }
    }

    public void registerXStreamMarshaller(XStreamMarshaller marshaller, ResourceRegister register) throws Exception {
        marshaller.setSupportedClasses(register.getTargetClasses());
        if (!register.isEmpty()) {
            Collection<ClassDescriptor> cls = register.getClassDescriptors();
            Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
            Map<Class<?>, String> omittedFields = new HashMap<Class<?>, String>();
            for (ClassDescriptor cl : cls) {
                Class<?> clazz = cl.getTargetClass();
                String name = cl.getName();
                if (!StringUtils.hasLength(name))
                    name = clazz.getSimpleName();
                aliases.put(name, clazz);
                Collection<String> fields = cl.getExcludesFields();
                for (String fieldName : fields) {
                    omittedFields.put(clazz, fieldName);
                }

            }
            marshaller.setOmittedFields(omittedFields);
            marshaller.setAliases(aliases);
        }

    }

    public void registerConverter(Class<? extends ConverterMatcher>... converterClass) throws Exception {
        XStreamMarshaller marshaller = (XStreamMarshaller) getMarshaller();
        marshaller.setConverters(new ConverterMatcher[] {});
        if (converterClass == null) {
            return;
        }

        List<ConverterMatcher> converters = new ArrayList<ConverterMatcher>(converterClass.length);
        for (Class<? extends ConverterMatcher> clazz : converterClass) {
            if (clazz == null)
                continue;
            ConverterMatcher converter = clazz.getConstructor(Mapper.class).newInstance(marshaller.getXStream().getMapper());
            converters.add(converter);

        }
        marshaller.setConverters(converters.toArray(new ConverterMatcher[converters.size()]));
    }

}