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
import com.zenfield.database.configuration.ReadOnly;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class FetchCommand extends AbstractCommand {

	private final Environment source;

	public FetchCommand(ProjectConfiguration configuration, Parameters parameters, Environment source) {
		super(configuration, parameters);
		Check.notNull(source);
		this.source = source;
	}

	@Override
	public Environment getDestination() {
		return null;
	}

	@Override
	public boolean run(Environment destination) {
		Check.notNull(destination);

		if (source.same(destination)) {
			System.err.println("Cannot fetch: source must be different from destination");
			return false;
		}

		if (destination.getReadOnly() == ReadOnly.TRUE) {
			System.err.println("Cannot fetch: destination is read-only");
			return false;
		}

		if (!canWrite(destination)) {
			return false;
		}

		File tmp = null;

		try {
			tmp = File.createTempFile("db-", ".sql");
			if (!getDialect().dump(source, tmp)) {
				System.err.println("Database dump failed");
				return false;
			}

			if (!getDialect().clear(destination)) {
				System.err.println("Cannot clear the database before fetch");
				return false;
			}

			executeHook("post-clear", getConfiguration().getPostClear(), destination);

			if (!getDialect().execute(destination, tmp)) {
				System.err.println("Database load failed");
				return false;
			}

			executeHook("post-fetch", getConfiguration().getPostFetch(), destination);

			System.err.println("Database fetch done");
			return true;

		} catch (IOException e) {
			System.err.println("Error while fetching a database");
			System.err.println();
			Exceptions.print(e, System.err);
			return false;

		} finally {
			Files.delete(tmp);
		}
	}
}
