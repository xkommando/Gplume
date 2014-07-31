package com.caibowen.gplume.misc.logging;

/**
 * A direct NOP (no operation) implementation of {@link Logger}.
 * 
 * @author Bowen Cai
 * 
 */
public class NOPLogger implements Logger {

	/**
	 * The unique instance of NOPLogger.
	 */
	public static final NOPLogger NOP_LOGGER = new NOPLogger();

	/**
	 * There is no point in creating multiple instances of NOPLOgger, except by
	 * derived classes, hence the protected access for the constructor.
	 */
	protected NOPLogger() {
	}

	/**
	 * Always returns the string value "NOP".
	 */
	public String name() {
		return "NOP";
	}

	/**
	 * Always returns false.
	 * 
	 * @return always false
	 */
	final public boolean isTraceEnabled() {
		return false;
	}

	/** A NOP implementation. */
	final public void trace(String msg) {
		// NOP
	}

	/** A NOP implementation. */
	public final void trace(String format, Object... argArray) {
		// NOP
	}

	/** A NOP implementation. */
	final public void trace(String msg, Throwable t, Object... argArray) {
		// NOP
	}

	/**
	 * Always returns false.
	 * 
	 * @return always false
	 */
	final public boolean isDebugEnabled() {
		return false;
	}

	/** A NOP implementation. */
	final public void debug(String msg) {
		// NOP
	}

	/** A NOP implementation. */
	public final void debug(String format, Object... argArray) {
		// NOP
	}

	/** A NOP implementation. */
	@Override
	final public void debug(String msg, Throwable t, Object... argArray) {
		// NOP
	}

	/**
	 * Always returns false.
	 * 
	 * @return always false
	 */
	final public boolean isInfoEnabled() {
		// NOP
		return false;
	}

	/** A NOP implementation. */
	final public void info(String msg) {
		// NOP
	}

	/** A NOP implementation. */
	public final void info(String format, Object... argArray) {
		// NOP
	}

	/** A NOP implementation. */
	final public void info(String msg, Throwable t, Object... argArray) {
		// NOP
	}

	/**
	 * Always returns false.
	 * 
	 * @return always false
	 */
	final public boolean isWarnEnabled() {
		return false;
	}

	/** A NOP implementation. */
	final public void warn(String msg) {
		// NOP
	}

	/** A NOP implementation. */
	public final void warn(String format, Object... argArray) {
		// NOP
	}

	/** A NOP implementation. */
	final public void warn(String msg, Throwable t, Object... argArray) {
		// NOP
	}

	/** A NOP implementation. */
	final public boolean isErrorEnabled() {
		return false;
	}

	/** A NOP implementation. */
	final public void error(String msg) {
		// NOP
	}

	/** A NOP implementation. */
	public final void error(String format, Object... argArray) {
		// NOP
	}

	/** A NOP implementation. */
	final public void error(String msg, Throwable t, Object... argArray) {
		// NOP
	}

	@Override
	public boolean isFatalEnabled() {
		return false;
	}

	/** A NOP implementation. */
	@Override
	public void fatal(String msg, Object... args) {
		// NOP
	}

	/** A NOP implementation. */
	@Override
	public void fatal(String msg, Throwable ex, Object... args) {
		// NOP
	}

}
