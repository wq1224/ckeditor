/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

import com.ckfinder.connector.data.EventArgs;
import java.io.File;

public class AfterFileUploadEventArgs
extends EventArgs {
    private String currentFolder;
    private byte[] fileContent;
    private File file;

    public final String getCurrentFolder() {
        return this.currentFolder;
    }

    public final void setCurrentFolder(String currentFolder) {
        this.currentFolder = currentFolder;
    }

    public final byte[] getFileContent() {
        return this.fileContent;
    }

    public final void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public final File getFile() {
        return this.file;
    }

    public final void setFile(File file) {
        this.file = file;
    }
}

