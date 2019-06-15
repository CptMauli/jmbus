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
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.MBusSap;

public class ScanForMeters {

	private static void printUsage() {
		System.out.println("SYNOPSIS\n\torg.openmuc.jmbus.app.ScanForMeters <serial_port> [<baud_rate>]");
		System.out
				.println("DESCRIPTION\n\tScans the primary addresses p1 to p250 for connected meters by sending REQ_UD2 packets and waiting for a response.");
		System.out.println("OPTIONS");
		System.out
				.println("\t<serial_port>\n\t    The serial port used for communication. Examples are /dev/ttyS0 (Linux) or COM1 (Windows)\n");
		System.out.println("\t<baud_rate>\n\t    The baud rate used to connect to the meter. Default is 2400.\n");
	}

	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			printUsage();
			System.exit(1);
		}

		String serialPortName = args[0];

		int baudRate = 2400;
		if (args.length == 2) {
			try {
				baudRate = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				System.out.println("Error, the <baud_rate> parameter is not an integer value.");
				return;
			}
		}

		MBusSap mBusSap = new MBusSap(serialPortName);
		try {
			mBusSap.open(baudRate);
		} catch (IOException e2) {
			System.out.println("Failed to open serial port: " + e2.getMessage());
			return;
		}

		mBusSap.setTimeout(1000);

		System.out.print("Scanning address: ");
		try {
			for (int i = 1; i <= 250; i++) {

				System.out.print(i + ",");
				String primaryAddress = "p" + i;
				try {
					mBusSap.read(primaryAddress);
				} catch (TimeoutException e) {
					continue;
				} catch (IOException e) {
					System.out.println();
					System.out.println("Error reading meter p" + i + ": " + e.getMessage());
					System.out.print("Scanning address: ");
					continue;
				}
				System.out.println();
				System.out.println("Found device at primary address p" + i + ".");
				System.out.print("Scanning address: ");
			}
		} finally {
			mBusSap.close();
		}
		System.out.println();
		System.out.println("Scan finished.");

	}

}
