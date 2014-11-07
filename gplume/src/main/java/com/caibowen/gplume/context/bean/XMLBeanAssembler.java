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
package com.caibowen.gplume.context.bean;

import com.caibowen.gplume.context.InputStreamCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * @author BowenCai
 * 
 * @version 1.0
 * @since 2013-12-24
 * 
 */
public class XMLBeanAssembler extends XMLBeanAssemblerBase
								implements Serializable {
	
	private static final long serialVersionUID = 1895612360389006713L;

	private static final Logger LOG = LoggerFactory.getLogger(XMLBeanAssembler.class);
	/**
	 * This is a compile flag.
	 * When this flag is enabled,fields that do not have a correspondent setter 
	 * will be set directly, regardless of its qualifier.
	 * 
	 * However, In some environment, e.g., Google App Engine, 
	 * you cannot reflect on private field on some classes 
	 * due to different security policy.
	 * So it is recommanded that this flag is not open.
	 * 
	 */
	public static final boolean	REFLECT_ON_PRIVATE = true;

	public XMLBeanAssembler() {}

	public void assemble(@Nonnull final InputSource in) throws Exception {
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(in);
		doc.getDocumentElement().normalize();
		super.doAssemble(doc);
		LOG.debug("Created {} beans", tree.size());
	}
	
	@Override
	public void assemble(@Nonnull final InputStream in) throws Exception {
		assemble(new InputSource(in));
	}
	
	@Override
	public void	assemble(@Nonnull final File file) throws Exception{
		assemble(new InputSource(file.toURI().toASCIIString()));
	}

	public void assemble(@Nonnull final String path) throws Exception {
		configCenter.withPath(path, new InputStreamCallback() {
            @Override
            public void doInStream(InputStream stream) throws Exception {
                assemble(stream);
            }
        });
	}

	/**
	 * @return null if not found or throw exception in failing to create non-singleton bean
	 */
	@Nullable
	@Override
	public <T> T getBean(@Nonnull String id) {

		String lid = getLocation(id);
		if (lid == null)
			return null;
		Pod pod = super.tree.findPod(lid);

		if (pod.isSingleton()) {
			Object bn = pod.getInstance();
			if (bn != null)
				return (T)bn;
			Pod p = super.tree.remove(lid);
			try {
				p.destroy();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return null;
		} else {
			try {
				return (T) buildBean(pod.getDescription());
			} catch (Exception e) {
				throw new BeanAssemblingException(
						"failed building non-singleton bean of id[" + lid + "]", e);
			}
		}
	}

    @Nonnull
    @Override
    public ConfigCenter configCenter() {
        return configCenter;
    }

    /**
     * will not increase the bean age
     * @param clazz
     * @return
     */
	@Override
	public Set<Object> getBeans(@Nonnull final Class<?> clazz) {
		final Set<Object> set = new HashSet<>(16);
		super.tree.intake(new BeanVisitor() {
			@Override
			public void visit(Object bean) {
				if (clazz.isInstance(bean))
					set.add(bean);
			}
		});
		return set;
	}
	
	@Override
	public boolean contains(@Nonnull Class<?> clazz) {
		Set<Object> beans = getBeans(clazz);
		return beans != null && beans.size() > 0;
	}

	
	@Override
	public void removeBean(@Nonnull String id) {
		String lid = getLocation(id);
		if (lid == null)
			return;
		Pod pod = super.tree.remove(lid);
		try {
			pod.destroy();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed destroy bean of id[" + lid + "]", e);
		}

	}

	@Override
	public <T> void updateBean(@Nonnull String id, @Nonnull T bean) {
		String lid = getLocation(id);
		if (lid == null)
			return;
		Pod pod = super.tree.findPod(lid);
		if (pod == null || pod.getInstance() == null)
			throw new NullPointerException("cannot find bean[" + lid + "]");

		pod.setInstance(bean);

        LOG.debug("{} of type {} updated", lid, bean.getClass().getName());
	}
	
	/**
	 * new bean must not be non-singleton
	 */
	@Override
	public boolean addBean(@Nonnull String id, 
							@Nonnull Object bean) {
		String lid = currentNamespace + XMLTags.NS_DELI + id;
		if (contains(lid))
			return false;

		Pod pod = new Pod(lid, null, bean);
		super.tree.addPod(lid, pod);
        LOG.trace("Bean [{}] of type [{}] added"
				, lid
				, bean.getClass().getName());

		return true;
	}
	
	@Override
	public boolean contains(String id) {
		return getLocation(id) != null;
	}

	@Override
	public boolean isSingletion(String id) {
		String lid = getLocation(id);
		if (lid != null) {
			Pod pod = super.tree.findPod(id);
			return pod.isSingleton();
		}
		else
			throw new NullPointerException("cannot find bean[" + id + "]");
	}

	private String getLocation(String id) {
		if (tree.findPod(id) != null)
			return id;
		String lid = currentNamespace + XMLTags.NS_DELI + id;
		if (tree.findPod(lid) != null)
			return lid;
		if (tree.findPod((lid = referedNamespace + XMLTags.NS_DELI + id)) != null)
			return lid;

		return null;
	}
	
	/**
	 * @param visitor
	 * @throws Exception 
	 */
	@Override
	public void inTake(BeanVisitor visitor) {
		Exception ex = null;
		String id = null;
		super.tree.intake(visitor);
	}

}
