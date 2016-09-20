/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspException
 *  javax.servlet.jsp.JspWriter
 *  javax.servlet.jsp.PageContext
 *  javax.servlet.jsp.tagext.TagSupport
 */
package com.ckfinder.connector;

import com.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

public class CKFinderTag
extends TagSupport {
    private static final long serialVersionUID = -9024559933529738729L;
    private static final String CKFINDER_DEFAULT_BASEPATH = "/ckfinder/";
    private static final String CKFINDER_DEFAULT_PAGE = "ckfinder.html";
    private static final String DEFAULT_HEIGHT = "400";
    private static final String DEFAULT_WIDTH = "100%";
    private String basePath;
    private String width;
    private String height;
    private String selectFunction;
    private String selectFunctionData;
    private String selectThumbnailFunction;
    private String selectThumbnailFunctionData;
    private boolean disableThumbnailSelection;
    private String className;
    private String id;
    private String startupPath;
    private String resourceType;
    private boolean rememberLastFolder = true;
    private boolean startupFolderExpanded;

    public int doStartTag() throws JspException {
        JspWriter out = this.pageContext.getOut();
        try {
            out.write("<iframe src=\"" + this.buildUrl() + "\" width=\"" + this.getWidth() + "\" height=\"" + this.getHeight() + "\" " + this.getClassName() + this.getId() + " frameborder=\"0\" scrolling=\"no\"></iframe>");
        }
        catch (Exception e) {
            try {
                HttpServletResponse resp = (HttpServletResponse)this.pageContext.getResponse();
                resp.reset();
                resp.sendError(500, "Problem with creating tag");
            }
            catch (IOException e1) {
                throw new JspException((Throwable)e1);
            }
        }
        return 6;
    }

    private String buildUrl() {
        String url = this.getBasePath();
        String qs = "";
        if (CKFinderTag.isNullOrEmpty(url)) {
            url = "/ckfinder/";
        }
        url = PathUtils.addSlashToEnd(url);
        url = url.concat("ckfinder.html");
        if (this.selectFunction != null && !this.selectFunction.equals("")) {
            qs = qs + "?action=js&amp;func=" + this.selectFunction;
        }
        if (this.selectFunctionData != null && !this.selectFunctionData.equals("")) {
            qs = qs + (!CKFinderTag.isNullOrEmpty(qs) ? "&amp;" : "?");
            try {
                qs = qs + "data=" + URLEncoder.encode(this.selectFunctionData, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                qs = qs + "data=" + this.selectFunctionData;
            }
        }
        if (this.disableThumbnailSelection) {
            qs = qs + (!CKFinderTag.isNullOrEmpty(qs) ? "&amp;" : "?");
            qs = qs + "dts=1";
        } else if (!CKFinderTag.isNullOrEmpty(this.selectThumbnailFunction) || !CKFinderTag.isNullOrEmpty(this.selectFunction)) {
            qs = qs + (!CKFinderTag.isNullOrEmpty(qs) ? "&amp;" : "?");
            qs = qs + "thumbFunc=" + (!CKFinderTag.isNullOrEmpty(this.selectThumbnailFunction) ? this.selectThumbnailFunction : this.selectFunction);
            if (!CKFinderTag.isNullOrEmpty(this.selectThumbnailFunctionData)) {
                try {
                    qs = qs + "&amp;tdata=" + URLEncoder.encode(this.selectThumbnailFunctionData, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    qs = qs + "&amp;tdata=" + this.selectThumbnailFunctionData;
                }
            } else if (CKFinderTag.isNullOrEmpty(this.selectThumbnailFunction) && !CKFinderTag.isNullOrEmpty(this.selectFunctionData)) {
                try {
                    qs = qs + "&amp;tdata=" + URLEncoder.encode(this.selectFunctionData, "UTF-8");
                }
                catch (UnsupportedEncodingException e) {
                    qs = qs + "&amp;tdata=" + this.selectFunctionData;
                }
            }
        }
        if (!CKFinderTag.isNullOrEmpty(this.startupPath)) {
            qs = qs + (!CKFinderTag.isNullOrEmpty(qs) ? "&amp;" : "?");
            try {
                qs = qs + "start=" + URLEncoder.encode(new StringBuilder().append(this.startupPath).append(this.startupFolderExpanded ? ":1" : ":0").toString(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                qs = qs + "start=" + (this.startupFolderExpanded ? ":1" : ":0");
            }
        }
        if (!CKFinderTag.isNullOrEmpty(this.resourceType)) {
            qs = qs + (!CKFinderTag.isNullOrEmpty(qs) ? "&amp;" : "?");
            try {
                qs = qs + "type=" + URLEncoder.encode(this.resourceType, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                qs = qs + "type=" + this.resourceType;
            }
        }
        if (this.rememberLastFolder) {
            qs = qs + (!CKFinderTag.isNullOrEmpty(qs) ? "&amp;" : "?");
            qs = qs + "rlf=0";
        }
        if (!CKFinderTag.isNullOrEmpty(this.id)) {
            qs = qs + (!CKFinderTag.isNullOrEmpty(qs) ? "&amp;" : "?");
            try {
                qs = qs + "id=" + URLEncoder.encode(this.id, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                qs = qs + "id=" + this.id;
            }
        }
        return url.concat(qs);
    }

    public final String getBasePath() {
        return this.basePath;
    }

    public final void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public final String getWidth() {
        if (CKFinderTag.isNullOrEmpty(this.width)) {
            return "100%";
        }
        return this.width;
    }

    public final void setWidth(String width) {
        this.width = width;
    }

    public final String getHeight() {
        if (CKFinderTag.isNullOrEmpty(this.height)) {
            return "400";
        }
        return this.height;
    }

    public final void setHeight(String height) {
        this.height = height;
    }

    public final String getSelectFunction() {
        return this.selectFunction;
    }

    public final void setSelectFunction(String selectFunction) {
        this.selectFunction = selectFunction;
    }

    public final String getSelectFunctionData() {
        return this.selectFunctionData;
    }

    public final void setSelectFunctionData(String selectFunctionData) {
        this.selectFunctionData = selectFunctionData;
    }

    public final String getSelectThumbnailFunction() {
        return this.selectThumbnailFunction;
    }

    public final void setSelectThumbnailFunction(String selectThumbnailFunction) {
        this.selectThumbnailFunction = selectThumbnailFunction;
    }

    public final String getSelectThumbnailFunctionData() {
        return this.selectThumbnailFunctionData;
    }

    public final void setSelectThumbnailFunctionData(String selectThumbnailFunctionData) {
        this.selectThumbnailFunctionData = selectThumbnailFunctionData;
    }

    public final boolean isDisableThumbnailSelection() {
        return this.disableThumbnailSelection;
    }

    public final void setDisableThumbnailSelection(boolean disableThumbnailSelection) {
        this.disableThumbnailSelection = disableThumbnailSelection;
    }

    public final String getClassName() {
        if (!CKFinderTag.isNullOrEmpty(this.className)) {
            return " class=\"" + this.className + "\"";
        }
        return "";
    }

    public final void setClassName(String className) {
        this.className = className;
    }

    public final String getId() {
        if (!CKFinderTag.isNullOrEmpty(this.id)) {
            return " id=\"" + this.id + "\"";
        }
        return "";
    }

    public final void setId(String id) {
        this.id = id;
    }

    public final String getStartupPath() {
        return this.startupPath;
    }

    public final void setStartupPath(String startupPath) {
        this.startupPath = startupPath;
    }

    public final String getResourceType() {
        return this.resourceType;
    }

    public final void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public final boolean isRememberLastFolder() {
        return this.rememberLastFolder;
    }

    public final void setRememberLastFolder(boolean rememberLastFolder) {
        this.rememberLastFolder = rememberLastFolder;
    }

    public final boolean isStartupFolderExpanded() {
        return this.startupFolderExpanded;
    }

    public final void setStartupFolderExpanded(boolean startupFolderExpanded) {
        this.startupFolderExpanded = startupFolderExpanded;
    }

    private static boolean isNullOrEmpty(String string) {
        return string == null || string.equals("");
    }
}

