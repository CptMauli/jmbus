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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gnu.io.SerialPort;

/**
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class WMBusSapRadioCrafts extends AbstractWMBusSap {

	private MessageReceiver receiver;

	private class MessageReceiver extends Thread {

		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		@Override
		public void run() {

			int timeElapsed = 0;
			int readBytesTotal = 0;
			int messageLength = -1;

			try {
				while (!closed) {

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}

					if (is.available() > 0) {

						int messageStartIndex = 0;
						timeElapsed = 0;

						readBytesTotal += is.read(inputBuffer, readBytesTotal, BUFFER_LENGTH - readBytesTotal);

						while ((readBytesTotal - messageStartIndex) > 10) {

							// no beginning of message has been found
							if (messageLength == -1) {
								for (int i = (messageStartIndex + 1); i < readBytesTotal; i++) {
									if (inputBuffer[i] == 0x44) {
										messageStartIndex = i - 1;
										messageLength = (inputBuffer[messageStartIndex] & 0xff) + 1;
										break;
									}
								}
								if (messageLength == -1) {
									discard(messageStartIndex, (readBytesTotal - messageStartIndex));
									messageStartIndex = readBytesTotal;
									break;
								}
							}

							if ((readBytesTotal - messageStartIndex) >= (messageLength + messageStartIndex)) {

								int rssi = inputBuffer[messageLength + messageStartIndex - 1] & 0xff;
								final Integer signalStrengthInDBm;

								signalStrengthInDBm = (rssi * -1) / 2;

								final byte[] messageBytes = new byte[messageLength - 1];
								System.arraycopy(inputBuffer, messageStartIndex, messageBytes, 0, messageLength - 1);
								messageBytes[0] = (byte) (messageBytes[0] - 1);

								executor.execute(new Runnable() {
									@Override
									public void run() {
										listener.newMessage(
												new WMBusMessage(messageBytes, signalStrengthInDBm, keyMap));
									}
								});

								messageStartIndex += messageLength;
								messageLength = -1;

							}
							else {
								break;
							}
						}
						if (messageStartIndex > 0) {
							for (int i = messageStartIndex; i < readBytesTotal; i++) {
								inputBuffer[i] = inputBuffer[i - messageStartIndex];
							}
						}
						readBytesTotal -= messageStartIndex;

					}
					else if (readBytesTotal > 0) {
						timeElapsed += 100;
						if (timeElapsed > 500) {
							discard(0, readBytesTotal);
							timeElapsed = 0;
							readBytesTotal = 0;
							messageLength = -1;
						}
					}

				}

			} catch (final Exception e) {
				close();
				executor.execute(new Runnable() {
					@Override
					public void run() {
						listener.stoppedListening(new IOException(e));
					}
				});

			} finally {
				executor.shutdown();
			}

		}

		private void discard(int offset, int length) {
			final byte[] discardedBytes = new byte[length];
			System.arraycopy(inputBuffer, offset, discardedBytes, 0, length);

			executor.execute(new Runnable() {
				@Override
				public void run() {
					listener.discardedBytes(discardedBytes);
				}
			});
		}
	}

	public WMBusSapRadioCrafts(String serialPortName, WMBusMode mode, WMBusListener listener) {
		super(mode, listener);
		this.serialTransceiver = new SerialTransceiver(serialPortName, 19200, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	}

	@Override
	public void open() throws IOException {
		if (!closed) {
			return;
		}
		serialTransceiver.open();
		os = serialTransceiver.getOutputStream();
		is = serialTransceiver.getInputStream();
		initializeWirelessTransceiver(mode);
		receiver = new MessageReceiver();
		closed = false;
		receiver.start();

	}

	/**
	 * @param mode
	 *            - the wMBus mode to be used for transmission
	 * @throws IOException
	 */
	private void initializeWirelessTransceiver(WMBusMode mode) throws IOException {
		enterConfigMode();

		switch (mode) {
		case S:

			/* Set S mode */
			sendByteInConfigMode(0x4d);
			os.write(0x03);
			os.write(0x00);
			sendByteInConfigMode(0xff);

			/* Set master mode */
			sendByteInConfigMode(0x4d);
			os.write(0x12);
			os.write(0x01);
			sendByteInConfigMode(0xff);

			/* Get RSSI information with corresponding message */
			sendByteInConfigMode(0x4d);
			os.write(0x05);
			os.write(0x01);
			sendByteInConfigMode(0xff);

			// /* Set Auto Answer Register */
			// sendByteInConfigMode(0x41);
			// sendByteInConfigMode(0xff);

			break;
		case T:
			/* Set T2 mode */
			sendByteInConfigMode(0x4d);
			os.write(0x03);
			os.write(0x02);
			sendByteInConfigMode(0xff);

			/* Set master mode */
			sendByteInConfigMode(0x4d);
			os.write(0x12);
			os.write(0x01);
			sendByteInConfigMode(0xff);

			/* Get RSSI information with corresponding message */
			sendByteInConfigMode(0x4d);
			os.write(0x05);
			os.write(0x01);
			sendByteInConfigMode(0xff);

			// /* Set Auto Answer Register */
			// sendByteInConfigMode(0x41);
			// sendByteInConfigMode(0xff);
			break;
		default:
			throw new IOException("wMBUS Mode '" + mode.toString() + "' is not supported");
		}

		leaveConfigMode();

	}

	private void leaveConfigMode() throws IOException {
		os.write(0x58);
	}

	private void enterConfigMode() throws IOException {
		sendByteInConfigMode(0x00);
	}

	private void sendByteInConfigMode(int b) throws IOException {
		int timeval = 0;
		int timeout = 500;
		int read;

		if (is.available() > 0) {
			read = is.read(inputBuffer);
		}

		os.write(b);

		while (timeval < timeout) {
			if (is.available() > 0) {
				read = is.read();
				if (read != 0x3e) {
					throw new IOException("sendByteInConfigMode failed");
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			timeval += 100;
		}

	}

}
