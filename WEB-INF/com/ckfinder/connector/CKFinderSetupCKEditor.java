/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.JspException
 *  javax.servlet.jsp.PageContext
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.ckfinder.connector;

import com.ckfinder.connector.utils.PathUtils;
import java.util.HashMap;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

public class CKFinderSetupCKEditor
extends TagSupport {
    private static final String CKFINDER_UPLOAD_URL = "/ckfinder/core/connector/java/connector.java?command=QuickUpload&type=";
    private static final String CKFINDER_PAGE = "/ckfinder.html";
    private static final String DEFAULT_IMAGE_TYPE = "Images";
    private static final String DEFAULT_FLASH_TYPE = "Flash";
    private static final long serialVersionUID = 3947714242365900445L;
    private String basePath;
    private String editor;
    private String imageType;
    private String flashType;

    public int doStartTag() throws JspException {
        if (this.imageType == null || this.imageType.equals("")) {
            this.imageType = "Images";
        }
        if (this.flashType == null || this.flashType.equals("")) {
            this.flashType = "Flash";
        }
        HashMap attr = new HashMap();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("filebrowserBrowseUrl", this.buildBrowseUrl(null));
        params.put("filebrowserUploadUrl", this.buildUploadUrl("Files"));
        params.put("filebrowserImageBrowseUrl", this.buildBrowseUrl(this.imageType));
        params.put("filebrowserImageUploadUrl", this.buildUploadUrl(this.imageType));
        params.put("filebrowserFlashBrowseUrl", this.buildBrowseUrl(this.flashType));
        params.put("filebrowserFlashUploadUrl", this.buildUploadUrl(this.flashType));
        if (this.editor == null || this.editor.equals("")) {
            attr.put("*", params);
        } else {
            attr.put(this.editor, params);
        }
        this.pageContext.setAttribute("ckeditor-params", attr);
        return 6;
    }

    private String buildBrowseUrl(String type) {
        return PathUtils.escape(this.basePath.concat("/ckfinder.html").concat(type == null ? "" : "?type=".concat(type)));
    }

    private String buildUploadUrl(String type) {
        if (this.pageContext.getRequest() instanceof HttpServletRequest) {
            return ((HttpServletRequest)this.pageContext.getRequest()).getContextPath().concat("/ckfinder/core/connector/java/connector.java?command=QuickUpload&type=").concat(type);
        }
        return "/ckfinder/core/connector/java/connector.java?command=QuickUpload&type=".concat(type);
    }

    public final void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public final void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public final void setFlashType(String flashType) {
        this.flashType = flashType;
    }

    public final void setEditor(String editor) {
        this.editor = editor;
    }
}

