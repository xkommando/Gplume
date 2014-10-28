package com.caibowen.gplume.annotation;

import java.lang.annotation.*;

/**
 * similar to CPP function qualifier 'const'
 *
 * @author BowenCai
 * @since 28-10-2014.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstMethod {
}
