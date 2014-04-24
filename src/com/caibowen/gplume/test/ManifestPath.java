package com.caibowen.gplume.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * specify manifest file path
 * file will be read using classLoader.getResourceAsStream
 * @see JunitPal
 * 
 * @author BowenCai
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ManifestPath {
	String value();
}
