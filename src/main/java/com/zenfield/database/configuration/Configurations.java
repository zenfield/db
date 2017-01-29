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
package com.zenfield.database.configuration;

import com.zenfield.core.Check;
import com.zenfield.core.Closeables;
import com.zenfield.core.Strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public final class Configurations {

	private Configurations() {
	}

	public static Set<String> getSubKeys(Map<String, String> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptySet();
		}

		Set<String> subKeys = new HashSet<>();
		for (String key : values.keySet()) {
			int index = key.indexOf('.');
			String subKey = key.substring(0, index);
			subKeys.add(subKey);
		}

		return subKeys;
	}

	public static Map<String, String> getSubMap(Map<String, String> values, String subKey) {
		// values
		Check.notEmpty(subKey);

		if (values == null || values.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, String> sub = new HashMap<>();
		for (String spot : values.keySet()) {
			int index = spot.indexOf('.');
			String outerKey = spot.substring(0, index);
			if (!Strings.isEqual(subKey, outerKey)) {
				continue;
			}

			String innerKey = spot.substring(index + 1);
			sub.put(innerKey, values.get(spot));
		}

		return sub;
	}

	public static Map<String, String> load(File file) throws IOException {
		BufferedReader in = null;

		try {
			int n = 0;
			Map<String, String> values = new HashMap<>();

			in = new BufferedReader(new FileReader(file));
			while (in.ready()) {
				n++;

				String line = in.readLine();
				if (line == null) {
					continue;
				}

				line = line.trim();
				if (line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}

				int index = line.indexOf('=');
				if (index == -1) {
					System.err.println("Error: invalid line at #" + n + ": " + line);
					break;
				}

				String name = line.substring(0, index);
				String value = line.substring(index + 1);
				values.put(name, value);
			}

			return values;

		} finally {
			Closeables.close(in);
		}
	}
}
