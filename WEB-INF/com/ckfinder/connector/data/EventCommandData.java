/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

import com.ckfinder.connector.data.IEventHandler;
import com.ckfinder.connector.data.PluginInfo;

public class EventCommandData {
    private Class<? extends IEventHandler> eventListener;
    private PluginInfo pluginInfo;

    public EventCommandData(Class<? extends IEventHandler> eventListener) {
        this.eventListener = eventListener;
    }

    public final Class<? extends IEventHandler> getEventListener() {
        return this.eventListener;
    }

    public final void setEventListener(Class<? extends IEventHandler> eventListener) {
        this.eventListener = eventListener;
    }

    public final PluginInfo getPluginInfo() {
        return this.pluginInfo;
    }

    public final void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }
}

