/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.IPostCommand;
import com.ckfinder.connector.handlers.command.XMLCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.XMLCreator;
import java.io.File;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CreateFolderCommand
extends XMLCommand
implements IPostCommand {
    private String newFolderName;

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (errorNum == 0) {
            this.createNewFolderElement(rootElement);
        }
    }

    private void createNewFolderElement(Element rootElement) {
        Element element = this.creator.getDocument().createElement("NewFolder");
        element.setAttribute("name", this.newFolderName);
        rootElement.appendChild(element);
    }

    @Override
    protected int getDataForXml() {
        try {
            this.checkParam(this.newFolderName);
        }
        catch (ConnectorException e) {
            return e.getErrorCode();
        }
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            return 12;
        }
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 2)) {
            return 103;
        }
        if (this.configuration.forceASCII()) {
            this.newFolderName = FileUtils.convertToASCII(this.newFolderName);
        }
        if (!FileUtils.checkFolderName(this.newFolderName, this.configuration)) {
            return 102;
        }
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            return 109;
        }
        if (FileUtils.checkIfDirIsHidden(this.newFolderName, this.configuration)) {
            return 102;
        }
        try {
            if (this.createFolder()) {
                return 0;
            }
            return 103;
        }
        catch (SecurityException e) {
            if (this.configuration.isDebugMode()) {
                throw e;
            }
            return 104;
        }
        catch (ConnectorException e) {
            return e.getErrorCode();
        }
    }

    private boolean createFolder() throws ConnectorException {
        File dir = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder + this.newFolderName);
        if (dir.exists()) {
            throw new ConnectorException(115);
        }
        return dir.mkdir();
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        if (this.configuration.isEnableCsrfProtection() && !this.checkCsrfToken(request, null)) {
            throw new ConnectorException(109, "CSRF Attempt");
        }
        this.newFolderName = this.getParameter(request, "NewFolderName");
    }
}

