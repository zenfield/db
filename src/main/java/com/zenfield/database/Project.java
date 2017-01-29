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
package com.zenfield.database;

import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.Configurations;
import com.zenfield.core.Check;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class Project {

	private final Map<String, Environment> environments;

	public Project(Map<String, Environment> environments) {
		Check.notNull(environments);
		this.environments = Collections.unmodifiableMap(environments);
	}

	public static Project create(Map<String, String> map) {
		Map<String, Environment> instance = new HashMap<>();

		for (String name : Configurations.getSubKeys(map)) {
			Map<String, String> subMap = Configurations.getSubMap(map, name);

			Environment environment = Environment.create(name, subMap);
			if (environment == null) {
				return null;
			}

			instance.put(name, environment);
		}

		return new Project(instance);
	}

	public Environment getEnvironment(String name) {
		Check.notEmpty(name);
		return environments.get(name);
	}

	public Stream<String> environmentNames() {
		return environments.keySet().stream();
	}
}
