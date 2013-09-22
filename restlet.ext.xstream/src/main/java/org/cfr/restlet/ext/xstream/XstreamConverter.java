package org.cfr.restlet.ext.xstream;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Converter between the XML/JSON and Representation classes based on XStream.
 *
 */
public class XstreamConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_APPLICATION_ALL_XML = new VariantInfo(MediaType.APPLICATION_ALL_XML);

    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(MediaType.APPLICATION_XML);

    private static final VariantInfo VARIANT_JSON = new VariantInfo(MediaType.APPLICATION_JSON);

    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(MediaType.TEXT_XML);

    /** The XStream XML driver class. */
    private Class<? extends HierarchicalStreamDriver> xmlDriverClass;

    /** The XStream JSON driver class. */
    private Class<? extends HierarchicalStreamDriver> jsonDriverClass;

    /** The modifiable XStream object. */
    private XStream xstreamXml;

    private XStream xstreamJson;

    public XstreamConverter() throws Exception {
        this.jsonDriverClass = JettisonMappedXmlDriver.class;
        this.xmlDriverClass = DomDriver.class;
    }

    public XStream getXstream(MediaType mediaType) {
        if (MediaType.APPLICATION_JSON.isCompatible(mediaType)) {
            if (this.xstreamJson == null) {
                this.xstreamJson = createXstream(mediaType);
            }
            return this.xstreamJson;
        } else {
            if (this.xstreamXml == null) {
                this.xstreamXml = createXstream(mediaType);
            }
            return this.xstreamXml;
        }
    }

    /**
     * Creates an XStream object based on a media type. By default, it creates a
     * {@link HierarchicalStreamDriver} or a {@link DomDriver}.
     *
     * @param mediaType
     *            The serialization media type.
     * @return The XStream object.
     */
    protected XStream createXstream(MediaType mediaType) {
        XStream result = null;

        try {
            if (MediaType.APPLICATION_JSON.isCompatible(mediaType)) {
                result = new XStream(getJsonDriverClass().newInstance());
                result.setMode(XStream.NO_REFERENCES);
            } else {
                result = new XStream(getXmlDriverClass().newInstance());
            }
            result.autodetectAnnotations(true);
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
     * Returns the XStream JSON driver class.
     *
     * @return TXStream JSON driver class.
     */
    public Class<? extends HierarchicalStreamDriver> getJsonDriverClass() {
        return jsonDriverClass;
    }

    /**
     * Sets the XStream JSON driver class.
     *
     * @param jsonDriverClass
     *            The XStream JSON driver class.
     */
    public void setJsonDriverClass(Class<? extends HierarchicalStreamDriver> jsonDriverClass) {
        this.jsonDriverClass = jsonDriverClass;
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
     * Creates the marshaling {@link XstreamRepresentation}.
     *
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link XstreamRepresentation}.
     */
    protected <T> XstreamRepresentation<T> create(MediaType mediaType, T source) {
        return new XstreamRepresentation<T>(this.getXstream(mediaType), mediaType, source);
    }

    /**
     * Creates the unmarshaling {@link XstreamRepresentation}.
     *
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @return The unmarshaling {@link XstreamRepresentation}.
     */
    protected <T> XstreamRepresentation<T> create(Representation source) {
        return new XstreamRepresentation<T>(getXstream(source.getMediaType()), source);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_APPLICATION_ALL_XML.isCompatible(source) || VARIANT_APPLICATION_XML.isCompatible(source) || VARIANT_TEXT_XML.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, XstreamRepresentation.class);
        } else if (VARIANT_JSON.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, XstreamRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            result = addVariant(result, VARIANT_JSON);
            result = addVariant(result, VARIANT_APPLICATION_ALL_XML);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_TEXT_XML);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof XstreamRepresentation<?>) {
            result = 1.0F;
        } else {
            if (VARIANT_JSON.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(target) || VARIANT_APPLICATION_XML.isCompatible(target)
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
            if (VARIANT_JSON.isCompatible(source)) {
                result = 0.8F;
            } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(source) || VARIANT_APPLICATION_XML.isCompatible(source)
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

        // The source for the XStream conversion
        XstreamRepresentation<?> xstreamSource = null;

        if (source instanceof EmptyRepresentation) {
            // nothing to do
        } else if (source instanceof XstreamRepresentation) {
            xstreamSource = ((XstreamRepresentation) source);
        } else if (VARIANT_JSON.isCompatible(source)) {
            xstreamSource = create(source);
        } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(source) || VARIANT_APPLICATION_XML.isCompatible(source)
                || VARIANT_TEXT_XML.isCompatible(source)) {
            xstreamSource = create(source);
        }

        if (xstreamSource != null) {
            // Handle the conversion
            if ((target != null) && XstreamRepresentation.class.isAssignableFrom(target)) {
                result = xstreamSource;
            } else {
                if (target != null) {
                    // XStream 1.3.1 does not process annotations when called
                    // using fromXML(InputStream) despite autoProcessAnnotations
                    // being set to "true". This call forces the processing.
                    xstreamSource.getXstream().processAnnotations(target);
                }

                result = xstreamSource.getObject();
            }
        }

        return (T) result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Representation toRepresentation(Object source, Variant target, UniformResource resource) {
        Representation result = null;

        if (source instanceof XstreamRepresentation) {
            result = (XstreamRepresentation) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.TEXT_XML);
            }

            if (VARIANT_JSON.isCompatible(target)) {
                XstreamRepresentation<Object> xstreamRepresentation = create(target.getMediaType(), source);
                result = xstreamRepresentation;
            } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(target) || VARIANT_APPLICATION_XML.isCompatible(target)
                    || VARIANT_TEXT_XML.isCompatible(target)) {
                result = create(target.getMediaType(), source);
            }
        }

        return result;
    }

    public void registerObjects(Class<?>... classes) {
        this.getXstream(MediaType.APPLICATION_JSON).processAnnotations(classes);
        this.getXstream(MediaType.TEXT_XML).processAnnotations(classes);
    }

}