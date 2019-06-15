package org.openmuc.jmbus;

/*
 * Copyright Fraunhofer ISE, 2010
 * Author(s): Michael Zillgith
 *            Stefan Feuerhahn
 *    
 * This file is part of jMBus.
 * For more information visit http://www.openmuc.org
 * 
 * jMBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.concurrent.TimeoutException;

/**
 * M-Bus Link Layer Service Access Point
 * 
 */
public class MBusLSAP {
	private CommPortIdentifier portId;
	private SerialPort serialPort;

	private byte[] outputBuffer = new byte[1000];
	private byte[] inputBuffer = new byte[1000];
	private DataOutputStream os;
	private DataInputStream inStream;
	private int timeout;

	public MBusLSAP(int timeout) {
		this.timeout = timeout;
	}

	public boolean selectComponent(int id, short manuf, byte version, byte medium) throws TimeoutException, IOException {
		ByteBuffer bf = ByteBuffer.allocate(8);
		byte[] ba = new byte[8];
		MBusLPDU msg;

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
			if (msg.msgType == MBusLPDU.MSG_TYPE_SIMPLE_CHAR) {
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
	public MBusLPDU receiveMessage() throws TimeoutException, IOException {

		int timeval = 0;
		int readBytes = 0;
		int i;
		int messageLength = -1;

		while (timeval < timeout && readBytes != messageLength) {
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

		} /* while */

		if (readBytes != messageLength) {
			throw new TimeoutException("Timeout listening for response message.");
		}

		byte[] lpdu = new byte[messageLength];
		for (i = 0; i < messageLength; i++) {
			lpdu[i] = inputBuffer[i];
		}

		MBusLPDU rcvdMessage = new MBusLPDU(lpdu);
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
	} /* sendLongMessage() */

	public void configureSerialPort(String device, String configStr) throws IOException {
		// TODO configStr is ignored for now

		Enumeration<?> portList;

		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

				if (portId.getName().equals(device)) {
					try {
						serialPort = (SerialPort) portId.open("mbus_connector", 2000);
					} catch (PortInUseException e) {
						throw new IOException("Port " + device + " is already in use!");
					}

					try {
						serialPort.setSerialPortParams(2400, // was 2400
								SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
					} catch (UnsupportedCommOperationException e) {
						throw new IOException("Error setting communication parameters!");
					}

					try {
						os = new DataOutputStream(serialPort.getOutputStream());
						inStream = new DataInputStream(serialPort.getInputStream());
					} catch (IOException e) {
						throw new IOException("Cannot catch output stream!");
					}

					return;
				}
			}
		} /* while (portList.hasMoreElements()) */

		throw new IOException("Port not found: " + device + '!');
	} /* initializeSerialPort() */

	/* This operation belongs to application layer !!! */
	public boolean masterToSlaveDataSend(int slaveAddr, byte[] data) throws TimeoutException, IOException {
		MBusLPDU msg;

		sendLongMessage(slaveAddr, 0x53, 0x51, data.length, data);
		msg = receiveMessage();

		if (msg != null)
			return true;
		else
			return false;
	}

	public void closeSerialPort() {
		if (serialPort != null) {
			serialPort.close();
		}
	}

} /* public class MBusConnector */
