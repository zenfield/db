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
import com.zenfield.core.Exceptions;
import com.zenfield.core.Strings;
import com.zenfield.database.dialect.Dialect;
import com.zenfield.database.dialect.Dialects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class ProjectConfiguration {

	private static final String FILENAME = ".db";

	private static final String KEY_NAME = "name";
	private static final String KEY_CREATE = "database.create";
	private static final String KEY_POPULATE = "database.populate";
	private static final String KEY_DIALECT = "database.dialect";

	private static final String KEY_HOOK_POST_FETCH = "database.post-fetch";
	private static final String KEY_HOOK_POST_CLEAR = "database.post-clear";

	// project
	private final String name;

	// database
	private final File create;
	private final File populate;
	private final Dialect dialect;
	private final File postFetch;
	private final File postClear;

	private ProjectConfiguration(String name, File create, File populate, Dialect dialect, File postFetch, File postClear) {
		this.name = name;
		this.create = create;
		this.populate = populate;
		this.dialect = dialect;
		this.postFetch = postFetch;
		this.postClear = postClear;
	}

	public String getName() {
		return name;
	}

	public File getCreate() {
		return create;
	}

	public File getPopulate() {
		return populate;
	}

	public Dialect getDialect() {
		return dialect;
	}

	public File getPostFetch() {
		return postFetch;
	}

	public File getPostClear() {
		return postClear;
	}

	public static ProjectConfiguration load() {
		File current = new File(System.getProperty("user.dir"));
		File file = find(current);
		if (file == null) {
			System.err.format("Cannot find the project configuration: %s\n", FILENAME);
			return null;
		}

		try {
			Map<String, String> map = Configurations.load(file);

			String name = map.get(KEY_NAME);
			if (Strings.isEmpty(name)) {
				System.err.format("Error: no %s found in the %s file\n", KEY_NAME, FILENAME);
				return null;
			}

			return new ProjectConfiguration(
					name,
					getCreate(file, map),
					getPopulate(file, map),
					getDialect(map),
					getHook(file, KEY_HOOK_POST_FETCH, map),
					getHook(file, KEY_HOOK_POST_CLEAR, map)
			);

		} catch (InvalidConfigurationException e) {
			return null;

		} catch (FileNotFoundException e) {
			System.err.format("Configuration file %s not found at %s\n", FILENAME, file.getAbsolutePath());
			return null;

		} catch (IOException e) {
			System.err.format("Cannot load the configuration file %s at %s\n", FILENAME, file.getAbsolutePath());
			Exceptions.print(e, System.err);
			return null;
		}
	}

	private static File getCreate(File file, Map<String, String> map) throws InvalidConfigurationException {
		Check.notNull(file);
		Check.notNull(map);

		String createValue = map.get(KEY_CREATE);
		if (Strings.isEmpty(createValue)) {
			return null;
		}

		File create = new File(file.getParentFile(), createValue);
		if (!create.exists()) {
			System.err.format("Error: cannot find the create script at %s\n", createValue);
			throw new InvalidConfigurationException();
		}

		if (create.isDirectory()) {
			System.err.format("Error: the create script is a directory at %s\n", createValue);
			throw new InvalidConfigurationException();
		}

		return create;
	}

	private static File getPopulate(File file, Map<String, String> map) throws InvalidConfigurationException {
		Check.notNull(file);
		Check.notNull(map);

		String populateValue = map.get(KEY_POPULATE);
		if (Strings.isEmpty(populateValue)) {
			return null;
		}

		File populate = new File(file.getParentFile(), populateValue);
		if (!populate.exists()) {
			System.err.format("Error: cannot find the populate path at %s\n", populateValue);
			throw new InvalidConfigurationException();
		}

		if (!populate.isDirectory()) {
			System.err.format("Error: populate path is not a directory at %s\n", populateValue);
			throw new InvalidConfigurationException();
		}

		return populate;
	}

	private static Dialect getDialect(Map<String, String> map) throws InvalidConfigurationException {
		Check.notNull(map);

		String dialectValue = map.get(KEY_DIALECT);
		if (Strings.isEmpty(dialectValue)) {
			return null;
		}

		Dialect dialect = Dialects.of(dialectValue);
		if (dialect == null) {
			System.err.format("Error: unknown %s in the %s file: %s\n", KEY_DIALECT, FILENAME, dialectValue);
			throw new InvalidConfigurationException();
		}

		return dialect;
	}

	private static File getHook(File file, String key, Map<String, String> map) throws InvalidConfigurationException {
		Check.notNull(file);
		Check.notEmpty(key);
		Check.notNull(map);

		String path = map.get(key);
		if (Strings.isEmpty(path)) {
			return null;
		}

		File hook = new File(file.getParentFile(), path);
		if (!hook.exists()) {
			System.err.format("Error: cannot find the hook file or directory at %s\n", path);
			throw new InvalidConfigurationException();
		}

		if (!hook.isFile() && !hook.isDirectory()) {
			System.err.format("Error: the create script is not a file nor a directory at %s\n", path);
			throw new InvalidConfigurationException();
		}

		return hook;
	}

	private static File find(File path) {
		if (path == null || !path.isDirectory()) {
			return null;
		}

		File file = new File(path, FILENAME);
		if (file.exists() && file.isFile()) {
			return file;
		}

		return find(path.getParentFile());
	}
}
