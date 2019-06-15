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

import java.nio.ByteBuffer;

public class Util {

	public static String vendorID(short id) {
		char c, c1, c2;

		c = (char) ((id & 0x001f) + 64);

		id = (short) (id >> 5);
		c1 = (char) ((id & 0x001f) + 64);

		id = (short) (id >> 5);
		c2 = (char) ((id & 0x001f) + 64);

		return "" + c2 + c1 + c;
	}

	public static byte[] createByteArrayFromString(String hexString) {
		byte[] retValue = null;

		if ((hexString.length() % 2) == 0) {
			String b;
			retValue = new byte[hexString.length() / 2];
			int bi = 0;

			try {
				for (int i = 0; i < hexString.length(); i += 2) {
					b = hexString.substring(i, i + 2);
					retValue[bi] = (byte) Short.valueOf(b, 16).shortValue();
					bi++;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			}
		}

		return retValue;
	} /* createByteArrayFromString() */

	public static String composeHexStringFromByteArray(byte[] data) {
		StringBuilder builder = new StringBuilder(data.length * 2);

		for (int i = 0; i < data.length; i++) {
			builder.append(String.format("%02x", 0xff & (int) data[i]));
		}

		return builder.toString();
	} /* composeHexStringFromByteArray() */

	public static byte[] getByteArray(ByteBuffer buf, int length) {
		byte[] array = new byte[length];

		for (int i = 0; i < length; i++) {
			array[i] = buf.get();
		}

		return array;
	}

	public final static int ADDR_TYPE_UNDEFINED = 0;
	public final static int ADDR_TYPE_PRIMARY = 1;
	public final static int ADDR_TYPE_SECONDARY = 2;

	public static int getAddrType(String meterAddr) {

		if (meterAddr != null) {
			if (meterAddr.charAt(0) == 'p')
				return ADDR_TYPE_PRIMARY;
			else if (meterAddr.charAt(0) == 's')
				return ADDR_TYPE_SECONDARY;
		}

		return ADDR_TYPE_UNDEFINED;
	}

	public static int getAddress(String meterAddr) {

		if (meterAddr != null) {
			int type = getAddrType(meterAddr);

			if (type == ADDR_TYPE_PRIMARY)
				return Integer.valueOf(meterAddr.substring(1)).intValue();
			else if (type == ADDR_TYPE_SECONDARY)
				return (int) (0x00000000ffffffffl & Long.parseLong(meterAddr.substring(1), 16));
			else
				return 0;
		} else
			return 0;
	}

}
