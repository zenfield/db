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
import com.zenfield.core.UnhandledCaseException;
import com.zenfield.database.Confirm;
import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.Parameters;
import com.zenfield.database.configuration.ProjectConfiguration;
import com.zenfield.database.dialect.Dialect;
import java.io.File;
import java.util.Arrays;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
abstract public class AbstractCommand implements Command {

	private final ProjectConfiguration configuration;
	private final Parameters parameters;

	public AbstractCommand(ProjectConfiguration configuration, Parameters parameters) {
		Check.notNull(configuration);
		Check.notNull(parameters);
		this.configuration = configuration;
		this.parameters = parameters;
	}

	public ProjectConfiguration getConfiguration() {
		return configuration;
	}

	protected final Dialect getDialect() {
		return configuration.getDialect();
	}

	public Parameters getParameters() {
		return parameters;
	}

	protected final boolean canWrite(Environment destination) {
		switch (destination.getReadOnly()) {
			case TRUE:
				return false;

			case FALSE:
				return true;

			case ASK:
				return Confirm.that("Are you sure? Type 'yes' to proceed: ");

			default:
				throw new UnhandledCaseException(destination.getReadOnly());
		}
	}

	protected final boolean executeHook(String hook, File source, Environment destination) {
		Check.notEmpty(hook);
		// source
		Check.notNull(destination);

		if (source == null) {
			return true;
		}

		if (source.isFile()) {
			if (getParameters().isSkipHooks()) {
				System.err.println(hook + ": " + source + " (skipped)");
				return true;
			}

			if (!configuration.getDialect().execute(destination, source)) {
				System.err.println("Could not run the " + hook + " hook: " + source);
				return false;
			}

			System.err.println(hook + ": " + source);
			return true;
		}

		if (source.isDirectory()) {
			File[] files = source.listFiles();
			Arrays.sort(files, (one, two) -> (one.compareTo(two)));

			for (File file : files) {
				if (getParameters().isSkipHooks()) {
					System.err.println(hook + ": " + file.getName() + " (skipped)");
					continue;
				}

				if (getParameters().isConfirmHooks()) {
					if (!Confirm.that(hook + ": " + file.getName() + " Type 'yes' to proceed: ")) {
						System.err.println(hook + ": " + file.getName() + " (skipped)");
						continue;
					}
				}

				if (!configuration.getDialect().execute(destination, file)) {
					System.err.println("Could not run the " + hook + " hook: " + file.getName());
					return false;
				}

				System.err.println(hook + ": " + file.getName());
			}

			return true;
		}

		System.err.println("Invalid hook, not a file nor a directory, " + hook + " at " + source);
		return true;
	}
}
