/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.utils;

import com.ckfinder.connector.errors.ConnectorException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLCreator {
    private Document document;
    private List<ErrorNode> errorList = new ArrayList<ErrorNode>();

    public void createDocument() throws ConnectorException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            this.document = documentBuilder.newDocument();
            this.document.setXmlStandalone(true);
        }
        catch (Exception e) {
            throw new ConnectorException(104, e);
        }
    }

    public Document getDocument() {
        return this.document;
    }

    public String getDocumentAsText() throws ConnectorException {
        try {
            StringWriter stw = new StringWriter();
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.transform(new DOMSource(this.document), new StreamResult(stw));
            return stw.toString();
        }
        catch (TransformerException e) {
            throw new ConnectorException(104, e);
        }
    }

    public void addErrorCommandToRoot(Element rootElement, int errorNum, String errorText) {
        Element element = this.getDocument().createElement("Error");
        element.setAttribute("number", String.valueOf(errorNum));
        if (errorText != null) {
            element.setTextContent(errorText);
        }
        rootElement.appendChild(element);
    }

    public void appendErrorNodeChild(int errorCode, String name, String path, String type) {
        ErrorNode errorNode = new ErrorNode(path, type, name, errorCode);
        this.errorList.add(errorNode);
    }

    public void addErrors(Element errorsNode) {
        for (ErrorNode item : this.errorList) {
            Element childElem = this.getDocument().createElement("Error");
            childElem.setAttribute("code", String.valueOf(item.errorCode));
            childElem.setAttribute("name", item.name);
            childElem.setAttribute("type", item.type);
            childElem.setAttribute("folder", item.folder);
            errorsNode.appendChild(childElem);
        }
    }

    public boolean hasErrors() {
        return !this.errorList.isEmpty();
    }

    private class ErrorNode {
        private String folder;
        private String type;
        private String name;
        private int errorCode;

        public ErrorNode(String folder, String type, String name, int errorCode) {
            this.folder = folder;
            this.type = type;
            this.name = name;
            this.errorCode = errorCode;
        }
    }

}

