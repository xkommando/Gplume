package com.caibowen.gplume.web.actions;

/**
 * resolve url
 */
public interface IPathValResolver {

	Object resolveAndCast(String path, String name);
	String parseArg(String uri, String name);
    String getArgName();
}