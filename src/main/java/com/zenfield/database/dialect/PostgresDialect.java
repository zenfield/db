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
import com.zenfield.core.Closeables;
import com.zenfield.core.Exceptions;
import com.zenfield.core.Files;
import com.zenfield.core.Lists;
import com.zenfield.core.Processes;
import com.zenfield.core.Strings;
import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.ReadOnly;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class PostgresDialect implements Dialect {

	private static final String QUERY_LIST_TABLES = "SELECT tablename FROM pg_tables WHERE schemaname='public'";
	private static final String QUERY_CLEAR = "DROP SCHEMA public CASCADE; CREATE SCHEMA public;";

	@Override
	public String getName() {
		return "PostgreSQL";
	}

	@Override
	public List<String> listTables(Environment environment) {
		Check.notNull(environment);

		try {
			ProcessBuilder builder = createQueryBuilder(environment, QUERY_LIST_TABLES);
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

			return result
					.stream()
					.map(s -> s == null ? "" : s.trim())
					.filter(s -> !Strings.isEmpty(s))
					.collect(Collectors.toList());

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
			ProcessBuilder builder = createQueryBuilder(environment, QUERY_CLEAR);
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

			if (Lists.isEmpty(result)) {
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
			System.err.println("Cannot execute a script: read-only environment");
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

		try {
			ProcessBuilder builder;

			if (environment.isSsh()) {
				builder = new ProcessBuilder(
						"ssh", "-C", environment.getSshUsernameHostname(),
						"PGPASSWORD=" + environment.getPassword(),
						"psql",
						"-wU", environment.getUsername(),
						"-h", environment.getHostname(),
						environment.getDatabase(),
						"-q1");
				builder.redirectInput(script.getAbsoluteFile());

			} else {
				builder = new ProcessBuilder(
						"psql",
						"-wU", environment.getUsername(),
						"-h", environment.getHostname(),
						environment.getDatabase(),
						"-q1f", script.getAbsolutePath());

				builder.environment().put("PGPASSWORD", environment.getPassword());
			}

			builder.redirectError(ProcessBuilder.Redirect.INHERIT);
			builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

			Process process = builder.start();
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				System.err.println("Cannot execute: exit code was " + exitCode);
				return false;
			}

			return true;

		} catch (InterruptedException e) {
			return false;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;
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
					"PGPASSWORD=" + environment.getPassword(),
					"pg_dump",
					"-wU", environment.getUsername(),
					"-h", environment.getHostname(),
					"--no-owner",
					environment.getDatabase());

		} else {
			builder = new ProcessBuilder(
					"pg_dump",
					"-wU", environment.getUsername(),
					"-h", environment.getHostname(),
					"--no-owner",
					environment.getDatabase());
			builder.environment().put("PGPASSWORD", environment.getPassword());
		}

		List<String> lines = new ArrayList<>();

		try {

			Integer exitCode = Processes.execute(builder, lines);
			if (exitCode == null) {
				System.err.println("Cannot dump the database: null exit code");
				return false;
			}

			if (exitCode != 0) {
				System.err.println("Cannot dump the database: exit code was " + exitCode);
				return false;
			}

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;
		}

		try (Writer writer = new FileWriter(file)) {
			for (String line : lines) {
				if (line != null
						&& !line.contains("REVOKE ALL ON SCHEMA")
						&& !line.contains("GRANT ALL ON SCHEMA")) {
					writer.write(line);
					writer.write('\n');
				}
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
					"PGPASSWORD=" + environment.getPassword(),
					"pg_dump",
					"-wU", environment.getUsername(),
					"-h", environment.getHostname(),
					"--no-owner",
					"-t", table,
					"-a",
					environment.getDatabase());

		} else {
			builder = new ProcessBuilder(
					"pg_dump",
					"-wU", environment.getUsername(),
					"-h", environment.getHostname(),
					"--no-owner",
					"-t", table,
					"-a",
					environment.getDatabase());
			builder.environment().put("PGPASSWORD", environment.getPassword());
		}

		try {
			Integer exitCode = Processes.save(builder, file);

			if (exitCode == null) {
				System.err.println("Cannot dump the database: null exit code");
				return false;
			}

			if (exitCode != 0) {
				System.err.println("Cannot dump the database: exit code was " + exitCode);
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

		File tmp = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;

		try {
			tmp = File.createTempFile("db-", ".sql");
			reader = new BufferedReader(new FileReader(file));
			writer = new BufferedWriter(new FileWriter(tmp));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line == null
						|| Strings.isEmpty(line.trim())
						|| line.startsWith("--")
						|| line.startsWith("SET ")) {
					continue;
				}

				writer.append(line);
				writer.newLine();
			}

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			Files.delete(tmp);
			return false;

		} finally {
			Closeables.close(reader);
			Closeables.close(writer);
		}

		if (tmp == null) {
			return false;
		}

		try {
			java.nio.file.Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return true;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return false;

		} finally {
			Files.delete(tmp);
		}
	}

	private ProcessBuilder createQueryBuilder(Environment environment, String query) {
		Check.notNull(environment);
		Check.notEmpty(query);

		if (environment.isSsh()) {
			return new ProcessBuilder(
					"ssh", "-C", environment.getSshUsernameHostname(),
					"PGPASSWORD=" + environment.getPassword(),
					"psql",
					"-wU", environment.getUsername(),
					"-h", environment.getHostname(),
					environment.getDatabase(),
					"-tc",
					"\"" + query + "\"");

		} else {
			ProcessBuilder builder = new ProcessBuilder(
					"psql",
					"-wU", environment.getUsername(),
					"-h", environment.getHostname(),
					environment.getDatabase(),
					"-tc",
					query);
			builder.environment().put("PGPASSWORD", environment.getPassword());
			return builder;
		}
	}
}
