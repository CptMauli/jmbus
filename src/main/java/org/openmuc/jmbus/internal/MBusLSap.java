/*
 * Copyright 2010-13 Fraunhofer ISE
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

package org.openmuc.jmbus.internal;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeoutException;

/**
 * M-Bus Link Layer Service Access Point
 * 
 */
public class MBusLSap {

	private SerialPort serialPort;

	private final byte[] outputBuffer = new byte[1000];
	private final byte[] inputBuffer = new byte[1000];
	private DataOutputStream os;
	private DataInputStream inStream;

	public boolean selectComponent(int id, short manuf, byte version, byte medium, int timeout)
			throws TimeoutException, IOException {
		ByteBuffer bf = ByteBuffer.allocate(8);
		byte[] ba = new byte[8];
		MBusLPdu msg;

		bf.order(ByteOrder.LITTLE_ENDIAN);

		bf.putInt(id);
		bf.putShort(manuf);
		bf.put(version);
		bf.put(medium);

		bf.position(0);
		bf.get(ba, 0, 8);

		// send select
		sendLongMessage(0xfd, 0x53, 0x52, 8, ba);

		msg = receiveMessage(timeout);
		if (msg != null) {
			msg.parse();
			if (msg.msgType == MBusLPdu.MSG_TYPE_SIMPLE_CHAR) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @throws TimeoutException
	 * @throws IOException
	 */
	public MBusLPdu receiveMessage(int timeout) throws TimeoutException, IOException {

		int timeval = 0;
		int readBytes = 0;
		int i;
		int messageLength = -1;

		while ((timeout == 0 || timeval < timeout) && readBytes != messageLength) {
			if (inStream.available() > 0) {
				timeval = 0;
				int read;

				read = inStream.read(inputBuffer, readBytes, 300 - readBytes);
				readBytes += read;

				if (messageLength == -1 && readBytes > 0) {

					if ((inputBuffer[0] & 0xff) == 0xe5) {
						messageLength = 1;
					}
					else if ((inputBuffer[0] & 0xff) == 0x68 && readBytes > 1) {
						messageLength = (inputBuffer[1] & 0xff) + 6;
					}
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			timeval += 100;

		}

		if (readBytes != messageLength) {
			throw new TimeoutException("Timeout listening for response message.");
		}

		byte[] lpdu = new byte[messageLength];
		for (i = 0; i < messageLength; i++) {
			lpdu[i] = inputBuffer[i];
		}

		MBusLPdu rcvdMessage = new MBusLPdu(lpdu);
		rcvdMessage.parse();

		return rcvdMessage;
	} /* receiveMessage() */

	public void sendShortMessage(int slaveAddr, int cmd) throws IOException {
		outputBuffer[0] = 0x10;
		outputBuffer[1] = (byte) (cmd);
		outputBuffer[2] = (byte) (slaveAddr);
		outputBuffer[3] = (byte) (cmd + slaveAddr);
		outputBuffer[4] = 0x16;
		os.write(outputBuffer, 0, 5);
	}

	public boolean sendLongMessage(int slaveAddr, int cmd, int ci, int length, byte[] data) {
		int i, j;
		int checksum = 0;

		outputBuffer[0] = 0x68;
		outputBuffer[1] = (byte) (length + 3);
		outputBuffer[2] = (byte) (length + 3);
		outputBuffer[3] = 0x68;
		outputBuffer[4] = (byte) cmd;
		outputBuffer[5] = (byte) slaveAddr;
		outputBuffer[6] = (byte) ci;

		for (i = 0; i < length; i++) {
			outputBuffer[7 + i] = data[i];
		}

		for (j = 4; j < (i + 7); j++) {
			checksum += outputBuffer[j];
		}

		outputBuffer[i + 7] = (byte) (checksum & 0xff);

		outputBuffer[i + 8] = 0x16;

		try {
			os.write(outputBuffer, 0, i + 9);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public void configureSerialPort(String portName, String serialPortParams) throws IOException,
			IllegalArgumentException {

		// default is 2400/8E1
		int baudrate = 2400;
		int databits = 8;
		int stopbits = 1;
		int parity = SerialPort.PARITY_EVEN;

		if (!serialPortParams.isEmpty()) {
			try {
				String[] splitConfig = serialPortParams.split("/");

				baudrate = Integer.parseInt(splitConfig[0]);

				databits = Character.digit(splitConfig[1].charAt(0), 10);

				if (databits > 8 || databits < 5) {
					throw new IllegalArgumentException();
				}

				stopbits = Character.digit(splitConfig[1].charAt(2), 10);

				if (stopbits < 1 || stopbits > 3) {
					throw new IllegalArgumentException();
				}

				char parityChar = splitConfig[1].charAt(1);

				switch (parityChar) {
				case 'N':
					parity = SerialPort.PARITY_NONE;
					break;
				case 'E':
					parity = SerialPort.PARITY_EVEN;
					break;
				default:
					throw new IllegalArgumentException();
				}
			} catch (Exception e) {
				throw new IllegalArgumentException();
			}
		}

		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e) {
			throw new IOException("Serial port with given name does not exist", e);
		}

		if (portIdentifier.isCurrentlyOwned()) {
			throw new IOException("Serial port is currently in use.");
		}

		CommPort commPort;
		try {
			commPort = portIdentifier.open(this.getClass().getName(), 2000);
		} catch (PortInUseException e) {
			throw new IOException("Serial port is currently in use.", e);
		}

		if (!(commPort instanceof SerialPort)) {
			commPort.close();
			throw new IOException("The specified CommPort is no serial port");
		}

		serialPort = (SerialPort) commPort;

		try {
			serialPort.setSerialPortParams(baudrate, databits, stopbits, parity);
		} catch (UnsupportedCommOperationException e) {
			serialPort.close();
			throw new IOException("Unable to set the given serial comm parameters");
		}

		try {
			os = new DataOutputStream(serialPort.getOutputStream());
			inStream = new DataInputStream(serialPort.getInputStream());
		} catch (IOException e) {
			serialPort.close();
			throw new IOException("Error getting input or output stream from serial port", e);
		}

	}

	/* This operation belongs to application layer !!! */
	// TODO: revise:
	// public boolean masterToSlaveDataSend(int slaveAddr, byte[] data) throws TimeoutException, IOException {
	// MBusLPdu msg;
	//
	// sendLongMessage(slaveAddr, 0x53, 0x51, data.length, data);
	//
	// msg = receiveMessage(timeout);
	//
	// if (msg != null)
	// return true;
	// else
	// return false;
	// return true
	// }

	public void closeSerialPort() {
		if (serialPort != null) {
			serialPort.close();
		}
	}

}
