package org.cfr.restlet.ext.shindig.protocol.multipart;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.shindig.protocol.multipart.FormDataItem;


/**
 * Implementation of FormDataItem using Apache commons FileItem.
 * Based on org.apache.shindig.protocol.multipart.CommonsFormData
 */
class CommonsFormDataItem implements FormDataItem {
    private final FileItem fileItem;

    CommonsFormDataItem(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    public byte[] get() {
        return fileItem.get();
    }

    public String getAsString() {
        return fileItem.getString();
    }

    public String getContentType() {
        return fileItem.getContentType();
    }

    public String getFieldName() {
        return fileItem.getFieldName();
    }

    public InputStream getInputStream() throws IOException {
        return fileItem.getInputStream();
    }

    public String getName() {
        return fileItem.getName();
    }

    public long getSize() {
        return fileItem.getSize();
    }

    public boolean isFormField() {
        return fileItem.isFormField();
    }
}
