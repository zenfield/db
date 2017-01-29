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
import com.zenfield.core.Files;
import com.zenfield.database.Project;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class Credentials {

	private static final String FILENAME = ".dbpass";

	private final Map<String, Project> projects;

	private Credentials(Map<String, Project> projects) {
		this.projects = Collections.unmodifiableMap(projects);
	}

	public static Credentials create(Map<String, String> map) throws IOException {
		Map<String, Project> projects = new HashMap<>();

		for (String name : Configurations.getSubKeys(map)) {
			Map<String, String> subMap = Configurations.getSubMap(map, name);

			Project project = Project.create(subMap);
			if (project == null) {
				return null;
			}

			projects.put(name, project);
		}

		return new Credentials(projects);
	}

	public static Credentials load() {
		File file = new File(Files.getHome(), FILENAME);
		if (!file.exists() || !file.isFile()) {
			System.err.println("Cannot load the credentials from " + file.getPath());
			return null;
		}

		try {
			Map<String, String> map = Configurations.load(file);
			return create(map);

		} catch (IOException e) {
			System.err.println("Cannot load the credentials from " + file.getPath());
			return null;
		}
	}

	public Project getProject(String name) {
		Check.notEmpty(name);
		return projects.get(name);
	}
}
