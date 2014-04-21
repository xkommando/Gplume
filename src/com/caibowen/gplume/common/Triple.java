/*******************************************************************************
 * Copyright (c) 2014 Bowen Cai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributor:
 *     Bowen Cai - initial API and implementation
 ******************************************************************************/
package com.caibowen.gplume.common;

import java.io.Serializable;

/**
 * 
 * @author BowenCai
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 */
public class Triple<T1, T2, T3> implements Serializable {
	
	private static final long serialVersionUID = 6206111099137834129L;
	
	public T1 first;
	public T2 second;
	public T3 third;

	public Triple() {}
	
	public Triple(T1 first, T2 second, T3 third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}

	@Override
	public String toString() {
		return "Triple [first=" + first + ", second=" + second + ", third="
				+ third + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
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
		Triple<?,?,?> other = (Triple<?,?,?>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (third == null) {
			if (other.third != null)
				return false;
		} else if (!third.equals(other.third))
			return false;
		return true;
	}
}
