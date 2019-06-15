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
 * Represents a wired M-Bus link layer message according to EN 13757-2. The messages are in format class FT 1.2
 * according to IEC 60870-5-2.
 * 
 * If the M-Bus message is of frame type Long Frame it contains user data and it contains the following fields:
 * <ul>
 * <li>Length (1 byte) -</li>
 * <li>Control field (1 byte) -</li>
 * <li>Address field (1 byte) -</li>
 * <li>CI field (1 byte) -</li>
 * <li>The APDU (Variable Data Response) -</li>
 * </ul>
 * 
 */
public class MBusMessage {

	enum FrameType {
		SIMPLE_CHAR, SHORT_FRAME, LONG_FRAME, CONTROL
	};

	byte[] buffer;

	private FrameType frameType;
	private byte controlField;
	private byte addressField;
	private VariableDataStructure vdr;
	private boolean decoded = false;

	public MBusMessage(byte[] buffer) {

		this.buffer = buffer;
	}

	public void decode() throws DecodingException {
		/* Determine message type */
		switch (0xff & buffer[0]) {
		case 0xe5: /* single char message */
			frameType = FrameType.SIMPLE_CHAR;
			break;
		case 0x68: /* long message (variable length frame) */
			int headerLength;

			frameType = FrameType.LONG_FRAME;
			if ((short) (0xff & buffer[3]) != 0x68) {
				throw new DecodingException("Error parsing LPDU");
			}
			headerLength = 0xff & buffer[1];
			if (headerLength != (buffer.length - 6)) {
				throw new DecodingException("Wrong frame length (header says " + headerLength
						+ ") but current length is " + buffer.length + " !");
			}

			if (headerLength != (short) (0xff & buffer[2])) {
				throw new DecodingException("Length fields are not identical in long frame!");
			}

			controlField = (byte) (0xff & buffer[4]);
			addressField = (byte) (0xff & buffer[5]);
			int apduLength = headerLength - 2;
			int apduStart = 6;

			vdr = new VariableDataStructure(buffer, apduStart, apduLength, null, null);
			break;
		case 0x10: /* short message (fixed length frame) */
			frameType = FrameType.SHORT_FRAME;
			break;
		default:
			throw new DecodingException("Error parsing LPDU");
		}
		decoded = true;
	}

	public void decodeDeep() throws DecodingException {
		decode();
		vdr.decodeDeep();
	}

	public int getAddressField() {
		return addressField;
	}

	public int getControlField() {
		return controlField;
	}

	public FrameType getFrameType() {
		return frameType;
	}

	public VariableDataStructure getVariableDataResponse() {
		return vdr;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (!decoded) {
			builder.append("Message has not been decoded. Bytes of this message:\n");
			HexConverter.appendHexStringFromByteArray(builder, buffer, 0, buffer.length);
			return builder.toString();
		}
		else {
			builder.append("control field: ");
			HexConverter.appendHexStringFromByte(controlField & 0xff, builder);
			builder.append("\naddress field: ");
			builder.append(addressField & 0xff);
			builder.append("\nVariable Data Response:\n").append(vdr);
			return builder.toString();
		}
	}

}
