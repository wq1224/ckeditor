/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

import com.ckfinder.connector.data.EventArgs;
import com.ckfinder.connector.utils.XMLCreator;
import org.w3c.dom.Element;

public class InitCommandEventArgs
extends EventArgs {
    private XMLCreator xml;
    private Element rootElement;

    public final Element getRootElement() {
        return this.rootElement;
    }

    public final void setRootElement(Element rootElement) {
        this.rootElement = rootElement;
    }

    public final XMLCreator getXml() {
        return this.xml;
    }

    public final void setXml(XMLCreator xml) {
        this.xml = xml;
    }
}

