package com.pmi.restlet.response.extjs;

public interface IResourceResponse {


    /**
     * Set an encoding used for reading/writing the model.
     *
     * @param modelEncoding the encoding used when reading/writing the model.
     */
    void setModelEncoding( String modelEncoding );

    /**
     * @return the current encoding used when reading/writing this model.
     */
    String getModelEncoding();
}
