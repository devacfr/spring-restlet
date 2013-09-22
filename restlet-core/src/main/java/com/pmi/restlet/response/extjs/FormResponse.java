package com.pmi.restlet.response.extjs;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class FormResponse extends DefaultResourceResponse {


	private Object data;

	public FormResponse() {
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
