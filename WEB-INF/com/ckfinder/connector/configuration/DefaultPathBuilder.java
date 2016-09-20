/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.configuration.IBasePathBuilder;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.PathUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

public class DefaultPathBuilder
implements IBasePathBuilder {
    private static final Logger DefPathBuilderLogger = Logger.getLogger(DefaultPathBuilder.class.getName());

    @Override
    public String getBaseDir(HttpServletRequest request) {
        String newBaseUrl = this.getBaseUrl(request);
        if (Pattern.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", newBaseUrl)) {
            if (newBaseUrl.indexOf(request.getContextPath()) >= 0) {
                newBaseUrl = newBaseUrl.substring(newBaseUrl.indexOf(request.getContextPath()));
            } else if (newBaseUrl.indexOf("/") >= 0) {
                newBaseUrl = PathUtils.removeSlashFromEnd(newBaseUrl);
                newBaseUrl = newBaseUrl.substring(newBaseUrl.lastIndexOf("/"));
            } else {
                return "/";
            }
        }
        try {
            return FileUtils.calculatePathFromBaseUrl(newBaseUrl);
        }
        catch (Exception e) {
            DefPathBuilderLogger.log(Level.SEVERE, "Could not create path for: " + newBaseUrl, e);
            return newBaseUrl;
        }
    }

    @Override
    public String getBaseUrl(HttpServletRequest request) {
        return request.getContextPath().concat("/userfiles");
    }
}

