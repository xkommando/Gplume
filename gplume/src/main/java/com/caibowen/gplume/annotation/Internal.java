package com.caibowen.gplume.annotation;

import java.lang.annotation.*;

/**
 *
 * similar to C# class qualifier 'internal'
 *
 * @author BowenCai
 * @since 28-10-2014.
 */
@Documented
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Internal {
    String value() default "internal use only";
}
