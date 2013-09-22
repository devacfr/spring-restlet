package org.cfr.restlet.ext.shindig.protocol.multipart;

import java.io.IOException;
import java.util.Collection;

import org.apache.shindig.protocol.multipart.FormDataItem;
import org.restlet.Request;

import com.google.inject.ImplementedBy;

/**
 * Class providing a facade over multipart form handling.
 * Based on {@link org.apache.shindig.protocol.multipart.MultipartFormParser}
 */
@ImplementedBy(DefaultMultipartFormParser.class)
public interface MultipartFormParser {

    /** Parse a request into a list of data items */
    Collection<FormDataItem> parse(Request request) throws IOException;

    /**
     * @return true if the request requires multipart parsing.
     */
    boolean isMultipartContent(Request request);
}