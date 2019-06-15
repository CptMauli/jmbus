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
package org.openmuc.jmbus.app;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.MBusSap;
import org.openmuc.jmbus.VariableDataStructure;

/**
 * 
 * @author Stefan Feuerhahn
 *
 */
public class ReadMeter {

	private static void printUsage() {
		System.out
				.println("SYNOPSIS\n\torg.openmuc.jmbus.app.ReadMeter <serial_port> (<primary_address> | <secondary_address>) [<baud_rate>]");
		System.out
				.println("DESCRIPTION\n\tReads the given meter connected to the given serial port and prints the received data to stdout. Errors are printed to stderr.");
		System.out.println("OPTIONS");
		System.out
				.println("\t<serial_port>\n\t    The serial port used for communication. Examples are /dev/ttyS0 (Linux) or COM1 (Windows)\n");
		System.out
				.println("\t<primary_address>\n\t    The primary address of the meter. Primary addresses range from 0 to 255. Regular primary address range from 1 to 250.\n");
		System.out
				.println("\t<secondary_address>\n\t    The secondary address of the meter. Secondary addresses are 8 bytes long and shall be entered in hexadecimal form (e.g. 3a453b4f4f343423)\n");
		System.out.println("\t<baud_rate>\n\t    The baud rate used to connect to the meter. Default is 2400.\n");
	}

	public static void main(String[] args) {
		if (args.length < 2 || args.length > 3) {
			printUsage();
			System.exit(1);
		}

		String serialPortName = args[0];
		int primaryAddress = 0;

		try {
			primaryAddress = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Error, the <primary_address> parameter is not an integer value.");
			System.exit(1);
		}

		int baudRate = 2400;
		if (args.length == 3) {
			try {
				baudRate = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.err.println("Error, the <baud_rate> parameter is not an integer value.");
				System.exit(1);
			}
		}

		MBusSap mBusSap = new MBusSap(serialPortName, baudRate);
		try {
			mBusSap.open();
		} catch (IOException e2) {
			System.err.println("Failed to open serial port: " + e2.getMessage());
			System.exit(1);
		}

		VariableDataStructure variableDataStructure = null;
		try {
			variableDataStructure = mBusSap.read(primaryAddress);
		} catch (IOException e) {
			System.err.println("Error reading meter: " + e.getMessage());
			mBusSap.close();
			System.exit(1);
		} catch (TimeoutException e) {
			System.err.print("Read attempt timed out");
			mBusSap.close();
			System.exit(1);
		}

		try {
			variableDataStructure.decodeDeep();
		} catch (DecodingException e) {
			e.printStackTrace();
			System.out.println("Unable to fully decode received message: " + e.getMessage());
		}

		System.out.println(variableDataStructure.toString());
		System.out.println();

		mBusSap.close();

	}

}
