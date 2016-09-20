/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.ckfinder.connector.data;

import com.ckfinder.connector.data.EventArgs;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BeforeExecuteCommandEventArgs
extends EventArgs {
    private String command;
    private HttpServletRequest request;
    private HttpServletResponse response;

    public final String getCommand() {
        return this.command;
    }

    public final void setCommand(String command) {
        this.command = command;
    }

    public final HttpServletRequest getRequest() {
        return this.request;
    }

    public final void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public final HttpServletResponse getResponse() {
        return this.response;
    }

    public final void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}

