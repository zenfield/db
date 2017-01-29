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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public final class Files {

	private static final int BUFFER_SIZE = 16384;

	private Files() {
	}

	public static File getHome() {
		return new File(System.getProperty("user.home"));
	}

	public static void copy(InputStream in, File dst, boolean append) throws IOException {
		Check.notNull(in);
		Check.notNull(dst);

		FileOutputStream out = null;

		try {
			out = new FileOutputStream(dst, append);

			byte[] buffer = new byte[BUFFER_SIZE];
			int readBytes;
			while ((readBytes = in.read(buffer)) > 0) {
				out.write(buffer, 0, readBytes);
			}

		} finally {
			Closeables.close(out);
		}
	}

	public static void copy(File src, File dst, boolean append) throws IOException {
		Check.notNull(src);
		Check.notNull(dst);

		FileInputStream in = null;

		try {
			in = new FileInputStream(src);
			copy(in, dst, append);

		} finally {
			Closeables.close(in);
		}
	}

	public static void write(String text, File dst) throws IOException {
		write(text, dst, false);
	}

	public static void append(String text, File dst) throws IOException {
		write(text, dst, true);
	}

	public static void write(String text, File dst, boolean append) throws IOException {
		Check.notNull(text);
		Check.notNull(dst);

		StringReader reader = null;
		FileWriter writer = null;

		try {
			reader = new StringReader(text);
			writer = new FileWriter(dst, append);
			copy(reader, writer);

		} finally {
			Closeables.close(reader);
			Closeables.close(writer);
		}
	}

	public static long copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[BUFFER_SIZE];
		long count = 0;
		int n = 0;

		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}

		return count;
	}

	public static String load(File file) throws IOException {
		Check.notNull(file);
		return load(new FileInputStream(file));
	}

	public static String load(InputStream input) throws IOException {
		Check.notNull(input);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line).append('\n');
			}

			return result.toString();

		} finally {
			Closeables.close(reader);
		}
	}

	public static List<String> loadLines(File file) throws IOException {
		Check.notNull(file);
		return loadLines(new FileInputStream(file));
	}

	public static List<String> loadLines(InputStream input) throws IOException {
		Check.notNull(input);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

			List<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

			return lines;

		} finally {
			Closeables.close(reader);
		}
	}

	public static final void delete(File file) {
		if (file != null) {
			file.delete();
		}
	}
}
