package com.caibowen.gplume.annotation;

import java.lang.annotation.*;

/**
 * similar to CPP qualifier 'const'
 *
 * @author BowenCai
 * @since 28-10-2014.
 */
@Documented
@Target({ElementType.METHOD,
            ElementType.PARAMETER,
            ElementType.FIELD,
            ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface Const {
}
