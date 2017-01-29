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
import com.zenfield.core.Lists;
import com.zenfield.core.Strings;
import java.util.List;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class Parameters {

	private final String command;
	private final List<String> arguments;
	private final boolean help;
	private final boolean skipHooks;
	private final boolean confirmHooks;

	private Parameters(String[] args) {
		OptionParser parser = new OptionParser();
		parser.nonOptions("command");
		parser.accepts("h");
		parser.accepts("help");
		parser.accepts("s");
		parser.accepts("skip-hooks");
		parser.accepts("c");
		parser.accepts("confirm-hooks");

		OptionSet options = parser.parse(args);

		command = parseCommand(options);
		arguments = parseArguments(options);
		help = options.has("h") || options.has("help");
		skipHooks = options.has("s") || options.has("skip-hooks");
		confirmHooks = options.has("c") || options.has("confirm-hooks");

		if (Strings.isEmpty(command)) {
			throw new RuntimeException("Missing command");
		}

		if (skipHooks && confirmHooks) {
			throw new RuntimeException("Invalid combination of options -s and -c");
		}
	}

	public static Parameters parse(String[] args) {
		try {
			return new Parameters(args);

		} catch (Exception e) {
			return null;
		}
	}

	public String getCommand() {
		return command;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public String getArgument(int index) {
		return arguments.get(index);
	}

	public int countArguments() {
		return arguments == null ? 0 : arguments.size();
	}

	public boolean isHelp() {
		return help;
	}

	public boolean isSkipHooks() {
		return skipHooks;
	}

	public boolean isConfirmHooks() {
		return confirmHooks;
	}

	private static String parseCommand(OptionSet options) {
		Check.notNull(options);

		List<?> nonOptions = options.nonOptionArguments();
		if (Lists.isEmpty(nonOptions)) {
			return null;
		}

		return nonOptions.get(0).toString().toLowerCase();
	}

	private static List<String> parseArguments(OptionSet options) {
		Check.notNull(options);

		List<?> nonOptions = options.nonOptionArguments();
		if (nonOptions == null || nonOptions.size() <= 1) {
			return null;
		}

		return nonOptions.stream()
				.skip(1)
				.map(object -> object.toString())
				.collect(Collectors.toList());
	}
}
