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
import java.io.File;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Element;

public class DeleteFolderCommand
extends XMLCommand
implements IPostCommand {
    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        if (this.configuration.isEnableCsrfProtection() && !this.checkCsrfToken(request, null)) {
            throw new ConnectorException(109, "CSRF Attempt");
        }
    }

    @Override
    protected void createXMLChildNodes(int errorNum, Element rootElement) throws ConnectorException {
    }

    @Override
    protected int getDataForXml() {
        block9 : {
            if (!this.checkIfTypeExists(this.type)) {
                this.type = null;
                return 12;
            }
            if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 8)) {
                return 103;
            }
            if (this.currentFolder.equals("/")) {
                return 109;
            }
            if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
                return 109;
            }
            File dir = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder);
            try {
                if (!dir.exists() || !dir.isDirectory()) {
                    return 116;
                }
                if (FileUtils.delete(dir)) {
                    File thumbDir = new File(this.configuration.getThumbsPath() + File.separator + this.type + this.currentFolder);
                    FileUtils.delete(thumbDir);
                    break block9;
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
}

