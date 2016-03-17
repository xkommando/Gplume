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
package com.caibowen.gplume.context;

import com.caibowen.gplume.context.bean.BeanVisitor;
import com.caibowen.gplume.core.Injector;
import com.caibowen.gplume.resource.InputStreamCallback;
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
public class XMLAssembler extends XMLAssemblerBase
								implements Serializable {
	
	private static final long serialVersionUID = 1895612360389006713L;

	private static final Logger LOG = LoggerFactory.getLogger(XMLAssembler.class);

	public XMLAssembler() {}
	public XMLAssembler(IBeanBuilder builder) {
		super(builder);
		builder.setAssembler(this);
	}

	public void assemble(@Nonnull final InputSource in) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setValidating(false);
		factory.setCoalescing(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		doc.getDocumentElement().normalize();
		super.doAssemble(doc);
	}
	
	@Override
	public void assemble(@Nonnull final InputStream in) throws Exception {
		assemble(new InputSource(in));
	}
	
	@Override
	public void	assemble(@Nonnull final File file) throws Exception{
		assemble(new InputSource(file.toURI().toASCIIString()));
	}

	@Override
	public void assemble(@Nonnull final String path) throws Exception {
		beanBuilder.getConfigCenter().withPath(path, new InputStreamCallback() {
			@Override
			public void doInStream(InputStream stream) throws Exception {
				assemble(stream);
			}
		});
	}

	@Nonnull
	@Override
	public ConfigCenter configCenter() {
		return beanBuilder.getConfigCenter();
	}

	/**
	 * @return null if not found or throw exception in failing to create non-singleton bean
	 */
	@Nullable
	@Override
	public <T> T getBean(@Nonnull String id) {

		Pod pod = super.tree.findByPartialId(id, currentNamespace, refNamespace);
		if (pod == null)
			return null;
		if (pod.isSingleton()) {
			Object bn = pod.instance;
			if (bn != null)
				return (T)bn;
			Pod p = super.tree.removeByPartialId(id, currentNamespace, refNamespace);
			try {
				p.destroy();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return null;
		} else {
			try {
				return (T) beanBuilder.buildBean(pod.description, pod.beanId);
			} catch (Exception e) {
				throw new BeanAssemblingException(
						"failed building non-singleton bean of id[" + id + "]", e);
			}
		}
	}


    /**
     * will not increase the bean age
     * @return a set of suitable beans
     */
	@Override
	public Set<Object> getBeans(@Nonnull final Class<?> clazz) {
		final Set<Object> set = new HashSet<>(16);
		super.tree.intake(new BeanVisitor<Pod>() {
			@Override
			public void visit(Pod p) {
				Object bn;
				if (null != p
						&& null != (bn = p.instance)
						&& clazz.isInstance(bn))
					set.add(bn);
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
		Pod pod = super.tree.removeByPartialId(id, currentNamespace, refNamespace);
		if (pod == null)
			return;
		try {
			pod.destroy();
		} catch (Exception e) {
			throw new RuntimeException(
					"failed destroy bean of id[" + id + "]", e);
		}

	}

	@Override
	public <T> boolean updateBean(@Nonnull String id, @Nonnull T bean) {
		Pod pod = super.tree.findByPartialId(id, currentNamespace, refNamespace);
		if (pod == null)
			return false;

		if (!pod.isSingleton())
			pod.description = null;

		pod.instance = bean;

        LOG.trace("{} of type {} updated", id, bean.getClass().getName());
		return true;
	}
	
	/**
	 * new bean must not be non-singleton
	 */
	@Override
	public boolean addBean(@Nonnull String id, 
							@Nonnull Object bean) {
		String lid = tree.createFullPath(id, currentNamespace);
		if (contains(lid))
			return false;

		Pod pod = new Pod(lid, null, bean);
		super.tree.put(lid, pod);
        LOG.trace("Bean [{}] of type [{}] added"
				, lid
				, bean.getClass().getName());

		return true;
	}
	
	@Override
	public boolean contains(@Nonnull String id) {
		return tree.findByPartialId(id, currentNamespace, refNamespace) != null;
	}

	@Override
	public boolean isSingleton(@Nonnull String id) {
		Pod pod = super.tree.findByPartialId(id, currentNamespace, refNamespace);
		if (pod != null) {
			return pod.isSingleton();
		}
		else
			throw new NullPointerException("cannot find bean[" + id + "]");
	}

	/**
	 * @param visitor
	 * @throws Exception 
	 */
	@Override
	public void inTake(@Nonnull BeanVisitor visitor) {
		super.tree.intake(visitor);
	}

	private XMLInjector injector = new XMLInjector(this);

	@Override
	public Injector getInjector() {
		return injector;
	}
}
