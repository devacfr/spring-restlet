package org.cfr.restlet.ext.spring.representation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.springframework.oxm.support.AbstractMarshaller;



/**
 * Representation based on the XStream library. It can serialize and deserialize
 * automatically in either JSON or XML.
 *
 * @see <a href="http://xstream.codehaus.org/">XStream project</a>
 * @param <T>
 *            The type to wrap.
 */
public class OxmRepresentation<T> extends OutputRepresentation {


    /** The (parsed) object to format. */
    private final T object;

    /** The representation to parse. */
    private final Representation representation;



    private AbstractMarshaller marshaller = null;




    /**
     * Constructor.
     *
     * @param mediaType
     *            The target media type.
     * @param object
     *            The object to format.
     */
    public OxmRepresentation(AbstractMarshaller marshaller, MediaType mediaType, T object) {
        super(mediaType);
        this.object = object;
        this.representation = null;
        this.marshaller = marshaller;
    }

    /**
     * Constructor.
     *
     * @param representation
     *            The representation to parse.
     */
    public OxmRepresentation(AbstractMarshaller marshaller, Representation representation) {
        super(representation.getMediaType());
        this.object = null;
        this.representation = representation;
        this.marshaller = marshaller;
    }


    public AbstractMarshaller getMarshaller() {
        return marshaller;
    }


    @SuppressWarnings("unchecked")
    public T getObject() {
        T result = null;

        if (this.object != null) {
            result = this.object;
        } else if (this.representation != null) {
            try {
                result = (T) getMarshaller().unmarshal(new StreamSource(this.representation.getStream() ));
            } catch (IOException e) {
                Context.getCurrentLogger().log(Level.WARNING,
                        "Unable to parse the object with XStream.", e);
            }
        }

        return result;
    }




    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (representation != null) {
            representation.write(outputStream);
        } else if (object != null) {
            StreamResult result = new StreamResult(outputStream);
            getMarshaller().marshal(object, result);
        }
    }
}