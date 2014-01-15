package com.caibowen.gplume.except;


/**
 * Two handlers have the same value for @Handle(httpAction)
 * @author BowenCai
 *
 */
public class DuplicatedActionNameException extends Exception {

	private static final long serialVersionUID = 1820317540554280383L;

	public DuplicatedActionNameException() {}
	public DuplicatedActionNameException(String msg) {
		super(msg);
	}
	
}
