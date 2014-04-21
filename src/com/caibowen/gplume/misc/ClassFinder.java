/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.misc;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 
 * @author BowenCai
 *
 */
public final class ClassFinder {

	public static List<Class<?> > find(String packageName, ClassLoader classLoader) {
		
		
	    String path = packageName.replace('.', '/');
	    Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(path);
		} catch (IOException e) {
			throw new IllegalArgumentException("cannot get resource from [" + path +"]", e);
		}
	    ArrayList<Class<?> > classes = new ArrayList<Class<?> >(256);
	    try {

			while (resources.hasMoreElements()) {
				List<Class<?>> subClasses = 
						scanClasses(
								new File(
									resources.nextElement().getFile())
									,packageName
									, classLoader);
				
				if (subClasses != null) {
					classes.addAll(subClasses);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("error loading class", e);
		}
	    return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?> > scanClasses(File directory,
												String packageName, 
												ClassLoader classLoader) throws ClassNotFoundException {

		if (!directory.exists()) {
	        return null;
	    }

		List<Class<?> > classes = new ArrayList<Class<?> >(64);
	    File[] files = directory.listFiles();
	    
	    for (File file : files) {
	        if (file.isDirectory()) {
	            classes.addAll(scanClasses(file, packageName + "." + file.getName(), classLoader));
	        
	        } else if (file.getName().endsWith(".class")) {
	        	final int len = file.getName().length() - 6;
	        	String clzzName = packageName + '.' + file.getName().subSequence(0, len);
	            classes.add(classLoader.loadClass(clzzName)); 
	        }
	    }
	    return classes;
	}

}




