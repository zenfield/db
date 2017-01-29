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
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class DumpCommand extends AbstractCommand {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private final Environment source;

	public DumpCommand(ProjectConfiguration configuration, Parameters parameters, Environment source) {
		super(configuration, parameters);
		Check.notNull(source);
		this.source = source;
	}

	@Override
	public Environment getDestination() {
		return null;
	}

	@Override
	public boolean run(Environment unused) {
		String filename = "";

		filename += getConfiguration().getName();
		filename += "-";
		filename += source.getName();
		filename += "-";
		filename += FORMATTER.format(LocalDateTime.now());
		filename += ".sql";

		File destination = new File(filename);

		try {
			if (!getDialect().dump(source, destination)) {
				System.err.println("Database dump failed");
				return false;
			}

			System.err.println("Database dump written to " + filename);
			return true;

		} catch (Exception e) {
			System.err.println("Error while dumping a database");
			System.err.println();
			Exceptions.print(e, System.err);
			return false;
		}
	}
}
