package org.cfr.restlet.ext.spring.representation;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

public class JavascriptExecutionRepresentation extends StringRepresentation {

    private static String createCommand(String command, Object... parameters) {
        StringBuilder text = new StringBuilder(command);
        text.append('(');
        int i = 0;
        int len = parameters.length;
        for (Object obj : parameters) {
            String value = null;
            boolean isString = true;
            if (obj instanceof String || obj instanceof StringBuilder) {
                value = obj.toString();
            } else if (obj instanceof Representation) {
                Representation representation = (Representation) obj;
                try {
                    value = representation.getText();
                } catch (IOException e) {

                }
                isString = !representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON);

            } else {
                value = obj.toString().replace("\n", "");
                isString = false;
            }

            if (value != null) {
                value = value.replace("\n", "");
                value = value.replace("\r", "");
                value = value.replace("'", "&quot;");
                if (isString)
                    text.append('\'');
                text.append(value);
                if (isString)
                    text.append('\'');
            }
            i++;
            if (i < len) {
                text.append(',');
            }
        }
        text.append(')');
        return text.toString();
    }

    public JavascriptExecutionRepresentation(String command, Object... parameters) {
        super(createCommand(command, parameters), MediaType.TEXT_JAVASCRIPT);
    }
}
