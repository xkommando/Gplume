package com.caibowen.gplume.core;


/**
 * BeanFactoryAware class will retain a reference to the beanfactory that created it
 * 
 * @see Injector
 * @author BowenCai
 *
 */
public interface IBeanFactoryAware {
	
	public void setBeanFactroy(IBeanFactory factory);
}
