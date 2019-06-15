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

package org.openmuc.jmbus;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.internal.MBusLPdu;
import org.openmuc.jmbus.internal.MBusLSap;

/**
 * M-Bus Application Layer Service Access Point
 * 
 */
public class MBusSap {

	public static final byte APL_RESET_ALL = 0x00;
	public static final byte APL_RESET_USER_DATA = 0x10;
	public static final byte APL_RESET_SIMPLE_BILLING = 0x20;
	public static final byte APL_RESET_ENHANCED_BILLING = 0x30;
	public static final byte APL_RESET_MULTI_TARIF_BILLING = 0x40;
	public static final byte APL_RESET_INST_VALUES = 0x50;
	public static final byte APL_RESET_LOAD_MANAGEMENT = 0x60;
	public static final byte APL_RESET_INSTALLATION = (byte) 0x80;
	public static final byte APL_RESET_TESTING = (byte) 0x90;
	public static final byte APL_RESET_CALIBRATION = (byte) 0xa0;
	public static final byte APL_RESET_MANUFACTURING = (byte) 0xb0;
	public static final byte APL_RESET_DEVELOPMENT = (byte) 0xc0;
	public static final byte APL_RESET_SELFTEST = (byte) 0xd0;

	private final String serialPort;
	private final MBusLSap mBusLSap;
	private final int defaultReadTimeout;

	private boolean debug = false;

	/**
	 * Creates an M-Bus Service Access Point that is used to read meters. Attempts to initialize the given serial port
	 * and throws an IOException if this fails. If the serialConfig syntax is not understood an IllegalArgumentException
	 * is thrown.
	 * 
	 * @param serialPort
	 *            examples for serial port identifiers on Linux are "/dev/ttyS0" or "/dev/ttyUSB0".
	 * @param serialConfig
	 *            the serial configuration, format is <baudrate>/<databits><parity><stopbits>. The empty string
	 *            indicates the default configuration. The default config is "2400/8E1".
	 * @param defaultReadTimeout
	 *            the default timeout for reading meters in ms
	 * @throws IOException
	 */
	public MBusSap(String serialPort, String serialConfig, int defaultReadTimeout) throws IOException {
		this.serialPort = serialPort;

		if (System.getProperty("org.openmuc.jmbus.debug") != null) {
			debug = true;
		}

		mBusLSap = new MBusLSap();
		mBusLSap.configureSerialPort(this.serialPort, serialConfig);

		this.defaultReadTimeout = defaultReadTimeout;

	}

	public VariableDataResponse readMeter(String meterAddr) throws IOException, TimeoutException {
		return readMeter(meterAddr, defaultReadTimeout);
	}

	/**
	 * 
	 * @param meterAddr
	 *            e.g. p1 or s...
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public VariableDataResponse readMeter(String meterAddr, int timeout) throws IOException, TimeoutException {

		if (meterAddr.charAt(0) == 'p') {
			mBusLSap.sendShortMessage(Util.getAddress(meterAddr), 0x5b);
		}
		else {
			if (mBusLSap.selectComponent(Util.getAddress(meterAddr), (short) 0xffff, (byte) 0xff, (byte) 0xff, timeout)) {
				mBusLSap.sendShortMessage(0xfd, 0x5b);
			}
			else {
				// select timeout
				throw new IOException("unbable to select component");
			}
		}

		MBusLPdu lpdu = mBusLSap.receiveMessage(timeout);
		if (debug) {
			System.out.println(lpdu.toString());
		}
		VariableDataResponse vdr = new VariableDataResponse();
		vdr.address = lpdu.getAField();
		vdr.parse(lpdu.getAPDU());

		return vdr;

	}

	private void applicationReset(String meterAddr, boolean hasSubCode, byte subCode) throws IOException,
			TimeoutException {
		byte[] subCodeArray;
		int len;

		if (hasSubCode) {
			len = 1;
			subCodeArray = new byte[1];
			subCodeArray[0] = subCode;
		}
		else {
			len = 0;
			subCodeArray = null;
		}

		if (meterAddr.charAt(0) == 'p') {
			/* SND_UD Application reset */
			mBusLSap.sendLongMessage(Util.getAddress(meterAddr), 0x53, 50, len, subCodeArray);
		}
		else {
			if (mBusLSap.selectComponent(Util.getAddress(meterAddr), (short) 0xffff, (byte) 0xff, (byte) 0xff,
					defaultReadTimeout)) {
				/* SND_UD Application reset */
				mBusLSap.sendLongMessage(0xfd, 0x53, 50, len, subCodeArray);
			}
			else {
				// select timeout
				throw new TimeoutException();
			}
		}

		MBusLPdu lpdu = mBusLSap.receiveMessage(defaultReadTimeout);
		lpdu.parse();
		if (debug) {
			System.out.println(lpdu.toString());
		}
		if (lpdu.getType() != MBusLPdu.MSG_TYPE_SIMPLE_CHAR) {
			throw new IOException("Invalid message received!");
		}
	}

	public void applicationReset(String meterAddr) throws IOException, TimeoutException {
		applicationReset(meterAddr, false, (byte) 0);
	}

	public void applicationReset(String meterAddr, byte subCode) throws IOException, TimeoutException {
		applicationReset(meterAddr, true, subCode);
	}

	public void closeSerialPort() {
		mBusLSap.closeSerialPort();
	}
}
