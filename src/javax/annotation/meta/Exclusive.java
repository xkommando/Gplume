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
package javax.annotation.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be applied to the value() element of an annotation that
 * is annotated as a TypeQualifier.
 * 
 * For example, the following defines a type qualifier such that if you know a
 * value is {@literal @Foo(1)}, then the value cannot be {@literal @Foo(2)} or {{@literal @Foo(3)}.
 * 
 * <code>
 * @TypeQualifier @interface Foo {
 *     @Exclusive int value();
 *     }
 *  </code>
 * 
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Exclusive {

}
