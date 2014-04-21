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

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A custom {@link javax.servlet.ServletOutputStream} for use by our filters
 *
 * @version $Id: FilterServletOutputStream.java 744 2008-08-16 20:10:49Z gregluck $
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 */
public class FilterServletOutputStream extends ServletOutputStream {

    private OutputStream stream;

    /**
     * Creates a FilterServletOutputStream.
     */
    public FilterServletOutputStream(final OutputStream stream) {
        this.stream = stream;
    }

    /**
     * Writes to the stream.
     */
    @Override
	public void write(final int b) throws IOException {
        stream.write(b);
    }

    /**
     * Writes to the stream.
     */
    @Override
	public void write(final byte[] b) throws IOException {
        stream.write(b);
    }

    /**
     * Writes to the stream.
     */
    @Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
        stream.write(b, off, len);
    }
}

