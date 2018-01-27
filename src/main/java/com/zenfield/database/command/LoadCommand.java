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
import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.Parameters;
import com.zenfield.database.configuration.ProjectConfiguration;
import com.zenfield.database.configuration.ReadOnly;
import java.io.File;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class LoadCommand extends AbstractCommand {

	private final String path;

	public LoadCommand(ProjectConfiguration configuration, Parameters parameters, String path) {
		super(configuration, parameters);

		Check.notEmpty(path);
		this.path = path;
	}

	@Override
	public Environment getDestination() {
		return null;
	}

	@Override
	public boolean run(Environment destination) {
		Check.notNull(destination);

		File file = new File(path);
		if (!file.exists()) {
			System.err.println("Cannot load: file not found at " + path);
			return false;
		}

		if (!file.canRead()) {
			System.err.println("Cannot load: cannot read file at " + path);
			return false;
		}

		if (file.isDirectory()) {
			System.err.println("Cannot load: " + path + " is a directory");
			return false;
		}

		if (destination.getReadOnly() == ReadOnly.TRUE) {
			System.err.println("Cannot load: destination is read-only");
			return false;
		}

		if (!canWrite(destination)) {
			return false;
		}

		try {
			if (!getDialect().clear(destination)) {
				System.err.println("Cannot clear the database before load");
				return false;
			}

			executeHook("post-clear", getConfiguration().getPostClear(), destination);

			if (!getDialect().execute(destination, file)) {
				System.err.println("Database load failed");
				return false;
			}

			System.err.println("Database loaded from " + path);
			return true;

		} catch (Exception e) {
			System.err.println("Error while loading from " + path);
			System.err.println();
			Exceptions.print(e, System.err);
			return false;
		}
	}
}
