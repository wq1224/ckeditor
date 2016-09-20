/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.Events;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.EventArgs;
import com.ckfinder.connector.data.InitCommandEventArgs;
import com.ckfinder.connector.data.PluginInfo;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.XMLCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.PathUtils;
import com.ckfinder.connector.utils.XMLCreator;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class InitCommand
extends XMLCommand {
    private static final int[] LICENSE_CHARS = new int[]{11, 0, 8, 12, 26, 2, 3, 25, 1};
    private static final int LICENSE_CHAR_NR = 5;
    private static final int LICENSE_KEY_LENGTH = 34;
    private String type;

    @Override
    protected int getDataForXml() {
        return 0;
    }

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (errorNum == 0) {
            block3 : {
                this.createConnectorData(rootElement);
                try {
                    this.createResouceTypesData(rootElement);
                }
                catch (Exception e) {
                    if (!this.configuration.isDebugMode()) break block3;
                    throw new ConnectorException(e);
                }
            }
            this.createPluginsData(rootElement);
        }
    }

    private void createConnectorData(Element rootElement) {
        Element element = this.creator.getDocument().createElement("ConnectorInfo");
        element.setAttribute("enabled", String.valueOf(this.configuration.enabled()));
        element.setAttribute("s", this.getLicenseName());
        element.setAttribute("c", this.createLicenseKey(this.configuration.getLicenseKey()));
        element.setAttribute("thumbsEnabled", String.valueOf(this.configuration.getThumbsEnabled()));
        element.setAttribute("uploadCheckImages", this.configuration.checkSizeAfterScaling() ? "false" : "true");
        if (this.configuration.getThumbsEnabled()) {
            element.setAttribute("thumbsUrl", this.configuration.getThumbsURL());
            element.setAttribute("thumbsDirectAccess", String.valueOf(this.configuration.getThumbsDirectAccess()));
            element.setAttribute("thumbsWidth", String.valueOf(this.configuration.getMaxThumbWidth()));
            element.setAttribute("thumbsHeight", String.valueOf(this.configuration.getMaxThumbHeight()));
        }
        element.setAttribute("imgWidth", String.valueOf(this.configuration.getImgWidth()));
        element.setAttribute("imgHeight", String.valueOf(this.configuration.getImgHeight()));
        element.setAttribute("csrfProtection", String.valueOf(this.configuration.isEnableCsrfProtection()));
        if (this.configuration.getPlugins().size() > 0) {
            element.setAttribute("plugins", this.getPlugins());
        }
        rootElement.appendChild(element);
    }

    private String getPlugins() {
        StringBuilder sb = new StringBuilder();
        boolean first = false;
        for (PluginInfo item : this.configuration.getPlugins()) {
            if (!item.isEnabled() || item.isInternal()) continue;
            if (first) {
                sb.append(",");
            }
            sb.append(item.getName());
            first = true;
        }
        return sb.toString();
    }

    private String getLicenseName() {
        int index;
        if (this.validateLicenseKey(this.configuration.getLicenseKey()) && ((index = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ".indexOf(this.configuration.getLicenseKey().charAt(0)) % 5) == 1 || index == 4)) {
            return this.configuration.getLicenseName();
        }
        return "";
    }

    private String createLicenseKey(String licenseKey) {
        if (this.validateLicenseKey(licenseKey)) {
            StringBuilder sb = new StringBuilder();
            for (int i : LICENSE_CHARS) {
                sb.append(licenseKey.charAt(i));
            }
            return sb.toString();
        }
        return "";
    }

    private boolean validateLicenseKey(String licenseKey) {
        return licenseKey != null && licenseKey.length() == 34;
    }

    public void createPluginsData(Element rootElement) throws ConnectorException {
        Element element = this.creator.getDocument().createElement("PluginsInfo");
        rootElement.appendChild(element);
        InitCommandEventArgs args = new InitCommandEventArgs();
        args.setXml(this.creator);
        args.setRootElement(rootElement);
        if (this.configuration.getEvents() != null) {
            this.configuration.getEvents().run(Events.EventTypes.InitCommand, args, this.configuration);
        }
    }

    private void createResouceTypesData(Element rootElement) throws Exception {
        Set<String> types;
        Element element = this.creator.getDocument().createElement("ResourceTypes");
        rootElement.appendChild(element);
        if (this.type != null && !this.type.equals("")) {
            types = new LinkedHashSet<String>();
            types.add(this.type);
        } else {
            types = this.getTypes();
        }
        for (String key : types) {
            ResourceType resourceType = this.configuration.getTypes().get(key);
            if (this.type != null && !this.type.equals(key) || resourceType == null || !AccessControlUtil.getInstance().checkFolderACL(key, "/", this.userRole, 1)) continue;
            Element childElement = this.creator.getDocument().createElement("ResourceType");
            childElement.setAttribute("name", resourceType.getName());
            childElement.setAttribute("acl", String.valueOf(AccessControlUtil.getInstance().checkACLForRole(key, "/", this.userRole)));
            childElement.setAttribute("hash", this.randomHash(resourceType.getPath()));
            childElement.setAttribute("allowedExtensions", resourceType.getAllowedExtensions());
            childElement.setAttribute("deniedExtensions", resourceType.getDeniedExtensions());
            childElement.setAttribute("url", resourceType.getUrl() + "/");
            Long maxSize = resourceType.getMaxSize();
            childElement.setAttribute("maxSize", maxSize != null && maxSize > 0 ? maxSize.toString() : "0");
            childElement.setAttribute("hasChildren", FileUtils.hasChildren("/", new File(PathUtils.escape(resourceType.getPath())), this.configuration, resourceType.getName(), this.userRole).toString());
            element.appendChild(childElement);
        }
    }

    private Set<String> getTypes() {
        if (this.configuration.getDefaultResourceTypes().size() > 0) {
            return this.configuration.getDefaultResourceTypes();
        }
        return this.configuration.getTypes().keySet();
    }

    private String randomHash(String folder) throws Exception {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
            algorithm.reset();
            try {
                algorithm.update(folder.getBytes("UTF8"));
            }
            catch (UnsupportedEncodingException e) {
                if (this.configuration.isDebugMode()) {
                    throw e;
                }
                algorithm.update(folder.getBytes());
            }
            byte[] messageDigest = algorithm.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; ++i) {
                hexString.append(Integer.toString((messageDigest[i] & 255) + 256, 16).substring(1));
            }
            return hexString.substring(0, 15);
        }
        catch (NoSuchAlgorithmException e) {
            if (this.configuration.isDebugMode()) {
                throw e;
            }
            return "";
        }
    }

    @Override
    protected boolean mustAddCurrentFolderNode() {
        return false;
    }

    @Override
    protected void getCurrentFolderParam(HttpServletRequest request) {
        this.currentFolder = null;
    }
}

