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
import com.ckfinder.connector.handlers.command.XMLCommand;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.PathUtils;
import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Element;

public class XMLErrorCommand
extends XMLCommand {
    private ConnectorException connectorException;

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        String tmpType;
        super.initParams(request, configuration, params);
        this.connectorException = (ConnectorException)params[0];
        if (this.connectorException.isAddCurrentFolder() && this.checkIfTypeExists(tmpType = this.getParameter(request, "type"))) {
            this.type = tmpType;
        }
    }

    @Override
    protected int getDataForXml() {
        return this.connectorException.getErrorCode();
    }

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
    }

    @Override
    protected String getErrorMsg(int errorNum) {
        return this.connectorException.getErrorMessage();
    }

    @Override
    protected boolean checkParam(String reqParam) throws ConnectorException {
        if (reqParam == null || reqParam.equals("")) {
            return true;
        }
        return !Pattern.compile("(/\\.|\\p{Cntrl}|//|\\\\|[:*?<>\"\\|])").matcher(reqParam).find();
    }

    @Override
    protected boolean checkConnector(HttpServletRequest request) throws ConnectorException {
        if (!this.configuration.enabled() || !this.configuration.checkAuthentication(request)) {
            this.connectorException = new ConnectorException(500);
            return false;
        }
        return true;
    }

    @Override
    protected boolean checkHidden() throws ConnectorException {
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            this.connectorException = new ConnectorException(500);
            return true;
        }
        return false;
    }

    @Override
    protected boolean checkIfCurrFolderExists(HttpServletRequest request) throws ConnectorException {
        String tmpType = this.getParameter(request, "type");
        if (this.checkIfTypeExists(tmpType)) {
            File currDir = new File(this.configuration.getTypes().get(tmpType).getPath() + this.currentFolder);
            if (currDir.exists() && currDir.isDirectory()) {
                return true;
            }
            this.connectorException = new ConnectorException(116);
            return false;
        }
        return false;
    }

    @Override
    protected boolean checkIfTypeExists(String type) {
        ResourceType testType = this.configuration.getTypes().get(type);
        if (testType == null) {
            this.connectorException = new ConnectorException(12, false);
            return false;
        }
        return true;
    }

    @Override
    protected boolean mustAddCurrentFolderNode() {
        return this.connectorException.isAddCurrentFolder();
    }

    @Override
    protected void getCurrentFolderParam(HttpServletRequest request) {
        String currFolder = this.getParameter(request, "currentFolder");
        if (currFolder != null && !currFolder.equals("")) {
            this.currentFolder = PathUtils.addSlashToBeginning(PathUtils.addSlashToEnd(currFolder));
        }
    }
}

