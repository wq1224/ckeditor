/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.Command;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.PathUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorCommand
extends Command {
    private ConnectorException e;
    private HttpServletResponse response;

    @Override
    public void execute(OutputStream out) throws ConnectorException {
        block6 : {
            try {
                this.response.setHeader("X-CKFinder-Error", String.valueOf(this.e.getErrorCode()));
                switch (this.e.getErrorCode()) {
                    case 102: 
                    case 103: 
                    case 109: 
                    case 501: {
                        this.response.sendError(403);
                        break block6;
                    }
                    case 104: {
                        this.response.sendError(500);
                        break block6;
                    }
                }
                this.response.sendError(404);
            }
            catch (IOException ioex) {
                throw new ConnectorException(ioex);
            }
        }
    }

    @Override
    public void setResponseHeader(HttpServletResponse response, ServletContext sc) {
        response.reset();
        this.response = response;
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        this.e = (ConnectorException)params[0];
    }

    @Override
    protected boolean checkParam(String reqParam) throws ConnectorException {
        if (reqParam == null || reqParam.equals("")) {
            return true;
        }
        return !Pattern.compile("(/\\.|\\p{Cntrl}|//|\\\\|[:*?<>\"\\|])").matcher(reqParam).find();
    }

    @Override
    protected boolean checkHidden() throws ConnectorException {
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            this.e = new ConnectorException(500);
            return true;
        }
        return false;
    }

    @Override
    protected boolean checkConnector(HttpServletRequest request) throws ConnectorException {
        if (!this.configuration.enabled() || !this.configuration.checkAuthentication(request)) {
            this.e = new ConnectorException(500);
            return false;
        }
        return true;
    }

    @Override
    protected boolean checkIfCurrFolderExists(HttpServletRequest request) throws ConnectorException {
        String tmpType = this.getParameter(request, "type");
        if (this.checkIfTypeExists(tmpType)) {
            File currDir = new File(this.configuration.getTypes().get(tmpType).getPath() + this.currentFolder);
            if (currDir.exists() && currDir.isDirectory()) {
                return true;
            }
            this.e = new ConnectorException(116);
            return false;
        }
        return false;
    }

    @Override
    protected boolean checkIfTypeExists(String type) {
        ResourceType testType = this.configuration.getTypes().get(type);
        if (testType == null) {
            this.e = new ConnectorException(12, false);
            return false;
        }
        return true;
    }

    @Override
    protected void getCurrentFolderParam(HttpServletRequest request) {
        String currFolder = this.getParameter(request, "currentFolder");
        if (currFolder != null && !currFolder.equals("")) {
            this.currentFolder = PathUtils.addSlashToBeginning(PathUtils.addSlashToEnd(currFolder));
        }
    }
}

