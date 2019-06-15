/*
 * Copyright 2010-15 Fraunhofer ISE
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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Representation of the data transmitted in RESP-UD (M-Bus) and SND-NR (wM-Bus) messages.
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class VariableDataStructure {

	private final byte[] buffer;
	private final int offset;
	private final int length;
	private final SecondaryAddress linkLayerSecondaryAddress;
	HashMap<String, byte[]> keyMap;

	private SecondaryAddress secondaryAddress;
	private int accessNumber;
	private int status;
	private EncryptionMode encryptionMode;
	private int numberOfEncryptedBlocks;
	private byte[] manufacturerData;
	private byte[] encryptedVariableDataResponse;

	private boolean decoded = false;

	private List<DataRecord> dataRecords;

	public VariableDataStructure(byte[] buffer, int offset, int length, SecondaryAddress linkLayerSecondaryAddress,
			HashMap<String, byte[]> keyMap) throws DecodingException {
		this.buffer = buffer;
		this.offset = offset;
		this.length = length;
		this.linkLayerSecondaryAddress = linkLayerSecondaryAddress;
		this.keyMap = keyMap;
	}

	public void decode() throws DecodingException {
		try {

			int ciField = buffer[offset] & 0xff;

			switch (ciField) {
			case 0x72: /* long header */
				decodeLongHeader(buffer, offset + 1);
				decodeDataRecords(buffer, offset + 13, length - 13);
				break;
			case 0x78: /* no header */
				decodeDataRecords(buffer, offset + 1, length - 1);
			case 0x7a: /* short header */
				decodeShortHeader(buffer, offset + 1);
				if (encryptionMode == EncryptionMode.AES_CBC_IV) {
					encryptedVariableDataResponse = new byte[length - 5];
					System.arraycopy(buffer, offset + 5, encryptedVariableDataResponse, 0, length - 5);

					byte[] key = keyMap
							.get(HexConverter.getShortHexStringFromByteArray(linkLayerSecondaryAddress.asByteArray(),
									0, linkLayerSecondaryAddress.asByteArray().length));
					if (key == null) {
						throw new DecodingException(
								"Unable to decode encrypted payload because no key for the following secondary address was registered: "
										+ linkLayerSecondaryAddress);
					}

					decodeDataRecords(decryptMessage(key), 0, length - 5);
					encryptedVariableDataResponse = null;
				}
				else if (encryptionMode == EncryptionMode.NONE) {
					decodeDataRecords(buffer, offset + 5, length - 5);
				}
				else {
					throw new DecodingException("Unsupported encryption mode used: " + encryptionMode);
				}
				break;
			default:
				if ((ciField >= 0xA0) && (ciField <= 0xB7)) {
					throw new DecodingException("Manufacturer specific CI: "
							+ HexConverter.getHexStringFromByte(ciField));
				}
				throw new DecodingException("Unable to decode message with this CI Field: "
						+ HexConverter.getHexStringFromByte(ciField));
			}
		} catch (Exception e) {
			throw new DecodingException(e);
		}

		decoded = true;
	}

	public void decodeDeep() throws DecodingException {
		decode();
		DecodingException e1 = null;
		for (DataRecord dataRecord : dataRecords) {
			try {
				dataRecord.decode();
			} catch (DecodingException e2) {
				if (e1 == null) {
					e1 = e2;
				}
			}
		}
		if (e1 != null) {
			throw new DecodingException(e1);
		}
	}

	public SecondaryAddress getSecondaryAddress() {
		return secondaryAddress;
	}

	public int getAccessNumber() {
		return accessNumber;
	}

	public EncryptionMode getEncryptionMode() {
		return encryptionMode;
	}

	public byte[] getManufacturerData() {
		return manufacturerData;
	}

	public int getNumberOfEncryptedBlocks() {
		return numberOfEncryptedBlocks;
	}

	public int getStatus() {
		return status;
	}

	public List<DataRecord> getDataRecords() {
		return dataRecords;
	}

	private void decodeLongHeader(byte[] buffer, int offset) {

		secondaryAddress = SecondaryAddress.getFromLongHeader(buffer, offset);

		decodeShortHeader(buffer, offset + 8);

	}

	private void decodeShortHeader(byte[] buffer, int offset) {

		int i = offset;

		accessNumber = buffer[i++] & 0xff;
		status = buffer[i++] & 0xff;
		numberOfEncryptedBlocks = (buffer[i++] & 0xf0) >> 4;
		encryptionMode = EncryptionMode.newEncryptionMode(buffer[i++] & 0x0f);

	}

	private void decodeDataRecords(byte[] buffer, int offset, int length) throws DecodingException {

		ByteBuffer buf;
		buf = ByteBuffer.wrap(buffer, offset, length);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		dataRecords = new ArrayList<DataRecord>();

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
				continue;
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
			case 0x08:
				dataLength = 0;
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
					throw new DecodingException("Unsupported LVAR Field: " + lvar + " at " + (buf.position() - 1));
				}
				break;
			case 0x0e:
				dataLength = 6;
				break;
			default:
				throw new DecodingException("Unsupported Data Field: " + dataField + " at " + difPosition);
			}

			/**
			 * 
			 * VIF equal to "E111 1100", allows user definable VIF´s (in plain ASCII-String) VIF is in following string
			 * and length is given in the first byte
			 * 
			 */
			int lengthOfUserDefinedVIF = 0;
			for (int i = 0; i < vibCounter; i++) {
				if ((vib[0] & 0x7F) == 0x7C) {
					lengthOfUserDefinedVIF = buf.get() & 0xFF;
					break;
				}
			}

			byte[] dibParam = new byte[dibCounter];
			byte[] vibParam = new byte[vibCounter + lengthOfUserDefinedVIF];
			byte[] dataParam = new byte[dataLength];

			for (int i = 0; i < dibCounter; i++) {
				dibParam[i] = dib[i];
			}

			for (int i = 0; i < vibCounter; i++) {
				vibParam[i] = vib[i];
			}

			for (int j = 0; j < lengthOfUserDefinedVIF; j++) {
				vibParam[j + vibCounter] = buf.get();
			}

			for (int i = 0; i < dataLength; i++) {
				dataParam[i] = buf.get();
			}

			DataRecord dataRecord = new DataRecord(dibParam, vibParam, dataParam, lvar);

			dataRecords.add(dataRecord);

		}

	}

	public byte[] decryptMessage(byte[] key) throws DecodingException {

		if (encryptionMode == EncryptionMode.NONE) {
			return encryptedVariableDataResponse;
		}

		if (encryptionMode != EncryptionMode.AES_CBC_IV) {
			throw new DecodingException("Unsupported encryption mode: " + encryptionMode);
		}

		if (key == null) {
			throw new DecodingException("No AES Key found for Device Address!");
		}

		AesCrypt tempcrypter = new AesCrypt(key, createInitializationVector(linkLayerSecondaryAddress));

		if (numberOfEncryptedBlocks * 16 > encryptedVariableDataResponse.length) {
			throw new DecodingException("Number of encrypted exceeds payload size!");
		}

		if (!tempcrypter.decrypt(encryptedVariableDataResponse, numberOfEncryptedBlocks * 16)) {
			throw new DecodingException("Decryption not successful!");
		}

		if (!(tempcrypter.getResult()[0] == 0x2f && tempcrypter.getResult()[1] == 0x2f)) {
			throw new DecodingException("Decryption unsuccessful! Wrong AES Key?");
		}

		System.arraycopy(tempcrypter.getResult(), 0, encryptedVariableDataResponse, 0, numberOfEncryptedBlocks * 16);

		return encryptedVariableDataResponse;
	}

	private byte[] createInitializationVector(SecondaryAddress linkLayerSecondaryAddress) {
		byte[] initializationVector = new byte[16];

		System.arraycopy(linkLayerSecondaryAddress.asByteArray(), 0, initializationVector, 0, 8);

		for (int i = 0; i < 8; i++) {
			initializationVector[8 + i] = (byte) accessNumber;
		}

		return initializationVector;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (!decoded) {
			builder.append("VariableDataResponse has not been decoded. Bytes:\n");
			HexConverter.appendHexStringFromByteArray(builder, buffer, offset, length);
			return builder.toString();
		}
		else {

			if (secondaryAddress != null) {
				builder.append("Secondary address -> ").append(secondaryAddress).append("\n");
			}
			builder.append("Short Header -> Access No.:").append(accessNumber).append(", status: ").append(status)
					.append(", encryption mode: ").append(encryptionMode).append(", number of encrypted blocks: ")
					.append(numberOfEncryptedBlocks);

			if (encryptedVariableDataResponse != null) {
				builder.append("\nencrypted variable data response: "
						+ getByteArrayString(encryptedVariableDataResponse));
			}
			else {
				for (DataRecord dataRecord : dataRecords) {
					builder.append("\n");
					builder.append(dataRecord.toString());
				}
			}
		}
		return builder.toString();

	}

	public static String getByteArrayString(byte[] byteArray) {
		StringBuilder builder = new StringBuilder();
		int l = 1;
		for (byte b : byteArray) {
			if ((l != 1) && ((l - 1) % 8 == 0)) {
				builder.append(' ');
			}
			if ((l != 1) && ((l - 1) % 16 == 0)) {
				builder.append('\n');
			}
			l++;
			builder.append("0x");
			String hexString = Integer.toHexString(b & 0xff);
			if (hexString.length() == 1) {
				builder.append(0);
			}
			builder.append(hexString + " ");
		}
		return builder.toString();
	}
}
