package org.cfr.restlet.ext.xstream;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import com.thoughtworks.xstream.XStream;

/**
 * Representation based on the XStream library. It can serialize and deserialize
 * automatically in either JSON or XML.
 * 
 * @see <a href="http://xstream.codehaus.org/">XStream project</a>
 * @param <T>
 *            The type to wrap.
 */
public class XstreamRepresentation<T> extends WriterRepresentation {

    /** The (parsed) object to format. */
    private final T object;

    /** The representation to parse. */
    private final Representation representation;

    private XStream stream = null;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The target media type.
     * @param object
     *            The object to format.
     */
    public XstreamRepresentation(XStream stream, MediaType mediaType, T object) {
        super(mediaType);
        this.object = object;
        this.representation = null;
        this.stream = stream;
    }

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation to parse.
     */
    public XstreamRepresentation(XStream stream, Representation representation) {
        super(representation.getMediaType());
        this.object = null;
        this.representation = representation;
        this.stream = stream;
    }

    public XStream getXstream() {
        return stream;
    }

    @SuppressWarnings("unchecked")
    public T getObject() {
        T result = null;

        if (this.object != null) {
            result = this.object;
        } else if (this.representation != null) {
            try {
                result = (T) getXstream().fromXML(this.representation.getStream());
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING, "Unable to parse the object with XStream.", e);
            }
        }

        return result;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (representation != null) {
            representation.write(writer);
        } else if (object != null) {
            CharacterSet charSet = (getCharacterSet() == null) ? CharacterSet.UTF_8 : getCharacterSet();
            if (!MediaType.APPLICATION_JSON.isCompatible(getMediaType())) {
                writer.append("<?xml version=\"1.0\" encoding=\"" + charSet.getName() + "\" ?>\n");
            }
            getXstream().toXML(object, writer);
        }
    }
}