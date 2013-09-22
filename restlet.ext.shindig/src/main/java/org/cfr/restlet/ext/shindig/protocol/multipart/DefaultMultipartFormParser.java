package org.cfr.restlet.ext.shindig.protocol.multipart;

import java.io.IOException;
import java.net.UnknownServiceException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.shindig.protocol.multipart.FormDataItem;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.fileupload.RestletFileUpload;

import com.google.common.collect.Lists;


/**
 * Implementation of MultipartFormParser using Apache Commons file upload.
 * Based on {@link org.apache.shindig.protocol.multipart.DefaultMultipartFormParser}
 */
public class DefaultMultipartFormParser implements MultipartFormParser {
    private static final String MULTIPART = "multipart/";

    public Collection<FormDataItem> parse(Request request)
    throws IOException  {
        FileItemFactory factory = new DiskFileItemFactory();
        //ServletFileUpload upload = new ServletFileUpload(factory);
        RestletFileUpload upload = new RestletFileUpload(factory);
        try {
            List<FileItem> fileItems = upload.parseRequest(request);
            return convertToFormData(fileItems);
        } catch (FileUploadException e) {
            UnknownServiceException use = new UnknownServiceException("File upload error.");
            use.initCause(e);
            throw use;
        }
    }

    private Collection<FormDataItem> convertToFormData(List<FileItem> fileItems) {
        List<FormDataItem> formDataItems =
            Lists.newArrayListWithCapacity(fileItems.size());
        for (FileItem item : fileItems) {
            formDataItems.add(new CommonsFormDataItem(item));
        }

        return formDataItems;
    }

    public boolean isMultipartContent(Request request) {

        if (!Method.POST.equals(request.getMethod())) {
            return false;
        }
        MediaType contentType = request.getEntity().getMediaType();
        if (contentType == null) {
            return false;
        }
        return contentType.getName().startsWith(MULTIPART);
    }
}