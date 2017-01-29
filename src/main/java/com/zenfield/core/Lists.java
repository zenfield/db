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

import java.util.Collections;
import java.util.List;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public final class Lists {

	private Lists() {
	}

	public static boolean isEmpty(List<?> list) {
		return list == null || list.size() <= 0;
	}

	public static <T> List<T> nullify(List<T> s) {
		return isEmpty(s) ? null : s;
	}

	public static <T> List<T> safe(List<T> list) {
		if (list == null) {
			return Collections.emptyList();
		}

		return list;
	}

	public static int size(List<?> list) {
		return list == null ? 0 : list.size();
	}
}
