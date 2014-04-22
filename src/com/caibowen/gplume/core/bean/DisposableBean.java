
package com.caibowen.gplume.core.bean;


/**
 * similar to Spring DisposableBean
 * @author BowenCai
 *
 */
public interface DisposableBean {

	void destroy() throws Exception;
}
