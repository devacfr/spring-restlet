package org.cfr.restlet.ext.spring.morpher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

import net.sf.ezmorph.MorphException;
import net.sf.ezmorph.ObjectMorpher;

public class SimpleDateMorpher implements ObjectMorpher {

	SimpleDateFormat dateParser = new SimpleDateFormat(DateFormatUtils.ISO_DATETIME_FORMAT.getPattern());

	public SimpleDateMorpher(String pattern) {
		dateParser = new SimpleDateFormat(pattern);
	}

	@Override
	public Object morph(Object value) {
		if (value == null) {
			return null;
		}

		if (Date.class.isAssignableFrom(value.getClass())) {
			return (Date) value;
		}

		if (!supports(value.getClass())) {
			throw new MorphException(value.getClass() + " is not supported");
		}
		try {
			return dateParser.parse(value.toString());
		} catch (ParseException e) {
			// no-op
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class morphsTo() {
		return Date.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean supports(Class clazz) {
		return String.class.isAssignableFrom(clazz);
	}
}
