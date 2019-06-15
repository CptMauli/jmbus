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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 
 * Represent and parse a data link layer message. Data link layer according to
 * EN 13757-2 (wired MBUS). Such messages are in format FT 1.2 according to IEC
 * 870-5-2:1992.
 * 
 */

// TODO - Implement checksum test! - Implement short frame parsing

public class MBusLPDU {

	// Message types according to IEC 870-2 FT1.2
	public static final int MSG_TYPE_SIMPLE_CHAR = 1;
	public static final int MSG_TYPE_SHORT_MSG = 2;
	// public static final int MSG_TYPE_CTR_MSG = 3;
	public static final int MSG_TYPE_LONG_MSG = 4;
	public static final int MSG_TYPE_UNKNOWN = 5;

	public int msgType;
	public byte cField;
	public short ciField;
	public byte aField; // mbus wireless uses other format
	public short length;
	public short checksum;

	/* private fields */
	protected byte[] lpdu;
	protected int apduStart;
	protected int apduLength;

	boolean parsed;

	public MBusLPDU(byte[] lpdu) {
		this.lpdu = lpdu;
		parsed = false;
	}

	public ByteBuffer getAPDU() {
		if (!parsed) {
			throw new RuntimeException("MBusLPDU was not parsed.");
		}
		ByteBuffer buf;
		buf = ByteBuffer.wrap(lpdu, apduStart, apduLength);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		return buf;
	}

	public byte getAField() {
		if (!parsed) {
			throw new RuntimeException("MBusLPDU was not parsed.");
		}
		return aField;
	}

	public byte getCField() {
		if (!parsed) {
			throw new RuntimeException("MBusLPDU was not parsed.");
		}
		return cField;
	}

	public int getType() {
		if (!parsed) {
			throw new RuntimeException("MBusLPDU was not parsed.");
		}
		return msgType;
	}

	public void parse() throws IOException {

		parsed = false;

		/* Determine message type */
		switch (0xff & lpdu[0]) {
		case 0xe5: /* single char message */
			this.msgType = MSG_TYPE_SIMPLE_CHAR;
			if (lpdu.length > 1) {
				throw new IOException("Wrong frame length (should be 1 byte)!");
			}
			break;
		case 0x68: /* long message (variable length frame) */
			int headerLength;

			this.msgType = MSG_TYPE_LONG_MSG;
			if ((short) (0xff & (int) lpdu[3]) != 0x68) {
				throw new IOException("Error parsing LPDU");
			}
			headerLength = 0xff & (int) lpdu[1];
			if (headerLength != (lpdu.length - 6)) {
				throw new IOException("Wrong frame length (header says " + headerLength + ") but current length is "
						+ lpdu.length + " !");
			}

			if (headerLength != (short) (0xff & (int) lpdu[2])) {
				throw new IOException("Length fields are not identical in long frame!");
			}

			this.cField = (byte) (0xff & lpdu[4]);
			this.aField = (byte) (0xff & lpdu[5]);
			this.apduLength = headerLength - 2;
			this.apduStart = 6;
			break;
		case 0x10: /* short message (fixed length frame) */
			this.msgType = MSG_TYPE_SHORT_MSG;
			break;
		default:
			this.msgType = MSG_TYPE_UNKNOWN;
			throw new IOException("Error parsing LPDU");
		}

		parsed = true;

	} /* parse() */

	public String toString() {
		return Util.composeHexStringFromByteArray(lpdu);
	}
}
