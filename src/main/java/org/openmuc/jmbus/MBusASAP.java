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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.MBusLSAP;
import org.openmuc.jmbus.MBusLPDU;

/**
 * M-Bus Application Layer Service Access Point
 * 
 */
public class MBusASAP {

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

	private String serialPort;
	private MBusLSAP mBusLSAP;

	private boolean debug = false;

	/**
	 * 
	 * @param serialPort
	 * @param configStr
	 * @param timeout
	 *            Socket receive timeout in millis
	 * @throws IOException
	 */
	public MBusASAP(String serialPort, String configStr, int timeout) throws IOException {
		if (serialPort.startsWith("com")) {
			this.serialPort = "/dev/ttyS" + serialPort.substring(3);
		} 
		else if (serialPort.startsWith("usb")) {
			this.serialPort = "/dev/ttyUSB" + serialPort.substring(3);
		} 
		else if (serialPort.startsWith("/dev/")) {
			this.serialPort = serialPort;
		}
		else {
			this.serialPort = "/dev/tty" + serialPort;
		}

		if (System.getProperty("org.openmuc.jmbus.debug") != null) {
			debug = true;
		}

		mBusLSAP = new MBusLSAP(timeout);
		mBusLSAP.configureSerialPort(this.serialPort, configStr);

	}

	/**
	 * 
	 * default timeout = 1s
	 * 
	 * @param serialPort
	 * @param configStr
	 * @throws IOException
	 */
	public MBusASAP(String serialPort, String configStr) throws IOException {
		this(serialPort, configStr, 1000);
	}

	/**
	 * 
	 * @param meterAddr
	 *            e.g. p1 or s...
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public VariableDataResponse readMeter(String meterAddr) throws IOException, TimeoutException {

		int addrType = Util.getAddrType(meterAddr);

		if (addrType == Util.ADDR_TYPE_PRIMARY) {
			mBusLSAP.sendShortMessage(Util.getAddress(meterAddr), 0x5b);
		} else {
			System.out.println(meterAddr);
			System.out.println(Util.getAddress(meterAddr));
			if (mBusLSAP.selectComponent(Util.getAddress(meterAddr), (short) 0xffff, (byte) 0xff, (byte) 0xff)) {
				System.out.println("Selected!");
				mBusLSAP.sendShortMessage(0xfd, 0x5b);
				// mBusLSAP.sendShortMessage(0xfd, 0x7b);
			} else {
				// select timeout
				System.out.println("Select failed!");
				throw new IOException("unbable to select component");
			}
		}

		MBusLPDU lpdu = mBusLSAP.receiveMessage();
		if (debug)
			System.out.println(lpdu.toString());
		VariableDataResponse vdr = new VariableDataResponse();
		vdr.address = lpdu.getAField();
		vdr.parse(lpdu.getAPDU());

		return vdr;

	}

	private void applicationReset(String meterAddr, boolean hasSubCode, byte subCode) throws IOException,
			TimeoutException {
		int addrType = Util.getAddrType(meterAddr);
		byte[] subCodeArray;
		int len;

		if (hasSubCode) {
			len = 1;
			subCodeArray = new byte[1];
			subCodeArray[0] = subCode;
		} else {
			len = 0;
			subCodeArray = null;
		}

		if (addrType == Util.ADDR_TYPE_PRIMARY) {
			/* SND_UD Application reset */
			mBusLSAP.sendLongMessage(Util.getAddress(meterAddr), 0x53, 50, len, subCodeArray);
		} else {
			if (mBusLSAP.selectComponent(Util.getAddress(meterAddr), (short) 0xffff, (byte) 0xff, (byte) 0xff)) {
				/* SND_UD Application reset */
				mBusLSAP.sendLongMessage(0xfd, 0x53, 50, len, subCodeArray);
			} else {
				// select timeout
				throw new TimeoutException();
			}
		}

		MBusLPDU lpdu = mBusLSAP.receiveMessage();
		lpdu.parse();
		if (debug)
			System.out.println(lpdu.toString());
		if (lpdu.getType() != MBusLPDU.MSG_TYPE_SIMPLE_CHAR) {
			throw new IOException("Invalid message received!");
		}
	} /* applicationReset() */

	public void applicationReset(String meterAddr) throws IOException, TimeoutException {
		applicationReset(meterAddr, false, (byte) 0);
	}

	public void applicationReset(String meterAddr, byte subCode) throws IOException, TimeoutException {
		applicationReset(meterAddr, true, subCode);
	}

	public void closeSerialPort() {
		mBusLSAP.closeSerialPort();
	}
}
