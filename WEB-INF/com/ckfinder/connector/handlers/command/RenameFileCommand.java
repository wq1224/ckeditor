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

public class RenameFileCommand
extends XMLCommand
implements IPostCommand {
    private String fileName;
    private String newFileName;
    private boolean renamed;
    private boolean addRenameNode;

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (this.addRenameNode) {
            this.createRenamedFileNode(rootElement);
        }
    }

    private void createRenamedFileNode(Element rootElement) {
        Element element = this.creator.getDocument().createElement("RenamedFile");
        element.setAttribute("name", this.fileName);
        if (this.renamed) {
            element.setAttribute("newName", this.newFileName);
        }
        rootElement.appendChild(element);
    }

    @Override
    protected int getDataForXml() {
        int checkFileExt;
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            return 12;
        }
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 64)) {
            return 103;
        }
        if (this.configuration.forceASCII()) {
            this.newFileName = FileUtils.convertToASCII(this.newFileName);
        }
        if (this.fileName != null && !this.fileName.equals("") && this.newFileName != null && !this.newFileName.equals("")) {
            this.addRenameNode = true;
        }
        if ((checkFileExt = FileUtils.checkFileExtension(this.newFileName, this.configuration.getTypes().get(this.type))) == 1) {
            return 105;
        }
        if (this.configuration.ckeckDoubleFileExtensions()) {
            this.newFileName = FileUtils.renameFileWithBadExt(this.configuration.getTypes().get(this.type), this.newFileName);
        }
        if (!FileUtils.checkFileName(this.fileName) || FileUtils.checkIfFileIsHidden(this.fileName, this.configuration)) {
            return 109;
        }
        if (!FileUtils.checkFileName(this.newFileName, this.configuration) || FileUtils.checkIfFileIsHidden(this.newFileName, this.configuration)) {
            return 102;
        }
        if (FileUtils.checkFileExtension(this.fileName, this.configuration.getTypes().get(this.type)) == 1) {
            return 109;
        }
        String dirPath = this.configuration.getTypes().get(this.type).getPath() + this.currentFolder;
        File file = new File(dirPath, this.fileName);
        File newFile = new File(dirPath, this.newFileName);
        File dir = new File(dirPath);
        try {
            if (!file.exists()) {
                return 117;
            }
            if (newFile.exists()) {
                return 115;
            }
            if (!dir.canWrite() || !file.canWrite()) {
                return 104;
            }
            this.renamed = file.renameTo(newFile);
            if (this.renamed) {
                this.renameThumb();
                return 0;
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

    private void renameThumb() {
        File thumbFile = new File(this.configuration.getThumbsPath() + File.separator + this.type + this.currentFolder, this.fileName);
        File newThumbFile = new File(this.configuration.getThumbsPath() + File.separator + this.type + this.currentFolder, this.newFileName);
        thumbFile.renameTo(newThumbFile);
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, new Object[0]);
        if (this.configuration.isEnableCsrfProtection() && !this.checkCsrfToken(request, null)) {
            throw new ConnectorException(109, "CSRF Attempt");
        }
        this.fileName = this.getParameter(request, "fileName");
        this.newFileName = this.getParameter(request, "newFileName");
    }
}

