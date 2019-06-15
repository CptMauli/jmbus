package org.openmuc.jmbus;

/*
 * Copyright Fraunhofer ISE, 2010
 * Author(s): Michael Zillgith
 *            Stefan Feuerhahn
 *    
 * This file is part of jMBus.
 * For more information visit http://www.openmuc.org
 * 
 * jMBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jMBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with jMBus.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/**
 * Encodes and decodes BCD encoded numbers.
 * 
 * This class subclasses the java.lang.Number class. Therefore it can be used to
 * convert BCD numbers to other Number subclasses in the java.lang package.
 */
public class BCD extends java.lang.Number {

	private static final long serialVersionUID = 1L;
	private byte[] value;

	public BCD(String str) {
		int i;

		value = new byte[str.length() / 2];

		for (i = 0; i < str.length() / 2; i++) {

		}
	}

	public BCD(int integer) {
		int testint = integer;
		int c = 0;
		int lowest;

		while (testint > 0) {
			testint = testint / 10;
			c++;
		}

		if ((c % 2) != 0) {
			c++;
		}

		value = new byte[c / 2];

		for (int i = 0; i < c / 2; i++) {

			lowest = integer % 10;
			integer = integer / 10;

			value[i] = (byte) lowest;

			lowest = integer % 10;
			integer = integer / 10;

			value[i] = (byte) ((int) (value[i] | (lowest << 4)));
		}
	}

	public BCD(byte[] bcd) {
		value = bcd;
	}

	public byte[] getByteString() {
		return value;
	}

	public String toString() {
		byte[] ba;
		int shift;
		ba = new byte[value.length * 2];
		int c = 0;

		for (int i = value.length - 1; i >= 0; i--) {
			// for (int i = 0; i < value.length; i++) {
			shift = value[i] >> 4;
			ba[c++] = (byte) ((shift & 0x0f) + 48);

			shift = value[i];
			ba[c++] = (byte) ((shift & 0x0f) + 48);
		}

		return new String(ba);
	}

	public int getDigits() {
		return value.length * 2;
	}

	public int toInteger() throws OutOfRangeException {
		int i;
		int resInt = 0;
		int base = 1;
		int add;
		int shift;

		if (value.length > 4)
			throw new OutOfRangeException();

		for (i = 0; i < value.length; i++) {
			shift = value[i];
			add = (shift & 0x0f) * base;
			resInt += add;
			base = base * 10;
			shift = shift >> 4;
			add = (shift & 0x0f) * base;
			resInt += add;
			base = base * 10;
		}

		return resInt;
	}

	public long toLong() throws OutOfRangeException {
		int i;
		long resInt = 0;
		long base = 1;
		long add;
		long shift;

		if (value.length > 9)
			throw new OutOfRangeException();

		for (i = 0; i < value.length; i++) {
			shift = value[i];
			add = (shift & 0x0f) * base;
			resInt += add;
			base = base * 10;
			shift = shift >> 4;
			add = (shift & 0x0f) * base;
			resInt += add;
			base = base * 10;

		}

		return resInt;
	}

	@Override
	public double doubleValue() {
		try {
			return (double) toInteger();
		} catch (Exception e) {
			return (double) 0;
		}
	}

	@Override
	public float floatValue() {
		try {
			return (float) toInteger();
		} catch (Exception e) {
			return (float) 0;
		}
	}

	@Override
	public int intValue() {
		try {
			return toInteger();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public long longValue() {
		try {
			return toLong();
		} catch (Exception e) {
			return 0;
		}
	}
}
