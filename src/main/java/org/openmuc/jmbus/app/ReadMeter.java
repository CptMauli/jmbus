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
package org.openmuc.jmbus.app;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.Bcd;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.MBusSap;
import org.openmuc.jmbus.VariableDataBlock;
import org.openmuc.jmbus.VariableDataResponse;

public class ReadMeter {

	private static void printUsage() {
		System.out.println("SYNOPSIS\n\torg.openmuc.jmbus.app.ReadMeter <serial_port> <meter_address> [<baud_rate>]");
		System.out
				.println("DESCRIPTION\n\tReads the given meter connected to the given serial port and prints the received data to stdout. Errors are printed to stderr.");
		System.out.println("OPTIONS");
		System.out
				.println("\t<serial_port>\n\t    The serial port used for communication. Examples are /dev/ttyS0 (Linux) or COM1 (Windows)\n");
		System.out
				.println("\t<meter_address>\n\t    The address of the meter. Primary addresses have the form p1 ... p255. Secondary addresses have the form s00000000 ... s99999999\n");
		System.out.println("\t<baud_rate>\n\t    The baud rate used to connect to the meter. Default is 2400.\n");
	}

	public static void main(String[] args) {
		if (args.length < 2 || args.length > 3) {
			printUsage();
			System.exit(1);
		}

		String serialPortName = args[0];
		String address = args[1];

		int baudRate = 2400;
		if (args.length == 3) {
			try {
				baudRate = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.err.println("Error, the <baud_rate> parameter is not an integer value.");
				System.exit(1);
			}
		}

		MBusSap mBusSap = new MBusSap(serialPortName);
		try {
			mBusSap.open(baudRate);
		} catch (IOException e2) {
			System.err.println("Failed to open serial port: " + e2.getMessage());
			System.exit(1);
		}

		VariableDataResponse response = null;
		try {
			response = mBusSap.read(address);
		} catch (IOException e) {
			System.err.println("Error reading meter: " + e.getMessage());
			mBusSap.close();
			System.exit(1);
		} catch (TimeoutException e) {
			System.err.print("Read attempt timed out");
			mBusSap.close();
			System.exit(1);
		}

		System.out.println("MeterID: " + response.getId() + ", ManufacturerID: " + response.getManufacturerId()
				+ ", Version: " + response.getVersion() + ", Device Type: " + response.getDeviceType() + ", Status: "
				+ response.getStatus());

		List<VariableDataBlock> blocks = response.getVariableDataBlocks();

		for (VariableDataBlock dataBlock : blocks) {

			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("DIB:").append(composeHexStringFromByteArray(dataBlock.getDIB()));
			stringBuilder.append(", VIB:").append(composeHexStringFromByteArray(dataBlock.getVIB()));

			try {
				dataBlock.decode();
			} catch (DecodingException e) {
				stringBuilder.append(", failed to decode DIB/VIB: ");
				stringBuilder.append(e.getMessage());
				System.out.println(stringBuilder.toString());
				continue;
			}

			stringBuilder.append(", descr:").append(dataBlock.getDescription());
			stringBuilder.append(", function:").append(dataBlock.getFunctionField());

			if (dataBlock.getStorageNumber() > 0) {
				stringBuilder.append(", storage:").append(dataBlock.getStorageNumber());
			}

			if (dataBlock.getTariff() > 0) {
				stringBuilder.append(", tariff:").append(dataBlock.getTariff());
			}

			if (dataBlock.getSubunit() > 0) {
				stringBuilder.append(", subunit:").append(dataBlock.getSubunit());
			}

			switch (dataBlock.getDataValueType()) {
			case DATE:
				stringBuilder.append(", value:").append(((Date) dataBlock.getDataValue()).toString());
				break;
			case STRING:
				stringBuilder.append(", value:").append((String) dataBlock.getDataValue());
				break;
			case DOUBLE:
				stringBuilder.append(", scaled value:").append(dataBlock.getScaledDataValue());
				break;
			case LONG:
				if (dataBlock.getMultiplierExponent() == 0) {
					stringBuilder.append(", value:").append(dataBlock.getDataValue());
				}
				else {
					stringBuilder.append(", scaled value:").append(dataBlock.getScaledDataValue());
				}
				break;
			case BCD:
				if (dataBlock.getMultiplierExponent() == 0) {
					stringBuilder.append(", value:").append(((Bcd) dataBlock.getDataValue()).toString());
				}
				else {
					stringBuilder.append(", scaled value:").append(dataBlock.getScaledDataValue());
				}
				break;
			}

			if (dataBlock.getUnit() != null) {
				stringBuilder.append(", unit:").append(dataBlock.getUnit());
			}

			System.out.println(stringBuilder.toString());
		}

		if (response.getManufacturerData() != null) {
			System.out.println("Manufacturer specific data follows:");
			int j = 0;
			for (byte element : response.manufacturerData) {
				System.out.printf("0x%02x ", element);
				j++;
				if (j % 10 == 0) {
					System.out.println();
				}
			}
		}

		System.out.println();
		mBusSap.close();

	}

	private static String composeHexStringFromByteArray(byte[] data) {
		StringBuilder builder = new StringBuilder(data.length * 2);

		for (byte element : data) {
			builder.append(String.format("%02x", 0xff & element));
		}

		return builder.toString();
	}

}
