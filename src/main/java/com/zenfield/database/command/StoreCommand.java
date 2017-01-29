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
package com.zenfield.database.command;

import com.zenfield.core.Check;
import com.zenfield.core.Exceptions;
import com.zenfield.core.Files;
import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.Parameters;
import com.zenfield.database.configuration.ProjectConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class StoreCommand extends AbstractCommand {

	private final String directory;

	public StoreCommand(ProjectConfiguration configuration, Parameters parameters, String directory) {
		super(configuration, parameters);

		Check.notNull(directory);
		this.directory = directory;
	}

	@Override
	public Environment getDestination() {
		return null;
	}

	@Override
	public boolean run(Environment source) {
		Check.notNull(source);

		File file = new File(getConfiguration().getPopulate(), directory);
		if (!file.exists()) {
			if (!file.mkdirs() || !file.exists()) {
				System.err.println("Populate directory not found and cannot create: " + directory);
				return false;
			}
		}

		if (!file.isDirectory()) {
			System.err.println("Not a directory: " + directory);
			return false;
		}

		List<String> tablesFromCreate = findTablesFromCreate();
		if (tablesFromCreate == null) {
			return false;
		}

		if (tablesFromCreate.isEmpty()) {
			System.err.println("No tables found in the create script");
			return true;
		}

		for (File spot : file.listFiles((File dir, String name) -> {
			return name.startsWith("dump-");
		})) {
			spot.delete();
		}

		boolean success = true;

		int i = 0;
		for (String table : tablesFromCreate) {
			System.err.format("- %-32s", table);
			i++;

			File skip = new File(file, table + ".skip");
			if (skip.exists()) {
				System.err.println("skipping");
				continue;
			}

			File dump = new File(file, String.format("dump-%02d-%s.sql", i, table));
			if (!getDialect().dump(source, table, dump)) {
				success = false;
				System.err.println("error");

			} else {
				System.err.println("ok");
			}
		}

		return success;
	}

	public List<String> findTablesFromCreate() {
		try {
			List<String> lines = Files.loadLines(getConfiguration().getCreate());
			if (lines == null) {
				return null;
			}

			if (lines.isEmpty()) {
				return Collections.emptyList();
			}

			List<String> tables = lines.stream()
					.filter(line -> line != null && line.contains("CREATE TABLE"))
					.map(line -> line.replace("CREATE TABLE", ""))
					.map(line -> line.replace("IF NOT EXISTS", ""))
					.map(line -> line.replace("(", ""))
					.map(line -> line.replace("'", ""))
					.map(line -> line.replace("`", ""))
					.map(line -> line.trim())
					.filter(line -> !line.isEmpty())
					.collect(Collectors.toList());

			return tables;

		} catch (IOException e) {
			Exceptions.print(e, System.err);
			return null;
		}
	}
}
