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
package com.caibowen.gplume.misc.test.jsp.tag;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.jsp.JspWriter;


public class JspWriterWrapper extends JspWriter {

    private final PrintStream ps;

	protected JspWriterWrapper(PrintStream printStream) {
		super(4096, false);
        this.ps = printStream;
	}

	@Override
	public void clear() throws IOException {

	}

	@Override
	public void clearBuffer() throws IOException {
	}

	@Override
	public void close() throws IOException {
        ps.close();
	}

	@Override
	public void flush() throws IOException {
		ps.flush();
	}

	@Override
	public int getRemaining() {
		return 0;
	}

	@Override
	public void newLine() throws IOException {
		ps.println();
	}

	@Override
	public void print(boolean arg0) throws IOException {
		ps.print(arg0);
	}

	@Override
	public void print(char arg0) throws IOException {
		ps.print(arg0);
	}

	@Override
	public void print(int arg0) throws IOException {
		ps.print(arg0);
	}

	@Override
	public void print(long arg0) throws IOException {
		ps.print(arg0);		
	}

	@Override
	public void print(float arg0) throws IOException {
		ps.print(arg0);		
	}

	@Override
	public void print(double arg0) throws IOException {
		ps.print(arg0);		
	}

	@Override
	public void print(char[] arg0) throws IOException {
		ps.print(arg0);
	}

	@Override
	public void print(String arg0) throws IOException {
		ps.print(arg0);
	}

	@Override
	public void print(Object arg0) throws IOException {
		ps.print(arg0);		
	}

	@Override
	public void println() throws IOException {
		ps.println();	
	}

	@Override
	public void println(boolean arg0) throws IOException {
		ps.println(arg0);
	}

	@Override
	public void println(char arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void println(int arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void println(long arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void println(float arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void println(double arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void println(char[] arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void println(String arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void println(Object arg0) throws IOException {
		ps.println(arg0);		
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int i = off; i < off + len; i++) {
			print(cbuf[i]);
		}
	}

}
