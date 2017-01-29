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
import com.zenfield.core.Processes;
import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.Parameters;
import com.zenfield.database.configuration.ProjectConfiguration;
import com.zenfield.database.configuration.ReadOnly;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class PopulateCommand extends AbstractCommand {

	private final String directory;
	private final Environment destination;

	public PopulateCommand(ProjectConfiguration configuration, Parameters parameters, String directory, Environment destination) {
		super(configuration, parameters);

		Check.notEmpty(directory);
		Check.notNull(destination);
		this.directory = directory;
		this.destination = destination;
	}

	@Override
	public Environment getDestination() {
		return destination;
	}

	@Override
	public boolean run(Environment unused) {
		if (destination.getReadOnly() == ReadOnly.TRUE) {
			System.err.println("Cannot populate: destination is read-only");
			return false;
		}

		File populate = getConfiguration().getPopulate();
		if (!populate.exists() || !populate.isDirectory()) {
			System.err.println("Populate directory not found: " + populate.getPath());
			return false;
		}

		File root = new File(populate, directory);
		if (!root.exists() || !root.isDirectory()) {
			System.err.println("Populate directory not found: " + directory + " in " + populate.getPath());
			return false;
		}

		if (!canWrite(destination)) {
			return false;
		}

		if (!getDialect().clear(destination)) {
			System.err.println("Cannot clear the database before populate");
			return false;
		}

		System.err.println("Database cleared");

		if (!getDialect().execute(destination, getConfiguration().getCreate())) {
			System.err.println("Cannot create the database before populate");
			return false;
		}

		System.err.println("Database created");

		boolean success = true;

		String[] files = root.list();
		Arrays.sort(files);

		for (String spot : files) {
			if (spot.toLowerCase().endsWith(".skip")) {
				System.err.println("Skipping: " + spot);
				continue;
			}

			if (spot.toLowerCase().endsWith(".sql")) {
				System.err.println("Executing SQL: " + spot);
				if (!getDialect().execute(destination, new File(root, spot))) {
					System.err.println("Cannot execute: " + spot);
					success = false;
				}
				continue;
			}

			if (new File(root, spot).canExecute()) {
				System.err.println("Executing script: " + spot);
				if (!executeScript(destination, new File(root, spot))) {
					success = false;
				}
				continue;
			}

			System.err.println("Skipping (unknown): " + spot);
		}

		return success;
	}

	private boolean executeScript(Environment destination, File script) {
		Check.notNull(destination);
		Check.notNull(script);

		File parent = script.getParentFile();
		if (parent == null) {
			System.err.println("Cannot run the script: no parent, script: " + script);
			return false;
		}

		if (!parent.isDirectory()) {
			System.err.println("Cannot run the script: parent is not a directory, parent: " + parent);
			return false;
		}

		File tmp = null;

		try {
			ProcessBuilder builder = new ProcessBuilder("./" + script.getName());
			builder.directory(parent);

			tmp = File.createTempFile("db-", ".sql");
			Integer exitCode = Processes.save(builder, tmp);

			if (exitCode == null) {
				System.err.println("Cannot run the script: null exit code, script: " + script);
				return false;
			}

			if (exitCode != 0) {
				System.err.println("Cannot run the script: exit code was " + exitCode + ", script: " + script);
				return false;
			}

			if (!getDialect().execute(destination, tmp)) {
				System.err.println("Cannot load: " + script);
				return false;
			}

			return true;

		} catch (IOException e) {
			System.err.println("Error while executing script: " + script);
			System.err.println();
			Exceptions.print(e, System.err);
			return false;

		} finally {
			Files.delete(tmp);
		}
	}
}
