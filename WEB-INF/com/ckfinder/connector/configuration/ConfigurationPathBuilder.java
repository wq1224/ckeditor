/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.configuration.ConfigurationFactory;
import com.ckfinder.connector.configuration.DefaultPathBuilder;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.utils.PathUtils;
import javax.servlet.http.HttpServletRequest;

public class ConfigurationPathBuilder
extends DefaultPathBuilder {
    @Override
    public String getBaseUrl(HttpServletRequest request) {
        String baseURL;
        try {
            IConfiguration conf = ConfigurationFactory.getInstace().getConfiguration();
            baseURL = conf.getBaseURL();
        }
        catch (Exception e) {
            baseURL = null;
        }
        if (baseURL == null || baseURL.equals("")) {
            baseURL = super.getBaseUrl(request);
        }
        return PathUtils.addSlashToBeginning(PathUtils.addSlashToEnd(baseURL));
    }

    @Override
    public String getBaseDir(HttpServletRequest request) {
        String baseDir;
        try {
            IConfiguration conf = ConfigurationFactory.getInstace().getConfiguration();
            baseDir = conf.getBaseDir();
        }
        catch (Exception e) {
            baseDir = null;
        }
        if (baseDir == null || baseDir.equals("")) {
            return super.getBaseDir(request);
        }
        return baseDir;
    }
}

