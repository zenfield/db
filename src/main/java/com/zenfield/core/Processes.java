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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class Processes {

	public static Integer execute(String command, List<String> output) throws IOException {
		Check.notEmpty(command);
		Check.notNull(output);

		Process process = Runtime.getRuntime().exec(command);
		return execute(process, output);
	}

	public static Integer execute(ProcessBuilder builder, List<String> output) throws IOException {
		Check.notNull(builder);
		Check.notNull(output);

		builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
		builder.redirectError(ProcessBuilder.Redirect.INHERIT);
		return execute(builder.start(), output);
	}

	public static Integer execute(Process process, List<String> output) throws IOException {
		Check.notNull(process);
		Check.notNull(output);

		InputStream is = process.getInputStream();
		if (is == null) {
			return null;
		}

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = reader.readLine()) != null) {
				output.add(line);
			}

			try {
				return process.waitFor();

			} catch (InterruptedException e) {
				return null;
			}

		} finally {
			Closeables.close(reader);
		}
	}

	public static Integer save(ProcessBuilder builder, File output) throws IOException {
		return save(builder, line -> true, output);
	}

	public static Integer save(ProcessBuilder builder, Predicate<String> filter, File output) throws IOException {
		Check.notNull(builder);
		Check.notNull(output);

		builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
		builder.redirectError(ProcessBuilder.Redirect.INHERIT);
		return save(builder.start(), filter, output);
	}

	public static Integer save(Process process, Predicate<String> filter, File output) throws IOException {
		Check.notNull(process);
		Check.notNull(filter);
		Check.notNull(output);

		InputStream is = process.getInputStream();
		if (is == null) {
			return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		Writer writer = null;

		try {
			writer = new FileWriter(output);

			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}

				if (!filter.test(line)) {
					continue;
				}

				writer.write(line);
				writer.write('\n');
			}

			try {
				return process.waitFor();

			} catch (InterruptedException e) {
				return null;
			}

		} finally {
			Closeables.close(reader);
			Closeables.close(writer);
		}
	}

	public static void destroy(Process process) {
		if (process != null) {
			process.destroy();
		}
	}
}
