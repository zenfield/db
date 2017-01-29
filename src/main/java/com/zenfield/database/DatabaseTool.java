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
package com.zenfield.database;

import com.zenfield.database.configuration.Environment;
import com.zenfield.database.configuration.Credentials;
import com.zenfield.database.configuration.ProjectConfiguration;
import com.zenfield.core.Check;
import com.zenfield.core.Exceptions;
import com.zenfield.core.Strings;
import com.zenfield.database.command.Command;
import com.zenfield.database.command.CreateCommand;
import com.zenfield.database.command.ClearCommand;
import com.zenfield.database.command.DumpCommand;
import com.zenfield.database.command.FetchCommand;
import com.zenfield.database.command.PopulateCommand;
import com.zenfield.database.command.InfoCommand;
import com.zenfield.database.command.StoreCommand;
import com.zenfield.database.configuration.Parameters;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class DatabaseTool {

	private static final String HELP
			= "Usage: db [options] <command> [parameter]\n"
			+ "\n"
			+ "Commands:\n"
			+ "\n"
			+ "  info <environment>\n"
			+ "  - shows a summary about the database\n"
			+ "\n"
			+ "  dump <environment>\n"
			+ "  - dumps the database\n"
			+ "\n"
			+ "  clear <environment>\n"
			+ "  - clears the database\n"
			+ "\n"
			+ "  create <environment>\n"
			+ "  - clears and creates the database based on the create-all.sql script\n"
			+ "\n"
			+ "  populate <directory> <environment>\n"
			+ "  - clears, creates and populates the database\n"
			+ "  - the command lists all files from the directory in order\n"
			+ "  - *.sql files will be directly executed\n"
			+ "  - the output of the executable files will be also be executed\n"
			+ "\n"
			+ "  fetch <environment>\n"
			+ "  - clears the database\n"
			+ "  - fetches data from a remote database\n"
			+ "  - dumps the data into the local database\n"
			+ "\n"
			+ "  store <directory>\n"
			+ "  - stores data from the local database in a directory per table\n"
			+ "  - tables with corresponding files names as e.g. ACCOUNT.skip will be skipped\n"
			+ "\n"
			+ "Options:\n"
			+ "\n"
			+ "  --help\n"
			+ "    show this help\n"
			+ "\n"
			+ "  -c or --confirm-hooks\n"
			+ "    ask for confirmation before running a hook\n"
			+ "\n"
			+ "  -s or --skip-hooks\n"
			+ "    do not run any hooks\n"
			+ "\n";

	private final ProjectConfiguration configuration;
	private final Project project;
	private final Environment environment;
	private final Parameters parameters;

	public DatabaseTool(ProjectConfiguration configuration, Project project, Environment environment, Parameters parameters) {
		Check.notNull(configuration);
		Check.notNull(project);
		Check.notNull(environment);
		Check.notNull(parameters);

		this.configuration = configuration;
		this.project = project;
		this.environment = environment;
		this.parameters = parameters;
	}

	private void run() {
		Command command = createCommand();
		if (command == null) {
			System.exit(1);
		}

		Environment destination = command.getDestination();
		if (destination == null) {
			destination = environment;
		}

		System.err.println("Project:     " + configuration.getName());
		System.err.println("Environment: " + destination.getName());

		System.err.print("Destination: ");
		System.err.print(destination.getUsername() + "@");
		System.err.print(destination.getHostname() + "/");
		System.err.print(destination.getDatabase());
		System.err.print(" (" + configuration.getDialect().getName() + ")");
		System.err.println();
		System.err.println();

		boolean success = command.run(environment);
		System.exit(success ? 0 : 1);
	}

	public static void main(String[] args) {
		try {
			String env = System.getenv("ENVIRONMENT");
			if (Strings.isEmpty(env)) {
				System.err.println("ENVIRONMENT is not set");
				System.exit(1);
			}

			ProjectConfiguration configuration = ProjectConfiguration.load();

			if (configuration == null) {
				System.exit(1);
			}

			Parameters parameters = Parameters.parse(args);
			if (parameters == null || parameters.isHelp()) {
				System.err.println(HELP);
				System.exit(1);
			}

			Credentials credentials = Credentials.load();
			if (credentials == null) {
				System.exit(1);
			}

			Project project = credentials.getProject(configuration.getName());
			if (project == null) {
				System.err.println("Credentials not found for project: " + configuration.getName());
				System.exit(1);
			}

			Environment environment = project.getEnvironment(env);
			if (environment == null) {
				System.err.println("Credentials not found for environment: " + env);
				System.exit(1);
			}

			new DatabaseTool(configuration, project, environment, parameters).run();

		} catch (Exception e) {
			Exceptions.print(e, System.err);
			System.exit(1);
		}
	}

	private Command createCommand() {
		switch (parameters.getCommand()) {
			case "info":
				switch (parameters.countArguments()) {
					case 0:
						return new InfoCommand(configuration, parameters, environment);

					case 1:
						Environment remote = findEnvironment(parameters.getArgument(0), project);
						if (remote == null) {
							return null;
						}

						return new InfoCommand(configuration, parameters, remote);

					default:
						System.err.println(HELP);
						return null;
				}

			case "dump":
				switch (parameters.countArguments()) {
					case 0:
						return new DumpCommand(configuration, parameters, environment);

					case 1:
						Environment remote = findEnvironment(parameters.getArgument(0), project);
						if (remote == null) {
							return null;
						}

						return new DumpCommand(configuration, parameters, remote);

					default:
						System.err.println(HELP);
						return null;
				}

			case "clear":
				switch (parameters.countArguments()) {
					case 0:
						return new ClearCommand(configuration, parameters, environment);

					case 1:
						Environment remote = findEnvironment(parameters.getArgument(0), project);
						if (remote == null) {
							return null;
						}

						return new ClearCommand(configuration, parameters, remote);

					default:
						System.err.println(HELP);
						return null;
				}

			case "create":
				switch (parameters.countArguments()) {
					case 0:
						return new CreateCommand(configuration, parameters, environment);

					case 1:
						Environment remote = findEnvironment(parameters.getArgument(0), project);
						if (remote == null) {
							return null;
						}

						return new CreateCommand(configuration, parameters, remote);

					default:
						System.err.println(HELP);
						return null;
				}

			case "populate":
				switch (parameters.countArguments()) {
					case 1:
						return new PopulateCommand(configuration, parameters, parameters.getArgument(0), environment);

					case 2:
						Environment remote = findEnvironment(parameters.getArgument(1), project);
						if (remote == null) {
							return null;
						}

						return new PopulateCommand(configuration, parameters, parameters.getArgument(0), remote);

					default:
						System.err.println(HELP);
						return null;
				}

			case "fetch":
				if (parameters.countArguments() != 1) {
					System.err.println("Error: invalid arguments");
					System.err.println();
					System.err.println(HELP);
					return null;
				}

				Environment source = findEnvironment(parameters.getArgument(0), project);
				if (source == null) {
					return null;
				}

				return new FetchCommand(configuration, parameters, source);

			case "store":
				if (parameters.countArguments() != 1) {
					System.err.println("Error: invalid arguments");
					System.err.println();
					System.err.println(HELP);
					return null;
				}

				return new StoreCommand(configuration, parameters, parameters.getArgument(0));

			default:
				System.err.println("Error: unknown command: " + parameters.getCommand());
				System.err.println();
				System.err.println(HELP);
				return null;
		}
	}

	private static Environment findEnvironment(String name, Project project) {
		// name
		Check.notNull(project);

		if (Strings.isEmpty(name)) {
			System.err.println("Error: invalid arguments");
			System.err.println();
			System.err.println(HELP);
			return null;
		}

		Environment environment = project.getEnvironment(name);
		if (environment != null) {
			return environment;
		}

		System.err.println("Error: unknown environment: " + name);
		System.err.println();
		System.err.println("Environments:");
		project.environmentNames().sorted().forEach((spot) -> {
			System.err.println("- " + spot);
		});
		return null;
	}
}
