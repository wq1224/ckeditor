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
import com.ckfinder.connector.utils.PathUtils;
import com.ckfinder.connector.utils.XMLCreator;
import java.io.File;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RenameFolderCommand
extends XMLCommand
implements IPostCommand {
    private String newFolderName;
    private String newFolderPath;

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (errorNum == 0) {
            this.createRenamedFolderNode(rootElement);
        }
    }

    private void createRenamedFolderNode(Element rootElement) {
        Element element = this.creator.getDocument().createElement("RenamedFolder");
        element.setAttribute("newName", this.newFolderName);
        element.setAttribute("newPath", this.newFolderPath);
        element.setAttribute("newUrl", this.configuration.getTypes().get(this.type).getUrl() + this.newFolderPath);
        rootElement.appendChild(element);
    }

    @Override
    protected int getDataForXml() {
        block13 : {
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
            if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 4)) {
                return 103;
            }
            if (this.configuration.forceASCII()) {
                this.newFolderName = FileUtils.convertToASCII(this.newFolderName);
            }
            if (FileUtils.checkIfDirIsHidden(this.newFolderName, this.configuration) || !FileUtils.checkFolderName(this.newFolderName, this.configuration)) {
                return 102;
            }
            if (this.currentFolder.equals("/")) {
                return 109;
            }
            File dir = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder);
            try {
                if (!dir.isDirectory()) {
                    return 109;
                }
                this.setNewFolder();
                File newDir = new File(this.configuration.getTypes().get(this.type).getPath() + this.newFolderPath);
                if (newDir.exists()) {
                    return 115;
                }
                if (dir.renameTo(newDir)) {
                    this.renameThumb();
                    break block13;
                }
                return 104;
            }
            catch (SecurityException e) {
                if (this.configuration.isDebugMode()) {
                    throw e;
                }
                return 104;
            }
        }
        return 0;
    }

    private void renameThumb() {
        File thumbDir = new File(this.configuration.getThumbsPath() + File.separator + this.type + this.currentFolder);
        File newThumbDir = new File(this.configuration.getThumbsPath() + File.separator + this.type + this.newFolderPath);
        thumbDir.renameTo(newThumbDir);
    }

    private void setNewFolder() {
        String tmp1 = this.currentFolder.substring(0, this.currentFolder.lastIndexOf(47));
        this.newFolderPath = tmp1.substring(0, tmp1.lastIndexOf(47) + 1).concat(this.newFolderName);
        this.newFolderPath = PathUtils.addSlashToEnd(this.newFolderPath);
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, new Object[0]);
        if (this.configuration.isEnableCsrfProtection() && !this.checkCsrfToken(request, null)) {
            throw new ConnectorException(109, "CSRF Attempt");
        }
        this.newFolderName = this.getParameter(request, "NewFolderName");
    }
}

