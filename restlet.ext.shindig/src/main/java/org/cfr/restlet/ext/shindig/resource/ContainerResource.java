package org.cfr.restlet.ext.shindig.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.util.ResourceUtils;


public class ContainerResource extends ServerResource {


    @Get
    public String getContainer() throws IOException {
        File file = ResourceUtils.getFile("classpath:config/container.js");

        InputStream input = new FileInputStream(file);
        try {
            return IOUtils.toString(input );
        } finally {
            IOUtils.closeQuietly(input);
        }
    }
}
