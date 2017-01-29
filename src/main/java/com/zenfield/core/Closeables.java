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
package com.zenfield.core;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public final class Closeables {

	private Closeables() {
	}

	public static void close(Closeable closeable) {
		if (closeable == null) {
			return;
		}

		try {
			closeable.close();

		} catch (IOException e) {
			// it's ok
		}
	}

	public static void close(Connection connection) {
		if (connection == null) {
			return;
		}

		try {
			connection.close();

		} catch (SQLException e) {
			// it's ok
		}
	}

	public static void close(Statement statement) {
		if (statement == null) {
			return;
		}

		try {
			statement.close();

		} catch (SQLException e) {
			// it's ok
		}
	}

	public static void close(ResultSet resultSet) {
		if (resultSet == null) {
			return;
		}

		try {
			resultSet.close();

		} catch (SQLException e) {
			// it's ok
		}
	}
}
