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

/**
 * Used to describe the relationship between a qualifier T and the set of values
 * S possible on an annotated element.
 * 
 * In particular, an issues should be reported if an ALWAYS or MAYBE value is
 * used where a NEVER value is required, or if a NEVER or MAYBE value is used
 * where an ALWAYS value is required.
 * 
 * 
 */
public enum When {
    /** S is a subset of T */
    ALWAYS,
    /** nothing definitive is known about the relation between S and T */
    UNKNOWN,
    /** S intersection T is non empty and S - T is nonempty */
    MAYBE,
    /** S intersection T is empty */
    NEVER;

}
