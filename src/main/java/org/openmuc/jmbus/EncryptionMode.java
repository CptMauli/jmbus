/*
 * Copyright 2010-14 Fraunhofer ISE
 *
 * This file is part of jMBus.
 * For more information visit http://www.openmuc.org
 *
 * jMBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jMBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jMBus.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jmbus;

/**
 * 
 * @author Stefan Feuerhahn
 * 
 */
public enum EncryptionMode {
	NONE(0), RESERVED_01(1), DES_CBC(2), DES_CBC_IV(3), RESERVED_04(4), AES_CBC_IV(5), RESERVED_06(6), RESERVED_07(7), RESERVED_08(
			8), RESERVED_09(9), RESERVED_10(10), RESERVED_11(11), RESERVED_12(12), RESERVED_13(13), RESERVED_14(14), RESERVED_15(
			15);
	private final int code;

	EncryptionMode(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static EncryptionMode newEncryptionMode(int code) {
		switch (code) {
		case 0:
			return EncryptionMode.NONE;
		case 1:
			return EncryptionMode.RESERVED_01;
		case 2:
			return EncryptionMode.DES_CBC;
		case 3:
			return EncryptionMode.DES_CBC_IV;
		case 4:
			return EncryptionMode.RESERVED_04;
		case 5:
			return EncryptionMode.AES_CBC_IV;
		case 6:
			return EncryptionMode.RESERVED_06;
		case 7:
			return EncryptionMode.RESERVED_07;
		case 8:
			return EncryptionMode.RESERVED_08;
		case 9:
			return EncryptionMode.RESERVED_09;
		case 10:
			return EncryptionMode.RESERVED_10;
		case 11:
			return EncryptionMode.RESERVED_11;
		case 12:
			return EncryptionMode.RESERVED_12;
		case 13:
			return EncryptionMode.RESERVED_13;
		case 14:
			return EncryptionMode.RESERVED_14;
		case 15:
			return EncryptionMode.RESERVED_15;
		default:
			throw new IllegalArgumentException("invalid encryption mode code: " + code);
		}
	}

}
