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
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThumbnailCommand
extends Command {
    private String fileName;
    private File thumbFile;
    private String ifNoneMatch;
    private long ifModifiedSince;
    private HttpServletResponse response;
    private String fullCurrentPath;
    private static final HashMap<String, String> imgMimeTypeMap = new HashMap(57);

    @Override
    public void setResponseHeader(HttpServletResponse response, ServletContext sc) {
        response.setHeader("Cache-Control", "public");
        String mimetype = this.getMimeTypeOfImage(sc, response);
        if (mimetype != null) {
            response.setContentType(mimetype);
        }
        response.addHeader("Content-Disposition", "attachment; filename=\"" + this.fileName + "\"");
        this.response = response;
    }

    private String getMimeTypeOfImage(ServletContext sc, HttpServletResponse response) {
        if (this.fileName == null || this.fileName.length() == 0) {
            response.setStatus(500);
            return null;
        }
        String tempFileName = this.fileName.substring(0, this.fileName.lastIndexOf(46) + 1).concat(FileUtils.getFileExtension(this.fileName).toLowerCase());
        String mimeType = sc.getMimeType(tempFileName);
        if (mimeType == null || mimeType.length() == 0) {
            mimeType = imgMimeTypeMap.get(this.fileName.toLowerCase().substring(this.fileName.lastIndexOf(".")));
        }
        if (mimeType == null) {
            response.setStatus(500);
            return null;
        }
        return mimeType;
    }

    @Override
    public void execute(OutputStream out) throws ConnectorException {
        this.validate();
        this.createThumb();
        if (this.setResponseHeadersAfterCreatingFile()) {
            try {
                FileUtils.printFileContentToResponse(this.thumbFile, out);
            }
            catch (IOException e) {
                if (this.configuration.isDebugMode()) {
                    throw new ConnectorException(e);
                }
                try {
                    this.response.sendError(403);
                }
                catch (IOException e1) {
                    throw new ConnectorException(e1);
                }
            }
        } else {
            try {
                this.response.reset();
                this.response.sendError(304);
            }
            catch (IOException e1) {
                throw new ConnectorException(e1);
            }
        }
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        this.fileName = this.getParameter(request, "FileName");
        try {
            this.ifModifiedSince = request.getDateHeader("If-Modified-Since");
        }
        catch (IllegalArgumentException e) {
            this.ifModifiedSince = 0;
        }
        this.ifNoneMatch = request.getHeader("If-None-Match");
    }

    private void validate() throws ConnectorException {
        if (!this.configuration.getThumbsEnabled()) {
            throw new ConnectorException(501);
        }
        if (!this.checkIfTypeExists(this.type)) {
            this.type = null;
            throw new ConnectorException(12, false);
        }
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 16)) {
            throw new ConnectorException(103);
        }
        if (!FileUtils.checkFileName(this.fileName)) {
            throw new ConnectorException(109);
        }
        if (FileUtils.checkIfFileIsHidden(this.fileName, this.configuration)) {
            throw new ConnectorException(117);
        }
        File typeThumbDir = new File(this.configuration.getThumbsPath() + File.separator + this.type);
        try {
            this.fullCurrentPath = typeThumbDir.getAbsolutePath() + this.currentFolder;
            if (!typeThumbDir.exists()) {
                typeThumbDir.mkdir();
            }
        }
        catch (SecurityException e) {
            throw new ConnectorException(104, e);
        }
    }

    private void createThumb() throws ConnectorException {
        block5 : {
            this.thumbFile = new File(this.fullCurrentPath, this.fileName);
            try {
                if (this.thumbFile.exists()) break block5;
                File orginFile = new File(this.configuration.getTypes().get(this.type).getPath() + this.currentFolder, this.fileName);
                if (!orginFile.exists()) {
                    throw new ConnectorException(117);
                }
                try {
                    ImageUtils.createThumb(orginFile, this.thumbFile, this.configuration);
                }
                catch (Exception e) {
                    this.thumbFile.delete();
                    throw new ConnectorException(104, e);
                }
            }
            catch (SecurityException e) {
                throw new ConnectorException(104, e);
            }
        }
    }

    private boolean setResponseHeadersAfterCreatingFile() throws ConnectorException {
        File file = new File(this.fullCurrentPath, this.fileName);
        try {
            String etag = Long.toHexString(file.lastModified()).concat("-").concat(Long.toHexString(file.length()));
            if (etag.equals(this.ifNoneMatch)) {
                return false;
            }
            this.response.setHeader("Etag", etag);
            if (file.lastModified() <= this.ifModifiedSince) {
                return false;
            }
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss z");
            this.response.setHeader("Last-Modified", df.format(date));
            this.response.setContentLength((int)file.length());
        }
        catch (SecurityException e) {
            throw new ConnectorException(104, e);
        }
        return true;
    }

    static {
        imgMimeTypeMap.put(".art", "image/x-jg");
        imgMimeTypeMap.put(".bm", "image/bmp");
        imgMimeTypeMap.put(".bmp", "image/bmp");
        imgMimeTypeMap.put(".dwg", "image/vnd.dwg");
        imgMimeTypeMap.put(".dxf", "image/vnd.dwg");
        imgMimeTypeMap.put(".fif", "image/fif");
        imgMimeTypeMap.put(".flo", "image/florian");
        imgMimeTypeMap.put(".fpx", "image/vnd.fpx");
        imgMimeTypeMap.put(".g3", "image/g3fax");
        imgMimeTypeMap.put(".gif", "image/gif");
        imgMimeTypeMap.put(".ico", "image/x-icon");
        imgMimeTypeMap.put(".ief", "image/ief");
        imgMimeTypeMap.put(".iefs", "image/ief");
        imgMimeTypeMap.put(".jut", "image/jutvision");
        imgMimeTypeMap.put(".mcf", "image/vasa");
        imgMimeTypeMap.put(".nap", "image/naplps");
        imgMimeTypeMap.put(".naplps", "image/naplps");
        imgMimeTypeMap.put(".nif", "image/x-niff");
        imgMimeTypeMap.put(".niff", "image/x-niff");
        imgMimeTypeMap.put(".pct", "image/x-pict");
        imgMimeTypeMap.put(".pcx", "image/x-pcx");
        imgMimeTypeMap.put(".pgm", "image/x-portable-graymap");
        imgMimeTypeMap.put(".pic", "image/pict");
        imgMimeTypeMap.put(".pict", "image/pict");
        imgMimeTypeMap.put(".pm", "image/x-xpixmap");
        imgMimeTypeMap.put(".png", "image/png");
        imgMimeTypeMap.put(".pnm", "image/x-portable-anymap");
        imgMimeTypeMap.put(".ppm", "image/x-portable-pixmap");
        imgMimeTypeMap.put(".ras", "image/x-cmu-raster");
        imgMimeTypeMap.put(".rast", "image/cmu-raster");
        imgMimeTypeMap.put(".rf", "image/vnd.rn-realflash");
        imgMimeTypeMap.put(".rgb", "image/x-rgb");
        imgMimeTypeMap.put(".rp", "  image/vnd.rn-realpix");
        imgMimeTypeMap.put(".svf", "image/vnd.dwg");
        imgMimeTypeMap.put(".svf", "image/x-dwg");
        imgMimeTypeMap.put(".tiff", "image/tiff");
        imgMimeTypeMap.put(".turbot", "image/florian");
        imgMimeTypeMap.put(".wbmp", "image/vnd.wap.wbmp");
        imgMimeTypeMap.put(".xif", "image/vnd.xiff");
        imgMimeTypeMap.put(".xpm", "image/x-xpixmap");
        imgMimeTypeMap.put(".x-png", "image/png");
        imgMimeTypeMap.put(".xwd", "image/x-xwindowdump");
    }
}

