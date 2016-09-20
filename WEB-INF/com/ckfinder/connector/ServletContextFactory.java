/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 */
package com.ckfinder.connector;

import com.ckfinder.connector.errors.ConnectorException;
import javax.servlet.ServletContext;

public class ServletContextFactory {
    private static ServletContext servletContext;

    static void setServletContext(ServletContext servletContext1) {
        servletContext = servletContext1;
    }

    public static ServletContext getServletContext() throws ConnectorException {
        if (servletContext != null) {
            return servletContext;
        }
        throw new ConnectorException(110, "Servlet contex is null. Try to restart server.");
    }
}

