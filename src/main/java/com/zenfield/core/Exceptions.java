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

import java.io.PrintStream;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public final class Exceptions {

	private static final int STACKTRACE_MAX_DEPTH = 40;

	private Exceptions() {
	}

	public static String getStackTrace(Exception exception) {
		Check.notNull(exception);

		StringBuilder result = new StringBuilder();
		addStackTrace(exception, result);
		return result.toString();
	}

	private static void addStackTrace(Throwable throwable, StringBuilder result) {
		Check.notNull(throwable);
		Check.notNull(result);

		StackTraceElement[] elements = throwable.getStackTrace();
		if (elements == null) {
			result.append("<empty>\n");
			return;
		}

		result.append(throwable.getClass().toString());
		result.append(":");
		result.append(throwable.getMessage());
		result.append("\n");

		for (StackTraceElement element : elements) {
			result.append("   at ");
			result.append(element.getClassName());
			result.append(":");
			result.append(element.getMethodName());
			result.append("(");

			if (element.getFileName() == null) {
				result.append("Unknown Source");
			} else {
				result.append(element.getFileName());
				result.append(":");
				result.append(element.getLineNumber());
			}
			result.append(")\n");
		}

		if (throwable.getCause() != null) {
			result.append("Caused by:");
			addStackTrace(throwable.getCause(), result);
		}
	}

	public static void print(Throwable th, PrintStream stream) {
		Check.notNull(th);
		Check.notNull(stream);

		print(th, stream, STACKTRACE_MAX_DEPTH);
	}

	private static void print(Throwable th, PrintStream stream, int maxDepth) {
		Check.notNull(th);
		Check.notNull(stream);
		// maxDepth

		if (maxDepth <= 0) {
			return;
		}

		stream.println(th.getClass().getName() + ": " + th.getMessage());

		StackTraceElement[] stackTrace = th.getStackTrace();
		for (StackTraceElement element : stackTrace) {
			stream.print("        at ");
			stream.print(element.getClassName());
			stream.print("." + element.getMethodName());

			if (element.isNativeMethod()) {
				stream.print("(Native Method)");

			} else {
				stream.print("(" + element.getFileName());
				stream.print(":" + element.getLineNumber());
				stream.print(")");
			}

			stream.println();
		}

		Throwable cause = th.getCause();
		if (cause != null) {
			stream.println("Caused by: ");
			print(cause, stream, maxDepth - 1);
		}
	}
}
