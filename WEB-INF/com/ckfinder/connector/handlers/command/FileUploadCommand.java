/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.fileupload.FileItem
 *  org.apache.commons.fileupload.FileItemFactory
 *  org.apache.commons.fileupload.FileUploadBase
 *  org.apache.commons.fileupload.FileUploadBase$FileSizeLimitExceededException
 *  org.apache.commons.fileupload.FileUploadBase$IOFileUploadException
 *  org.apache.commons.fileupload.FileUploadBase$InvalidContentTypeException
 *  org.apache.commons.fileupload.FileUploadBase$SizeLimitExceededException
 *  org.apache.commons.fileupload.disk.DiskFileItemFactory
 *  org.apache.commons.fileupload.servlet.ServletFileUpload
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.Events;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.AfterFileUploadEventArgs;
import com.ckfinder.connector.data.EventArgs;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.errors.ErrorUtils;
import com.ckfinder.connector.handlers.command.Command;
import com.ckfinder.connector.handlers.command.IPostCommand;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.FileUtils;
import com.ckfinder.connector.utils.ImageUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUploadCommand
extends Command
implements IPostCommand {
    protected String fileName = "";
    protected String newFileName = "";
    protected String ckEditorFuncNum;
    protected String responseType;
    protected String ckFinderFuncNum;
    private String langCode;
    protected boolean uploaded;
    protected int errorCode = 0;
    protected String customErrorMsg;
    private static final char[] UNSAFE_FILE_NAME_CHARS = new char[]{':', '*', '?', '|', '/'};

    public FileUploadCommand() {
        this.type = "";
        this.uploaded = false;
    }

    @Override
    public void execute(OutputStream out) throws ConnectorException {
        if (this.configuration.isDebugMode() && this.exception != null) {
            throw new ConnectorException(this.errorCode, this.exception);
        }
        try {
            String errorMsg = this.errorCode == 0 ? "" : (this.errorCode == 1 ? this.customErrorMsg : ErrorUtils.getInstance().getErrorMsgByLangAndCode(this.langCode, this.errorCode, this.configuration));
            errorMsg = errorMsg.replaceAll("%1", Matcher.quoteReplacement(this.newFileName));
            String path = "";
            if (!this.uploaded) {
                this.newFileName = "";
                this.currentFolder = "";
            } else {
                path = this.configuration.getTypes().get(this.type).getUrl() + this.currentFolder;
            }
            if (this.responseType != null && this.responseType.equals("txt")) {
                out.write((this.newFileName + "|" + errorMsg).getBytes("UTF-8"));
            } else if (this.checkFuncNum()) {
                this.handleOnUploadCompleteCallFuncResponse(out, errorMsg, path);
            } else {
                this.handleOnUploadCompleteResponse(out, errorMsg);
            }
        }
        catch (IOException e) {
            throw new ConnectorException(104, e);
        }
    }

    protected boolean checkFuncNum() {
        return this.ckFinderFuncNum != null;
    }

    protected void handleOnUploadCompleteCallFuncResponse(OutputStream out, String errorMsg, String path) throws IOException {
        this.ckFinderFuncNum = this.ckFinderFuncNum.replaceAll("[^\\d]", "");
        out.write("<script type=\"text/javascript\">".getBytes("UTF-8"));
        out.write(("window.parent.CKFinder.tools.callFunction(" + this.ckFinderFuncNum + ", '" + path + FileUtils.backupWithBackSlash(this.newFileName, "'") + "', '" + errorMsg + "');").getBytes("UTF-8"));
        out.write("</script>".getBytes("UTF-8"));
    }

    protected void handleOnUploadCompleteResponse(OutputStream out, String errorMsg) throws IOException {
        out.write("<script type=\"text/javascript\">".getBytes("UTF-8"));
        out.write("window.parent.OnUploadCompleted(".getBytes("UTF-8"));
        out.write(("'" + FileUtils.backupWithBackSlash(this.newFileName, "'") + "'").getBytes("UTF-8"));
        out.write((", '" + (this.errorCode != 0 ? errorMsg : "") + "'").getBytes("UTF-8"));
        out.write(");".getBytes("UTF-8"));
        out.write("</script>".getBytes("UTF-8"));
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        this.ckFinderFuncNum = request.getParameter("CKFinderFuncNum");
        this.ckEditorFuncNum = request.getParameter("CKEditorFuncNum");
        this.responseType = request.getParameter("response_type") != null ? request.getParameter("response_type") : request.getParameter("responseType");
        this.langCode = request.getParameter("langCode");
        if (this.errorCode == 0) {
            this.uploaded = this.uploadFile(request);
        }
    }

    private boolean uploadFile(HttpServletRequest request) {
        if (!AccessControlUtil.getInstance().checkFolderACL(this.type, this.currentFolder, this.userRole, 32)) {
            this.errorCode = 103;
            return false;
        }
        return this.fileUpload(request);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean fileUpload(HttpServletRequest request) {
        try {
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            ServletFileUpload uploadHandler = new ServletFileUpload((FileItemFactory)fileItemFactory);
            List items = uploadHandler.parseRequest(request);
            Collections.sort(items, new Comparator<FileItem>(){

                @Override
                public int compare(FileItem a, FileItem b) {
                    if (a.getFieldName().equals("ckCsrfToken")) {
                        return -1;
                    }
                    if (b.getFieldName().equals("ckCsrfToken")) {
                        return 1;
                    }
                    return 0;
                }
            });
            int i = 0;
            int j = items.size();
            do {
                if (i >= j) {
                    return false;
                }
                FileItem item = (FileItem)items.get(i);
                if (this.configuration.isEnableCsrfProtection() && (!((FileItem)items.get(0)).getFieldName().equals("ckCsrfToken") || item.getFieldName().equals("ckCsrfToken") && !this.checkCsrfToken(request, item.getString()))) {
                    throw new ConnectorException(109, "CSRF Attempt");
                }
                if (((FileItem)items.get(0)).getFieldName().equals("ckCsrfToken") && items.size() == 1) {
                    throw new ConnectorException(1, "No file provided in the request.");
                }
                if (!item.isFormField()) {
                    String path = this.configuration.getTypes().get(this.type).getPath() + this.currentFolder;
                    this.fileName = this.getFileItemName(item);
                    try {
                        if (this.validateUploadItem(item, path)) {
                            boolean bl = this.saveTemporaryFile(path, item);
                            return bl;
                        }
                    }
                    finally {
                        item.delete();
                    }
                }
                ++i;
            } while (true);
        }
        catch (FileUploadBase.InvalidContentTypeException e) {
            if (this.configuration.isDebugMode()) {
                this.exception = e;
            }
            this.errorCode = 204;
            return false;
        }
        catch (FileUploadBase.IOFileUploadException e) {
            if (this.configuration.isDebugMode()) {
                this.exception = e;
            }
            this.errorCode = 104;
            return false;
        }
        catch (FileUploadBase.SizeLimitExceededException e) {
            this.errorCode = 203;
            return false;
        }
        catch (FileUploadBase.FileSizeLimitExceededException e) {
            this.errorCode = 203;
            return false;
        }
        catch (ConnectorException e) {
            this.errorCode = e.getErrorCode();
            if (this.errorCode == 1) {
                this.customErrorMsg = e.getErrorMsg();
            }
            return false;
        }
        catch (Exception e) {
            if (this.configuration.isDebugMode()) {
                this.exception = e;
            }
            this.errorCode = 104;
            return false;
        }
    }

    private boolean saveTemporaryFile(String path, FileItem item) throws Exception {
        File file = new File(path, this.newFileName);
        AfterFileUploadEventArgs args = new AfterFileUploadEventArgs();
        args.setCurrentFolder(this.currentFolder);
        args.setFile(file);
        args.setFileContent(item.get());
        if (!ImageUtils.isImage(file)) {
            item.write(file);
            if (this.configuration.getEvents() != null) {
                this.configuration.getEvents().run(Events.EventTypes.AfterFileUpload, args, this.configuration);
            }
            return true;
        }
        if (ImageUtils.checkImageSize(item.getInputStream(), this.configuration) || this.configuration.checkSizeAfterScaling()) {
            ImageUtils.createTmpThumb(item.getInputStream(), file, this.getFileItemName(item), this.configuration);
            if (!this.configuration.checkSizeAfterScaling() || FileUtils.checkFileSize(this.configuration.getTypes().get(this.type), file.length())) {
                if (this.configuration.getEvents() != null) {
                    this.configuration.getEvents().run(Events.EventTypes.AfterFileUpload, args, this.configuration);
                }
                return true;
            }
            file.delete();
            this.errorCode = 203;
            return false;
        }
        this.errorCode = 203;
        return false;
    }

    private String getFinalFileName(String path, String name) {
        File file = new File(path, name);
        int number = 0;
        String nameWithoutExtension = FileUtils.getFileNameWithoutExtension(name, false);
        Pattern p = Pattern.compile("^(AUX|COM\\d|CLOCK\\$|CON|NUL|PRN|LPT\\d)$", 2);
        Matcher m = p.matcher(nameWithoutExtension);
        boolean protectedName = m.find();
        while (file.exists() || protectedName) {
            StringBuilder sb = new StringBuilder();
            sb.append(FileUtils.getFileNameWithoutExtension(name, false));
            sb.append("(").append(++number).append(").");
            sb.append(FileUtils.getFileExtension(name, false));
            this.newFileName = sb.toString();
            file = new File(path, this.newFileName);
            this.errorCode = 201;
            protectedName = false;
        }
        return this.newFileName;
    }

    private boolean validateUploadItem(FileItem item, String path) {
        if (item.getName() == null || item.getName().length() <= 0) {
            this.errorCode = 202;
            return false;
        }
        this.fileName = this.getFileItemName(item);
        this.newFileName = this.fileName;
        for (char c : UNSAFE_FILE_NAME_CHARS) {
            this.newFileName = this.newFileName.replace(c, '_');
        }
        if (this.configuration.isDisallowUnsafeCharacters()) {
            this.newFileName = this.newFileName.replace(';', '_');
        }
        if (this.configuration.forceASCII()) {
            this.newFileName = FileUtils.convertToASCII(this.newFileName);
        }
        if (!this.newFileName.equals(this.fileName)) {
            this.errorCode = 207;
        }
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            this.errorCode = 109;
            return false;
        }
        if (!FileUtils.checkFileName(this.newFileName) || FileUtils.checkIfFileIsHidden(this.newFileName, this.configuration)) {
            this.errorCode = 102;
            return false;
        }
        ResourceType resourceType = this.configuration.getTypes().get(this.type);
        int checkFileExt = FileUtils.checkFileExtension(this.newFileName, resourceType);
        if (checkFileExt == 1) {
            this.errorCode = 105;
            return false;
        }
        if (this.configuration.ckeckDoubleFileExtensions()) {
            this.newFileName = FileUtils.renameFileWithBadExt(resourceType, this.newFileName);
        }
        try {
            File file = new File(path, this.getFinalFileName(path, this.newFileName));
            if (!(ImageUtils.isImage(file) && this.configuration.checkSizeAfterScaling() || FileUtils.checkFileSize(resourceType, item.getSize()))) {
                this.errorCode = 203;
                return false;
            }
            if (this.configuration.getSecureImageUploads() && ImageUtils.isImage(file) && !ImageUtils.checkImageFile(item)) {
                this.errorCode = 204;
                return false;
            }
            if (!FileUtils.checkIfFileIsHtmlFile(file.getName(), this.configuration) && FileUtils.detectHtml(item)) {
                this.errorCode = 206;
                return false;
            }
        }
        catch (SecurityException e) {
            if (this.configuration.isDebugMode()) {
                this.exception = e;
            }
            this.errorCode = 104;
            return false;
        }
        catch (IOException e) {
            if (this.configuration.isDebugMode()) {
                this.exception = e;
            }
            this.errorCode = 104;
            return false;
        }
        return true;
    }

    @Override
    public void setResponseHeader(HttpServletResponse response, ServletContext sc) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html");
    }

    private String getFileItemName(FileItem item) {
        Pattern p = Pattern.compile("[^\\\\/]+$");
        Matcher m = p.matcher(item.getName());
        return m.find() ? m.group(0) : "";
    }

    @Override
    protected boolean checkParam(String reqParam) throws ConnectorException {
        if (reqParam == null || reqParam.equals("")) {
            return true;
        }
        if (Pattern.compile("(/\\.|\\p{Cntrl}|//|\\\\|[:*?<>\"\\|])").matcher(reqParam).find()) {
            this.errorCode = 102;
            return false;
        }
        return true;
    }

    @Override
    protected boolean checkHidden() throws ConnectorException {
        if (FileUtils.checkIfDirIsHidden(this.currentFolder, this.configuration)) {
            this.errorCode = 109;
            return true;
        }
        return false;
    }

    @Override
    protected boolean checkConnector(HttpServletRequest request) throws ConnectorException {
        if (!this.configuration.enabled() || !this.configuration.checkAuthentication(request)) {
            this.errorCode = 500;
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
            this.errorCode = 116;
            return false;
        }
        return false;
    }

    @Override
    protected boolean checkIfTypeExists(String type) {
        ResourceType testType = this.configuration.getTypes().get(type);
        if (testType == null) {
            this.errorCode = 12;
            return false;
        }
        return true;
    }

}

