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
import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.Parameters;
import com.zenfield.database.configuration.ProjectConfiguration;
import java.util.List;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class InfoCommand extends AbstractCommand {

	private final Environment destination;

	public InfoCommand(ProjectConfiguration configuration, Parameters parameters, Environment destination) {
		super(configuration, parameters);

		Check.notNull(destination);
		this.destination = destination;
	}

	@Override
	public Environment getDestination() {
		return destination;
	}

	@Override
	public boolean run(Environment unused) {
		List<String> tables = getDialect().listTables(destination);
		if (tables == null) {
			return false;
		}

		if (tables.isEmpty()) {
			System.out.println("No tables");
			return true;
		}

		System.out.println("Tables:");
		tables.stream().sorted().forEachOrdered((table) -> {
			int rows = getDialect().countRows(destination, table);
			System.out.println("- " + table + ": " + (rows < 0 ? "ERROR" : rows));
		});

		return true;
	}
}
