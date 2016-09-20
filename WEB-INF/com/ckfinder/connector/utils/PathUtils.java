/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.utils;

import java.util.regex.Pattern;

public class PathUtils {
    public static String escape(String string) {
        if (string == null || string.equals("")) {
            return string;
        }
        String prefix = "";
        if (string.indexOf("://") != -1) {
            prefix = string.substring(0, string.indexOf("://") + 3);
            string = string.replaceFirst(prefix, "");
        }
        string = (string = string.replaceAll("\\\\", "/")).startsWith("//") ? "/" + string.replaceAll("/+", "/") : string.replaceAll("/+", "/");
        return prefix.concat(string);
    }

    public static String addSlashToEnd(String string) {
        if (string != null && !string.equals("") && string.charAt(string.length() - 1) != '/') {
            return string.concat("/");
        }
        return string;
    }

    public static String addSlashToBeginning(String string) {
        if (string == null || string.charAt(0) == '/' || Pattern.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", string)) {
            return string;
        }
        return "/".concat(string);
    }

    public static String removeSlashFromBeginning(String string) {
        if (string != null && !string.equals("") && string.charAt(0) == '/') {
            return string.substring(1, string.length());
        }
        return string;
    }

    public static String removeSlashFromEnd(String string) {
        if (string != null && !string.equals("") && string.charAt(string.length() - 1) == '/') {
            return string.substring(0, string.length() - 1);
        }
        return string;
    }
}

