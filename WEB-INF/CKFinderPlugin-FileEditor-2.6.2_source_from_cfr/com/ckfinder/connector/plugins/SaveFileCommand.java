/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  com.ckfinder.connector.configuration.IConfiguration
 *  com.ckfinder.connector.data.BeforeExecuteCommandEventArgs
 *  com.ckfinder.connector.data.EventArgs
 *  com.ckfinder.connector.data.IEventHandler
 *  com.ckfinder.connector.data.ResourceType
 *  com.ckfinder.connector.errors.ConnectorException
 *  com.ckfinder.connector.handlers.command.XMLCommand
 *  com.ckfinder.connector.utils.AccessControlUtil
 *  com.ckfinder.connector.utils.FileUtils
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.ckfinder.connector.plugins;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.BeforeExecuteCommandEventArgs;
import com.ckfinder.connector.data.EventArgs;
import com.ckfinder.connector.data.IEventHandler;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.XMLCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Element;

public class SaveFileCommand
extends XMLCommand
implements IEventHandler {
    private String fileName;
    private String fileContent;
    private HttpServletRequest request;

    protected void createXMLChildNodes(int arg0, Element arg1) throws ConnectorException {
    }

    protected int getDataForXml() {
        if (this.configuration.isEnableCsrfProtection() && !this.checkCsrfToken(this.request, null)) {
            return 109;
        }
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            return 12;
        }
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 128)) {
            return 103;
        }
        if (this.fileName == null || this.fileName.equals("")) {
            return 102;
        }
        if (this.fileContent == null || this.fileContent.equals("")) {
            return 109;
        }
        if (FileUtils.checkFileExtension((String)this.fileName, (ResourceType)((ResourceType)this.configuration.getTypes().get(this.type))) == 1) {
            return 105;
        }
        if (!FileUtils.checkFileName((String)this.fileName)) {
            return 109;
        }
        File sourceFile = new File(((ResourceType)this.configuration.getTypes().get(this.type)).getPath() + this.currentFolder, this.fileName);
        try {
            if (!sourceFile.exists() || !sourceFile.isFile()) {
                return 117;
            }
            FileOutputStream fos = new FileOutputStream(sourceFile);
            fos.write(this.fileContent.getBytes("UTF-8"));
            fos.flush();
            fos.close();
        }
        catch (SecurityException e) {
            if (this.configuration.isDebugMode()) {
                this.exception = e;
            }
            return 104;
        }
        catch (FileNotFoundException e) {
            return 117;
        }
        catch (IOException e) {
            if (this.configuration.isDebugMode()) {
                this.exception = e;
            }
            return 104;
        }
        return 0;
    }

    public boolean runEventHandler(EventArgs args, IConfiguration configuration1) throws ConnectorException {
        BeforeExecuteCommandEventArgs args1 = (BeforeExecuteCommandEventArgs)args;
        if ("SaveFile".equals(args1.getCommand())) {
            this.runCommand(args1.getRequest(), args1.getResponse(), configuration1, new Object[0]);
            return false;
        }
        return true;
    }

    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        this.currentFolder = request.getParameter("currentFolder");
        this.type = request.getParameter("type");
        this.fileContent = request.getParameter("content");
        this.fileName = request.getParameter("fileName");
        this.request = request;
    }
}

