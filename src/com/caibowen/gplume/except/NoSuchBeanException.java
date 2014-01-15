package com.caibowen.gplume.except;

public class NoSuchBeanException extends Exception {
	private static final long serialVersionUID = -9028198302683038022L;

	public NoSuchBeanException(){}
	
	public NoSuchBeanException(String msg) {
		super(msg);
	}
}
