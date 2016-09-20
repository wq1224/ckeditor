/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  org.apache.commons.fileupload.FileItem
 *  org.jboss.vfs.VFS
 *  org.jboss.vfs.VFSUtils
 *  org.jboss.vfs.VirtualFile
 */
package com.ckfinder.connector.utils;

import com.ckfinder.connector.ServletContextFactory;
import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.ResourceType;
import com.ckfinder.connector.errors.ConnectorException;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.ckfinder.connector.utils.PathUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import org.apache.commons.fileupload.FileItem;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

public class FileUtils {
    private static final int MAX_BUFFER_SIZE = 1024;
    private static final Map<String, String> UTF8_LOWER_ACCENTS = new HashMap<String, String>();
    private static final Map<String, String> UTF8_UPPER_ACCENTS = new HashMap<String, String>();
    private static final Map<String, String> encodingMap;
    private static final Pattern drivePatt;
    private static final Pattern invalidFileNamePatt;
    private static final Logger fileUtilsLogger;
    private static final String WEB_INF_FOLDER_NAME = "/WEB-INF/";
    private static final String ROOT_FOLDER_NAME = "/ROOT";
    private static final String DOMAINS_FOLDER_NAME = "/domains/";
    private static final String DOCROOT_FOLDER_NAME = "/docroot";
    private static final String CKFINDER_FOLDER_NAME = "/ckfinder";
    private static String fuClassPath;

    public static List<String> findChildrensList(File dir, boolean searchDirs) {
        ArrayList<String> files = new ArrayList<String>();
        for (String subFiles : dir.list()) {
            File file = new File(dir + "/" + subFiles);
            if ((!searchDirs || !file.isDirectory()) && (searchDirs || file.isDirectory())) continue;
            files.add(file.getName());
        }
        return files;
    }

