package com.caibowen.gplume.core;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * @author BowenCai
 * @since 21/11/2014.
 */
public interface Injector {
	/**
	 *
	 * for non-public filed without public setter throws IllegalAccessException
	 * else try
	 * 1. @Named("id") 					-> get by ${id}
	 * 2. @Named() no value set 		-> get by field id
	 * 3. @Inject 						-> get by field class
	 * 4. @Inject 						-> get by field id
	 *
	 * still not found -> failed !
	 *
	 * @param object
	 * @throws Exception
	 */
	void inject(@Nonnull Object object);


	void injectMediate(@Nonnull Object object);

}
