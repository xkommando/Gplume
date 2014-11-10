package com.caibowen.gplume.jdbc;

import com.caibowen.gplume.annotation.Internal;
import com.caibowen.gplume.jdbc.transaction.TransactionConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author BowenCai
 * @since 27-10-2014.
 */
@Internal
public class ConnectionHolder {

    public Connection currentCon;
    public boolean tnxActive = false;
    public int queryTimeout = TransactionConfig.DEFAULT_TIMEOUT;
//    private int savepointCount = 0;

    private int refCount = 0;

    public ConnectionHolder(Connection currentCon) {
        this.currentCon = currentCon;
    }

    public void addRef() {
        refCount++;
    }

    public void deRef() throws SQLException {
        refCount--;
        if (!tnxActive && refCount <= 0 && currentCon != null) {
            currentCon.close();
            currentCon = null;
        }
    }

    public int countRef() {
        return refCount;
    }

    public void release() throws SQLException {
        if (currentCon != null)
            currentCon.close();
        currentCon = null;
        refCount = 0;
    }

    @Override
    public String toString() {
        return "ConnectionHolder{" +
                "currentCon=" + currentCon +
                ", tnxActive=" + tnxActive +
                ", queryTimeout=" + queryTimeout +
                ", refCount=" + refCount +
                '}';
    }

    /**
     *  note that equals and hashCode only use the currentCon field
     *  since one connection can be held by one holder at a time
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionHolder)) return false;

        ConnectionHolder holder = (ConnectionHolder) o;

        return !(currentCon != null ? !currentCon.equals(holder.currentCon) : holder.currentCon != null);
    }

    @Override
    public int hashCode() {
        return currentCon != null ? currentCon.hashCode() : 0;
    }
}
