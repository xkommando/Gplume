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
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 
 * This annotation is applied to a annotation, and marks the annotation as being
 * a qualifier nickname. Applying a nickname annotation X to a element Y should
 * be interpreted as having the same meaning as applying all of annotations of X
 * (other than QualifierNickname) to Y.
 * 
 * <p>
 * Thus, you might define a qualifier SocialSecurityNumber as follows:
 * </p>
 * 
 * 
 * <code>
 @Documented
 @TypeQualifierNickname @Pattern("[0-9]{3}-[0-9]{2}-[0-9]{4}") 
 @Retention(RetentionPolicy.RUNTIME)
 public @interface SocialSecurityNumber {
 }
 </code>
 * 
 * 
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
public @interface TypeQualifierNickname {

}
