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
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;

public class SimpleMBusSlave extends Thread {

	/**
	 * Variables
	 */
	CommPortIdentifier serialPortId;
	Enumeration enumComm;
	SerialPort serialPort;
	OutputStream outputStream;
	InputStream inputStream;
	Boolean isSerialPortOpen = false;

	int baudrate = 2400;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	static String portName = "/dev/ttyS99";

	static long secTimeout = 10;

	public SimpleMBusSlave() {
		System.out.println("Constructor: RxTx_MBus-Server");
	}

	public static void main(String[] args) {
		System.out.println(args.length);

		secTimeout = Long.MAX_VALUE / 1000;

		switch (args.length) {
		case 0:
			break;
		case 2:
			secTimeout = Long.parseLong(args[1]);
		case 1:
			portName = args[0];
			break;
		default:
			printUsage();
			System.exit(1);
			break;
		}

		SimpleMBusSlave simpleMBusServer = new SimpleMBusSlave();
		simpleMBusServer.start();
		System.out.println("main() finished");
	}

	@Override
	public void run() {
		if (openSerialPort(portName) != true) {
			return;
		}

		try {
			Thread.sleep(1000 * secTimeout);
		} catch (InterruptedException e) {
		}

		closeSerialPort();

	}

	boolean openSerialPort(String portName) {
		Boolean foundPort = false;
		if (isSerialPortOpen != false) {
			System.out.println("Serial port is already open.");
			return false;
		}
		System.out.println("Open Serial port " + portName);
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while (enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}
		if (foundPort != true) {
			System.out.println("Serial port not found: " + portName);
			return false;
		}
		try {
			serialPort = (SerialPort) serialPortId.open("Open and send", 500);
		} catch (PortInUseException e) {
			System.out.println("Port busy already.");
		}

		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("No access to OutputStream");
		}

		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println("No access to InputStream");
		}
		try {
			serialPort.addEventListener(new serialPortEventListener());
		} catch (TooManyListenersException e) {
			System.out.println("TooManyListenersException for Serialport");
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch (UnsupportedCommOperationException e) {
			System.out.println("Can't set paramter");
		}

		isSerialPortOpen = true;
		return true;
	}

	void closeSerialPort() {
		if (isSerialPortOpen == true) {
			System.out.println("Closing serial port");
			serialPort.close();
			isSerialPortOpen = false;
		}
		else {
			System.out.println("Serial port is closed already");
		}
	}

	void readDataFromPort() {
		byte[] requestP1 = { 16, 91, 1, 92, 22 };
		byte[] answerP1 = { 104, 50, 50, 104, 8, 1, 114, 8, 6, 16, 48, 82, 59, 1, 2, 2, 0, 0, 0, 4, 3, -25, 37, 0, 0,
				4, -125, 127, -25, 37, 0, 0, 2, -3, 72, 54, 9, 2, -3, 91, 0, 0, 2, 43, 0, 0, 12, 120, 8, 6, 16, 48, 15,
				63, 0, 0 }; // dummy CRC
		byte[] requestP3 = { 16, 91, 3, 94, 22 };
		byte[] answerP3 = { 104, 50, 50, 104, 8, 3, 114, 8, 6, 16, 48, 82, 59, 1, 2, 2, 0, 0, 0, 4, 3, -25, 37, 0, 0,
				4, -125, 127, -25, 37, 0, 0, 2, -3, 72, 54, 9, 2, -3, 91, 0, 0, 2, 43, 0, 0, 12, 120, 8, 6, 16, 48, 15,
				63, 0, 0 }; // dummy CRC
		byte[] requestP5 = { 16, 91, 5, 96, 22 }; // NZR Einphasen-Stromzähler
		byte[] answerP5 = { 104, 50, 50, 104, 8, 5, 114, 8, 6, 16, 48, 82, 59, 1, 2, 2, 0, 0, 0, 4, 3, -25, 37, 0, 0,
				4, -125, 127, -25, 37, 0, 0, 2, -3, 72, 54, 9, 2, -3, 91, 0, 0, 2, 43, 0, 0, 12, 120, 8, 6, 16, 48, 15,
				63, -79, 22 };
		byte[] requestP7 = { 16, 91, 7, 98, 22 };
		byte[] answerP7 = { 104, 50, 50, 104, 8, 7, 114, 8, 6, 16, 48, 82, 59, 1, 2, 2, 0, 0, 0, 4, 3, -25, 37, 0, 0,
				4, -125, 127, -25, 37, 0, 0, 2, -3, 72, 54, 9, 2, -3, 91, 0, 0, 2, 43, 0, 0, 12, 120, 8, 6, 16, 48, 15,
				63, 0, 0 }; // dummy CRC
		byte[] requestP60 = { 16, 91, 60, (byte) 151, 22 }; // SIEMENS UH50-A60
		byte[] answerP60 = { 0x68, (byte) 0xf8, (byte) 0xf8, 0x68, 0x8, 0x0, 0x72, 0x74, (byte) 0x97, 0x32, 0x67,
				(byte) 0xa7, 0x32, 0x4, 0x4, 0x0, 0x0, 0x0, 0x0, 0x9, 0x74, 0x4, 0x9, 0x70, 0x4, 0x0c, 0x6, 0x44, 0x5,
				0x5, 0x0, 0x0c, 0x14, 0x69, 0x37, 0x32, 0x0, 0x0b, 0x2d, 0x71, 0x0, 0x0, 0x0b, 0x3b, 0x50, 0x13, 0x0,
				0x0a, 0x5b, 0x43, 0x0, 0x0a, 0x5f, 0x39, 0x0, 0x0a, 0x62, 0x46, 0x0, 0x4c, 0x14, 0x0, 0x0, 0x0, 0x0,
				0x4c, 0x6, 0x0, 0x0, 0x0, 0x0, 0x0c, 0x78, 0x74, (byte) 0x97, 0x32, 0x67, (byte) 0x89, 0x10, 0x71,
				0x60, (byte) 0x9b, 0x10, 0x2d, 0x62, 0x5, 0x0, (byte) 0xdb, 0x10, 0x2d, 0x0, 0x0, 0x0, (byte) 0x9b,
				0x10, 0x3b, 0x20, 0x22, 0x0, (byte) 0x9a, 0x10, 0x5b, 0x76, 0x0, (byte) 0x9a, 0x10, 0x5f, 0x66, 0x0,
				0x0c, 0x22, 0x62, 0x32, 0x0, 0x0, 0x3c, 0x22, 0x56, 0x4, 0x0, 0x0, 0x7c, 0x22, 0x0, 0x0, 0x0, 0x0,
				0x42, 0x6c, 0x1, 0x1, (byte) 0x8c, 0x20, 0x6, 0x0, 0x0, 0x0, 0x0, (byte) 0x8c, 0x30, 0x6, 0x0, 0x0,
				0x0, 0x0, (byte) 0x8c, (byte) 0x80, 0x10, 0x6, 0x0, 0x0, 0x0, 0x0, (byte) 0xcc, 0x20, 0x6, 0x0, 0x0,
				0x0, 0x0, (byte) 0xcc, 0x30, 0x6, 0x0, 0x0, 0x0, 0x0, (byte) 0xcc, (byte) 0x80, 0x10, 0x6, 0x0, 0x0,
				0x0, 0x0, (byte) 0x9a, 0x11, 0x5b, 0x69, 0x0, (byte) 0x9a, 0x11, 0x5f, 0x64, 0x0, (byte) 0x9b, 0x11,
				0x3b, 0x20, 0x16, 0x0, (byte) 0x9b, 0x11, 0x2d, 0x62, 0x5, 0x0, (byte) 0xbc, 0x1, 0x22, 0x56, 0x4, 0x0,
				0x0, (byte) 0x8c, 0x1, 0x6, 0x10, 0x62, 0x4, 0x0, (byte) 0x8c, 0x21, 0x6, 0x0, 0x0, 0x0, 0x0,
				(byte) 0x8c, 0x31, 0x6, 0x0, 0x0, 0x0, 0x0, (byte) 0x8c, (byte) 0x81, 0x10, 0x6, 0x0, 0x0, 0x0, 0x0,
				(byte) 0x8c, 0x1, 0x14, 0x44, 0x27, 0x26, 0x0, 0x4, 0x6d, 0x2a, 0x14, (byte) 0xba, 0x17, 0x0f, 0x21,
				0x4, 0x0, 0x10, (byte) 0xa0, (byte) 0xa9, 0x16 };

		byte[] inBuffer = new byte[30];

		try {
			while (inputStream.available() > 0) {
				try {
					// stupid wait for message to be completed
					// TODO: filter and wait for valid messages
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				inputStream.read(inBuffer, 0, inBuffer.length);
				System.out.println("Received: " + new String(Arrays.toString(inBuffer)));

				if (Helper.ArraysEqual(requestP1, inBuffer, requestP1.length)) {
					writeDataToPort(answerP1);
				}
				else if (Helper.ArraysEqual(requestP3, inBuffer, requestP3.length)) {
					writeDataToPort(answerP3);
				}
				else if (Helper.ArraysEqual(requestP5, inBuffer, requestP5.length)) {
					writeDataToPort(answerP5);
				}
				else if (Helper.ArraysEqual(requestP7, inBuffer, requestP7.length)) {
					writeDataToPort(answerP7);
				}
				else if (Helper.ArraysEqual(requestP60, inBuffer, requestP60.length)) {
					writeDataToPort(answerP60);
				}

			}
		} catch (IOException e) {
			System.out.println("Error during read data.");
		}

	}

	class serialPortEventListener implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			System.out.print("New event ... ");
			switch (event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				readDataFromPort();
				break;
			case SerialPortEvent.BI:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.FE:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			case SerialPortEvent.PE:
			case SerialPortEvent.RI:
			default:
			}
		}
	}

	void writeDataToPort(byte[] message) {
		System.out.println("Sending: " + new String(Arrays.toString(message)));
		if (isSerialPortOpen != true) {
			return;
		}
		try {
			outputStream.write(message);
		} catch (IOException e) {
			System.out.println("Error during send.");
		}
	}

	private static void printUsage() {
		System.out.println("SYNOPSIS\n\tSimpleMBusServer [<serial_port>] [<timout_sec>]");
		System.out.println("DESCRIPTION\n\tOpens a serial port and waits for a MBus read requenst on address p5.");
		System.out.println("\tAnswers this request via serial port with a fixed valid MBus message.");
		System.out
				.println("\tThis program can be used to write jUnit testcases communicating with a simulated MBus device.");
		System.out.println("OPTIONS");
		System.out
				.println("\t<serial_port>\n\t    The serial port used for communication. Examples /dev/ttyS0 ; default /dev/ttyS99\n");
		System.out.println("\t<timeout>\n\t    The runtime of the server given in minutes\n");
	}
}

class Helper {
	/**
	 * @param byteArray1
	 *            first byte array
	 * @param byteArray2
	 *            second byte array
	 * @param n
	 *            number of bytes to be compared.
	 * @return true if first n bytes of byteArray1 and byteArray2 are equal
	 */
	static public boolean ArraysEqual(byte[] byteArray1, byte[] byteArray2, int n) {
		assert byteArray1.length >= n && byteArray2.length >= n;

		for (int i = 0; i < n; i++) {
			if (byteArray1[i] != byteArray2[i]) {
				return false;
			}
		}
		return true;
	}
}
