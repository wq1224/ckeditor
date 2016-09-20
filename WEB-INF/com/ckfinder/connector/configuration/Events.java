/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.EventArgs;
import com.ckfinder.connector.data.EventCommandData;
import com.ckfinder.connector.data.IEventHandler;
import com.ckfinder.connector.data.PluginInfo;
import com.ckfinder.connector.errors.ConnectorException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events {
    private Map<EventTypes, List<EventCommandData>> eventHandlers = new HashMap<EventTypes, List<EventCommandData>>();

    public Events() {
        this.eventHandlers.put(EventTypes.AfterFileUpload, new ArrayList());
        this.eventHandlers.put(EventTypes.InitCommand, new ArrayList());
        this.eventHandlers.put(EventTypes.BeforeExecuteCommand, new ArrayList());
    }

    public void addEventHandler(EventTypes event, Class<? extends IEventHandler> eventHandler) {
        EventCommandData eventCommandData = new EventCommandData(eventHandler);
        this.eventHandlers.get((Object)event).add(eventCommandData);
    }

    public void addEventHandler(EventTypes event, Class<? extends IEventHandler> eventHandler, PluginInfo pluginInfo) {
        EventCommandData eventCommandData = new EventCommandData(eventHandler);
        eventCommandData.setPluginInfo(pluginInfo);
        this.eventHandlers.get((Object)event).add(eventCommandData);
    }

    public boolean run(EventTypes eventTyp, EventArgs args, IConfiguration configuration) throws ConnectorException {
        for (EventCommandData eventCommandData : this.eventHandlers.get((Object)eventTyp)) {
            try {
                IEventHandler events = eventCommandData.getPluginInfo() != null ? eventCommandData.getEventListener().getConstructor(PluginInfo.class).newInstance(eventCommandData.getPluginInfo()) : eventCommandData.getEventListener().newInstance();
                if (events.runEventHandler(args, configuration)) continue;
                return false;
            }
            catch (ConnectorException ex) {
                throw ex;
            }
            catch (Exception e) {
                throw new ConnectorException(e);
            }
        }
        return true;
    }

    public static enum EventTypes {
        BeforeExecuteCommand,
        AfterFileUpload,
        InitCommand;
        

        private EventTypes() {
        }
    }

}

