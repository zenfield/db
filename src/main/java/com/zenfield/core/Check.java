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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author MICSKO Viktor (viktor@zenfield.com)
 */
public class Check {

	private static final float EPS = 10e-10f;

	private Check() {
	}

	public static void that(boolean condition) {
		if (!condition) {
			throw new IllegalArgumentException("Condition failed");
		}
	}

	public static void that(boolean condition, String message) {
		if (!condition) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void notNull(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Must not be null");
		}
	}

	public static void isNull(Object object) {
		if (object != null) {
			throw new IllegalArgumentException("Must be null");
		}
	}

	public static void bothOrNoneNull(Object one, Object two) {
		if (one == null && two != null) {
			throw new IllegalArgumentException("Both or none must be null (one == null)");
		}
		if (one != null && two == null) {
			throw new IllegalArgumentException("Both or none must be null (two == null)");
		}
	}

	public static void exactlyOneNull(Object one, Object two) {
		if (one == null && two == null) {
			throw new IllegalArgumentException("Exactly one must be (neither are)");
		}
		if (one != null && two != null) {
			throw new IllegalArgumentException("Exactly one must be (both are)");
		}
	}

	public static void between(int value, int min, int max) {
		if (value < min || value > max) {
			throw new FormattedIllegalArgumentException("Must be between %d and %d: %d", min, max, value);
		}
	}

	public static void between(long value, long min, long max) {
		if (value < min || value > max) {
			throw new FormattedIllegalArgumentException("Must be between %d and %d: %d", min, max, value);
		}
	}

	public static void between(float value, float min, float max) {
		if (value < min || value > max) {
			throw new FormattedIllegalArgumentException("Must be between %f and %f: %f", min, max, value);
		}
	}

	public static void between(double value, double min, double max) {
		if (value < min || value > max) {
			throw new FormattedIllegalArgumentException("Must be between %f and %f: %f", min, max, value);
		}
	}

	public static void positive(int value) {
		if (value <= 0) {
			throw new FormattedIllegalArgumentException("Must be positive: %d", value);
		}
	}

	public static void positive(long value) {
		if (value <= 0) {
			throw new FormattedIllegalArgumentException("Must be positive: %d", value);
		}
	}

	public static void positive(float value) {
		if (value <= 0) {
			throw new FormattedIllegalArgumentException("Must be positive: %f", value);
		}
	}

	public static void positive(double value) {
		if (value <= 0) {
			throw new FormattedIllegalArgumentException("Must be positive: %f", value);
		}
	}

	public static void positive(BigDecimal value) {
		Check.notNull(value);
		if (value.signum() < 1) {
			throw new FormattedIllegalArgumentException("Must be positive: %d", value);
		}
	}

	public static void nullOrPositive(Integer value) {
		if (value != null && value <= 0) {
			throw new FormattedIllegalArgumentException("Must be null or positive: %d", value);
		}
	}

	public static void nullOrPositive(BigDecimal value) {
		if (value != null && value.signum() < 1) {
			throw new FormattedIllegalArgumentException("Must be null or positive: %d", value);
		}
	}

	public static void notNegative(int value) {
		if (value < 0) {
			throw new FormattedIllegalArgumentException("Must not be negative: %d", value);
		}
	}

	public static void notNegative(long value) {
		if (value < 0) {
			throw new FormattedIllegalArgumentException("Must not be negative: %d", value);
		}
	}

	public static void notNegative(double value) {
		if (value < 0) {
			throw new FormattedIllegalArgumentException("Must not be negative: %f", value);
		}
	}

	public static void notNegative(float value) {
		if (value < 0) {
			throw new FormattedIllegalArgumentException("Must not be negative: %f", value);
		}
	}

	public static void notNegative(BigDecimal value) {
		Check.notNull(value);
		if (value.signum() == -1) {
			throw new FormattedIllegalArgumentException("Must not be negative: %s", value.toString());
		}
	}

	public static void less(int a, int b) {
		if (a >= b) {
			throw new FormattedIllegalArgumentException("%d must be less than %d", a, b);
		}
	}

	public static void greater(int a, int b) {
		if (a <= b) {
			throw new FormattedIllegalArgumentException("%d must be greater than %d", a, b);
		}
	}

	public static void notGreater(int a, int b) {
		if (a > b) {
			throw new FormattedIllegalArgumentException("%d must not be greater than %d", a, b);
		}
	}

	public static void notGreater(long a, long b) {
		if (a > b) {
			throw new FormattedIllegalArgumentException("%d must not be greater than %d", a, b);
		}
	}

	public static void notGreater(float a, float b) {
		if (a > b) {
			throw new FormattedIllegalArgumentException("%f must not be greater than %f", a, b);
		}
	}

	public static void notGreater(double a, double b) {
		if (a > b) {
			throw new FormattedIllegalArgumentException("%f must not be greater than %f", a, b);
		}
	}

	public static void notGreater(BigDecimal a, BigDecimal b) {
		Check.notNull(a);
		Check.notNull(b);

		if (a.compareTo(b) > 0) {
			throw new FormattedIllegalArgumentException("%s must not be greater than %s", a.toString(), b.toString());
		}
	}

	public static void equals(int value, int expected) {
		if (value != expected) {
			throw new FormattedIllegalArgumentException("%d must equal %d", value, expected);
		}
	}

