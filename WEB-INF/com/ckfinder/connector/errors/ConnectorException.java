/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.errors;

public class ConnectorException
extends Exception {
    private static final long serialVersionUID = -8643752550259111562L;
    private int errorCode;
    private String errorMsg;
    private boolean addCurrentFolder;
    private Exception exception;

    public ConnectorException(int errorCode) {
        this.addCurrentFolder = true;
        this.errorCode = errorCode;
    }

    public ConnectorException(int errorCode, boolean addCurrentFolder) {
        this(errorCode);
        this.addCurrentFolder = addCurrentFolder;
    }

    public ConnectorException(int errorCode, String errorMsg) {
        this(errorCode);
        this.errorMsg = errorMsg;
    }

    public ConnectorException(int errorCode, Exception e) {
        this(errorCode);
        this.exception = e;
        this.addCurrentFolder = false;
        this.errorMsg = e.getMessage();
    }

    public ConnectorException(Exception e) {
        this.exception = e;
        if (e instanceof ConnectorException) {
            this.errorCode = ((ConnectorException)e).getErrorCode();
            this.addCurrentFolder = ((ConnectorException)e).addCurrentFolder;
        } else {
            this.addCurrentFolder = false;
            this.errorCode = 110;
            this.errorMsg = e.getMessage();
        }
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMsg;
    }

    public boolean isAddCurrentFolder() {
        return this.addCurrentFolder;
    }

    public final String getErrorMsg() {
        return this.errorMsg;
    }

    public final void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public final Exception getException() {
        return this.exception;
    }

    public final void setException(Exception exception) {
        this.exception = exception;
    }
}

