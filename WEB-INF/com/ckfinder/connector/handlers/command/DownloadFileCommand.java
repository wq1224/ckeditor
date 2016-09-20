/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.mail.internet.MimeUtility
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.Command;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DownloadFileCommand
extends Command {
    private File file;
    private String fileName;
    private Object format;
    private String newFileName;

    @Override
    public void execute(OutputStream out) throws ConnectorException {
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            throw new ConnectorException(12, false);
        }
        this.file = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder, this.fileName);
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 16)) {
            throw new ConnectorException(103);
        }
        if (!FileUtils.checkFileName(this.fileName) || FileUtils.checkFileExtension(this.fileName, this.configuration.getTypes().get(this.type)) == 1) {
            throw new ConnectorException(109);
        }
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            throw new ConnectorException(109);
        }
        try {
            if (!this.file.exists() || !this.file.isFile() || FileUtils.checkIfFileIsHidden(this.fileName, this.configuration)) {
                throw new ConnectorException(117);
            }
            FileUtils.printFileContentToResponse(this.file, out);
        }
        catch (IOException e) {
            throw new ConnectorException(104, e);
        }
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        this.newFileName = request.getParameter("FileName").replaceAll("\"", "\\\\\"");
        this.fileName = this.getParameter(request, "FileName");
        try {
            if (request.getHeader("User-Agent").indexOf("MSIE") != -1) {
                this.newFileName = URLEncoder.encode(this.newFileName, "UTF-8");
                this.newFileName = this.newFileName.replace("+", " ").replace("%2E", ".");
            } else {
                this.newFileName = MimeUtility.encodeWord((String)this.newFileName, (String)"utf-8", (String)"Q");
            }
        }
        catch (UnsupportedEncodingException var4_4) {
            // empty catch block
        }
    }

    @Override
    public void setResponseHeader(HttpServletResponse response, ServletContext sc) {
        String mimetype = sc.getMimeType(this.fileName);
        response.setCharacterEncoding("utf-8");
        if (this.format != null && this.format.equals("text")) {
            response.setContentType("text/plain; charset=utf-8");
        } else {
            if (mimetype != null) {
                response.setContentType(mimetype);
            } else {
                response.setContentType("application/octet-stream");
            }
            if (this.file != null) {
                response.setContentLength((int)this.file.length());
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + this.newFileName + "\"");
        }
        response.setHeader("Cache-Control", "cache, must-revalidate");
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");
    }
}

