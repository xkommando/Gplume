package com.caibowen.gplume.test.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.caibowen.gplume.context.AppContext;
import com.caibowen.gplume.context.ClassLoaderInputStreamProvider;
import com.caibowen.gplume.context.ContextBooter;
import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.test.ManifestPath;


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
		booter.setProvider(new ClassLoaderInputStreamProvider(JunitPal.class.getClassLoader()));
		booter.boot();
	}
}
