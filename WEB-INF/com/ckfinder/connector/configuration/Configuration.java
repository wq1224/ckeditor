/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletConfig
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.configuration.AccessControlLevelsList;
import com.ckfinder.connector.configuration.ConfigurationPathBuilder;
import com.ckfinder.connector.configuration.Events;
import com.ckfinder.connector.configuration.IBasePathBuilder;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.configuration.Plugin;
import com.ckfinder.connector.data.AccessControlLevel;
import com.ckfinder.connector.data.PluginInfo;
import com.ckfinder.connector.data.PluginParam;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.PathUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuration
implements IConfiguration {
    protected static final int MAX_QUALITY = 100;
    protected static final float MAX_QUALITY_FLOAT = 100.0f;
    private long lastCfgModificationDate;
    protected boolean enabled;
    protected String xmlFilePath;
    protected String baseDir;
    protected String baseURL;
    protected String licenseName;
    protected String licenseKey;
    protected Integer imgWidth;
    protected Integer imgHeight;
    protected float imgQuality;
    protected Map<String, ResourceType> types;
    protected boolean thumbsEnabled;
    protected String thumbsURL;
    protected String thumbsDir;
    protected String thumbsPath;
    protected boolean thumbsDirectAccess;
    protected Integer thumbsMaxHeight;
    protected Integer thumbsMaxWidth;
    protected float thumbsQuality;
    protected AccessControlLevelsList<AccessControlLevel> accessControlLevels;
    protected List<String> hiddenFolders;
    protected List<String> hiddenFiles;
    protected boolean doubleExtensions;
    protected boolean forceASCII;
    protected boolean checkSizeAfterScaling;
    protected String uriEncoding;
    protected String userRoleSessionVar;
    protected List<PluginInfo> plugins;
    protected boolean secureImageUploads;
    protected List<String> htmlExtensions;
    protected Set<String> defaultResourceTypes;
    protected IBasePathBuilder basePathBuilder;
    protected boolean disallowUnsafeCharacters;
    protected boolean enableCsrfProtection;
    private boolean loading;
    private Events events;
    private boolean debug;
    protected ServletConfig servletConf;
    private static final Logger configurationLogger = Logger.getLogger(Configuration.class.getName());

    public Configuration(ServletConfig servletConfig) {
        this.servletConf = servletConfig;
        this.xmlFilePath = servletConfig.getInitParameter("XMLConfig");
        this.plugins = new ArrayList<PluginInfo>();
        this.htmlExtensions = new ArrayList<String>();
        this.hiddenFolders = new ArrayList<String>();
        this.hiddenFiles = new ArrayList<String>();
        this.defaultResourceTypes = new LinkedHashSet<String>();
    }

    private void clearConfiguration() {
        this.debug = false;
        this.enabled = false;
        this.baseDir = "";
        this.baseURL = "";
        this.licenseName = "";
        this.licenseKey = "";
        this.imgWidth = 500;
        this.imgHeight = 400;
        this.imgQuality = 0.8f;
        this.types = new LinkedHashMap<String, ResourceType>();
        this.thumbsEnabled = false;
        this.thumbsURL = "";
        this.thumbsDir = "";
        this.thumbsPath = "";
        this.thumbsQuality = 0.8f;
        this.thumbsDirectAccess = false;
        this.thumbsMaxHeight = 100;
        this.thumbsMaxWidth = 100;
        this.accessControlLevels = new AccessControlLevelsList(true);
        this.hiddenFolders = new ArrayList<String>();
        this.hiddenFiles = new ArrayList<String>();
        this.doubleExtensions = false;
        this.forceASCII = false;
        this.checkSizeAfterScaling = false;
        this.uriEncoding = "UTF-8";
        this.userRoleSessionVar = "";
        this.plugins = new ArrayList<PluginInfo>();
        this.secureImageUploads = false;
        this.htmlExtensions = new ArrayList<String>();
        this.defaultResourceTypes = new LinkedHashSet<String>();
        this.events = new Events();
        this.basePathBuilder = null;
        this.disallowUnsafeCharacters = false;
        this.enableCsrfProtection = true;
    }

    @Override
    public void init() throws Exception {
        this.clearConfiguration();
        this.loading = true;
        File file = new File(this.getFullConfigPath());
        this.lastCfgModificationDate = file.lastModified();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.normalize();
        Node node = doc.getFirstChild();
        if (node != null) {
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node childNode = nodeList.item(i);
                if (childNode.getNodeName().equals("enabled")) {
                    this.enabled = Boolean.valueOf(this.nullNodeToString(childNode));
                }
                if (childNode.getNodeName().equals("baseDir")) {
                    this.baseDir = this.nullNodeToString(childNode);
                    this.baseDir = PathUtils.escape(this.baseDir);
                    this.baseDir = PathUtils.addSlashToEnd(this.baseDir);
                }
                if (childNode.getNodeName().equals("baseURL")) {
                    this.baseURL = this.nullNodeToString(childNode);
                    this.baseURL = PathUtils.escape(this.baseURL);
                    this.baseURL = PathUtils.addSlashToEnd(this.baseURL);
                }
                if (childNode.getNodeName().equals("licenseName")) {
                    this.licenseName = this.nullNodeToString(childNode);
                }
                if (childNode.getNodeName().equals("licenseKey")) {
                    this.licenseKey = this.nullNodeToString(childNode);
                }
                if (childNode.getNodeName().equals("imgWidth")) {
                    String width = this.nullNodeToString(childNode);
                    width = width.replaceAll("//D", "");
                    try {
                        this.imgWidth = Integer.valueOf(width);
                    }
                    catch (NumberFormatException e) {
                        this.imgWidth = null;
                    }
                }
                if (childNode.getNodeName().equals("imgQuality")) {
                    String quality = this.nullNodeToString(childNode);
                    quality = quality.replaceAll("//D", "");
                    this.imgQuality = this.adjustQuality(quality);
                }
                if (childNode.getNodeName().equals("imgHeight")) {
                    String height = this.nullNodeToString(childNode);
                    height = height.replaceAll("//D", "");
                    try {
                        this.imgHeight = Integer.valueOf(height);
                    }
                    catch (NumberFormatException e) {
                        this.imgHeight = null;
                    }
                }
                if (childNode.getNodeName().equals("thumbs")) {
                    this.setThumbs(childNode.getChildNodes());
                }
                if (childNode.getNodeName().equals("accessControls")) {
                    this.setACLs(childNode.getChildNodes());
                }
                if (childNode.getNodeName().equals("hideFolders")) {
                    this.setHiddenFolders(childNode.getChildNodes());
                }
                if (childNode.getNodeName().equals("hideFiles")) {
                    this.setHiddenFiles(childNode.getChildNodes());
                }
                if (childNode.getNodeName().equals("checkDoubleExtension")) {
                    this.doubleExtensions = Boolean.valueOf(this.nullNodeToString(childNode));
                }
                if (childNode.getNodeName().equals("disallowUnsafeCharacters")) {
                    this.disallowUnsafeCharacters = Boolean.valueOf(this.nullNodeToString(childNode));
                }
                if (childNode.getNodeName().equals("forceASCII")) {
                    this.forceASCII = Boolean.valueOf(this.nullNodeToString(childNode));
                }
                if (childNode.getNodeName().equals("checkSizeAfterScaling")) {
                    this.checkSizeAfterScaling = Boolean.valueOf(this.nullNodeToString(childNode));
                }
                if (childNode.getNodeName().equals("enableCsrfProtection")) {
                    this.enableCsrfProtection = Boolean.valueOf(this.nullNodeToString(childNode));
                }
                if (childNode.getNodeName().equals("htmlExtensions")) {
                    String htmlExt = this.nullNodeToString(childNode);
                    Scanner scanner = new Scanner(htmlExt).useDelimiter(",");
                    while (scanner.hasNext()) {
                        String val = scanner.next();
                        if (val == null || val.equals("")) continue;
                        this.htmlExtensions.add(val.trim().toLowerCase());
                    }
                }
                if (childNode.getNodeName().equals("secureImageUploads")) {
                    this.secureImageUploads = Boolean.valueOf(this.nullNodeToString(childNode));
                }
                if (childNode.getNodeName().equals("uriEncoding")) {
                    this.uriEncoding = this.nullNodeToString(childNode);
                }
                if (childNode.getNodeName().equals("userRoleSessionVar")) {
                    this.userRoleSessionVar = this.nullNodeToString(childNode);
                }
                if (childNode.getNodeName().equals("defaultResourceTypes")) {
                    String value = this.nullNodeToString(childNode);
                    Scanner sc = new Scanner(value).useDelimiter(",");
                    while (sc.hasNext()) {
                        this.defaultResourceTypes.add(sc.next());
                    }
                }
                if (childNode.getNodeName().equals("plugins")) {
                    this.setPlugins(childNode);
                }
                if (!childNode.getNodeName().equals("basePathBuilderImpl")) continue;
                this.setBasePathImpl(this.nullNodeToString(childNode));
            }
        }
        this.setTypes(doc);
        this.events = new Events();
        this.registerEventHandlers();
        this.loading = false;
    }

    private String nullNodeToString(Node childNode) {
        return childNode.getTextContent() == null ? "" : childNode.getTextContent().trim();
    }

    private String getFullConfigPath() throws ConnectorException {
        File cfgFile = null;
        String path = FileUtils.getFullPath(this.xmlFilePath, false, true);
        if (path == null) {
            throw new ConnectorException(117, "Configuration file could not be found under specified location.");
        }
        cfgFile = new File(path);
        if (cfgFile.exists() && cfgFile.isFile()) {
            return cfgFile.getAbsolutePath();
        }
        return this.xmlFilePath;
    }

    private void setBasePathImpl(String value) {
        try {
            Class clazz = Class.forName(value);
            this.basePathBuilder = (IBasePathBuilder)clazz.newInstance();
        }
        catch (Exception e) {
            this.basePathBuilder = new ConfigurationPathBuilder();
        }
    }

    private float adjustQuality(String imgQuality) {
        float helper;
        try {
            helper = Math.abs(Float.parseFloat(imgQuality));
        }
        catch (NumberFormatException e) {
            return 0.8f;
        }
        if (helper == 0.0f || helper == 1.0f) {
            return helper;
        }
        helper = helper > 0.0f && helper < 1.0f ? (float)Math.round(helper * 100.0f) / 100.0f : (helper > 1.0f && helper <= 100.0f ? (float)Math.round(helper) / 100.0f : 0.8f);
        return helper;
    }

    protected void registerEventHandlers() {
        for (PluginInfo item : this.plugins) {
            try {
                Class clazz = Class.forName(item.getClassName());
                Plugin plugin = (Plugin)clazz.newInstance();
                plugin.setPluginInfo(item);
                plugin.registerEventHandlers(this.events);
                item.setEnabled(true);
            }
            catch (ClassCastException e) {
                item.setEnabled(false);
            }
            catch (ClassNotFoundException e) {
                item.setEnabled(false);
            }
            catch (IllegalAccessException e) {
                item.setEnabled(false);
            }
            catch (InstantiationException e) {
                item.setEnabled(false);
            }
        }
    }

    private void setHiddenFiles(NodeList childNodes) {
        int j = childNodes.getLength();
        for (int i = 0; i < j; ++i) {
            String val;
            Node node = childNodes.item(i);
            if (!node.getNodeName().equals("file") || (val = this.nullNodeToString(node)).equals("")) continue;
            this.hiddenFiles.add(val.trim());
        }
    }

    private void setHiddenFolders(NodeList childNodes) {
        int j = childNodes.getLength();
        for (int i = 0; i < j; ++i) {
            String val;
            Node node = childNodes.item(i);
            if (!node.getNodeName().equals("folder") || (val = this.nullNodeToString(node)).equals("")) continue;
            this.hiddenFolders.add(val.trim());
        }
    }

    private void setACLs(NodeList childNodes) {
        int j = childNodes.getLength();
        for (int i = 0; i < j; ++i) {
            AccessControlLevel acl;
            Node childNode = childNodes.item(i);
            if (!childNode.getNodeName().equals("accessControl") || (acl = this.getACLFromNode(childNode)) == null) continue;
            this.accessControlLevels.addItem(acl, false);
        }
    }

    private AccessControlLevel getACLFromNode(Node childNode) {
        AccessControlLevel acl = new AccessControlLevel();
        int j = childNode.getChildNodes().getLength();
        for (int i = 0; i < j; ++i) {
            Node childChildNode = childNode.getChildNodes().item(i);
            if (childChildNode.getNodeName().equals("role")) {
                acl.setRole(this.nullNodeToString(childChildNode));
            }
            if (childChildNode.getNodeName().equals("resourceType")) {
                acl.setResourceType(this.nullNodeToString(childChildNode));
            }
            if (childChildNode.getNodeName().equals("folder")) {
                acl.setFolder(this.nullNodeToString(childChildNode));
            }
            if (childChildNode.getNodeName().equals("folderView")) {
                acl.setFolderView(Boolean.valueOf(this.nullNodeToString(childChildNode)));
            }
            if (childChildNode.getNodeName().equals("folderCreate")) {
                acl.setFolderCreate(Boolean.valueOf(this.nullNodeToString(childChildNode)));
            }
            if (childChildNode.getNodeName().equals("folderRename")) {
                acl.setFolderRename(Boolean.valueOf(this.nullNodeToString(childChildNode)));
            }
            if (childChildNode.getNodeName().equals("folderDelete")) {
                acl.setFolderDelete(Boolean.valueOf(this.nullNodeToString(childChildNode)));
            }
            if (childChildNode.getNodeName().equals("fileView")) {
                acl.setFileView(Boolean.valueOf(this.nullNodeToString(childChildNode)));
            }
            if (childChildNode.getNodeName().equals("fileUpload")) {
                acl.setFileUpload(Boolean.valueOf(this.nullNodeToString(childChildNode)));
            }
            if (childChildNode.getNodeName().equals("fileRename")) {
                acl.setFileRename(Boolean.valueOf(this.nullNodeToString(childChildNode)));
            }
            if (!childChildNode.getNodeName().equals("fileDelete")) continue;
            acl.setFileDelete(Boolean.valueOf(this.nullNodeToString(childChildNode)));
        }
        if (acl.getResourceType() == null || acl.getRole() == null) {
            return null;
        }
        if (acl.getFolder() == null || acl.getFolder().equals("")) {
            acl.setFolder("/");
        }
        return acl;
    }

    private void setThumbs(NodeList childNodes) {
        int j = childNodes.getLength();
        for (int i = 0; i < j; ++i) {
            String width;
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("enabled")) {
                this.thumbsEnabled = Boolean.valueOf(this.nullNodeToString(childNode));
            }
            if (childNode.getNodeName().equals("url")) {
                this.thumbsURL = this.nullNodeToString(childNode);
            }
            if (childNode.getNodeName().equals("directory")) {
                this.thumbsDir = this.nullNodeToString(childNode);
            }
            if (childNode.getNodeName().equals("directAccess")) {
                this.thumbsDirectAccess = Boolean.valueOf(this.nullNodeToString(childNode));
            }
            if (childNode.getNodeName().equals("maxHeight")) {
                width = this.nullNodeToString(childNode);
                width = width.replaceAll("//D", "");
                try {
                    this.thumbsMaxHeight = Integer.valueOf(width);
                }
                catch (NumberFormatException e) {
                    this.thumbsMaxHeight = null;
                }
            }
            if (childNode.getNodeName().equals("maxWidth")) {
                width = this.nullNodeToString(childNode);
                width = width.replaceAll("//D", "");
                try {
                    this.thumbsMaxWidth = Integer.valueOf(width);
                }
                catch (NumberFormatException e) {
                    this.thumbsMaxWidth = null;
                }
            }
            if (!childNode.getNodeName().equals("quality")) continue;
            String quality = this.nullNodeToString(childNode);
            quality = quality.replaceAll("//D", "");
            this.thumbsQuality = this.adjustQuality(quality);
        }
    }

    private void setTypes(Document doc) {
        this.types = new LinkedHashMap<String, ResourceType>();
        NodeList list = doc.getElementsByTagName("type");
        int j = list.getLength();
        for (int i = 0; i < j; ++i) {
            Element element = (Element)list.item(i);
            String name = element.getAttribute("name");
            if (name == null || name.equals("")) continue;
            ResourceType resourceType = this.createTypeFromXml(name, element.getChildNodes());
            this.types.put(name, resourceType);
        }
    }

    private ResourceType createTypeFromXml(String typeName, NodeList childNodes) {
        ResourceType resourceType = new ResourceType(typeName);
        int j = childNodes.getLength();
        for (int i = 0; i < j; ++i) {
            String url;
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("url")) {
                url = this.nullNodeToString(childNode);
                resourceType.setUrl(url);
            }
            if (childNode.getNodeName().equals("directory")) {
                url = this.nullNodeToString(childNode);
                resourceType.setPath(url);
            }
            if (childNode.getNodeName().equals("maxSize")) {
                resourceType.setMaxSize(this.nullNodeToString(childNode));
            }
            if (childNode.getNodeName().equals("allowedExtensions")) {
                resourceType.setAllowedExtensions(this.nullNodeToString(childNode));
            }
            if (!childNode.getNodeName().equals("deniedExtensions")) continue;
            resourceType.setDeniedExtensions(this.nullNodeToString(childNode));
        }
        return resourceType;
    }

    @Override
    public boolean checkAuthentication(HttpServletRequest request) {
        return true;
    }

    @Override
    public boolean enabled() {
        return this.enabled && !this.loading;
    }

    @Override
    public boolean isDisallowUnsafeCharacters() {
        return this.disallowUnsafeCharacters;
    }

    @Override
    public boolean isEnableCsrfProtection() {
        return this.enableCsrfProtection;
    }

    @Override
    public String getBaseDir() {
        return this.baseDir;
    }

    @Override
    public String getBaseURL() {
        return this.baseURL;
    }

    @Override
    public Integer getImgHeight() {
        if (this.imgHeight != null) {
            return this.imgHeight;
        }
        return 400;
    }

    @Override
    public Integer getImgWidth() {
        if (this.imgWidth != null) {
            return this.imgWidth;
        }
        return 500;
    }

    @Override
    public float getImgQuality() {
        return this.imgQuality;
    }

    @Override
    public String getLicenseKey() {
        return this.licenseKey;
    }

    @Override
    public String getLicenseName() {
        return this.licenseName;
    }

    @Override
    public Map<String, ResourceType> getTypes() {
        return this.types;
    }

    @Override
    public boolean getThumbsDirectAccess() {
        return this.thumbsDirectAccess;
    }

    @Override
    public int getMaxThumbHeight() {
        if (this.thumbsMaxHeight != null) {
            return this.thumbsMaxHeight;
        }
        return 100;
    }

    @Override
    public int getMaxThumbWidth() {
        if (this.thumbsMaxWidth != null) {
            return this.thumbsMaxWidth;
        }
        return 100;
    }

    @Override
    public boolean getThumbsEnabled() {
        return this.thumbsEnabled;
    }

    @Override
    public String getThumbsURL() {
        return this.thumbsURL;
    }

    @Override
    public String getThumbsDir() {
        return this.thumbsDir;
    }

    @Override
    public String getThumbsPath() {
        return this.thumbsPath;
    }

    @Override
    public float getThumbsQuality() {
        return this.thumbsQuality;
    }

    @Override
    public void setThumbsPath(String directory) {
        this.thumbsPath = directory;
    }

    public AccessControlLevelsList<AccessControlLevel> getAccessConrolLevels() {
        return this.accessControlLevels;
    }

    @Override
    public List<String> getHiddenFolders() {
        return this.hiddenFolders;
    }

    @Override
    public List<String> getHiddenFiles() {
        return this.hiddenFiles;
    }

    @Override
    public boolean ckeckDoubleFileExtensions() {
        return this.doubleExtensions;
    }

    @Override
    public boolean forceASCII() {
        return this.forceASCII;
    }

    @Override
    public boolean checkSizeAfterScaling() {
        return this.checkSizeAfterScaling;
    }

    @Override
    public String getUriEncoding() {
        if (this.uriEncoding == null || this.uriEncoding.length() == 0) {
            return "UTF-8";
        }
        return this.uriEncoding;
    }

    @Override
    public String getUserRoleName() {
        return this.userRoleSessionVar;
    }

    @Override
    public List<PluginInfo> getPlugins() {
        return this.plugins;
    }

    @Override
    public boolean getSecureImageUploads() {
        return this.secureImageUploads;
    }

    @Override
    public List<String> getHTMLExtensions() {
        return this.htmlExtensions;
    }

    @Override
    public Events getEvents() {
        return this.events;
    }

    @Override
    public Set<String> getDefaultResourceTypes() {
        return this.defaultResourceTypes;
    }

    @Override
    public boolean isDebugMode() {
        return this.debug;
    }

    @Override
    public IBasePathBuilder getBasePathBuilder() {
        if (this.basePathBuilder == null) {
            this.basePathBuilder = new ConfigurationPathBuilder();
        }
        return this.basePathBuilder;
    }

    @Override
    public boolean checkIfReloadConfig() throws ConnectorException {
        String path = FileUtils.getFullPath(this.xmlFilePath, false, true);
        if (path == null) {
            if (this.debug) {
                throw new ConnectorException(117, "Configuration file could not be found under specified location.");
            }
            configurationLogger.log(Level.SEVERE, "Configuration file could not be found under specified location.");
            return false;
        }
        File cfgFile = new File(path);
        return cfgFile.lastModified() > this.lastCfgModificationDate;
    }

    @Override
    public void prepareConfigurationForRequest(HttpServletRequest request) {
    }

    private void setPlugins(Node childNode) {
        NodeList nodeList = childNode.getChildNodes();
        int j = nodeList.getLength();
        for (int i = 0; i < j; ++i) {
            Node childChildNode = nodeList.item(i);
            if (!childChildNode.getNodeName().equals("plugin")) continue;
            this.plugins.add(this.createPluginFromNode(childChildNode));
        }
    }

    private PluginInfo createPluginFromNode(Node element) {
        PluginInfo info = new PluginInfo();
        NodeList list = element.getChildNodes();
        int l = list.getLength();
        for (int i = 0; i < l; ++i) {
            Node childElem = list.item(i);
            String nodeName = childElem.getNodeName();
            String textContent = this.nullNodeToString(childElem);
            if ("name".equals(nodeName)) {
                info.setName(textContent);
            }
            if ("class".equals(nodeName)) {
                info.setClassName(textContent);
            }
            if ("internal".equals(nodeName)) {
                info.setInternal(Boolean.parseBoolean(textContent));
            }
            if (!"params".equals(nodeName)) continue;
            NodeList paramLlist = childElem.getChildNodes();
            if (list.getLength() > 0) {
                info.setParams(new ArrayList<PluginParam>());
            }
            int m = paramLlist.getLength();
            for (int j = 0; j < m; ++j) {
                Node node = paramLlist.item(j);
                if (!"param".equals(node.getNodeName())) continue;
                NamedNodeMap map = node.getAttributes();
                PluginParam pp = new PluginParam();
                int o = map.getLength();
                for (int k = 0; k < o; ++k) {
                    if ("name".equals(map.item(k).getNodeName())) {
                        pp.setName(this.nullNodeToString(map.item(k)));
                    }
                    if (!"value".equals(map.item(k).getNodeName())) continue;
                    pp.setValue(this.nullNodeToString(map.item(k)));
                }
                info.getParams().add(pp);
            }
        }
        return info;
    }

    @Override
    public void setThumbsURL(String url) {
        this.thumbsURL = url;
    }

    @Override
    public void setThumbsDir(String dir) {
        this.thumbsDir = dir;
    }

    @Override
    public final void setDebugMode(boolean mode) {
        this.debug = mode;
    }

    @Override
    public final IConfiguration cloneConfiguration() {
        Configuration configuration = this.createConfigurationInstance();
        this.copyConfFields(configuration);
        return configuration;
    }

    protected Configuration createConfigurationInstance() {
        return new Configuration(this.servletConf);
    }

    protected void copyConfFields(Configuration configuration) {
        configuration.loading = this.loading;
        configuration.xmlFilePath = this.xmlFilePath;
        configuration.debug = this.debug;
        configuration.lastCfgModificationDate = this.lastCfgModificationDate;
        configuration.enabled = this.enabled;
        configuration.xmlFilePath = this.xmlFilePath;
        configuration.baseDir = this.baseDir;
        configuration.baseURL = this.baseURL;
        configuration.licenseName = this.licenseName;
        configuration.licenseKey = this.licenseKey;
        configuration.imgWidth = this.imgWidth;
        configuration.imgHeight = this.imgHeight;
        configuration.imgQuality = this.imgQuality;
        configuration.thumbsEnabled = this.thumbsEnabled;
        configuration.thumbsURL = this.thumbsURL;
        configuration.thumbsDir = this.thumbsDir;
        configuration.thumbsDirectAccess = this.thumbsDirectAccess;
        configuration.thumbsMaxHeight = this.thumbsMaxHeight;
        configuration.thumbsMaxWidth = this.thumbsMaxWidth;
        configuration.thumbsQuality = this.thumbsQuality;
        configuration.doubleExtensions = this.doubleExtensions;
        configuration.forceASCII = this.forceASCII;
        configuration.disallowUnsafeCharacters = this.disallowUnsafeCharacters;
        configuration.enableCsrfProtection = this.enableCsrfProtection;
        configuration.checkSizeAfterScaling = this.checkSizeAfterScaling;
        configuration.secureImageUploads = this.secureImageUploads;
        configuration.uriEncoding = this.uriEncoding;
        configuration.userRoleSessionVar = this.userRoleSessionVar;
        configuration.events = this.events;
        configuration.basePathBuilder = this.basePathBuilder;
        configuration.htmlExtensions = new ArrayList<String>();
        configuration.htmlExtensions.addAll(this.htmlExtensions);
        configuration.hiddenFolders = new ArrayList<String>();
        configuration.hiddenFiles = new ArrayList<String>();
        configuration.hiddenFiles.addAll(this.hiddenFiles);
        configuration.hiddenFolders.addAll(this.hiddenFolders);
        configuration.defaultResourceTypes = new LinkedHashSet<String>();
        configuration.defaultResourceTypes.addAll(this.defaultResourceTypes);
        configuration.types = new LinkedHashMap<String, ResourceType>();
        configuration.accessControlLevels = new AccessControlLevelsList(false);
        configuration.plugins = new ArrayList<PluginInfo>();
        this.copyTypes(configuration.types);
        this.copyACls(configuration.accessControlLevels);
        this.copyPlugins(configuration.plugins);
    }

    private void copyPlugins(List<PluginInfo> newPlugins) {
        for (PluginInfo pluginInfo : this.plugins) {
            newPlugins.add(new PluginInfo(pluginInfo));
        }
    }

    private void copyACls(AccessControlLevelsList<AccessControlLevel> newAccessControlLevels) {
        for (AccessControlLevel acl : this.accessControlLevels) {
            newAccessControlLevels.addItem(new AccessControlLevel(acl), false);
        }
    }

    private void copyTypes(Map<String, ResourceType> newTypes) {
        for (String name : this.types.keySet()) {
            newTypes.put(name, new ResourceType(this.types.get(name)));
        }
    }
}

