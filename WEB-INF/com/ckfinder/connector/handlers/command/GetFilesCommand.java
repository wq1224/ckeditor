/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.data.XmlAttribute;
import com.ckfinder.connector.data.XmlElementData;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.XMLCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.ImageUtils;
import com.ckfinder.connector.utils.XMLCreator;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GetFilesCommand
extends XMLCommand {
    private static final float BYTES = 1024.0f;
    private List<String> files;
    private String fullCurrentPath;
    private String showThumbs;

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, new Object[0]);
        this.showThumbs = request.getParameter("showThumbs");
    }

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (errorNum == 0) {
            this.createFilesData(rootElement);
        }
    }

    @Override
    protected int getDataForXml() {
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            return 12;
        }
        this.fullCurrentPath = this.configuration.getTypes().get(this.type).getPath() + this.currentFolder;
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 16)) {
            return 103;
        }
        File dir = new File(this.fullCurrentPath);
        try {
            if (!dir.exists()) {
                return 116;
            }
            this.files = FileUtils.findChildrensList(dir, false);
        }
        catch (SecurityException e) {
            if (this.configuration.isDebugMode()) {
                throw e;
            }
            return 104;
        }
        this.filterListByHiddenAndNotAllowed();
        Collections.sort(this.files);
        return 0;
    }

    private void filterListByHiddenAndNotAllowed() {
        ArrayList<String> tmpFiles = new ArrayList<String>();
        for (String file : this.files) {
            if (FileUtils.checkFileExtension(file, this.configuration.getTypes().get(this.type)) != 0 || FileUtils.checkIfFileIsHidden(file, this.configuration)) continue;
            tmpFiles.add(file);
        }
        this.files.clear();
        this.files.addAll(tmpFiles);
    }

    private void createFilesData(Element rootElement) {
        Element element = this.creator.getDocument().createElement("Files");
        for (String filePath : this.files) {
            String attr;
            File file = new File(this.fullCurrentPath, filePath);
            if (!file.exists()) continue;
            XmlElementData elementData = new XmlElementData("File");
            XmlAttribute attribute = new XmlAttribute("name", filePath);
            elementData.getAttributes().add(attribute);
            attribute = new XmlAttribute("date", FileUtils.parseLastModifDate(file));
            elementData.getAttributes().add(attribute);
            attribute = new XmlAttribute("size", this.getSize(file));
            elementData.getAttributes().add(attribute);
            if (ImageUtils.isImage(file) && this.isAddThumbsAttr() && !(attr = this.createThumbAttr(file)).equals("")) {
                attribute = new XmlAttribute("thumb", attr);
                elementData.getAttributes().add(attribute);
            }
            elementData.addToDocument(this.creator.getDocument(), element);
        }
        rootElement.appendChild(element);
    }

    private String createThumbAttr(File file) {
        File thumbFile = new File(this.configuration.getThumbsPath() + File.separator + this.type + this.currentFolder, file.getName());
        if (thumbFile.exists()) {
            return file.getName();
        }
        if (this.isShowThumbs()) {
            return "?".concat(file.getName());
        }
        return "";
    }

    private String getSize(File file) {
        if (file.length() > 0 && (float)file.length() < 1024.0f) {
            return "1";
        }
        return String.valueOf(Math.round((float)file.length() / 1024.0f));
    }

    private boolean isAddThumbsAttr() {
        return this.configuration.getThumbsEnabled() && (this.configuration.getThumbsDirectAccess() || this.isShowThumbs());
    }

    private boolean isShowThumbs() {
        return this.showThumbs != null && this.showThumbs.equals("1");
    }
}

