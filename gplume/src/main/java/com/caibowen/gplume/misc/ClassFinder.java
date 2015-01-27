/*******************************************************************************
 * Copyright 2014 Bowen Cai
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.caibowen.gplume.misc;


import javax.annotation.Nullable;
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

	public static List<Class<?>> find(String packageName, ClassLoader classLoader) {
	    String path = packageName.replace('.', File.separatorChar);
	    Enumeration<URL> resources;
		try {
			resources = classLoader.getResources(path);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not get resource from [" + path +"] with class loader[" + classLoader + "]", e);
		}
		ArrayList<Class<?>> classes = new ArrayList<>(128);
		try {
			while (resources.hasMoreElements()) {
				File f = new File(resources.nextElement().getFile());
				if (!f.exists())
					continue;
				List<Class<?>> subClasses;
				subClasses = scanClasses(f, packageName, classLoader);

				if (subClasses != null && subClasses.size() > 0)
					classes.addAll(subClasses);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not load class with class loader[" + classLoader + "]", e);
		}
		return classes;
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package id for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	@Nullable
	private static List<Class<?> > scanClasses(File directory,
												String packageName, 
												ClassLoader classLoader) throws ClassNotFoundException {

	    File[] files = directory.listFiles();
	    if (files == null || files.length == 0)
			return null;
		List<Class<?> > classes = new ArrayList<>(32);
	    for (File file : files) {
	        if (file.isDirectory()) {
				List<Class<?>> ls = scanClasses(file, packageName + "." + file.getName(), classLoader);
				if (ls != null && ls.size() > 0)
		            classes.addAll(ls);
	        } else if (file.getName().endsWith(".class")) {
	        	final int len = file.getName().length() - 6;
	        	String clzzName = packageName + '.' + file.getName().subSequence(0, len);
	            classes.add(classLoader.loadClass(clzzName)); 
	        }
	    }
	    return classes;
	}

}




