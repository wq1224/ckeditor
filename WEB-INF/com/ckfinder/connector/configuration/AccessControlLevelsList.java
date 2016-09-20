/*
 * Decompiled with CFR 0_115.
 */
package com.ckfinder.connector.configuration;

import com.ckfinder.connector.utils.AccessControlUtil;
import java.util.ArrayList;
import java.util.Collection;

public class AccessControlLevelsList<E>
extends ArrayList<E> {
    public AccessControlLevelsList(boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
    }

    public AccessControlLevelsList(int capacity, boolean modified) {
        super(capacity);
        if (modified) {
            this.resetACLUtilConfiguration();
        }
    }

    public AccessControlLevelsList(Collection<? extends E> c, boolean modified) {
        super(c);
        if (modified) {
            this.resetACLUtilConfiguration();
        }
    }

    public boolean addItem(E item, boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        return super.add(item);
    }

    public void addItem(int index, E item, boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        super.add(index, item);
    }

    public boolean addAllItems(Collection<? extends E> c, boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        return super.addAll(c);
    }

    public boolean addAllItems(int index, Collection<? extends E> c, boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        return super.addAll(index, c);
    }

    public E removeItem(int index, boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        return super.remove(index);
    }

    public boolean removeItem(E item, boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        return super.remove(item);
    }

    public boolean removeAllItems(Collection<?> c, boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        return super.removeAll(c);
    }

    public void clear(boolean modified) {
        if (modified) {
            this.resetACLUtilConfiguration();
        }
        super.clear();
    }

    private void resetACLUtilConfiguration() {
        AccessControlUtil.getInstance().resetConfiguration();
    }
}

