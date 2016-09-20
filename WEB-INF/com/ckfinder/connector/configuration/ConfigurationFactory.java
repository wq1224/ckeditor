/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.configuration.IBasePathBuilder;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.PathUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public final class ConfigurationFactory {
    private static ConfigurationFactory instance;
    private IConfiguration configuration;

    private ConfigurationFactory() {
    }

    public static ConfigurationFactory getInstace() {
        if (instance == null) {
            instance = new ConfigurationFactory();
        }
        return instance;
    }

    public final IConfiguration getConfiguration() throws Exception {
        if (this.configuration != null && this.configuration.checkIfReloadConfig()) {
            this.configuration.init();
            AccessControlUtil.getInstance().resetConfiguration();
            AccessControlUtil.getInstance().loadConfiguration(this.configuration);
        }
        return this.configuration;
    }

    public final IConfiguration getConfiguration(HttpServletRequest request) throws Exception {
        IConfiguration baseConf = this.getConfiguration();
        return this.prepareConfiguration(request, baseConf);
    }

    public IConfiguration prepareConfiguration(HttpServletRequest request, IConfiguration baseConf) throws Exception {
        if (baseConf != null) {
            IConfiguration conf = baseConf.cloneConfiguration();
            conf.prepareConfigurationForRequest(request);
            this.updateResourceTypesPaths(request, conf);
            AccessControlUtil.getInstance().loadConfiguration(conf);
            return conf;
        }
        return null;
    }

    public final void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    private void updateResourceTypesPaths(HttpServletRequest request, IConfiguration conf) throws Exception {
        String baseFolder = this.getBaseFolder(conf, request);
        baseFolder = conf.getThumbsDir().replace("%BASE_DIR%", baseFolder);
        baseFolder = PathUtils.escape(baseFolder);
        baseFolder = PathUtils.removeSlashFromEnd(baseFolder);
        if ((baseFolder = FileUtils.getFullPath(baseFolder, true, false)) == null) {
            throw new ConnectorException(116, "Thumbs directory could not be created using specified path.");
        }
        File file = new File(baseFolder);
        if (!file.exists() && !request.getParameter("command").equals("Init")) {
            file.mkdir();
        }
        conf.setThumbsPath(file.getAbsolutePath());
        String thumbUrl = conf.getThumbsURL();
        thumbUrl = thumbUrl.replaceAll("%BASE_URL%", conf.getBasePathBuilder().getBaseUrl(request));
        conf.setThumbsURL(PathUtils.escape(thumbUrl));
        for (ResourceType item : conf.getTypes().values()) {
            String resourcePath;
            String url = item.getUrl();
            url = url.replaceAll("%BASE_URL%", conf.getBasePathBuilder().getBaseUrl(request));
            url = PathUtils.escape(url);
            url = PathUtils.removeSlashFromEnd(url);
            item.setUrl(url);
            baseFolder = this.getBaseFolder(conf, request);
            baseFolder = item.getPath().replace("%BASE_DIR%", baseFolder);
            baseFolder = PathUtils.escape(baseFolder);
            baseFolder = PathUtils.removeSlashFromEnd(baseFolder);
            boolean isFromUrl = false;
            if (baseFolder == null || baseFolder.equals("")) {
                baseFolder = PathUtils.removeSlashFromBeginning(url);
                isFromUrl = true;
            }
            String string = resourcePath = isFromUrl ? FileUtils.calculatePathFromBaseUrl(baseFolder) : FileUtils.getFullPath(baseFolder, true, false);
            if (resourcePath == null) {
                throw new ConnectorException(116, "Resource directory could not be created using specified path.");
            }
            file = new File(resourcePath);
            if (!file.exists() && !request.getParameter("command").equals("Init")) {
                FileUtils.createPath(file, false);
            }
            item.setPath(file.getAbsolutePath());
        }
    }

    private String getBaseFolder(IConfiguration conf, HttpServletRequest request) throws ConnectorException {
        String baseFolder = conf.getBasePathBuilder().getBaseDir(request);
        File baseDir = new File(baseFolder);
        if (!baseDir.exists()) {
            try {
                FileUtils.createPath(baseDir, false);
            }
            catch (IOException e) {
                throw new ConnectorException(e);
            }
        }
        return PathUtils.addSlashToEnd(baseFolder);
    }
}

