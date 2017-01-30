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
package com.zenfield.database.dialect;

import com.zenfield.core.Check;
import com.zenfield.core.Exceptions;
import com.zenfield.core.Files;
import com.zenfield.core.Lists;
import com.zenfield.core.Processes;
import com.zenfield.core.Strings;
import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.ReadOnly;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class MysqlDialect implements Dialect {

	private static final String QUERY_CLEAR = "DROP DATABASE {name}; CREATE DATABASE {name};";
	private static final String DEFINER_START = "/*!50013 DEFINER=";

	@Override
	public String getName() {
		return "MySQL";
	}

	@Override
	public List<String> listTables(Environment environment) {
		Check.notNull(environment);

		try {
			ProcessBuilder builder = createQueryBuilder(environment, "show tables");
			List<String> result = new ArrayList<>();
			Integer exitCode = Processes.execute(builder, result);

			if (exitCode == null) {
				System.err.println("Cannot retrieve the tables: null exit code");
				return null;
			}

			if (exitCode != 0) {
				System.err.println("Cannot retrieve the tables: exit code was " + exitCode);
				return null;
			}

			return Collections.unmodifiableList(result);

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return null;
		}
	}

	@Override
	public int countRows(Environment environment, String table) {
		Check.notNull(environment);
		Check.notEmpty(table);

		try {
			ProcessBuilder builder = createQueryBuilder(environment, "SELECT COUNT(*) FROM " + table);
			List<String> result = new ArrayList<>();
			Integer exitCode = Processes.execute(builder, result);

			if (exitCode == null) {
				System.err.println("Cannot count rows: null exit code");
				return -1;
			}

			if (exitCode != 0) {
				System.err.println("Cannot count rows: exit code was " + exitCode);
				return -1;
			}

			if (Lists.isEmpty(result)) {
				return -1;
			}

			String first = result.get(0);
			if (Strings.isEmpty(first)) {
				return -1;
			}

			first = first.trim();
			return Integer.parseInt(first);

		} catch (NumberFormatException e) {
			return -1;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return -1;
		}
	}

	@Override
	public boolean clear(Environment environment) {
		Check.notNull(environment);

		if (environment.getReadOnly() == ReadOnly.TRUE) {
			System.err.println("Cannot clear the database: read-only environment");
			return false;
		}

		try {
			String query = QUERY_CLEAR;
			query = query.replace("{name}", environment.getDatabase());

			ProcessBuilder builder = createQueryBuilder(environment, query);
			List<String> result = new ArrayList<>();
			Integer exitCode = Processes.execute(builder, result);

			if (exitCode == null) {
				System.err.println("Cannot clear the database: null exit code");
				return false;
			}

			if (exitCode != 0) {
				System.err.println("Cannot clear the database: exit code was " + exitCode);
				return false;
			}

			for (String line : result) {
				System.out.println(line);
			}

			return true;

		} catch (NumberFormatException e) {
			return false;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;
		}
	}

	@Override
	public boolean execute(Environment environment, File script) {
		Check.notNull(environment);
		Check.notNull(script);

		if (environment.getReadOnly() == ReadOnly.TRUE) {
			System.err.println("Cannot execute: read-only environment");
			return false;
		}

		if (!script.exists()) {
			System.err.println("File not found: " + script.getName());
			return false;
		}

		if (!script.isFile()) {
			System.err.println("Not a file: " + script.getName());
			return false;
		}

		File tmp = null;

		try {
			tmp = File.createTempFile("db-", ".sql");
			Files.write("SET autocommit=0;", tmp);
			Files.copy(script, tmp, true);
			Files.append("COMMIT;", tmp);

			ProcessBuilder builder;
			if (environment.isSsh()) {
				builder = new ProcessBuilder(
						"ssh", "-C", environment.getSshUsernameHostname(),
						"mysql",
						"--host", environment.getHostname(),
						"--user", environment.getUsername(),
						"--password=" + environment.getPassword(),
						environment.getDatabase());

			} else {
				builder = new ProcessBuilder(
						"mysql",
						"--host", environment.getHostname(),
						"--user", environment.getUsername(),
						"--password=" + environment.getPassword(),
						environment.getDatabase());
			}

			builder.redirectInput(tmp);

			List<String> result = new ArrayList<>();
			Integer exitCode = Processes.execute(builder, result);

			if (exitCode == null) {
				System.err.println("Cannot execute: null exit code");
				return false;
			}

			if (exitCode != 0) {
				System.err.println("Cannot execute: exit code was " + exitCode);
				return false;
			}

			result.stream().forEachOrdered((line) -> {
				System.err.println(line);
			});

			return true;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;

		} finally {
			Files.delete(tmp);
		}
	}

	@Override
	public boolean dump(Environment environment, File file) {
		Check.notNull(environment);
		Check.notNull(file);

		ProcessBuilder builder;

		if (environment.isSsh()) {
			builder = new ProcessBuilder(
					"ssh", "-C", environment.getSshUsernameHostname(),
					"mysqldump",
					"--host", environment.getHostname(),
					"--user", environment.getUsername(),
					"--password=" + environment.getPassword(),
					environment.getDatabase());

		} else {
			builder = new ProcessBuilder(
					"mysqldump",
					"--host", environment.getHostname(),
					"--user", environment.getUsername(),
					"--password=" + environment.getPassword(),
					environment.getDatabase());
		}

		try {
			Integer exitCode = Processes.save(builder, line -> !line.startsWith(DEFINER_START), file);

			if (exitCode == null) {
				System.err.println("Cannot dump the database: null exit code");
				return false;
			}

			if (exitCode != 0) {
				System.err.println("Cannot dump the database: exit code was " + exitCode);
				return false;
			}

			return true;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;
		}
	}

	@Override
	public boolean dump(Environment environment, String table, File file) {
		Check.notNull(environment);
		Check.notEmpty(table);
		Check.notNull(file);

		ProcessBuilder builder;

		if (environment.isSsh()) {
			builder = new ProcessBuilder(
					"ssh", "-C", environment.getSshUsernameHostname(),
					"mysqldump",
					"-c", "--compact", "--no-create-info", "--order-by-primary",
					"--host", environment.getHostname(),
					"--user", environment.getUsername(),
					"--password=" + environment.getPassword(),
					environment.getDatabase(),
					table);

		} else {
			builder = new ProcessBuilder(
					"mysqldump",
					"-c", "--compact", "--no-create-info", "--order-by-primary",
					"--host", environment.getHostname(),
					"--user", environment.getUsername(),
					"--password=" + environment.getPassword(),
					environment.getDatabase(),
					table);
		}

		try {
			Integer exitCode = Processes.save(builder, file);

			if (exitCode == null) {
				System.err.println("Cannot dump the table " + table + ": null exit code");
				return false;
			}

			if (exitCode != 0) {
				System.err.println("Cannot dump the table " + table + ": exit code was " + exitCode);
				return false;
			}

			return cleanup(file);

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;
		}
	}

	private boolean cleanup(File file) {
		Check.notNull(file);

		if (!file.exists()) {
			System.err.println("File not found: " + file.getAbsolutePath());
			return false;
		}

		if (!file.isFile()) {
			System.err.println("Not a file: " + file.getAbsolutePath());
			return false;
		}

		try {
			Pattern itemsPattern = Pattern.compile("\\),\\(", Pattern.MULTILINE);
			Pattern valuesPattern = Pattern.compile("\\) VALUES \\(", Pattern.MULTILINE);

			String content = Files.load(file);
			if (Strings.isEmpty(content)) {
				return false;
			}

			Matcher itemsMatcher = itemsPattern.matcher(content);
			content = itemsMatcher.replaceAll("),\n(");

			Matcher valuesMatcher = valuesPattern.matcher(content);
			content = valuesMatcher.replaceAll(") VALUES\n(");

			Files.write(content, file);
			return true;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;
		}
	}

	private ProcessBuilder createQueryBuilder(Environment environment, String query) {
		Check.notNull(environment);
		Check.notEmpty(query);

		if (environment.isSsh()) {
			return new ProcessBuilder(
					"ssh", "-C", environment.getSshUsernameHostname(),
					"mysql",
					"--host", environment.getHostname(),
					"--user", environment.getUsername(),
					"--password=" + environment.getPassword(),
					environment.getDatabase(),
					"-Ne",
					"\"" + query + "\"");

		} else {
			return new ProcessBuilder(
					"mysql",
					"--host", environment.getHostname(),
					"--user", environment.getUsername(),
					"--password=" + environment.getPassword(),
					environment.getDatabase(),
					"-Ne",
					query);
		}
	}
}
