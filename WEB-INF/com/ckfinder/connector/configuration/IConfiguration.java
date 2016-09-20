/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.configuration.Events;
import com.ckfinder.connector.configuration.IBasePathBuilder;
import com.ckfinder.connector.data.AccessControlLevel;
import com.ckfinder.connector.data.PluginInfo;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

public interface IConfiguration {
    public static final int DEFAULT_IMG_WIDTH = 500;
    public static final int DEFAULT_IMG_HEIGHT = 400;
    public static final int DEFAULT_THUMB_MAX_WIDTH = 100;
    public static final int DEFAULT_THUMB_MAX_HEIGHT = 100;
    public static final float DEFAULT_IMG_QUALITY = 0.8f;
    public static final String DEFAULT_THUMBS_URL = "_thumbs/";
    public static final String DEFAULT_THUMBS_DIR = "%BASE_DIR%_thumbs/";
    public static final boolean DEFAULT_CHECKAUTHENTICATION = true;
    public static final String DEFAULT_URI_ENCODING = "UTF-8";
    public static final String DEFAULT_BASE_URL = "/userfiles";

    public IConfiguration cloneConfiguration();

    public void init() throws Exception;

    public boolean checkAuthentication(HttpServletRequest var1);

    public String getUserRoleName();

    public Map<String, ResourceType> getTypes();

    public String getBaseDir();

    public String getBaseURL();

    public String getLicenseKey();

    public String getLicenseName();

    public Integer getImgWidth();

    public Integer getImgHeight();

    public float getImgQuality();

    public boolean enabled();

    public boolean getThumbsEnabled();

    public String getThumbsURL();

    public String getThumbsDir();

    public String getThumbsPath();

    public float getThumbsQuality();

    public void setThumbsPath(String var1);

    public boolean getThumbsDirectAccess();

    public int getMaxThumbWidth();

    public int getMaxThumbHeight();

    public List<AccessControlLevel> getAccessConrolLevels();

    public List<String> getHiddenFolders();

    public List<String> getHiddenFiles();

    public boolean ckeckDoubleFileExtensions();

    public boolean forceASCII();

    public boolean isDisallowUnsafeCharacters();

    public boolean isEnableCsrfProtection();

    public boolean checkSizeAfterScaling();

    public String getUriEncoding();

    public void prepareConfigurationForRequest(HttpServletRequest var1);

    public boolean checkIfReloadConfig() throws ConnectorException;

    public List<PluginInfo> getPlugins();

    public Events getEvents();

    public boolean getSecureImageUploads();

    public List<String> getHTMLExtensions();

    public Set<String> getDefaultResourceTypes();

    public void setThumbsURL(String var1);

    public void setThumbsDir(String var1);

    public boolean isDebugMode();

    public void setDebugMode(boolean var1);

    public IBasePathBuilder getBasePathBuilder();
}

