package com.pmi.restlet.utils;

import org.apache.commons.lang.StringUtils;


public final class StrUtils {

    private StrUtils() {
    }

    /*
     * public static boolean startsWith (String str1, String str2) { return
     * startsWith (str1, str2, false); }
     * 
     * 
     * public static boolean startsWith (String str1, String str2, boolean
     * ignore_case) { int l2 = str2.length(); if (l2 == 0) return true;
     * 
     * int l1 = str1.length(); if (l2 > l1) return false;
     * 
     * return (0 == String.Compare (str1, 0, str2, 0, l2, ignore_case)); }
     * 
     * public static boolean endsWith (String str1, String str2) { return
     * endsWith (str1, str2, false); }
     * 
     * public static boolean endsWith (String str1, String str2, boolean
     * ignore_case) { int l2 = str2.length(); if (l2 == 0) return true;
     * 
     * int l1 = str1.length(); if (l2 > l1) return false;
     * 
     * return (0 == String.Compare (str1, l1 - l2, str2, 0, l2, ignore_case)); }
     * 
     */

    public static String escapeQuotesAndBackslashes(String attributeValue) {
        StringBuilder sb = null;
        for (int i = 0; i < attributeValue.length(); i++) {
            char ch = attributeValue.charAt(i);
            if (ch == '\'' || ch == '"' || ch == '\\') {
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append(attributeValue.substring(0, i));
                }
                sb.append('\\');
                sb.append(ch);
            } else {
                if (sb != null) {
                    sb.append(ch);
                }
            }
        }
        if (sb != null) {
            return sb.toString();
        }
        return attributeValue;
    }

    public static boolean isNullOrEmpty(String value) {
        return !StringUtils.isNotEmpty(value);
    }

}