	public static void equals(String one, String two) {
		if (one == null || two == null || !one.equals(two)) {
			throw new FormattedIllegalArgumentException("'%s' and '%s' must equal", one, two);
		}
	}

	public static <T> void notEquals(T one, T other) {
		if (one == null || other == null) {
			return;
		}

		if (one.equals(other)) {
			throw new FormattedIllegalArgumentException("%s must be equal to %s", one, other);
		}
	}

	public static void zero(int value) {
		if (value == 0) {
			throw new FormattedIllegalArgumentException("%d must be zero", value);
		}
	}

	public static void notZero(long value) {
		if (value == 0) {
			throw new FormattedIllegalArgumentException("%d must not be zero", value);
		}
	}

	public static void notZero(int value) {
		if (value == 0) {
			throw new FormattedIllegalArgumentException("%d must not be zero", value);
		}
	}

	public static void notZero(float value) {
		if (Math.abs(value) < EPS) {
			throw new FormattedIllegalArgumentException("%f must not be zero", value);
		}
	}

	public static void notZero(double value) {
		if (Math.abs(value) < EPS) {
			throw new FormattedIllegalArgumentException("%f must not be zero", value);
		}
	}

	public static <T> void notEmpty(T[] array) {
		if (array == null) {
			throw new IllegalArgumentException("This array must not be null");
		}
		if (array.length == 0) {
			throw new IllegalArgumentException("This array must not be empty");
		}
	}

	public static void notEmpty(int[] array) {
		if (array == null) {
			throw new IllegalArgumentException("This array must not be null");
		}
		if (array.length == 0) {
			throw new IllegalArgumentException("This array must not be empty");
		}
	}

	public static void notEmpty(char[] array) {
		if (array == null) {
			throw new IllegalArgumentException("This array must not be null");
		}
		if (array.length == 0) {
			throw new IllegalArgumentException("This array must not be empty");
		}
	}

	public static void notEmpty(String string) {
		if (string == null) {
			throw new IllegalArgumentException("This string must not be null");
		}
		if (string.length() == 0) {
			throw new IllegalArgumentException("This string must not be empty");
		}
	}

	public static void notEmpty(Collection<?> collection) {
		if (collection == null) {
			throw new IllegalArgumentException("This collection must not be null");
		}
		if (collection.isEmpty()) {
			throw new IllegalArgumentException("This collection must not be empty");
		}
	}

	public static void notEmpty(Map<?, ?> map) {
		if (map == null) {
			throw new IllegalArgumentException("This map must not be null");
		}
		if (map.isEmpty()) {
			throw new IllegalArgumentException("This map must not be empty");
		}
	}

	public static <T> void noneNull(T[] array) {
		if (array == null || array.length == 0) {
			return;
		}

		for (T item : array) {
			if (item == null) {
				throw new IllegalArgumentException("This array must contain no null elements");
			}
		}
	}

	public static <T> void noneNull(Collection<T> collection) {
		if (collection == null || collection.isEmpty()) {
			return;
		}

		collection.stream().forEach(item -> {
			if (item == null) {
				throw new IllegalArgumentException("This collection must contain no null elements");
			}
		});
	}

	public static <K, V> void noneNull(Map<K, V> map) {
		if (map == null || map.isEmpty()) {
			return;
		}

		if (map.keySet().stream().anyMatch(key -> key == null)) {
			throw new IllegalArgumentException("This map must contain no null keys");
		}

		if (map.values().stream().anyMatch(value -> value == null)) {
			throw new IllegalArgumentException("This map must contain no null values");
		}
	}

	public static void maxLength(String string, int length) {
		Check.notNegative(length);

		if (string == null) {
			return;
		}

		if (string.length() > length) {
			throw new FormattedIllegalArgumentException("This string must not be longer than %d: '%s'", length, string);
		}
	}

	public static <T> void contains(Collection<T> collection, T item) {
		Check.notNull(collection);

		if (!collection.contains(item)) {
			throw new FormattedIllegalArgumentException("This collection must contain %s", item);
		}
	}

	public static void before(Date one, Date other) {
		Check.notNull(one);
		Check.notNull(other);

		if (!one.before(other)) {
			throw new FormattedIllegalArgumentException("%s must be before %s", one, other);
		}
	}

	public static void notBefore(Date one, Date other) {
		Check.notNull(one);
		Check.notNull(other);

		if (one.before(other)) {
			throw new FormattedIllegalArgumentException("%s must not be before %s", one, other);
		}
	}

	public static void after(Date one, Date other) {
		Check.notNull(one);
		Check.notNull(other);

		if (!one.after(other)) {
			throw new FormattedIllegalArgumentException("%s must be after %s", one, other);
		}
	}

	public static void notAfter(Date one, Date other) {
		Check.notNull(one);
		Check.notNull(other);

		if (one.after(other)) {
			throw new FormattedIllegalArgumentException("%s must be before %s", one, other);
		}
	}

	public static void nullOrInstanceOf(Object object, Class<?> clazz) {
		// object
		Check.notNull(clazz);

		if (object != null && !clazz.isAssignableFrom(clazz)) {
			throw new FormattedIllegalArgumentException("Must be an instance of %s", clazz.getSimpleName());
		}
	}
}
