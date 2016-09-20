/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

public class ResourceType {
    private static final int BYTES = 1024;
    private String name;
    private String url;
    private String path;
    private String maxSize;
    private String allowedExtensions;
    private String deniedExtensions;

    public ResourceType(String name, String url, String directory, String maxSize, String allowedExtensions, String deniedExtensions) {
        this.allowedExtensions = allowedExtensions;
        this.deniedExtensions = deniedExtensions;
        this.path = directory;
        this.maxSize = maxSize;
        this.name = name;
        this.url = url;
    }

    public ResourceType(String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getUrl() {
        if (this.url == null) {
            return "%BASE_URL%".concat("/").concat(this.name.toLowerCase()).concat("/");
        }
        return this.url;
    }

    public final void setUrl(String url) {
        this.url = url;
    }

    public final String getPath() {
        if (this.path == null) {
            return "%BASE_DIR%".concat(this.name.toLowerCase()).concat("/");
        }
        return this.path;
    }

    public final void setPath(String directory) {
        this.path = directory;
    }

    public final Long getMaxSize() {
        try {
            if (this.maxSize == null || this.maxSize.equals("") || this.maxSize.equals("0")) {
                return null;
            }
            return this.parseMaxSize();
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private long parseMaxSize() {
        int a;
        char lastChar = this.maxSize.toLowerCase().charAt(this.maxSize.length() - 1);
        switch (lastChar) {
            case 'k': {
                a = 1024;
                break;
            }
            case 'm': {
                a = 1048576;
                break;
            }
            case 'g': {
                a = 1073741824;
                break;
            }
            default: {
                return 0;
            }
        }
        long value = Long.valueOf(this.maxSize.substring(0, this.maxSize.length() - 1));
        return value * (long)a;
    }

    public final void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    public final String getAllowedExtensions() {
        if (this.allowedExtensions == null) {
            return "";
        }
        return this.allowedExtensions;
    }

    public final void setAllowedExtensions(String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public final String getDeniedExtensions() {
        if (this.deniedExtensions == null) {
            return "";
        }
        return this.deniedExtensions;
    }

    public final void setDeniedExtensions(String deniedExtensions) {
        this.deniedExtensions = deniedExtensions;
    }

    public ResourceType(ResourceType type) {
        this.name = type.name;
        this.url = type.url;
        this.path = type.path;
        this.maxSize = type.maxSize;
        this.allowedExtensions = type.allowedExtensions;
        this.deniedExtensions = type.deniedExtensions;
    }
}

