/*
 * Copyright (c) 2015-2017 Zenfield Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zenfield.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public final class IOStreams {

	private static final int BUFFER_SIZE = 16384;

	private IOStreams() {
	}

	/**
	 * Copies data from src into dst. The streams must be closed by the caller.
	 *
	 * @param in
	 * @param out
	 * @throws IOException
	 */
//	public static void copy(InputStream in, OutputStream out) throws IOException {
//		Check.notNull(in);
//		Check.notNull(out);
//
//		byte[] buffer = new byte[BUFFER_SIZE];
//		int length;
//
//		while ((length = in.read(buffer)) > 0) {
//			out.write(buffer, 0, length);
//		}
//	}
}
