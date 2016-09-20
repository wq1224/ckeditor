/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.Cookie
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.ServletContextFactory;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.PathUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public abstract class Command {
    protected Exception exception;
    protected IConfiguration configuration = null;
    protected String userRole = null;
    protected String currentFolder = null;
    protected String type = null;
    protected final String tokenParamName = "ckCsrfToken";

    public /* varargs */ void runCommand(HttpServletRequest request, HttpServletResponse response, IConfiguration configuration, Object ... params) throws ConnectorException {
        this.initParams(request, configuration, params);
        try {
            this.setResponseHeader(response, ServletContextFactory.getServletContext());
            this.execute((OutputStream)response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
        catch (IOException e) {
            throw new ConnectorException(104, e);
        }
    }

    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        if (configuration != null) {
            this.configuration = configuration;
            this.userRole = (String)request.getSession().getAttribute(this.configuration.getUserRoleName());
            this.getCurrentFolderParam(request);
            if (this.checkConnector(request) && this.checkParam(this.currentFolder)) {
                this.currentFolder = PathUtils.escape(this.currentFolder);
                if (!this.checkHidden() && (this.currentFolder == null || this.currentFolder.equals("") || this.checkIfCurrFolderExists(request))) {
                    this.type = this.getParameter(request, "type");
                }
            }
        }
    }

    protected boolean checkConnector(HttpServletRequest request) throws ConnectorException {
        if (!this.configuration.enabled() || !this.configuration.checkAuthentication(request)) {
            throw new ConnectorException(500, false);
        }
        return true;
    }

    protected boolean checkIfCurrFolderExists(HttpServletRequest request) throws ConnectorException {
        String tmpType = this.getParameter(request, "type");
        if (tmpType != null) {
            if (this.checkIfTypeExists(tmpType)) {
                File currDir = new File(this.configuration.getTypes().get(tmpType).getPath() + this.currentFolder);
                if (!currDir.exists() || !currDir.isDirectory()) {
                    throw new ConnectorException(116, false);
                }
                return true;
            }
            return false;
        }
        return true;
    }

    protected boolean checkIfTypeExists(String type) {
        ResourceType testType = this.configuration.getTypes().get(type);
        return testType != null;
    }

    protected boolean checkHidden() throws ConnectorException {
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            throw new ConnectorException(109, false);
        }
        return false;
    }

    public abstract void execute(OutputStream var1) throws ConnectorException;

    public abstract void setResponseHeader(HttpServletResponse var1, ServletContext var2);

    protected boolean checkParam(String reqParam) throws ConnectorException {
        if (reqParam == null || reqParam.equals("")) {
            return true;
        }
        if (Pattern.compile("(/\\.|\\p{Cntrl}|//|\\\\|[:*?<>\"\\|])").matcher(reqParam).find()) {
            throw new ConnectorException(102, false);
        }
        return true;
    }

    protected String getParameter(HttpServletRequest request, String paramName) {
        if (request.getParameter(paramName) == null) {
            return null;
        }
        return FileUtils.convertFromUriEncoding(request.getParameter(paramName), this.configuration);
    }

    protected void getCurrentFolderParam(HttpServletRequest request) {
        String currFolder = this.getParameter(request, "currentFolder");
        this.currentFolder = currFolder == null || currFolder.equals("") ? "/" : PathUtils.addSlashToBeginning(PathUtils.addSlashToEnd(currFolder));
    }

    protected boolean checkCsrfToken(HttpServletRequest request, String csrfTokenValue) {
        String paramToken;
        String tokenCookieName = "ckCsrfToken";
        int minTokenLength = 32;
        if (csrfTokenValue != null) {
            paramToken = csrfTokenValue;
        } else {
            this.getClass();
            paramToken = this.nullToString(request.getParameter("ckCsrfToken")).trim();
        }
        Cookie[] cookies = request.getCookies();
        String cookieToken = "";
        for (Cookie cookie : cookies) {
            if (!cookie.getName().equals("ckCsrfToken")) continue;
            cookieToken = this.nullToString(cookie.getValue()).trim();
            break;
        }
        if (paramToken.length() >= 32 && cookieToken.length() >= 32) {
            return paramToken.equals(cookieToken);
        }
        return false;
    }

    protected String nullToString(String s) {
        return s == null ? "" : s;
    }
}

