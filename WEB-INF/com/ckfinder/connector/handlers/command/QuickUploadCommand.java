/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletResponse
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.handlers.command.FileUploadCommand;
import com.ckfinder.connector.utils.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

public class QuickUploadCommand
extends FileUploadCommand {
    @Override
    protected void handleOnUploadCompleteResponse(OutputStream out, String errorMsg) throws IOException {
        if (this.responseType != null && this.responseType.equalsIgnoreCase("json")) {
            this.handleJSONResponse(out, errorMsg, null);
        } else {
            out.write("<script type=\"text/javascript\">".getBytes("UTF-8"));
            out.write("window.parent.OnUploadCompleted(".getBytes("UTF-8"));
            out.write(("" + this.errorCode + ", ").getBytes("UTF-8"));
            if (this.uploaded) {
                out.write(("'" + this.configuration.getTypes().get(this.type).getUrl() + this.currentFolder + FileUtils.backupWithBackSlash(FileUtils.encodeURIComponent(this.newFileName), "'") + "', ").getBytes("UTF-8"));
                out.write(("'" + FileUtils.backupWithBackSlash(this.newFileName, "'") + "', ").getBytes("UTF-8"));
            } else {
                out.write("'', '', ".getBytes("UTF-8"));
            }
            out.write("''".getBytes("UTF-8"));
            out.write(");".getBytes("UTF-8"));
            out.write("</script>".getBytes("UTF-8"));
        }
    }

    @Override
    protected void handleOnUploadCompleteCallFuncResponse(OutputStream out, String errorMsg, String path) throws IOException {
        if (this.responseType != null && this.responseType.equalsIgnoreCase("json")) {
            this.handleJSONResponse(out, errorMsg, path);
        } else {
            out.write("<script type=\"text/javascript\">".getBytes("UTF-8"));
            this.ckEditorFuncNum = this.ckEditorFuncNum.replaceAll("[^\\d]", "");
            out.write(("window.parent.CKEDITOR.tools.callFunction(" + this.ckEditorFuncNum + ", '" + path + FileUtils.backupWithBackSlash(FileUtils.encodeURIComponent(this.newFileName), "'") + "', '" + errorMsg + "');").getBytes("UTF-8"));
            out.write("</script>".getBytes("UTF-8"));
        }
    }

    @Override
    protected boolean checkFuncNum() {
        return this.ckEditorFuncNum != null;
    }

    @Override
    public void setResponseHeader(HttpServletResponse response, ServletContext sc) {
        response.setCharacterEncoding("utf-8");
        if (this.responseType != null && this.responseType.equalsIgnoreCase("json")) {
            response.setContentType("application/json");
        } else {
            response.setContentType("text/html");
        }
    }

    private void handleJSONResponse(OutputStream out, String errorMsg, String path) throws IOException {
        Gson gson = new GsonBuilder().serializeNulls().create();
        HashMap<String, Object> jsonObj = new HashMap<String, Object>();
        jsonObj.put("fileName", this.newFileName);
        jsonObj.put("uploaded", this.uploaded ? new Integer(1) : new Integer(0));
        if (this.uploaded) {
            if (path != null && !path.equals("")) {
                jsonObj.put("url", path + FileUtils.backupWithBackSlash(FileUtils.encodeURIComponent(this.newFileName), "'"));
            } else {
                jsonObj.put("url", this.configuration.getTypes().get(this.type).getUrl() + this.currentFolder + FileUtils.backupWithBackSlash(FileUtils.encodeURIComponent(this.newFileName), "'"));
            }
        }
        if (errorMsg != null && !errorMsg.equals("")) {
            HashMap<String, Object> jsonErrObj = new HashMap<String, Object>();
            jsonErrObj.put("number", this.errorCode);
            jsonErrObj.put("message", errorMsg);
            jsonObj.put("error", jsonErrObj);
        }
        out.write(gson.toJson(jsonObj).getBytes("UTF-8"));
    }
}

