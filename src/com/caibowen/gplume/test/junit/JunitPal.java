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
package com.caibowen.gplume.test.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.context.ClassLoaderInputStreamProvider;
import com.caibowen.gplume.context.ContextBooter;
import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.misc.Str;


/**
 * run with Junit4, will set up context and inject all properties for test instance
 * 
 * app-manifest.xml is specified in ManifestPath
 * 
 * @author BowenCai
 *
 */
public class JunitPal extends BlockJUnit4ClassRunner {

	public JunitPal(Class<?> klass) throws InitializationError {
		super(klass);
	}
	
	@Override
	protected Object createTest() throws Exception {
		Object testObj = super.createTest();
		if (testObj.getClass().isAnnotationPresent(ManifestPath.class)) {
			ManifestPath ann = testObj.getClass().getAnnotation(ManifestPath.class);
			String path = ann.value();
			if (!Str.Utils.notBlank(path)) {
				throw new NullPointerException("empty path");
			}
			prepareTest(testObj, path);
			Injector injector = new Injector();
			injector.setBeanAssembler(AppContext.beanAssembler);
			injector.inject(testObj);
		}
		return testObj;
	}
	
	public void prepareTest(Object obj, String mani) {
		ContextBooter booter = new ContextBooter();
		booter.setManifestPath(mani);
		booter.setStreamProvider(new ClassLoaderInputStreamProvider(JunitPal.class.getClassLoader()));
		booter.boot();
	}
}
