/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.ckfinder.connector.handlers.command;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.handlers.command.Command;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.XMLCreator;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class XMLCommand
extends Command {
    protected XMLCreator creator;

    @Override
    public void setResponseHeader(HttpServletResponse response, ServletContext sc) {
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("utf-8");
    }

    @Override
    public void execute(OutputStream out) throws ConnectorException {
        try {
            this.createXMLResponse(this.getDataForXml());
            out.write(this.creator.getDocumentAsText().getBytes("UTF-8"));
        }
        catch (ConnectorException e) {
            throw e;
        }
        catch (IOException e) {
            throw new ConnectorException(104, e);
        }
    }

    private void createXMLResponse(int errorNum) throws ConnectorException {
        if (this.configuration.isDebugMode() && this.exception != null) {
            throw new ConnectorException(this.exception);
        }
        Element rootElement = this.creator.getDocument().createElement("Connector");
        if (this.type != null && !this.type.equals("")) {
            rootElement.setAttribute("resourceType", this.type);
        }
        if (this.mustAddCurrentFolderNode()) {
            this.createCurrentFolderNode(rootElement);
        }
        this.creator.addErrorCommandToRoot(rootElement, errorNum, this.getErrorMsg(errorNum));
        this.createXMLChildNodes(errorNum, rootElement);
        this.creator.getDocument().appendChild(rootElement);
    }

    protected String getErrorMsg(int errorNum) {
        return null;
    }

    protected abstract void createXMLChildNodes(int var1, Element var2) throws ConnectorException;

    protected abstract int getDataForXml();

    protected void createCurrentFolderNode(Element rootElement) {
        Element element = this.creator.getDocument().createElement("CurrentFolder");
        element.setAttribute("path", this.currentFolder);
        element.setAttribute("url", this.configuration.getTypes().get(this.type).getUrl() + this.currentFolder);
        element.setAttribute("acl", String.valueOf(AccessControlUtil.getInstance().checkACLForRole(this.type, this.currentFolder, this.userRole)));
        rootElement.appendChild(element);
    }

    @Override
    public /* varargs */ void initParams(HttpServletRequest request, IConfiguration configuration, Object ... params) throws ConnectorException {
        super.initParams(request, configuration, params);
        this.creator = new XMLCreator();
        this.creator.createDocument();
    }

    protected boolean mustAddCurrentFolderNode() {
        return this.type != null && this.currentFolder != null;
    }
}

