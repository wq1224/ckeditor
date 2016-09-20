/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.data;

import com.ckfinder.connector.data.XmlAttribute;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlElementData {
    private String name;
    private String value;
    private List<XmlAttribute> attributes;
    private List<XmlElementData> childrens;

    public XmlElementData(String name) {
        this.name = name;
        this.attributes = new ArrayList<XmlAttribute>();
        this.childrens = new ArrayList<XmlElementData>();
    }

    public void addToDocument(Document document, Element parent) {
        Element element = this.toElement(document);
        for (XmlElementData xmlElementData : this.childrens) {
            element.appendChild(xmlElementData.toElement(document));
        }
        for (XmlAttribute attribute : this.attributes) {
            element.setAttribute(attribute.getKey(), attribute.getValue());
        }
        if (parent != null) {
            parent.appendChild(element);
        } else {
            document.appendChild(element);
        }
    }

    private Element toElement(Document document) {
        return document.createElement(this.name);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<XmlAttribute> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(List<XmlAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<XmlElementData> getChildrens() {
        return this.childrens;
    }

    public void setChildrens(List<XmlElementData> childrens) {
        this.childrens = childrens;
    }
}

