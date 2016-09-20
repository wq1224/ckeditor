/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package com.ckfinder.connector.configuration;

import javax.servlet.http.HttpServletRequest;

public interface IBasePathBuilder {
    public String getBaseDir(HttpServletRequest var1);

    public String getBaseUrl(HttpServletRequest var1);
}

