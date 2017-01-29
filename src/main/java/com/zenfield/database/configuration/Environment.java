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
import com.zenfield.core.Strings;
import java.util.Map;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class Environment {

	private final String name;
	private final String username;
	private final String password;
	private final String database;
	private final String hostname;
	private final String sshHostname;
	private final String sshUsername;
	private final ReadOnly readOnly;

	private Environment(String name, String username, String password, String database, String hostname, String sshHostname, String sshUsername, ReadOnly readOnly) {
		Check.notEmpty(name);
		Check.notEmpty(username);
		Check.notEmpty(password);
		Check.notEmpty(database);
		Check.notEmpty(hostname);
		// readOnly
		// sshHostname
		// sshUsername

		this.name = name;
		this.username = username;
		this.password = password;
		this.database = database;
		this.hostname = hostname;
		this.sshHostname = sshHostname;
		this.sshUsername = sshUsername;
		this.readOnly = readOnly;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}

	public String getHostname() {
		return hostname;
	}

	public String getSshHostname() {
		return sshHostname;
	}

	public String getSshUsername() {
		return sshUsername;
	}

	public String getSshUsernameHostname() {
		if (Strings.isEmpty(sshHostname)) {
			return null;
		}

		if (Strings.isEmpty(sshUsername)) {
			return sshHostname;
		}

		return sshUsername + '@' + sshHostname;
	}

	public ReadOnly getReadOnly() {
		return readOnly;
	}

	public boolean same(Environment other) {
		return other != null
				&& Strings.isEqualIgnoreCase(database, other.database)
				&& Strings.isEqualIgnoreCase(hostname, other.hostname)
				&& Strings.isEqualIgnoreCase(sshHostname, other.sshHostname);
	}

	public static Environment create(String name, Map<String, String> map) {
		Check.notEmpty(name);
		Check.notNull(map);

		String username = map.get("username");
		if (Strings.isEmpty(username)) {
			System.err.println("Error: username not found for environment: " + name);
			return null;
		}

		String password = map.get("password");
		if (Strings.isEmpty(password)) {
			System.err.println("Error: password not found for environment: " + name);
			return null;
		}

		String database = map.get("database");
		if (Strings.isEmpty(database)) {
			System.err.println("Error: database not found for environment: " + name);
			return null;
		}

		String hostname = map.get("hostname");
		if (Strings.isEmpty(hostname)) {
			System.err.println("Error: hostname not found for environment: " + name);
			return null;
		}

		String sshHostname = map.get("ssh.hostname");
		if ("localhost".equalsIgnoreCase(sshHostname) || "127.0.0.1".equals(sshHostname)) {
			System.err.println("Error: ssh.hostname cannot be set to " + sshHostname);
			return null;
		}

		String sshUsername = map.get("ssh.username");

		String readOnlyValue = map.get("readonly");
		ReadOnly readOnly = ReadOnly.parse(readOnlyValue);

		if (readOnly == null) {
			System.err.println("Error: invalid readonly value: " + readOnlyValue);
			return null;
		}

		return new Environment(name, username, password, database, hostname, sshHostname, sshUsername, readOnly);
	}

	public boolean isSsh() {
		return !Strings.isEmpty(sshHostname);
	}
}