    public static String getFileExtension(String fileName, boolean shortExtensionMode) {
        if (shortExtensionMode) {
            return FileUtils.getFileExtension(fileName);
        }
        if (fileName == null || fileName.indexOf(".") == -1 || fileName.indexOf(".") == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(fileName.indexOf(".") + 1);
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1 || fileName.lastIndexOf(".") == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String getFileNameWithoutExtension(String fileName, boolean shortExtensionMode) {
        if (shortExtensionMode) {
            return FileUtils.getFileNameWithoutExtension(fileName);
        }
        if (fileName == null || fileName.indexOf(".") == -1) {
            return null;
        }
        return fileName.substring(0, fileName.indexOf("."));
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return null;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static void printFileContentToResponse(File file, OutputStream out) throws IOException {
        FileInputStream in = null;
        if (file.length() == 0) {
            return;
        }
        try {
            int numRead;
            in = new FileInputStream(file);
            byte[] buf = file.length() < 1024 ? new byte[(int)file.length()] : new byte[1024];
            while ((numRead = in.read(buf)) != -1) {
                out.write(buf, 0, numRead);
            }
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e) {
                fileUtilsLogger.log(Level.SEVERE, "Error when closing stream.", e);
            }
        }
    }

    public static boolean copyFromSourceToDestFile(File sourceFile, File destFile, boolean move, IConfiguration conf) throws IOException {
        int len;
        FileUtils.createPath(destFile, true);
        FileInputStream in = new FileInputStream(sourceFile);
        FileOutputStream out = new FileOutputStream(destFile);
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        if (move) {
            sourceFile.delete();
        }
        return true;
    }

    public static String getFullPath(String path, boolean isAbsolute, boolean shouldExist) throws ConnectorException {
        if (path != null && !path.equals("")) {
            if (isAbsolute) {
                String temporary;
                if (path.startsWith("/") && FileUtils.isStartsWithPattern(drivePatt, temporary = PathUtils.removeSlashFromBeginning(path))) {
                    path = temporary;
                }
                return FileUtils.checkAndReturnPath(shouldExist, path);
            }
            ServletContext sc = ServletContextFactory.getServletContext();
            String tempPath = PathUtils.addSlashToEnd(PathUtils.addSlashToBeginning(path));
            try {
                URL url = sc.getResource(tempPath);
                if (url != null && url.getProtocol() != null && url.getProtocol().equalsIgnoreCase("jndi")) {
                    String result = sc.getRealPath(tempPath.replace(sc.getContextPath(), ""));
                    if (result != null) {
                        return result;
                    }
                    result = FileUtils.getClassPath();
                    if (tempPath.indexOf(sc.getContextPath() + "/") >= 0 && result.indexOf(sc.getContextPath() + "/") >= 0) {
                        result = result.substring(0, result.indexOf(sc.getContextPath()));
                        result = result + tempPath;
                    } else if (result.indexOf(sc.getContextPath() + "/") >= 0) {
                        result = result.substring(0, result.indexOf(sc.getContextPath()) + sc.getContextPath().length());
                        result = result + tempPath;
                    }
                    result = FileUtils.checkAndReturnPath(shouldExist, result);
                    if (result != null) {
                        return result;
                    }
                    if (result == null) {
                        url = null;
                    }
                }
                if (path.startsWith("/") || FileUtils.isStartsWithPattern(drivePatt, path)) {
                    String absolutePath = FileUtils.checkAndReturnPath(shouldExist, path);
                    if (absolutePath != null && !absolutePath.equals("")) {
                        return absolutePath;
                    }
                    return sc.getRealPath(path.replace(sc.getContextPath(), ""));
                }
            }
            catch (IOException ioex) {
                throw new ConnectorException(ioex);
            }
        }
        return null;
    }

    private static String checkAndReturnPath(boolean shouldExist, String path) {
        if (!shouldExist) {
            return path;
        }
        if (FileUtils.isFileExist(path)) {
            return path;
        }
        return null;
    }

    private static boolean isFileExist(String path) {
        File f = new File(path);
        return f.exists();
    }

    public static String calculatePathFromBaseUrl(String path) throws ConnectorException {
        if (path != null && !path.equals("")) {
            String finalPath;
            ServletContext sc = ServletContextFactory.getServletContext();
            String tempPath = PathUtils.addSlashToBeginning(path);
            if (tempPath.startsWith(sc.getContextPath() + "/")) {
                finalPath = sc.getRealPath(tempPath.replace(sc.getContextPath(), ""));
                if (finalPath != null) {
                    return finalPath;
                }
                finalPath = sc.getRealPath("/ckfinder");
                if (finalPath != null) {
                    finalPath = PathUtils.escape(finalPath);
                    return finalPath.substring(0, finalPath.lastIndexOf("/ckfinder")) + tempPath.replace(sc.getContextPath(), "");
                }
                finalPath = FileUtils.getClassPath();
                if (finalPath.indexOf(sc.getContextPath()) >= 0) {
                    finalPath = finalPath.substring(0, finalPath.indexOf(sc.getContextPath()));
                    finalPath = finalPath + tempPath;
                    return finalPath;
                }
                finalPath = null;
            } else {
                finalPath = FileUtils.getClassPath();
                String tcPath = FileUtils.getTomcatRootPath(sc, finalPath);
                String gfPath = FileUtils.getGlassFishRootPath(sc, finalPath);
                if (!tcPath.equals("")) {
                    tempPath = FileUtils.filterRelativePathChars(tempPath);
                    finalPath = tcPath + tempPath;
                } else if (!gfPath.equals("")) {
                    tempPath = FileUtils.filterRelativePathChars(tempPath);
                    finalPath = gfPath + tempPath;
                } else {
                    String realPath = sc.getRealPath(tempPath);
                    if (realPath != null) {
                        return realPath;
                    }
                    if (finalPath.indexOf(sc.getContextPath() + "/") >= 0) {
                        finalPath = finalPath.substring(0, finalPath.indexOf(sc.getContextPath()) + sc.getContextPath().length());
                        tempPath = FileUtils.filterRelativePathChars(tempPath);
                        finalPath = finalPath + tempPath;
                    } else {
                        finalPath = null;
                    }
                }
            }
            return finalPath;
        }
        return null;
    }

    private static String getTomcatRootPath(ServletContext sc, String path) {
        String finalPath = "";
        int index = path.indexOf(sc.getContextPath() + "/WEB-INF/");
        if (index >= 0) {
            path = path.substring(0, index);
            if (FileUtils.isFileExist(path = path + "/ROOT")) {
                finalPath = path;
            }
        }
        return finalPath;
    }

    private static String getGlassFishRootPath(ServletContext sc, String path) {
        String finalPath = "";
        int index = (path = path.toLowerCase()).indexOf("/domains/");
        if (index >= 0) {
            String key = (path = PathUtils.addSlashToEnd(path)).substring(index + "/domains/".length());
            if (!key.equals("")) {
                if (key.indexOf("/") > 0) {
                    key = key.substring(0, key.indexOf("/"));
                }
                path = path.substring(0, path.indexOf(key) + key.length()) + "/docroot";
            }
            if (FileUtils.isFileExist(path)) {
                finalPath = path;
            }
        }
        return finalPath;
    }

    private static String filterRelativePathChars(String path) {
        StringBuffer s = new StringBuffer(path);
        int index = s.indexOf("..");
        if (index >= 0) {
            s = s.delete(index, index + 2);
        }
        return s.toString();
    }

    private static boolean isStartsWithPattern(Pattern pattern, String path) {
        Matcher m = pattern.matcher(path);
        if (m.find()) {
            return m.start() == 0;
        }
        return false;
    }

    private static String getClassPath() throws ConnectorException {
        if (fuClassPath == null || fuClassPath.equals("")) {
            String temporary;
            URL url = FileUtils.class.getResource("FileUtils.class");
            String finalPath = null;
            String filePathPrefix = "file:/";
            if ("vfs".equalsIgnoreCase(url.getProtocol())) {
                try {
                    VirtualFile vFile = VFS.getChild((String)url.getPath());
                    finalPath = VFSUtils.getPhysicalURI((VirtualFile)vFile).getPath();
                }
                catch (IOException ioex) {
                    throw new ConnectorException(ioex);
                }
            }
            try {
                finalPath = url.toURI().getSchemeSpecificPart();
            }
            catch (URISyntaxException ueex) {
                throw new ConnectorException(ueex);
            }
            if (finalPath != null && finalPath.startsWith(filePathPrefix)) {
                finalPath = finalPath.substring(filePathPrefix.length());
            }
            if (finalPath != null && finalPath.startsWith("/") && FileUtils.isStartsWithPattern(drivePatt, temporary = PathUtils.removeSlashFromBeginning(finalPath))) {
                finalPath = temporary;
            }
            fuClassPath = finalPath;
        }
        return fuClassPath;
    }

    public static String parseLastModifDate(File file) {
        Date date = new Date(file.lastModified());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        return dateFormat.format(date);
    }

    public static boolean checkIfDirIsHidden(String dirName, IConfiguration conf) {
        if (dirName == null || dirName.equals("")) {
            return false;
        }
        String dir = PathUtils.removeSlashFromEnd(PathUtils.escape(dirName));
        Scanner sc = new Scanner(dir).useDelimiter("/");
        while (sc.hasNext()) {
            boolean check = Pattern.compile(FileUtils.getHiddenFileOrFolderRegex(conf.getHiddenFolders())).matcher(sc.next()).matches();
            if (!check) continue;
            return true;
        }
        return false;
    }

    public static boolean checkIfFileIsHidden(String fileName, IConfiguration conf) {
        return Pattern.compile(FileUtils.getHiddenFileOrFolderRegex(conf.getHiddenFiles())).matcher(fileName).matches();
    }

    private static String getHiddenFileOrFolderRegex(List<String> hiddenList) {
        StringBuilder sb = new StringBuilder("(");
        Iterator<String> iterator = hiddenList.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            if (sb.length() > 3) {
                sb.append("|");
            }
            item = item.replaceAll("\\.", "\\\\.");
            item = item.replaceAll("\\*", ".+");
            item = item.replaceAll("\\?", ".");
            sb.append("(");
            sb.append(item);
            sb.append(")");
        }
        sb.append(")+");
        return sb.toString();
    }

    public static boolean delete(File file) {
        if (file.isDirectory()) {
            for (String item : file.list()) {
                File subFile = new File(file.getAbsolutePath() + File.separator + item);
                if (FileUtils.delete(subFile)) continue;
                return false;
            }
        }
        return file.delete();
    }

    public static boolean checkFileName(String fileName) {
        return fileName != null && !fileName.equals("") && fileName.charAt(fileName.length() - 1) != '.' && !fileName.contains("..") && !FileUtils.checkFolderNamePattern(fileName);
    }

    private static boolean checkFolderNamePattern(String fileName) {
        return invalidFileNamePatt.matcher(fileName).find();
    }

    public static int checkFileExtension(String fileName, ResourceType type) {
        if (type == null || fileName == null) {
            return 1;
        }
        if (fileName.indexOf(46) == -1) {
            return 0;
        }
        return FileUtils.checkSingleExtension(FileUtils.getFileExtension(fileName), type) ? 0 : 1;
    }

    private static boolean checkSingleExtension(String fileExt, ResourceType type) {
        Scanner scanner = new Scanner(type.getDeniedExtensions()).useDelimiter(",");
        while (scanner.hasNext()) {
            if (!scanner.next().equalsIgnoreCase(fileExt)) continue;
            return false;
        }
        scanner = new Scanner(type.getAllowedExtensions()).useDelimiter(",");
        if (!scanner.hasNext()) {
            return true;
        }
        while (scanner.hasNext()) {
            if (!scanner.next().equalsIgnoreCase(fileExt)) continue;
            return true;
        }
        return false;
    }

    public static String convertFromUriEncoding(String fileName, IConfiguration configuration) {
        try {
            return new String(fileName.getBytes(configuration.getUriEncoding()), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return fileName;
        }
    }

    public static String convertToASCII(String fileName) {
        String newFileName = fileName;
        FileUtils.fillLowerAccents();
        FileUtils.fillUpperAccents();
        for (String s2 : UTF8_LOWER_ACCENTS.keySet()) {
            newFileName = newFileName.replace(s2, UTF8_LOWER_ACCENTS.get(s2));
        }
        for (String s2 : UTF8_UPPER_ACCENTS.keySet()) {
            newFileName = newFileName.replace(s2, UTF8_UPPER_ACCENTS.get(s2));
        }
        return newFileName;
    }

    public static void createPath(File file, boolean asFile) throws IOException {
        String path = file.getAbsolutePath();
        String dirPath = asFile ? path.substring(0, path.lastIndexOf(File.separator)) : path;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (asFile) {
            file.createNewFile();
        }
    }

    public static boolean checkFileSize(ResourceType type, long fileSize) {
        Long maxSize = type.getMaxSize();
        return maxSize == null || maxSize > fileSize;
    }

    public static boolean checkIfFileIsHtmlFile(String file, IConfiguration configuration) {
        return configuration.getHTMLExtensions().contains(FileUtils.getFileExtension(file).toLowerCase());
    }

    public static boolean detectHtml(FileItem item) throws IOException {
        byte[] buff = new byte[1024];
        InputStream is = null;
        try {
            String[] tags;
            is = item.getInputStream();
            is.read(buff, 0, 1024);
            String content = new String(buff);
            content = content.toLowerCase().trim();
            if (Pattern.compile("<!DOCTYPE\\W+X?HTML.+", 42).matcher(content).matches()) {
                boolean bl = true;
                return bl;
            }
            for (String tag : tags = new String[]{"<body", "<head", "<html", "<img", "<pre", "<script", "<table", "<title"}) {
                if (content.indexOf(tag) == -1) continue;
                boolean bl = true;
                return bl;
            }
            if (Pattern.compile("type\\s*=\\s*['\"]?\\s*(?:\\w*/)?(?:ecma|java)", 42).matcher(content).find()) {
                boolean bl = true;
                return bl;
            }
            if (Pattern.compile("(?:href|src|data)\\s*=\\s*['\"]?\\s*(?:ecma|java)script:", 42).matcher(content).find()) {
                boolean bl = true;
                return bl;
            }
            if (Pattern.compile("url\\s*\\(\\s*['\"]?\\s*(?:ecma|java)script:", 42).matcher(content).find()) {
                boolean bl = true;
                return bl;
            }
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
        return false;
    }

    public static Boolean hasChildren(String dirPath, File dir, IConfiguration configuration, String resourceType, String currentUserRole) {
        FileFilter fileFilter = new FileFilter(){

            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        File[] subDirsList = dir.listFiles(fileFilter);
        if (subDirsList != null) {
            for (File subDirsList1 : subDirsList) {
                String subDirName = subDirsList1.getName();
                if (FileUtils.checkIfDirIsHidden(subDirName, configuration) || !AccessControlUtil.getInstance().checkFolderACL(resourceType, dirPath + subDirName, currentUserRole, 1)) continue;
                return true;
            }
        }
        return false;
    }

    public static String renameFileWithBadExt(ResourceType type, String fileName) {
        if (type == null || fileName == null) {
            return null;
        }
        if (fileName.indexOf(46) == -1) {
            return fileName;
        }
        StringTokenizer tokens = new StringTokenizer(fileName, ".");
        String cfileName = tokens.nextToken();
        while (tokens.hasMoreTokens()) {
            String currToken = tokens.nextToken();
            if (tokens.hasMoreElements()) {
                cfileName = cfileName.concat(FileUtils.checkSingleExtension(currToken, type) ? "." : "_");
                cfileName = cfileName.concat(currToken);
                continue;
            }
            cfileName = cfileName.concat(".".concat(currToken));
        }
        return cfileName;
    }

    public static String encodeURIComponent(String fileName) throws UnsupportedEncodingException {
        String fileNameHelper = URLEncoder.encode(fileName, "utf-8");
        for (Map.Entry<String, String> entry : encodingMap.entrySet()) {
            fileNameHelper = fileNameHelper.replaceAll(entry.getKey(), entry.getValue());
        }
        return fileNameHelper;
    }

    public static boolean checkFolderName(String folderName, IConfiguration configuration) {
        return (!configuration.isDisallowUnsafeCharacters() || !folderName.contains(".") && !folderName.contains(";")) && !FileUtils.checkFolderNamePattern(folderName);
    }

    public static boolean checkFileName(String fileName, IConfiguration configuration) {
        return (!configuration.isDisallowUnsafeCharacters() || !fileName.contains(";")) && FileUtils.checkFileName(fileName);
    }

    public static String backupWithBackSlash(String fileName, String toReplace) {
        return fileName.replaceAll(toReplace, "\\\\" + toReplace);
    }

    private static void fillUpperAccents() {
        if (UTF8_UPPER_ACCENTS.size() == 0) {
            UTF8_UPPER_ACCENTS.put("\u00c0", "A");
            UTF8_UPPER_ACCENTS.put("\u00d4", "O");
            UTF8_UPPER_ACCENTS.put("\u010e", "D");
            UTF8_UPPER_ACCENTS.put("\u1e1e", "F");
            UTF8_UPPER_ACCENTS.put("\u00cb", "E");
            UTF8_UPPER_ACCENTS.put("\u0160", "S");
            UTF8_UPPER_ACCENTS.put("\u01a0", "O");
            UTF8_UPPER_ACCENTS.put("\u0102", "A");
            UTF8_UPPER_ACCENTS.put("\u0158", "R");
            UTF8_UPPER_ACCENTS.put("\u021a", "T");
            UTF8_UPPER_ACCENTS.put("\u0147", "N");
            UTF8_UPPER_ACCENTS.put("\u0100", "A");
            UTF8_UPPER_ACCENTS.put("\u0136", "K");
            UTF8_UPPER_ACCENTS.put("\u015c", "S");
            UTF8_UPPER_ACCENTS.put("\u1ef2", "Y");
            UTF8_UPPER_ACCENTS.put("\u0145", "N");
            UTF8_UPPER_ACCENTS.put("\u0139", "L");
            UTF8_UPPER_ACCENTS.put("\u0126", "H");
            UTF8_UPPER_ACCENTS.put("\u1e56", "P");
            UTF8_UPPER_ACCENTS.put("\u00d3", "O");
            UTF8_UPPER_ACCENTS.put("\u00da", "U");
            UTF8_UPPER_ACCENTS.put("\u011a", "E");
            UTF8_UPPER_ACCENTS.put("\u00c9", "E");
            UTF8_UPPER_ACCENTS.put("\u00c7", "C");
            UTF8_UPPER_ACCENTS.put("\u1e80", "W");
            UTF8_UPPER_ACCENTS.put("\u010a", "C");
            UTF8_UPPER_ACCENTS.put("\u00d5", "O");
            UTF8_UPPER_ACCENTS.put("\u1e60", "S");
            UTF8_UPPER_ACCENTS.put("\u00d8", "O");
            UTF8_UPPER_ACCENTS.put("\u0122", "G");
            UTF8_UPPER_ACCENTS.put("\u0166", "T");
            UTF8_UPPER_ACCENTS.put("\u0218", "S");
            UTF8_UPPER_ACCENTS.put("\u0116", "E");
            UTF8_UPPER_ACCENTS.put("\u0108", "C");
            UTF8_UPPER_ACCENTS.put("\u015a", "S");
            UTF8_UPPER_ACCENTS.put("\u00ce", "I");
            UTF8_UPPER_ACCENTS.put("\u0170", "U");
            UTF8_UPPER_ACCENTS.put("\u0106", "C");
            UTF8_UPPER_ACCENTS.put("\u0118", "E");
            UTF8_UPPER_ACCENTS.put("\u0174", "W");
            UTF8_UPPER_ACCENTS.put("\u1e6a", "T");
            UTF8_UPPER_ACCENTS.put("\u016a", "U");
            UTF8_UPPER_ACCENTS.put("\u010c", "C");
            UTF8_UPPER_ACCENTS.put("\u00d6", "Oe");
            UTF8_UPPER_ACCENTS.put("\u00c8", "E");
            UTF8_UPPER_ACCENTS.put("\u0176", "Y");
            UTF8_UPPER_ACCENTS.put("\u0104", "A");
            UTF8_UPPER_ACCENTS.put("\u0141", "L");
            UTF8_UPPER_ACCENTS.put("\u0172", "U");
            UTF8_UPPER_ACCENTS.put("\u016e", "U");
            UTF8_UPPER_ACCENTS.put("\u015e", "S");
            UTF8_UPPER_ACCENTS.put("\u011e", "G");
            UTF8_UPPER_ACCENTS.put("\u013b", "L");
            UTF8_UPPER_ACCENTS.put("\u0191", "F");
            UTF8_UPPER_ACCENTS.put("\u017d", "Z");
            UTF8_UPPER_ACCENTS.put("\u1e82", "W");
            UTF8_UPPER_ACCENTS.put("\u1e02", "B");
            UTF8_UPPER_ACCENTS.put("\u00c5", "A");
            UTF8_UPPER_ACCENTS.put("\u00cc", "I");
            UTF8_UPPER_ACCENTS.put("\u00cf", "I");
            UTF8_UPPER_ACCENTS.put("\u1e0a", "D");
            UTF8_UPPER_ACCENTS.put("\u0164", "T");
            UTF8_UPPER_ACCENTS.put("\u0156", "R");
            UTF8_UPPER_ACCENTS.put("\u00c4", "Ae");
            UTF8_UPPER_ACCENTS.put("\u00cd", "I");
            UTF8_UPPER_ACCENTS.put("\u0154", "R");
            UTF8_UPPER_ACCENTS.put("\u00ca", "E");
            UTF8_UPPER_ACCENTS.put("\u00dc", "Ue");
            UTF8_UPPER_ACCENTS.put("\u00d2", "O");
            UTF8_UPPER_ACCENTS.put("\u0112", "E");
            UTF8_UPPER_ACCENTS.put("\u00d1", "N");
            UTF8_UPPER_ACCENTS.put("\u0143", "N");
            UTF8_UPPER_ACCENTS.put("\u0124", "H");
            UTF8_UPPER_ACCENTS.put("\u011c", "G");
            UTF8_UPPER_ACCENTS.put("\u0110", "D");
            UTF8_UPPER_ACCENTS.put("\u0134", "J");
            UTF8_UPPER_ACCENTS.put("\u0178", "Y");
            UTF8_UPPER_ACCENTS.put("\u0168", "U");
            UTF8_UPPER_ACCENTS.put("\u016c", "U");
            UTF8_UPPER_ACCENTS.put("\u01af", "U");
            UTF8_UPPER_ACCENTS.put("\u0162", "T");
            UTF8_UPPER_ACCENTS.put("\u00dd", "Y");
            UTF8_UPPER_ACCENTS.put("\u0150", "O");
            UTF8_UPPER_ACCENTS.put("\u0178", "Y");
            UTF8_UPPER_ACCENTS.put("\u0168", "U");
            UTF8_UPPER_ACCENTS.put("\u016c", "U");
            UTF8_UPPER_ACCENTS.put("\u01af", "U");
            UTF8_UPPER_ACCENTS.put("\u0162", "T");
            UTF8_UPPER_ACCENTS.put("\u00dd", "Y");
            UTF8_UPPER_ACCENTS.put("\u0150", "O");
            UTF8_UPPER_ACCENTS.put("\u00c2", "A");
            UTF8_UPPER_ACCENTS.put("\u013d", "L");
            UTF8_UPPER_ACCENTS.put("\u1e84", "W");
            UTF8_UPPER_ACCENTS.put("\u017b", "Z");
            UTF8_UPPER_ACCENTS.put("\u012a", "I");
            UTF8_UPPER_ACCENTS.put("\u00c3", "A");
            UTF8_UPPER_ACCENTS.put("\u0120", "G");
            UTF8_UPPER_ACCENTS.put("\u1e40", "M");
            UTF8_UPPER_ACCENTS.put("\u014c", "O");
            UTF8_UPPER_ACCENTS.put("\u0128", "I");
            UTF8_UPPER_ACCENTS.put("\u00d9", "U");
            UTF8_UPPER_ACCENTS.put("\u012e", "I");
            UTF8_UPPER_ACCENTS.put("\u0179", "Z");
            UTF8_UPPER_ACCENTS.put("\u00c1", "A");
            UTF8_UPPER_ACCENTS.put("\u00db", "U");
            UTF8_UPPER_ACCENTS.put("\u00de", "Th");
            UTF8_UPPER_ACCENTS.put("\u00d0", "Dh");
            UTF8_UPPER_ACCENTS.put("\u00c6", "Ae");
            UTF8_UPPER_ACCENTS.put("\u0114", "E");
        }
    }

    private static void fillLowerAccents() {
        if (UTF8_LOWER_ACCENTS.size() == 0) {
            UTF8_LOWER_ACCENTS.put("\u00e0", "a");
            UTF8_LOWER_ACCENTS.put("\u00f4", "o");
            UTF8_LOWER_ACCENTS.put("\u010f", "d");
            UTF8_LOWER_ACCENTS.put("\u1e1f", "f");
            UTF8_LOWER_ACCENTS.put("\u00eb", "e");
            UTF8_LOWER_ACCENTS.put("\u0161", "s");
            UTF8_LOWER_ACCENTS.put("\u01a1", "o");
            UTF8_LOWER_ACCENTS.put("\u00df", "ss");
            UTF8_LOWER_ACCENTS.put("\u0103", "a");
            UTF8_LOWER_ACCENTS.put("\u0159", "r");
            UTF8_LOWER_ACCENTS.put("\u021b", "t");
            UTF8_LOWER_ACCENTS.put("\u0148", "n");
            UTF8_LOWER_ACCENTS.put("\u0101", "a");
            UTF8_LOWER_ACCENTS.put("\u0137", "k");
            UTF8_LOWER_ACCENTS.put("\u015d", "s");
            UTF8_LOWER_ACCENTS.put("\u1ef3", "y");
            UTF8_LOWER_ACCENTS.put("\u0146", "n");
            UTF8_LOWER_ACCENTS.put("\u013a", "l");
            UTF8_LOWER_ACCENTS.put("\u0127", "h");
            UTF8_LOWER_ACCENTS.put("\u1e57", "p");
            UTF8_LOWER_ACCENTS.put("\u00f3", "o");
            UTF8_LOWER_ACCENTS.put("\u00fa", "u");
            UTF8_LOWER_ACCENTS.put("\u011b", "e");
            UTF8_LOWER_ACCENTS.put("\u00e9", "e");
            UTF8_LOWER_ACCENTS.put("\u00e7", "c");
            UTF8_LOWER_ACCENTS.put("\u1e81", "w");
            UTF8_LOWER_ACCENTS.put("\u010b", "c");
            UTF8_LOWER_ACCENTS.put("\u00f5", "o");
            UTF8_LOWER_ACCENTS.put("\u1e61", "s");
            UTF8_LOWER_ACCENTS.put("\u00f8", "o");
            UTF8_LOWER_ACCENTS.put("\u0123", "g");
            UTF8_LOWER_ACCENTS.put("\u0167", "t");
            UTF8_LOWER_ACCENTS.put("\u0219", "s");
            UTF8_LOWER_ACCENTS.put("\u0117", "e");
            UTF8_LOWER_ACCENTS.put("\u0109", "c");
            UTF8_LOWER_ACCENTS.put("\u015b", "s");
            UTF8_LOWER_ACCENTS.put("\u00ee", "i");
            UTF8_LOWER_ACCENTS.put("\u0171", "u");
            UTF8_LOWER_ACCENTS.put("\u0107", "c");
            UTF8_LOWER_ACCENTS.put("\u0119", "e");
            UTF8_LOWER_ACCENTS.put("\u0175", "w");
            UTF8_LOWER_ACCENTS.put("\u1e6b", "t");
            UTF8_LOWER_ACCENTS.put("\u016b", "u");
            UTF8_LOWER_ACCENTS.put("\u010d", "c");
            UTF8_LOWER_ACCENTS.put("\u00f6", "oe");
            UTF8_LOWER_ACCENTS.put("\u00e8", "e");
            UTF8_LOWER_ACCENTS.put("\u0177", "y");
            UTF8_LOWER_ACCENTS.put("\u0105", "a");
            UTF8_LOWER_ACCENTS.put("\u0142", "l");
            UTF8_LOWER_ACCENTS.put("\u0173", "u");
            UTF8_LOWER_ACCENTS.put("\u016f", "u");
            UTF8_LOWER_ACCENTS.put("\u015f", "s");
            UTF8_LOWER_ACCENTS.put("\u011f", "g");
            UTF8_LOWER_ACCENTS.put("\u013c", "l");
            UTF8_LOWER_ACCENTS.put("\u0192", "f");
            UTF8_LOWER_ACCENTS.put("\u017e", "z");
            UTF8_LOWER_ACCENTS.put("\u1e83", "w");
            UTF8_LOWER_ACCENTS.put("\u1e03", "b");
            UTF8_LOWER_ACCENTS.put("\u00e5", "a");
            UTF8_LOWER_ACCENTS.put("\u00ec", "i");
            UTF8_LOWER_ACCENTS.put("\u00ef", "i");
            UTF8_LOWER_ACCENTS.put("\u1e0b", "d");
            UTF8_LOWER_ACCENTS.put("\u0165", "t");
            UTF8_LOWER_ACCENTS.put("\u0157", "r");
            UTF8_LOWER_ACCENTS.put("\u00e4", "ae");
            UTF8_LOWER_ACCENTS.put("\u00ed", "i");
            UTF8_LOWER_ACCENTS.put("\u0155", "r");
            UTF8_LOWER_ACCENTS.put("\u00ea", "e");
            UTF8_LOWER_ACCENTS.put("\u00fc", "ue");
            UTF8_LOWER_ACCENTS.put("\u00f2", "o");
            UTF8_LOWER_ACCENTS.put("\u0113", "e");
            UTF8_LOWER_ACCENTS.put("\u00f1", "n");
            UTF8_LOWER_ACCENTS.put("\u0144", "n");
            UTF8_LOWER_ACCENTS.put("\u0125", "h");
            UTF8_LOWER_ACCENTS.put("\u011d", "g");
            UTF8_LOWER_ACCENTS.put("\u0111", "d");
            UTF8_LOWER_ACCENTS.put("\u0135", "j");
            UTF8_LOWER_ACCENTS.put("\u00ff", "y");
            UTF8_LOWER_ACCENTS.put("\u0169", "u");
            UTF8_LOWER_ACCENTS.put("\u016d", "u");
            UTF8_LOWER_ACCENTS.put("\u01b0", "u");
            UTF8_LOWER_ACCENTS.put("\u0163", "t");
            UTF8_LOWER_ACCENTS.put("\u00fd", "y");
            UTF8_LOWER_ACCENTS.put("\u0151", "o");
            UTF8_LOWER_ACCENTS.put("\u00e2", "a");
            UTF8_LOWER_ACCENTS.put("\u013e", "l");
            UTF8_LOWER_ACCENTS.put("\u1e85", "w");
            UTF8_LOWER_ACCENTS.put("\u017c", "z");
            UTF8_LOWER_ACCENTS.put("\u012b", "i");
            UTF8_LOWER_ACCENTS.put("\u00e3", "a");
            UTF8_LOWER_ACCENTS.put("\u0121", "g");
            UTF8_LOWER_ACCENTS.put("\u1e41", "m");
            UTF8_LOWER_ACCENTS.put("\u014d", "o");
            UTF8_LOWER_ACCENTS.put("\u0129", "i");
            UTF8_LOWER_ACCENTS.put("\u00f9", "u");
            UTF8_LOWER_ACCENTS.put("\u012f", "i");
            UTF8_LOWER_ACCENTS.put("\u017a", "z");
            UTF8_LOWER_ACCENTS.put("\u00e1", "a");
            UTF8_LOWER_ACCENTS.put("\u00fb", "u");
            UTF8_LOWER_ACCENTS.put("\u00fe", "th");
            UTF8_LOWER_ACCENTS.put("\u00f0", "dh");
            UTF8_LOWER_ACCENTS.put("\u00e6", "ae");
            UTF8_LOWER_ACCENTS.put("\u00b5", "u");
            UTF8_LOWER_ACCENTS.put("\u0115", "e");
        }
    }

    static {
        drivePatt = Pattern.compile("^[a-zA-Z]{1}:[/\\\\]");
        invalidFileNamePatt = Pattern.compile("\\p{Cntrl}|[/\\\\\\:\\*\\?\"\\<\\>\\|]");
        fileUtilsLogger = Logger.getLogger(FileUtils.class.getName());
        HashMap<String, String> mapHelper = new HashMap<String, String>();
        mapHelper.put("%21", "!");
        mapHelper.put("%27", "'");
        mapHelper.put("%28", "(");
        mapHelper.put("%29", ")");
        mapHelper.put("%7E", "~");
        mapHelper.put("[+]", "%20");
        encodingMap = Collections.unmodifiableMap(mapHelper);
    }

}

