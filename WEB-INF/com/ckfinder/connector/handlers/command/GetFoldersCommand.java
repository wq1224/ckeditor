/*
 * Decompiled with CFR 0_115.
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
import com.ckfinder.connector.utils.XMLCreator;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GetFoldersCommand
extends XMLCommand {
    private List<String> directories;

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
        if (errorNum == 0) {
            this.createFoldersData(rootElement);
        }
    }

    @Override
    protected int getDataForXml() {
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            return 12;
        }
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 1)) {
            return 103;
        }
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            return 109;
        }
        File dir = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder);
        try {
            if (!dir.exists()) {
                return 116;
            }
            this.directories = FileUtils.findChildrensList(dir, true);
        }
        catch (SecurityException e) {
            if (this.configuration.isDebugMode()) {
                throw e;
            }
            return 104;
        }
        this.filterListByHiddenAndNotAllowed();
        Collections.sort(this.directories);
        return 0;
    }

    private void filterListByHiddenAndNotAllowed() {
        ArrayList<String> tmpDirs = new ArrayList<String>();
        for (String dir : this.directories) {
            if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder + dir, this.userRole, 1) || FileUtils.checkIfDirIsHidden(dir, this.configuration)) continue;
            tmpDirs.add(dir);
        }
        this.directories.clear();
        this.directories.addAll(tmpDirs);
    }

    private void createFoldersData(Element rootElement) {
        Element element = this.creator.getDocument().createElement("Folders");
        for (String dirPath : this.directories) {
            File dir = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder + dirPath);
            if (!dir.exists()) continue;
            XmlElementData xmlElementData = new XmlElementData("Folder");
            xmlElementData.getAttributes().add(new XmlAttribute("name", dirPath));
            xmlElementData.getAttributes().add(new XmlAttribute("hasChildren", FileUtils.hasChildren(this.currentFolder + dirPath + "/", dir, this.configuration, this.type, this.userRole).toString()));
            xmlElementData.getAttributes().add(new XmlAttribute("acl", String.valueOf(AccessControlUtil.getInstance().checkACLForRole(this.type, this.currentFolder + dirPath, this.userRole))));
            xmlElementData.addToDocument(this.creator.getDocument(), element);
        }
        rootElement.appendChild(element);
    }
}

