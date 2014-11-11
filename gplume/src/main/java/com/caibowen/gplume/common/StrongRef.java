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
 * 
 * @author BowenCai
 *
 */
public class StrongRef<T> extends WeakReference<T> {

	private T ref;
	public StrongRef(T referent) {
		super(null);
		ref = referent;
	}

    @Override
	public T get() {
        return this.ref;
    }
    
	public void set(T v) {
        this.ref = v;
    }
    
    @Override
	public void clear() {
        this.ref = null;
    }

    @Override
	public boolean isEnqueued() {
    	return false;
    }

    @Override
	public boolean enqueue() {
    	return false;
    }


	@Override
	public int hashCode() {
		return ref.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StrongRef)) {
			return false;
		}
		StrongRef<?> other = (StrongRef<?>) obj;
		return this.ref.equals(other.ref);
	}

}
