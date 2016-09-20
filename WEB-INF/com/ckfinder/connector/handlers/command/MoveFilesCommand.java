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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MoveFilesCommand
extends XMLCommand
implements IPostCommand {
    private List<FilePostParam> files;
    private int filesMoved;
    private int movedAll;
    private boolean addMoveNode;

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (this.creator.hasErrors()) {
            Element errorsNode = this.creator.getDocument().createElement("Errors");
            this.creator.addErrors(errorsNode);
            rootElement.appendChild(errorsNode);
        }
        if (this.addMoveNode) {
            this.createMoveFielsNode(rootElement);
        }
    }

    private void createMoveFielsNode(Element rootElement) {
        Element element = this.creator.getDocument().createElement("MoveFiles");
        element.setAttribute("moved", String.valueOf(this.filesMoved));
        element.setAttribute("movedTotal", String.valueOf(this.movedAll + this.filesMoved));
        rootElement.appendChild(element);
    }

    @Override
    protected int getDataForXml() {
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            return 12;
        }
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 224)) {
            return 103;
        }
        try {
            return this.moveFiles();
        }
        catch (Exception e) {
            this.exception = e;
            return 110;
        }
    }

    private int moveFiles() throws IOException {
        this.filesMoved = 0;
        this.addMoveNode = false;
        for (FilePostParam file : this.files) {
            if (!FileUtils.checkFileName(file.getName())) {
                return 109;
            }
            if (Pattern.compile("(/\\.|\\p{Cntrl}|//|\\\\|[:*?<>\"\\|])").matcher(file.getFolder()).find()) {
                return 109;
            }
            if (this.configuration.getTypes().get(file.getType()) == null) {
                return 109;
            }
            if (file.getFolder() == null || file.getFolder().equals("")) {
                return 109;
            }
            if (FileUtils.checkFileExtension(file.getName(), this.configuration.getTypes().get(this.type)) == 1) {
                this.creator.appendErrorNodeChild(105, file.getName(), file.getFolder(), file.getType());
                continue;
            }
            if (!this.type.equals(file.getType()) && FileUtils.checkFileExtension(file.getName(), this.configuration.getTypes().get(file.getType())) == 1) {
                this.creator.appendErrorNodeChild(105, file.getName(), file.getFolder(), file.getType());
                continue;
            }
            if (FileUtils.checkIfFileIsHidden(file.getName(), this.configuration)) {
                return 109;
            }
            if (FileUtils.checkIfDirIsHidden(file.getFolder(), this.configuration)) {
                return 109;
            }
            if (!AccessControlUtil.getInstance().checkFolderACL(file.getType(), file.getFolder(), this.userRole, 16)) {
                return 103;
            }
            File sourceFile = new File(this.configuration.getTypes().get(file.getType()).getPath() + file.getFolder(), file.getName());
            File destFile = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder, file.getName());
            File sourceThumb = new File(this.configuration.getThumbsPath() + File.separator + file.getType() + file.getFolder() + file.getName());
            try {
                Long maxSize;
                if (!sourceFile.exists() || !sourceFile.isFile()) {
                    this.creator.appendErrorNodeChild(117, file.getName(), file.getFolder(), file.getType());
                    continue;
                }
                if (!this.type.equals(file.getType()) && (maxSize = this.configuration.getTypes().get(this.type).getMaxSize()) != null && maxSize < sourceFile.length()) {
                    this.creator.appendErrorNodeChild(203, file.getName(), file.getFolder(), file.getType());
                    continue;
                }
                if (sourceFile.equals(destFile)) {
                    this.creator.appendErrorNodeChild(118, file.getName(), file.getFolder(), file.getType());
                    continue;
                }
                if (destFile.exists()) {
                    if (file.getOptions() != null && file.getOptions().indexOf("overwrite") != -1) {
                        if (!this.handleOverwrite(sourceFile, destFile)) {
                            this.creator.appendErrorNodeChild(104, file.getName(), file.getFolder(), file.getType());
                            continue;
                        }
                        ++this.filesMoved;
                        FileUtils.delete(sourceThumb);
                        continue;
                    }
                    if (file.getOptions() != null && file.getOptions().indexOf("autorename") != -1) {
                        if (!this.handleAutoRename(sourceFile, destFile)) {
                            this.creator.appendErrorNodeChild(104, file.getName(), file.getFolder(), file.getType());
                            continue;
                        }
                        ++this.filesMoved;
                        FileUtils.delete(sourceThumb);
                        continue;
                    }
                    this.creator.appendErrorNodeChild(115, file.getName(), file.getFolder(), file.getType());
                    continue;
                }
                if (!FileUtils.copyFromSourceToDestFile(sourceFile, destFile, true, this.configuration)) continue;
                ++this.filesMoved;
                this.moveThumb(file);
            }
            catch (SecurityException e) {
                if (this.configuration.isDebugMode()) {
                    throw e;
                }
                this.creator.appendErrorNodeChild(104, file.getName(), file.getFolder(), file.getType());
            }
            catch (IOException e) {
                if (this.configuration.isDebugMode()) {
                    throw e;
                }
                this.creator.appendErrorNodeChild(104, file.getName(), file.getFolder(), file.getType());
            }
        }
        this.addMoveNode = true;
        if (this.creator.hasErrors()) {
            return 300;
        }
        return 0;
    }

    private boolean handleAutoRename(File sourceFile, File destFile) throws IOException {
        int counter = 1;
        do {
            String newFileName = FileUtils.getFileNameWithoutExtension(destFile.getName(), false) + "(" + counter + ")." + FileUtils.getFileExtension(destFile.getName(), false);
            File newDestFile = new File(destFile.getParent(), newFileName);
            if (!newDestFile.exists()) {
                return FileUtils.copyFromSourceToDestFile(sourceFile, newDestFile, true, this.configuration);
            }
            ++counter;
        } while (true);
    }

    private boolean handleOverwrite(File sourceFile, File destFile) throws IOException {
        return FileUtils.delete(destFile) && FileUtils.copyFromSourceToDestFile(sourceFile, destFile, true, this.configuration);
    }

    private void moveThumb(FilePostParam file) throws IOException {
        File sourceThumbFile = new File(this.configuration.getThumbsPath() + File.separator + file.getType() + file.getFolder() + file.getName());
        File destThumbFile = new File(this.configuration.getThumbsPath() + File.separator + this.type + this.currentFolder + file.getName());
        FileUtils.copyFromSourceToDestFile(sourceThumbFile, destThumbFile, true, this.configuration);
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, new Object[0]);
        if (this.configuration.isEnableCsrfProtection() && !this.checkCsrfToken(request, null)) {
            throw new ConnectorException(109, "CSRF Attempt");
        }
        this.files = new ArrayList<FilePostParam>();
        this.movedAll = request.getParameter("moved") != null ? Integer.valueOf(request.getParameter("moved")) : 0;
        this.getFilesListFromRequest(request);
    }

    private void getFilesListFromRequest(HttpServletRequest request) {
        String paramName;
        int i = 0;
        while (request.getParameter(paramName = "files[" + i + "][name]") != null) {
            FilePostParam file = new FilePostParam();
            file.setName(this.getParameter(request, paramName));
            file.setFolder(this.getParameter(request, "files[" + i + "][folder]"));
            file.setOptions(this.getParameter(request, "files[" + i + "][options]"));
            file.setType(this.getParameter(request, "files[" + i + "][type]"));
            this.files.add(file);
            ++i;
        }
    }
}

