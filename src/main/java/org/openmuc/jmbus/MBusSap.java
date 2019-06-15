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

import org.openmuc.jmbus.internal.MBusLPdu;

/**
 * M-Bus Application Layer Service Access Point
 * 
 */
public final class MBusSap {

	private final String serialPortName;
	private SerialPort serialPort;

	private DataOutputStream os;
	private DataInputStream is;

	private final byte[] outputBuffer = new byte[1000];
	private final byte[] inputBuffer = new byte[1000];

	private int timeout = 5000;

	/**
	 * Creates an M-Bus Service Access Point that is used to read meters.
	 * 
	 * @param serialPort
	 *            examples for serial port identifiers are on Linux "/dev/ttyS0" or "/dev/ttyUSB0" and on Windows "COM1"
	 */
	public MBusSap(String serialPort) {
		serialPortName = serialPort;
	}

	/**
	 * Sets the maximum time in ms to wait for new data from the remote device. A timeout of zero is interpreted as an
	 * infinite timeout.
	 * 
	 * @param timeout
	 *            the maximum time in ms to wait for new data.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Returns the timeout in ms.
	 * 
	 * @return the timeout in ms.
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Opens the serial port. The serial port needs to be opened befor attempting to read a device.
	 * 
	 * @param initialBaudRate
	 * @throws IOException
	 *             if any kind of error occurs opening the serial port.
	 */
	public void open(int initialBaudRate) throws IOException {
		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
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
			throw new IOException("The specified CommPort is not a serial port");
		}

		serialPort = (SerialPort) commPort;

		try {
			serialPort.setSerialPortParams(initialBaudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_EVEN);
		} catch (UnsupportedCommOperationException e) {
			serialPort.close();
			serialPort = null;
			throw new IOException("Unable to set the baud rate or other serial port parameters", e);
		}

		try {
			os = new DataOutputStream(serialPort.getOutputStream());
			is = new DataInputStream(serialPort.getInputStream());
		} catch (IOException e) {
			serialPort.close();
			serialPort = null;
			throw new IOException("Error getting input or output or input stream from serial port", e);
		}

	}

	/**
	 * Closes the serial port.
	 */
	public void close() {
		if (serialPort == null) {
			return;
		}
		serialPort.close();
		serialPort = null;
	}

	/**
	 * Sends a data request (REQ_UD2) to the remote device and returns the Variable Data Response received.
	 * 
	 * @param meterAddress
	 *            e.g. p1 or s...
	 * @return the data response from the meter.
	 * @throws IOException
	 *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
	 *             connection is not closed when an IOException is thrown.
	 * @throws TimeoutException
	 *             if no response at all (not even a single byte) was received from the meter within the timeout span.
	 */
	public VariableDataResponse read(String meterAddress) throws IOException, TimeoutException {

		if (serialPort == null) {
			throw new IllegalStateException("Serial port is not open.");
		}

		if (meterAddress.charAt(0) == 'p') {
			sendShortMessage(getAddress(meterAddress), 0x5b);
		}
		else {
			if (selectComponent(getAddress(meterAddress), (short) 0xffff, (byte) 0xff, (byte) 0xff, timeout)) {
				sendShortMessage(0xfd, 0x5b);
			}
			else {
				// select timeout
				throw new IOException("unbable to select component");
			}
		}

		MBusLPdu lpdu = receiveMessage();

		VariableDataResponse vdr = new VariableDataResponse();
		vdr.address = lpdu.getAField();
		vdr.parse(lpdu.getAPDU());

		return vdr;

	}

	private void sendShortMessage(int slaveAddr, int cmd) throws IOException {
		outputBuffer[0] = 0x10;
		outputBuffer[1] = (byte) (cmd);
		outputBuffer[2] = (byte) (slaveAddr);
		outputBuffer[3] = (byte) (cmd + slaveAddr);
		outputBuffer[4] = 0x16;
		os.write(outputBuffer, 0, 5);
	}

	private boolean sendLongMessage(int slaveAddr, int cmd, int ci, int length, byte[] data) {
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

	private boolean selectComponent(int id, short manuf, byte version, byte medium, int timeout) throws IOException,
			TimeoutException {
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

		msg = receiveMessage();
		if (msg != null) {
			msg.parse();
			if (msg.msgType == MBusLPdu.MSG_TYPE_SIMPLE_CHAR) {
				return true;
			}
		}

		return false;
	}

	private MBusLPdu receiveMessage() throws IOException, TimeoutException {

		int timeval = 0;
		int readBytes = 0;
		int i;
		int messageLength = -1;

		while ((timeout == 0 || timeval < timeout) && readBytes != messageLength) {
			if (is.available() > 0) {

				int numBytesRead = is.read(inputBuffer, readBytes, 300 - readBytes);
				readBytes += numBytesRead;

				if (numBytesRead > 0) {
					timeval = 0;
				}

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

		if (readBytes == 0) {
			throw new TimeoutException();
		}

		if (readBytes != messageLength) {
			throw new IOException("Incomplete response message received.");
		}

		byte[] lpdu = new byte[messageLength];
		for (i = 0; i < messageLength; i++) {
			lpdu[i] = inputBuffer[i];
		}

		MBusLPdu rcvdMessage = new MBusLPdu(lpdu);
		rcvdMessage.parse();

		return rcvdMessage;
	}

	// private void applicationReset(String meterAddr, boolean hasSubCode, byte subCode) throws IOException,
	// TimeoutException {
	// byte[] subCodeArray;
	// int len;
	//
	// if (hasSubCode) {
	// len = 1;
	// subCodeArray = new byte[1];
	// subCodeArray[0] = subCode;
	// }
	// else {
	// len = 0;
	// subCodeArray = null;
	// }
	//
	// if (meterAddr.charAt(0) == 'p') {
	// /* SND_UD Application reset */
	// sendLongMessage(getAddress(meterAddr), 0x53, 50, len, subCodeArray);
	// }
	// else {
	// // TODO change timeout
	// if (selectComponent(getAddress(meterAddr), (short) 0xffff, (byte) 0xff, (byte) 0xff, 2000)) {
	// /* SND_UD Application reset */
	// sendLongMessage(0xfd, 0x53, 50, len, subCodeArray);
	// }
	// else {
	// // select timeout
	// throw new TimeoutException();
	// }
	// }
	//
	// MBusLPdu lpdu = receiveMessage();
	// lpdu.parse();
	//
	// if (lpdu.getType() != MBusLPdu.MSG_TYPE_SIMPLE_CHAR) {
	// throw new IOException("Invalid message received!");
	// }
	// }

	// public void applicationReset(String meterAddr) throws IOException, TimeoutException {
	// applicationReset(meterAddr, false, (byte) 0);
	// }
	//
	// public void applicationReset(String meterAddr, byte subCode) throws IOException, TimeoutException {
	// applicationReset(meterAddr, true, subCode);
	// }

	private static int getAddress(String meterAddr) {

		if (meterAddr != null) {

			if (meterAddr.charAt(0) == 'p') {
				return Integer.valueOf(meterAddr.substring(1)).intValue();
			}
			else {
				return (int) (0x00000000ffffffffl & Long.parseLong(meterAddr.substring(1), 16));
			}
		}
		else {
			return 0;
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

}
