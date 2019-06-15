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

import gnu.io.SerialPort;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.MBusMessage.FrameType;

/**
 * M-Bus Application Layer Service Access Point - Use this access point to communicate using the M-Bus wired protocol.
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class MBusSap {

	private final SerialTransceiver serialTransceiver;

	private final byte[] outputBuffer = new byte[1000];
	private final byte[] inputBuffer = new byte[1000];

	private DataOutputStream os;
	private DataInputStream is;

	private int timeout = 5000;
	private SecondaryAddress secondaryAddress = null;

	/**
	 * Creates an M-Bus Service Access Point that is used to read meters.
	 * 
	 * @param serialPortName
	 *            examples for serial port identifiers are on Linux "/dev/ttyS0" or "/dev/ttyUSB0" and on Windows "COM1"
	 * @param baudRate
	 *            the baud rate to use.
	 */
	public MBusSap(String serialPortName, int baudRate) {
		serialTransceiver = new SerialTransceiver(serialPortName, baudRate, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
	}

	/**
	 * Opens the serial port. The serial port needs to be opened before attempting to read a device.
	 * 
	 * @throws IOException
	 *             if any kind of error occurs opening the serial port.
	 */
	public void open() throws IOException {
		serialTransceiver.open();
		os = serialTransceiver.getOutputStream();
		is = serialTransceiver.getInputStream();
	}

	/**
	 * Closes the serial port.
	 */
	public void close() {
		serialTransceiver.close();
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
	 * Reads a meter using primary addressing. Sends a data request (REQ_UD2) to the remote device and returns the
	 * variable data structure from the received RSP_UD frame.
	 * 
	 * @param primaryAddress
	 *            the primary address of the meter to read. For secondary address use 0xfd.
	 * @return the variable data structure from the received RSP_UD frame
	 * @throws IOException
	 *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
	 *             connection is not closed when an IOException is thrown.
	 * @throws TimeoutException
	 *             if no response at all (not even a single byte) was received from the meter within the timeout span.
	 */
	public VariableDataStructure read(int primaryAddress) throws IOException, TimeoutException {

		if (serialTransceiver.isClosed() == true) {
			throw new IllegalStateException("Serial port is not open.");
		}

		sendShortMessage(primaryAddress, 0x5b);
		MBusMessage mBusMessage = receiveMessage();
		try {
			mBusMessage.decode();
		} catch (DecodingException e) {
			throw new IOException(e);
		}
		return mBusMessage.getVariableDataResponse();

	}

	/**
	 * Selects the meter with the specified secondary address. After this the meter can be read on primary address 0xfd.
	 * 
	 * @param secondaryAddress
	 *            the secondary address of the meter to select.
	 * @throws IOException
	 *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
	 *             connection is not closed when an IOException is thrown.
	 * @throws TimeoutException
	 *             if no response at all (not even a single byte) was received from the meter within the timeout span.
	 */
	public void selectComponent(SecondaryAddress secondaryAddress) throws IOException, TimeoutException {
		this.secondaryAddress = secondaryAddress;
		componentSelection(false);
	}

	/**
	 * Deselects the previously selected meter.
	 * 
	 * @throws IOException
	 *             if any kind of error (including timeout) occurs while trying to read the remote device. Note that the
	 *             connection is not closed when an IOException is thrown.
	 * @throws TimeoutException
	 *             if no response at all (not even a single byte) was received from the meter within the timeout span.
	 */
	public void deselectComponent() throws IOException, TimeoutException {
		if (secondaryAddress != null) {
			componentSelection(true);
			secondaryAddress = null;
		}
	}

	private void componentSelection(boolean deselect) throws IOException, TimeoutException {
		ByteBuffer bf = ByteBuffer.allocate(8);
		byte[] ba = new byte[8];
		MBusMessage lPdu;

		bf.order(ByteOrder.LITTLE_ENDIAN);

		bf.put(secondaryAddress.asByteArray());

		bf.position(0);
		bf.get(ba, 0, 8);

		// send select/deselect
		if (deselect) {
			sendLongMessage(0xfd, 0x53, 0x56, 8, ba);
		}
		else {
			sendLongMessage(0xfd, 0x53, 0x52, 8, ba);
		}

		lPdu = receiveMessage();

		if (lPdu == null || lPdu.getFrameType() != FrameType.SIMPLE_CHAR) {
			throw new IOException("unbable to select component");
		}
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

	private MBusMessage receiveMessage() throws IOException, TimeoutException {

		int timeval = 0;
		int readBytes = 0;
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

		byte[] messageBytes = new byte[messageLength];
		System.arraycopy(inputBuffer, 0, messageBytes, 0, messageLength);

		MBusMessage mBusMessage = new MBusMessage(messageBytes);
		try {
			mBusMessage.decode();
		} catch (DecodingException e) {
		}
		return mBusMessage;

	}

}
