package org.cfr.restlet.ext.shindig.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.shindig.common.util.JsonConversionUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * 
 * @author cfriedri
 * @see JsonConversionUtil
 */
public class ShindigJsonConversionUtil {

    private static final Pattern ARRAY_MATCH = Pattern.compile("(\\w+)\\((\\d+)\\)");

    private static final Set<String> RESERVED_PARAMS = ImmutableSet.of("method", "id", "st", "oauth_token");

    public static JSONObject fromRequest(Form form) throws JSONException {
        Map<String, String> params = form.getValuesMap();

        if (!params.containsKey("method")) {
            return null;
        }
        JSONObject root = new JSONObject();
        root.put("method", params.get("method"));
        if (params.containsKey("id")) {
            root.put("id", params.get("id"));
        }
        JSONObject paramsRoot = new JSONObject();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!RESERVED_PARAMS.contains(entry.getKey().toLowerCase())) {
                String[] path = StringUtils.splitPreserveAllTokens(entry.getKey(), '.');
                JSONObject holder = buildHolder(paramsRoot, path, 0);
                holder.put(path[path.length - 1], convertToJsonValue(entry.getValue()));
            }
        }
        if (paramsRoot.length() > 0) {
            root.put("params", paramsRoot);
        }
        return root;

    }

    public static Map<String, String> fromJson(JSONObject obj) throws JSONException {
        Map<String, String> result = Maps.newHashMap();
        collect(obj, "", result);
        return result;
    }

    private static void collect(Object current, String prefix, Map<String, String> result) throws JSONException {
        if (current == null) {
            result.put(prefix, "null");
            return;
        }

        if (current instanceof JSONObject) {
            JSONObject json = (JSONObject) current;
            Iterator<?> keys = json.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (json.isNull(key)) {
                    result.put(prefix + '.' + key, "null");
                } else {
                    collect(json.get(key), prefix + '.' + key, result);
                }
            }
        } else if (current instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) current;
            if (isAllLiterals(jsonArr)) {
                // The array is all simple value types
                String jsonArrayString = jsonArr.toString();
                //Strip [ & ]
                jsonArrayString = jsonArrayString.substring(1, jsonArrayString.length() - 1);
                if (jsonArr.length() == 1) {
                    jsonArrayString = '(' + jsonArrayString + ')';
                }
                result.put(prefix, jsonArrayString);
            } else {
                for (int i = 0; i < jsonArr.length(); i++) {
                    if (jsonArr.isNull(i)) {
                        result.put(prefix + '(' + i + ')', "null");
                    } else {
                        collect(jsonArr.get(i), prefix + '(' + i + ')', result);
                    }
                }
            }
        } else {
            result.put(prefix, current.toString());
        }
    }

    public static boolean isAllLiterals(JSONArray jsonArr) throws JSONException {
        for (int i = 0; i < jsonArr.length(); i++) {
            if (!jsonArr.isNull(i) && (jsonArr.get(i) instanceof JSONObject || jsonArr.get(i) instanceof JSONArray)) {
                return false;
            }
        }
        return true;
    }

    static JSONObject parametersToJsonObject(Map<String, String> params) throws JSONException {
        JSONObject root = new JSONObject();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String[] path = StringUtils.splitPreserveAllTokens(entry.getKey(), '.');
            JSONObject holder = buildHolder(root, path, 0);
            if (path.length > 1) {
                holder.put(path[path.length - 1], convertToJsonValue(entry.getValue()));
            } else {
                holder.put(path[0], convertToJsonValue(entry.getValue()));
            }
        }

        return root;
    }

    /**
     * Parse the steps in the path into JSON Objects.
     */
    static JSONObject buildHolder(JSONObject root, String[] steps, int currentStep) throws JSONException {
        if (currentStep > steps.length - 2) {
            return root;
        } else {
            Matcher matcher = ARRAY_MATCH.matcher(steps[currentStep]);
            if (matcher.matches()) {
                // Handle as array
                String fieldName = matcher.group(1);
                int index = Integer.parseInt(matcher.group(2));
                JSONArray newArrayStep;
                if (root.has(fieldName)) {
                    newArrayStep = root.getJSONArray(fieldName);
                } else {
                    newArrayStep = new JSONArray();
                    root.put(fieldName, newArrayStep);
                }
                JSONObject newStep = new JSONObject();
                newArrayStep.put(index, newStep);
                return buildHolder(newStep, steps, ++currentStep);
            } else {
                JSONObject newStep;
                if (root.has(steps[currentStep])) {
                    newStep = root.getJSONObject(steps[currentStep]);
                } else {
                    newStep = new JSONObject();
                    root.put(steps[currentStep], newStep);
                }
                return buildHolder(newStep, steps, ++currentStep);
            }
        }
    }

    static Object convertToJsonValue(String value) throws JSONException {
        if (value == null) {
            return null;
        } else if (value.startsWith("(") && value.endsWith(")")) {
            // explicit form of literal array
            return new JSONArray('[' + value.substring(1, value.length() - 1) + ']');
        } else {
            try {
                // inferred parsing of literal array
                // Attempt to parse as an array of literals
                JSONArray parsedArray = new JSONArray('[' + value + ']');
                if (parsedArray.length() == 1) {
                    // Not an array.
                    Object obj = parsedArray.get(0);
                    if (obj instanceof Double && !obj.toString().equals(value)) {
                        // Numeric overflow or truncation occurred. ie. large int/long
                        // converted to Double with exponent or Double truncated.
                        // In Shindig we return this as a verbatim String to avoid
                        // loss of data on input, as with lengthy ID values consisting
                        // of only numbers.
                        return value;
                    }
                    return obj;
                }
                return parsedArray;
            } catch (JSONException je) {
                return value;
            }
        }
    }
}
