/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  org.jboss.vfs.TempFileProvider
 *  org.jboss.vfs.VFS
 *  org.jboss.vfs.VFSUtils
 *  org.jboss.vfs.VirtualFile
 *  org.jboss.vfs.spi.MountHandle
 */
package com.ckfinder.connector.errors;

import com.ckfinder.connector.ConnectorServlet;
import com.ckfinder.connector.configuration.IConfiguration;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jboss.vfs.TempFileProvider;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.spi.MountHandle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ErrorUtils {
    private static ErrorUtils errorUtils;
    private static Map<String, Map<Integer, String>> langMap;

    public String getErrorMsgByLangAndCode(String lang, int errorCode, IConfiguration conf) {
        if (lang != null && langMap.get(lang) != null) {
            return langMap.get(lang).get(errorCode);
        }
        if (langMap.get("en") != null) {
            return langMap.get("en").get(errorCode);
        }
        if (conf.isDebugMode()) {
            return "Unable to load error message";
        }
        return "";
    }

    public static ErrorUtils getInstance() {
        if (errorUtils == null) {
            errorUtils = new ErrorUtils();
        }
        return errorUtils;
    }

    private ErrorUtils() {
        List<String> allAvailLangCodes = this.getLangCodeFromJars();
        langMap = new HashMap<String, Map<Integer, String>>();
        for (String langCode : allAvailLangCodes) {
            langMap.put(langCode, this.getMessagesByLangCode(langCode));
        }
    }

    private List<String> getLangCodeFromJars() {
        ArrayList<String> langFiles = new ArrayList<String>();
        try {
            URL dirURL = ConnectorServlet.class.getResource("/lang/");
            String protocol = dirURL.getProtocol();
            if ("file".equalsIgnoreCase(protocol)) {
                String path = URLDecoder.decode(dirURL.getPath(), "UTF-8");
                File f = new File(path);
                if (f.exists() && f.isDirectory()) {
                    for (File file : f.listFiles()) {
                        langFiles.add(file.getName().replaceAll(".xml", ""));
                    }
                }
            } else if ("jar".equalsIgnoreCase(protocol) || "zip".equalsIgnoreCase(protocol)) {
                String jarPath = dirURL.getPath().substring("zip".equalsIgnoreCase(protocol) ? 0 : 5, dirURL.getPath().indexOf("!"));
                JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                Enumeration<JarEntry> entries = jarFile.entries();
                Pattern pattern = Pattern.compile("lang.+\\.xml");
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (!this.checkJarEntry(jarEntry, pattern)) continue;
                    langFiles.add(jarEntry.getName().replaceAll("lang/", "").replaceAll(".xml", ""));
                }
            } else if ("vfs".equalsIgnoreCase(protocol)) {
                VirtualFile langDir = VFS.getChild((String)dirURL.getPath());
                List langFilesList = langDir.getChildren();
                if (langFilesList.size() > 0) {
                    for (VirtualFile vFile : langFilesList) {
                        File contentsFile = vFile.getPhysicalFile();
                        langFiles.add(contentsFile.getName().replaceAll(".xml", ""));
                    }
                } else {
                    VirtualFile jarFile = VFS.getChild((String)dirURL.getPath().substring(1, dirURL.getPath().indexOf(".jar") + 4));
                    if (jarFile.exists()) {
                        TempFileProvider tempFileProvider = TempFileProvider.create((String)"tmpjar", (ScheduledExecutorService)Executors.newScheduledThreadPool(2));
                        MountHandle jarHandle = (MountHandle)VFS.mountZip((VirtualFile)jarFile, (VirtualFile)jarFile, (TempFileProvider)tempFileProvider);
                        File mountJar = jarHandle.getMountSource();
                        this.addFileNamesToList(mountJar, jarFile, langFiles);
                        VFSUtils.safeClose((Closeable)jarHandle);
                    } else {
                        File mountWar;
                        TempFileProvider tempWarFileProvider;
                        MountHandle warHandle;
                        VirtualFile warFile = VFS.getChild((String)dirURL.getPath().substring(1, dirURL.getPath().indexOf(".war") + 4));
                        if (warFile.exists() && (mountWar = (warHandle = (MountHandle)VFS.mountZip((VirtualFile)warFile, (VirtualFile)warFile, (TempFileProvider)(tempWarFileProvider = TempFileProvider.create((String)"tmpwar", (ScheduledExecutorService)Executors.newScheduledThreadPool(2))))).getMountSource()).exists()) {
                            TempFileProvider tempJarFileProvider = TempFileProvider.create((String)"tmpjar", (ScheduledExecutorService)Executors.newScheduledThreadPool(2));
                            MountHandle jarHandle = (MountHandle)VFS.mountZip((VirtualFile)jarFile, (VirtualFile)jarFile, (TempFileProvider)tempJarFileProvider);
                            File mountJar = jarHandle.getMountSource();
                            this.addFileNamesToList(mountJar, jarFile, langFiles);
                            VFSUtils.safeClose((Closeable)jarHandle);
                            VFSUtils.safeClose((Closeable)warHandle);
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            return null;
        }
        return langFiles;
    }

    private void addFileNamesToList(File mountJar, VirtualFile jarFile, List<String> langFiles) throws IOException {
        if (mountJar.exists()) {
            List jarLangFilesList = jarFile.getChild("lang").getChildrenRecursively();
            for (VirtualFile vFile : jarLangFilesList) {
                File contentsFile = vFile.getPhysicalFile();
                langFiles.add(contentsFile.getName().replaceAll(".xml", ""));
            }
        }
    }

    private boolean checkJarEntry(JarEntry jarEntry, Pattern pattern) {
        return pattern.matcher(jarEntry.getName()).matches();
    }

    private Map<Integer, String> getMessagesByLangCode(String langCode) {
        HashMap<Integer, String> langCodeMap = new HashMap<Integer, String>();
        try {
            InputStream is = ConnectorServlet.class.getResourceAsStream("/lang/" + langCode + ".xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList unkonwErrornodeList = doc.getElementsByTagName("errorUnknown");
            NodeList errorNodeList = doc.getElementsByTagName("error");
            Element unkonwErrorElem = (Element)unkonwErrornodeList.item(0);
            langCodeMap.put(1, unkonwErrorElem.getTextContent());
            int j = errorNodeList.getLength();
            for (int i = 0; i < j; ++i) {
                Element element = (Element)errorNodeList.item(i);
                langCodeMap.put(Integer.valueOf(element.getAttribute("number")), element.getTextContent());
            }
        }
        catch (Exception e) {
            return null;
        }
        return langCodeMap;
    }
}

