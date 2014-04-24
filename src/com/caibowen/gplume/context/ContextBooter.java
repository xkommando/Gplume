package com.caibowen.gplume.context;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.caibowen.gplume.misc.Str;
import com.caibowen.gplume.web.i18n.WebI18nService;


/**
 * 
 * @author BowenCai
 *
 */
public class ContextBooter {

	private static final Logger LOG 
					= Logger.getLogger(ContextBooter.class.getName());

	/**
	 * bean name for internationalization
	 */
	private static final String I18N_SERVICE_BEAN_ID = "i18nService";
	

	// optional
	private InputStreamSupport streamSupport = new InputStreamSupport();
	// optinal
	private ClassLoader classLoader = ContextBooter.class.getClassLoader();
	// required
	private String manifestPath;
	
	// require streamProvider
	
	public void boot() {
		
		// set classloader for beanAssembler
		AppContext.beanAssembler.setClassLoader(this.classLoader);

		if (Str.Utils.notBlank(manifestPath)) {
			// build beans
			streamSupport.withPath(manifestPath, new InputStreamCallback() {
				@Override
				public void doInStream(InputStream stream) throws Exception {
					AppContext.beanAssembler.assemble(stream);
				}
			});
		} else {
			LOG.log(Level.WARNING, "no manifest file specified in web.xml, "
					+ "check your web.xml for context-param["
					+ AppContext.MANIFEST + "]");
			return;
		}

		// register listeners
		AppContext.beanAssembler
				.inTake(AppContext.broadcaster.listenerRetreiver);
		
		// load language packages
		WebI18nService service = AppContext.beanAssembler.getBean(I18N_SERVICE_BEAN_ID);
		if (service != null) {
			try {
				service.loadFiles(streamSupport.getStreamProvider());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			LOG.info(I18N_SERVICE_BEAN_ID + " ready to roll!");
		} else {
			LOG.warning("cannot find " + I18N_SERVICE_BEAN_ID);
		}
	}
	
	/**
	 * @return the manifestPath
	 */
	public String getManifestPath() {
		return manifestPath;
	}

	/**
	 * @param manifestPath the manifestPath to set
	 */
	public void setManifestPath(String manifestPath) {
		this.manifestPath = manifestPath;
	}

	/**
	 * @param provider the provider to set
	 */
	public void setProvider(InputStreamProvider provider) {
		this.streamSupport.setStreamProvider(provider);
	}

	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * @param classLoader the classLoader to set
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
