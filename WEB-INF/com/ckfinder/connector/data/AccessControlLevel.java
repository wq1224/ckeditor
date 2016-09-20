/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

public class AccessControlLevel {
    private String role;
    private String resourceType;
    private String folder;
    private boolean folderView;
    private boolean folderCreate;
    private boolean folderRename;
    private boolean folderDelete;
    private boolean fileView;
    private boolean fileUpload;
    private boolean fileRename;
    private boolean fileDelete;

    public final String getRole() {
        return this.role;
    }

    public final void setRole(String role) {
        this.role = role;
    }

    public final String getResourceType() {
        return this.resourceType;
    }

    public final void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public final String getFolder() {
        return this.folder;
    }

    public final void setFolder(String folder) {
        this.folder = folder;
    }

    public final boolean isFolderView() {
        return this.folderView;
    }

    public final void setFolderView(boolean folderView) {
        this.folderView = folderView;
    }

    public final boolean isFolderCreate() {
        return this.folderCreate;
    }

    public final void setFolderCreate(boolean folderCreate) {
        this.folderCreate = folderCreate;
    }

    public final boolean isFolderRename() {
        return this.folderRename;
    }

    public final void setFolderRename(boolean folderRename) {
        this.folderRename = folderRename;
    }

    public final boolean isFolderDelete() {
        return this.folderDelete;
    }

    public final void setFolderDelete(boolean folderDelete) {
        this.folderDelete = folderDelete;
    }

    public final boolean isFileView() {
        return this.fileView;
    }

    public final void setFileView(boolean fileView) {
        this.fileView = fileView;
    }

    public final boolean isFileUpload() {
        return this.fileUpload;
    }

    public final void setFileUpload(boolean fileUpload) {
        this.fileUpload = fileUpload;
    }

    public final boolean isFileRename() {
        return this.fileRename;
    }

    public final void setFileRename(boolean fileRename) {
        this.fileRename = fileRename;
    }

    public final boolean isFileDelete() {
        return this.fileDelete;
    }

    public final void setFileDelete(boolean fileDelete) {
        this.fileDelete = fileDelete;
    }

    public AccessControlLevel(AccessControlLevel acl) {
        this.role = acl.role;
        this.resourceType = acl.resourceType;
        this.folder = acl.folder;
        this.folderView = acl.folderView;
        this.folderCreate = acl.folderCreate;
        this.folderRename = acl.folderRename;
        this.folderDelete = acl.folderDelete;
        this.fileView = acl.fileView;
        this.fileUpload = acl.fileUpload;
        this.fileRename = acl.fileRename;
        this.fileDelete = acl.fileDelete;
    }

    public AccessControlLevel() {
    }
}

