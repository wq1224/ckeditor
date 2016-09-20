/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.utils;

import com.ckfinder.connector.configuration.IConfiguration;
import com.ckfinder.connector.data.AccessControlLevel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AccessControlUtil {
    public static final int CKFINDER_CONNECTOR_ACL_FOLDER_VIEW = 1;
    public static final int CKFINDER_CONNECTOR_ACL_FOLDER_CREATE = 2;
    public static final int CKFINDER_CONNECTOR_ACL_FOLDER_RENAME = 4;
    public static final int CKFINDER_CONNECTOR_ACL_FOLDER_DELETE = 8;
    public static final int CKFINDER_CONNECTOR_ACL_FILE_VIEW = 16;
    public static final int CKFINDER_CONNECTOR_ACL_FILE_UPLOAD = 32;
    public static final int CKFINDER_CONNECTOR_ACL_FILE_RENAME = 64;
    public static final int CKFINDER_CONNECTOR_ACL_FILE_DELETE = 128;
    private List<ACLEntry> aclEntries;
    private IConfiguration configuration;
    private static AccessControlUtil util;

    public static AccessControlUtil getInstance() {
        if (util == null) {
            util = new AccessControlUtil();
        }
        return util;
    }

    private AccessControlUtil() {
    }

    public boolean checkFolderACL(String resourceType, String folder, String currentUserRole, int acl) {
        return (this.checkACLForRole(resourceType, folder, currentUserRole) & acl) == acl;
    }

    public int checkACLForRole(String resourceType, String folder, String currentUserRole) {
        CheckEntry[] ce = new CheckEntry[currentUserRole != null ? 4 : 2];
        ce[0] = new CheckEntry("*", "*");
        ce[1] = new CheckEntry("*", resourceType);
        if (currentUserRole != null) {
            ce[2] = new CheckEntry(currentUserRole, "*");
            ce[3] = new CheckEntry(currentUserRole, resourceType);
        }
        int acl = 0;
        for (CheckEntry checkEntry : ce) {
            List<ACLEntry> aclEntrieForType = this.findACLEntryByRoleAndType(checkEntry.type, checkEntry.role);
            block1 : for (ACLEntry aclEntry : aclEntrieForType) {
                String cuttedPath = folder;
                do {
                    if (cuttedPath.length() > 1 && cuttedPath.lastIndexOf("/") == cuttedPath.length() - 1) {
                        cuttedPath = cuttedPath.substring(0, cuttedPath.length() - 1);
                    }
                    if (aclEntry.folder.equals(cuttedPath)) {
                        acl = this.checkACLForFolder(aclEntry, cuttedPath);
                        continue block1;
                    }
                    if (cuttedPath.length() == 1 || cuttedPath.lastIndexOf("/") <= -1) continue block1;
                    cuttedPath = cuttedPath.substring(0, cuttedPath.lastIndexOf("/") + 1);
                } while (true);
            }
        }
        return acl;
    }

    public void resetConfiguration() {
        this.configuration = null;
        this.aclEntries = null;
    }

    public void loadConfiguration(IConfiguration configuration) {
        if (this.configuration == null || this.aclEntries == null) {
            this.configuration = configuration;
            this.loadACLConfig();
        }
    }

    private void loadACLConfig() {
        this.aclEntries = new ArrayList<ACLEntry>();
        for (AccessControlLevel item : this.configuration.getAccessConrolLevels()) {
            ACLEntry aclEntry = new ACLEntry();
            aclEntry.role = item.getRole();
            aclEntry.type = item.getResourceType();
            aclEntry.folder = item.getFolder();
            aclEntry.fileDelete = item.isFileDelete();
            aclEntry.fileRename = item.isFileRename();
            aclEntry.fileUpload = item.isFileUpload();
            aclEntry.fileView = item.isFileView();
            aclEntry.folderCreate = item.isFolderCreate();
            aclEntry.folderDelete = item.isFolderDelete();
            aclEntry.folderRename = item.isFolderRename();
            aclEntry.folderView = item.isFolderView();
            this.aclEntries.add(aclEntry);
        }
    }

    private int checkACLForFolder(ACLEntry entry, String folder) {
        int acl = 0;
        if (folder.contains(entry.folder) || entry.folder.equals(File.separator)) {
            acl = this.countAclByEntry(acl, entry);
        }
        return acl;
    }

    private int countAclByEntry(int acl, ACLEntry entry) {
        return entry.countACL() ^ acl;
    }

    private List<ACLEntry> findACLEntryByRoleAndType(String type, String role) {
        ArrayList<ACLEntry> res = new ArrayList<ACLEntry>();
        for (ACLEntry item : this.aclEntries) {
            if (!item.role.equals(role) || !item.type.equals(type)) continue;
            res.add(item);
        }
        return res;
    }

    class CheckEntry {
        private String role;
        private String type;

        public CheckEntry(String role, String type) {
            this.role = role;
            this.type = type;
        }
    }

    private static class ACLEntry {
        private String role;
        private String type;
        private String folder;
        private boolean folderView;
        private boolean folderCreate;
        private boolean folderRename;
        private boolean folderDelete;
        private boolean fileView;
        private boolean fileUpload;
        private boolean fileRename;
        private boolean fileDelete;

        private ACLEntry() {
        }

        private int countACL() {
            int acl = 0;
            acl += this.folderView ? 1 : 0;
            acl += this.folderCreate ? 2 : 0;
            acl += this.folderRename ? 4 : 0;
            acl += this.folderDelete ? 8 : 0;
            acl += this.fileView ? 16 : 0;
            acl += this.fileUpload ? 32 : 0;
            acl += this.fileRename ? 64 : 0;
            return acl += this.fileDelete ? 128 : 0;
        }

        public String toString() {
            return this.role + " " + this.type + " " + this.folder;
        }
    }

}

