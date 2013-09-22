package org.cfr.restlet.ext.velocity;

import java.io.IOException;
import java.util.List;

import org.apache.velocity.Template;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the Velocity Template objects and Representations. The
 * adjoined data model is based on the request and response objects.
 *
 * @author Thierry Boileau.
 */
public class VelocityConverter extends ConverterHelper {

    public VelocityConverter() throws Exception {
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        return null;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        return null;
    }

    @Override
    public <T> float score(Representation source, Class<T> target, UniformResource resource) {
        return -1.0f;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {

        if (source instanceof Template) {
            return 1.0f;
        }

        return -1.0f;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target, UniformResource resource) throws IOException {
        return null;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target, UniformResource resource) throws IOException {

        if (source instanceof Template) {
            TemplateRepresentation tr = new TemplateRepresentation((Template) source, target.getMediaType());
            tr.setDataModel(resource.getRequest(), resource.getResponse());
            return tr;
        }
        return null;
    }
}