/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.configuration.Events;
import com.ckfinder.connector.data.PluginInfo;

public abstract class Plugin {
    protected PluginInfo pluginInfo;

    public abstract void registerEventHandlers(Events var1);

    public final void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }
}

