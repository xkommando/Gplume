package com.caibowen.gplume.context;

import java.io.InputStream;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.caibowen.gplume.misc.Str;


/**
 * 
 * @author bowen.cbw
 *
 */
public class InputStreamProviderProxy implements InputStreamProvider {

	public static final InputStreamProvider FILE_PROVIDER = new FileInputStreamProvider();
	public static final InputStreamProvider URL_PROVIDER = new URLInputStreamProvider();

	public InputStreamProvider servletContextProvider;
	public InputStreamProvider classPathProvider;
	public InputStreamProvider defaultProvider;
	
	@Override
	public InputStream getStream(String path) {
		InputStreamProvider providerToUse = null;
		if (Str.Utils.notBlank(path)) {
			// build beans
			if (path.startsWith("classpath:")) {
				providerToUse = classPathProvider;
				path = path.substring(10, path.length());
				
			} else if (path.startsWith("file:")) {
				providerToUse = FILE_PROVIDER;
				path = path.substring(5, path.length());
				
			} else if (path.startsWith("url:")) {
				providerToUse = URL_PROVIDER;
				path = path.substring(4, path.length());
				
			} else {
				providerToUse = defaultProvider;
			}
		}
		return providerToUse.getStream(path);
	}

	@Override
	public String getContextPath() {
		throw new NotImplementedException();
	}

}
