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
/**
 *  Copyright 2003-2009 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.caibowen.gplume.web.cache;

import java.io.Serializable;

/**
 * Generic implementation of a HTTP header. Handles String, Int and Date typed headers.
 * 
 * @author Eric Dalquist
 * @author BowenCai
 * 
 * @param <T> The type of Header value being stored. Must implement {@link Serializable}
 */
public class Header<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = -1372157390574439064L;
	
    private final String name;
    private final T value;

    /**
     * Create a new Header
     * 
     * @param id Name of the header, may not be null
     * @param value Value of the header, may not be null
     */
    public Header(String name, T value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return Name of the header, will never be null
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return Value for the header, will never be null
     */
    public T getValue() {
        return this.value;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Header[id=" + this.name + ", value=" + this.value + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Header<?> other = (Header<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
