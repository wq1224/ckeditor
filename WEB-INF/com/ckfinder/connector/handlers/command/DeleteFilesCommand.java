/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.FilePostParam;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.IPostCommand;
import com.ckfinder.connector.handlers.command.XMLCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.XMLCreator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DeleteFilesCommand
extends XMLCommand
implements IPostCommand {
    private List<FilePostParam> files;
    private int filesDeleted;
    private boolean addDeleteNode;

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (this.creator.hasErrors()) {
            Element errorsNode = this.creator.getDocument().createElement("Errors");
            this.creator.addErrors(errorsNode);
            rootElement.appendChild(errorsNode);
        }
        if (this.addDeleteNode) {
            this.createDeleteFielsNode(rootElement);
        }
    }

    private void createDeleteFielsNode(Element rootElement) {
        Element element = this.creator.getDocument().createElement("DeleteFiles");
        element.setAttribute("deleted", String.valueOf(this.filesDeleted));
        rootElement.appendChild(element);
    }

    @Override
    protected int getDataForXml() {
        this.filesDeleted = 0;
        this.addDeleteNode = false;
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            return 12;
        }
        for (FilePostParam fileItem : this.files) {
            if (!FileUtils.checkFileName(fileItem.getName())) {
                return 109;
            }
            if (this.configuration.getTypes().get(fileItem.getType()) == null) {
                return 109;
            }
            if (fileItem.getFolder() == null || fileItem.getFolder().equals("") || Pattern.compile("(/\\.|\\p{Cntrl}|//|\\\\|[:*?<>\"\\|])").matcher(fileItem.getFolder()).find()) {
                return 109;
            }
            if (FileUtils.checkIfDirIsHidden(fileItem.getFolder(), this.configuration)) {
                return 109;
            }
            if (FileUtils.checkIfFileIsHidden(fileItem.getName(), this.configuration)) {
                return 109;
            }
            if (FileUtils.checkFileExtension(fileItem.getName(), this.configuration.getTypes().get(fileItem.getType())) == 1) {
                return 109;
            }
            if (!AccessControlUtil.getInstance().checkFolderACL(fileItem.getType(), fileItem.getFolder(), this.userRole, 128)) {
                return 103;
            }
            File file = new File(this.configuration.getTypes().get(fileItem.getType()).getPath() + fileItem.getFolder(), fileItem.getName());
            try {
                this.addDeleteNode = true;
                if (!file.exists()) {
                    this.creator.appendErrorNodeChild(117, fileItem.getName(), fileItem.getFolder(), fileItem.getType());
                    continue;
                }
                if (FileUtils.delete(file)) {
                    File thumbFile = new File(this.configuration.getThumbsPath() + File.separator + fileItem.getType() + this.currentFolder, fileItem.getName());
                    ++this.filesDeleted;
                    try {
                        FileUtils.delete(thumbFile);
                    }
                    catch (Exception var5_6) {}
                    continue;
                }
                this.creator.appendErrorNodeChild(104, fileItem.getName(), fileItem.getFolder(), fileItem.getType());
                continue;
            }
            catch (SecurityException e) {
                if (this.configuration.isDebugMode()) {
                    throw e;
                }
                return 104;
            }
        }
        if (this.creator.hasErrors()) {
            return 302;
        }
        return 0;
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, new Object[0]);
        if (this.configuration.isEnableCsrfProtection() && !this.checkCsrfToken(request, null)) {
            throw new ConnectorException(109, "CSRF Attempt");
        }
        this.files = new ArrayList<FilePostParam>();
        this.getFilesListFromRequest(request);
    }

    private void getFilesListFromRequest(HttpServletRequest request) {
        int i = 0;
        String paramName = "files[" + i + "][name]";
        while (request.getParameter(paramName) != null) {
            FilePostParam file = new FilePostParam();
            file.setName(this.getParameter(request, paramName));
            file.setFolder(this.getParameter(request, "files[" + i + "][folder]"));
            file.setOptions(this.getParameter(request, "files[" + i + "][options]"));
            file.setType(this.getParameter(request, "files[" + i + "][type]"));
            this.files.add(file);
            paramName = "files[" + ++i + "][name]";
        }
    }
}

