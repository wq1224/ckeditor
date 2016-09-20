/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.EventArgs;
import com.ckfinder.connector.errors.ConnectorException;

public interface IEventHandler {
    public boolean runEventHandler(EventArgs var1, IConfiguration var2) throws ConnectorException;
}

