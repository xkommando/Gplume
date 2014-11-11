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
package com.caibowen.gplume.common;

import java.lang.ref.WeakReference;


/**
 * wrapper for java.lang.ref.WeakReference
 * provided hashCode() and equals()
 * 
 * @author BowenCai
 *
 * @param <T>
 */
public class WeakRef<T> extends WeakReference<T> {

	/**
	 * wrapper for java.lang.ref.WeakReference
	 * provided hashCode() and equals()
	 * @param referent
	 */
	public WeakRef(T referent) {
		super(referent);
	}
	
	@Override
	public int hashCode() {
		if (get() != null) {
			return get().hashCode();
		} else {
			return super.hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof WeakRef)) {
			return false;
		}
		WeakRef<?> other = (WeakRef<?>) obj;
		if (this.get() != null
				&& other.get() != null) {
			return this.get().equals(other.get());
		} else {
			return false;
		}
	}

}
