package com.caibowen.gplume.web.builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IpathValResolver {

	public abstract Object resolveAndCast(String path, String name);

	public abstract String parseArg(String uri, String name);

}