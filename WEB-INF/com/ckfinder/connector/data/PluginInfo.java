/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

import com.ckfinder.connector.data.PluginParam;
import java.util.List;

public class PluginInfo {
    private String name;
    private String className;
    private List<PluginParam> params;
    private boolean enabled;
    private boolean internal;

    public final boolean isEnabled() {
        return this.enabled;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public final boolean isInternal() {
        return this.internal;
    }

    public final void setInternal(boolean internal) {
        this.internal = internal;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getClassName() {
        return this.className;
    }

    public final void setClassName(String className) {
        this.className = className;
    }

    public final List<PluginParam> getParams() {
        return this.params;
    }

    public final void setParams(List<PluginParam> params) {
        this.params = params;
    }

    public PluginInfo(PluginInfo info) {
        this.name = info.name;
        this.className = info.className;
        this.params = info.params;
        this.enabled = info.enabled;
        this.internal = info.internal;
    }

    public PluginInfo() {
    }
}

