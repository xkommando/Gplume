
package com.caibowen.gplume.jdbc.transaction;

import java.io.Serializable;


/**
 * @author BowenCai
 * @since 08.05.2003
 */
public class TransactionConfig implements Serializable {

    public static final int DEFAULT_ISOLATION = -1;

    public static final int DEFAULT_TIMEOUT = -1;


	private int isolationLevel = DEFAULT_ISOLATION;

	private boolean readOnly = false;
    private boolean resetReadOnly = true;

    private int timeout = DEFAULT_TIMEOUT;


    public static final TransactionConfig DEFAULT
            = new TransactionConfig(DEFAULT_ISOLATION, DEFAULT_TIMEOUT, false, true);

	public TransactionConfig() {
	}

    public TransactionConfig(int isolationLevel, int timeout, boolean readOnly, boolean resetReadonly) {
        this.isolationLevel = isolationLevel;
        this.timeout = timeout;
        this.readOnly = readOnly;
        this.resetReadOnly = resetReadonly;
    }


    public int getIsolationLevel() {
        return isolationLevel;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isResetReadOnly() {
        return resetReadOnly;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionConfig)) return false;

        TransactionConfig that = (TransactionConfig) o;

        if (isolationLevel != that.isolationLevel) return false;
        if (readOnly != that.readOnly) return false;
        return timeout == that.timeout;

    }

    @Override
    public int hashCode() {
        int result = isolationLevel;
        result = 31 * result + timeout;
        result = 31 * result + (readOnly ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TransactionConfig{" +
                "isolationLevel=" + isolationLevel +
                ", timeout=" + timeout +
                ", readOnly=" + readOnly +
                '}';
    }
}
