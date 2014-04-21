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

}
