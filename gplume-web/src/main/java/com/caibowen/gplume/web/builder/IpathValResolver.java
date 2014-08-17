package com.caibowen.gplume.web.builder;

/**
 * resolve url
 */
public interface IPathValResolver {

	public abstract Object resolveAndCast(String path, String name);

	public abstract String parseArg(String uri, String name);

}