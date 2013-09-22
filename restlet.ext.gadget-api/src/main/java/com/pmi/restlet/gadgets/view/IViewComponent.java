package com.pmi.restlet.gadgets.view;

import java.io.IOException;
import java.io.Writer;

public interface IViewComponent {

    public abstract void writeTo(Writer writer) throws IOException;
}