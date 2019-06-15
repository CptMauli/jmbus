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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;

/**
 * Representation of the RESP-UD message. Will be returned by MBusSap.readMeter().
 */
public class VariableDataResponse {

	// Device Types
	public static final byte OTHER = 0, OIL = 1, ELECTRICITY = 2, GAS = 3, HEAT = 4, STEAM = 5, WARM_WATER = 6,
			WATER = 7, HEAT_COST_ALLOCATOR = 8;

	// primary address for wired M-Bus, is not used for wireless M-Bus
	public byte address;

	public byte[] Id; // secondary address for wired M-Bus
	public short manufacturerID;
	public byte version;
	public byte deviceType; // medium

	public byte accessNumber;
	public byte status;

	public List<VariableDataBlock> variableDataBlocks;

	public byte[] manufacturerData;

	VariableDataResponse(byte address, byte[] id, short manufacturerID, byte version, byte deviceType,
			byte accessNumber, byte status) {
		this.address = address;
		Id = id;
		this.manufacturerID = manufacturerID;
		this.version = version;
		this.deviceType = deviceType;
		this.accessNumber = accessNumber;
		this.status = status;
	}

	VariableDataResponse() {
		Id = new byte[4];
		variableDataBlocks = new Vector<VariableDataBlock>();
	}

	public byte getAddress() {
		return address;
	}

	public Bcd getId() {
		return new Bcd(Id);
	}

	public String getManufacturerId() {
		char c, c1, c2;

		c = (char) ((manufacturerID & 0x001f) + 64);

		manufacturerID = (short) (manufacturerID >> 5);
		c1 = (char) ((manufacturerID & 0x001f) + 64);

		manufacturerID = (short) (manufacturerID >> 5);
		c2 = (char) ((manufacturerID & 0x001f) + 64);

		return "" + c2 + c1 + c;
	}

	public byte getVersion() {
		return version;
	}

	public DeviceType getDeviceType() {
		return DeviceType.newDevice(deviceType);
	}

	public byte getAccessNumber() {
		return accessNumber;
	}

	public byte getStatus() {
		return status;
	}

	public List<VariableDataBlock> getVariableDataBlocks() {
		return variableDataBlocks;
	}

	public byte[] getManufacturerData() {
		return manufacturerData;
	}

	private void parseLongHeader(ByteBuffer buf) {

		Id[0] = buf.get();
		Id[1] = buf.get();
		Id[2] = buf.get();
		Id[3] = buf.get();
		manufacturerID = buf.getShort();
		version = buf.get();
		deviceType = buf.get();
		accessNumber = buf.get();
		status = buf.get();

		// signature is ignored
		buf.getShort();
	}

	private void parseVariableDataBlocks(ByteBuffer buf) throws IOException {

		// a maximum of 10 DIFEs are allowed in the spec
		byte[] dib = new byte[11];
		byte[] vib = new byte[11];

		Integer lvar = null;

		int dataField;
		int dibCounter;
		int vibCounter;

		while (buf.position() < buf.limit()) {

			dib[0] = buf.get();

			int difPosition = buf.position();

			if (((dib[0] & 0xef) == 0x0f)) {
				// Manufacturer specific data

				ByteArrayOutputStream tmp = new ByteArrayOutputStream();
				while (buf.position() < buf.limit()) {
					tmp.write(buf.get());
				}
				manufacturerData = tmp.toByteArray();

				return;
			}
			if (dib[0] == 0x2f) {
				// this is a fill byte
				break;
			}

			dataField = dib[0] & 0x0f;

			dibCounter = 1;

			// while extension bit is set
			while ((dib[dibCounter - 1] & 0x80) == 0x80) {
				dib[dibCounter] = buf.get();
				dibCounter++;
			}

			vibCounter = 0;
			do {
				vib[vibCounter] = buf.get();
				vibCounter++;
			} while ((vib[vibCounter - 1] & 0x80) == 0x80);

			int dataLength;

			switch (dataField) {
			case 0x00:
				dataLength = 0;
				break;
			case 0x01:
				dataLength = 1;
				break;
			case 0x02:
				dataLength = 2;
				break;
			case 0x03:
				dataLength = 3;
				break;
			case 0x04:
				dataLength = 4;
				break;
			case 0x06:
				dataLength = 6;
				break;
			case 0x07:
				dataLength = 8;
				break;
			case 0x09:
				dataLength = 1;
				break;
			case 0x0a:
				dataLength = 2;
				break;
			case 0x0b:
				dataLength = 3;
				break;
			case 0x0c:
				dataLength = 4;
				break;
			case 0x0d:
				lvar = 0xff & buf.get();

				if (lvar < 0xc0) {
					dataLength = lvar;
				}
				else if ((lvar >= 0xc0) && (lvar <= 0xc9)) {
					dataLength = 2 * (lvar - 0xc0);
				}
				else if ((lvar >= 0xd0) && (lvar <= 0xd9)) {
					dataLength = 2 * (lvar - 0xd0);
				}
				else if ((lvar >= 0xe0) && (lvar <= 0xef)) {
					dataLength = lvar - 0xe0;
				}
				else if (lvar == 0xf8) {
					dataLength = 4;
				}
				else {
					throw new IOException("Unsupported LVAR Field: " + lvar + " at " + (buf.position() - 1));
				}
				break;
			case 0x0e:
				dataLength = 6;
				break;
			default:
				throw new IOException("Unsupported Data Field: " + dataField + " at " + difPosition);
			}

			byte[] dibParam = new byte[dibCounter];
			byte[] vibParam = new byte[vibCounter];
			byte[] dataParam = new byte[dataLength];

			for (int i = 0; i < dibCounter; i++) {
				dibParam[i] = dib[i];
			}

			for (int i = 0; i < vibCounter; i++) {
				vibParam[i] = vib[i];
			}

			for (int i = 0; i < dataLength; i++) {
				dataParam[i] = buf.get();
			}

			VariableDataBlock vdb = new VariableDataBlock(dibParam, vibParam, dataParam, lvar);

			variableDataBlocks.add(vdb);

		}

	}

	/**
	 * Parses the apdu and stores the result in VariableDataBlock
	 */
	VariableDataResponse parse(ByteBuffer buf) throws IOException {

		try {

			byte ciField = buf.get();

			switch (ciField) {
			case 0x72: /* long header */
				VariableDataResponse vdr = new VariableDataResponse();
				parseLongHeader(buf);
				parseVariableDataBlocks(buf);
				return vdr;
			case 0x7a: /* short header */
				throw new IOException("Decoding short header not implemented.");
			default:
				if ((ciField >= 0xA0) && (ciField <= 0xB7)) {
					throw new IOException("Manufacturer specific CI");
				}
				throw new IOException("Unable to decode message with this CI Field: " + ciField);
			}
		} catch (RuntimeException e) {
			throw new IOException(e);
		}

	}

}
