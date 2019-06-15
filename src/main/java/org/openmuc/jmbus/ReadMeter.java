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

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class ReadMeter {

	private static void printUsage() {
		System.out.println("SYNOPSIS\n\torg.openmuc.jmbus.ReadMeter <serial_port> <meter_address> [<baud_rate>]");
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

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
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

		System.out.println("Vendor ID: " + response.getManufacturerID());

		List<VariableDataBlock> blocks = response.getVariableDataBlocks();

		for (VariableDataBlock dataBlock : blocks) {
			System.out.print("DIB:" + composeHexStringFromByteArray(dataBlock.getDIB()) + ", VIB:"
					+ composeHexStringFromByteArray(dataBlock.getVIB()));

			try {
				dataBlock.parse();
			} catch (ParseException e) {
				System.out.println(", failed to parse: " + e.getMessage());
				continue;
			}
			if (dataBlock.getDescription() != null) {

				System.out.print(", descr:" + dataBlock.getDescription() + ", function:" + dataBlock.getFunctionField()
						+ ", data type:" + dataBlock.getDataType() + ", value:");

				switch (dataBlock.getDataType()) {
				case DATE:
					System.out.println(dataBlock.getData());
				case STRING:
					System.out.println((String) dataBlock.getData());
					break;
				case DOUBLE:
					System.out.println(dataBlock.getData() + ", multiplier:" + dataBlock.getMultiplier()
							+ ", scaled data:" + dataBlock.getScaledValue() + " unit:" + dataBlock.getUnit());
					break;
				case LONG:
					System.out.println(dataBlock.getData() + ", multiplier:" + dataBlock.getMultiplier()
							+ ", scaled data:" + dataBlock.getScaledValue() + " unit:" + dataBlock.getUnit());
					break;

				}
			}

		}

		if (response.hasManufacturerData()) {
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
