/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  com.ckfinder.connector.configuration.Events
 *  com.ckfinder.connector.configuration.Events$EventTypes
 *  com.ckfinder.connector.configuration.Plugin
 */
package com.ckfinder.connector.plugins;

import com.ckfinder.connector.configuration.Events;
import com.ckfinder.connector.configuration.Plugin;
import com.ckfinder.connector.plugins.SaveFileCommand;

public class FileEditor
extends Plugin {
    public void registerEventHandlers(Events events) {
        events.addEventHandler(Events.EventTypes.BeforeExecuteCommand, (Class)SaveFileCommand.class);
    }
}

