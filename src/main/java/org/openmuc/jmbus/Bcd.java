/*
 * Copyright 2010-14 Fraunhofer ISE
 *
 * This file is part of jMBus.
 * For more information visit http://www.openmuc.org
 *
 * jMBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
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
package org.openmuc.jmbus;

/**
 * Encodes and decodes BCD encoded numbers.
 * 
 */
public final class Bcd {

	private byte[] value;

	public Bcd(int integer) {
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

			value[i] = (byte) (value[i] | (lowest << 4));
		}
	}

	public Bcd(byte[] bcd) {
		value = bcd;
	}

	public byte[] getBytes() {
		return value;
	}

	@Override
	public String toString() {
		byte[] ba;
		int shift;
		ba = new byte[value.length * 2];
		int c = 0;

		for (int i = value.length - 1; i >= 0; i--) {
			shift = value[i] >> 4;
			ba[c++] = (byte) ((shift & 0x0f) + 48);

			shift = value[i];
			ba[c++] = (byte) ((shift & 0x0f) + 48);
		}

		return new String(ba);
	}

	public int toInteger() {
		int result = 0;
		int factor = 1;

		for (byte element : value) {
			result += (element & 0x0f) * factor;
			factor = factor * 10;
			result += ((element >> 4) & 0x0f) * factor;
			factor = factor * 10;
		}

		return result;
	}

	public long toLong() {

		long result = 0l;
		long factor = 1l;

		for (byte element : value) {
			result += (element & 0x0f) * factor;
			factor = factor * 10l;
			result += ((element >> 4) & 0x0f) * factor;
			factor = factor * 10l;
		}

		return result;
	}

}
