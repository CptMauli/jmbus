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
//package org.openmuc.jmbus;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmuc.jmbus.MBusSap;
import org.openmuc.jmbus.VariableDataResponse;

public class MBusCommTest {

	@BeforeClass
	public static void setUpCommTest() {
		MBusCommSetup.setUpMBusTest();
		System.out.println("Start jUnit test now!");
		System.out.println("===================\n");

		SimpleMBusSlave simpleMBusServer = new SimpleMBusSlave();
		simpleMBusServer.start();

	}

	@AfterClass
	public static void shutDownCommTest() {
		System.out.println("===================");
		System.out.println("End of jUnit");
		MBusCommSetup.shutDownMBusTest();
	}

	@Test(timeout = 5000)
	public void testCommP5() {
		int baudRate = 2400;
		String myMBusAdr = "p5";

		MBusSap mBusSap = new MBusSap(MBusCommSetup.myMBusPort);
		try {
			mBusSap.open(baudRate);
		} catch (IOException e2) {
			fail("Failed to open serial port: " + e2.getMessage());
		}

		mBusSap.setTimeout(2000);

		VariableDataResponse response = null;
		try {
			response = mBusSap.read(myMBusAdr);
		} catch (IOException e) {
			mBusSap.close();
			fail("Error reading meter: " + e.getMessage());
		} catch (TimeoutException e) {
			mBusSap.close();
			fail("Read attempt timed out");
		}

		String meterID = new String(response.getId().toString());
		String manufId = new String(response.getManufacturerId());
		String version = new String(Byte.toString(response.getVersion()));
		String devType = new String(response.getDeviceType().toString());
		String status = new String(Byte.toString(response.getStatus()));

		System.out.println("MeterID: " + meterID + ", ManufacturerID: " + manufId + ", Version: " + version
				+ ", Device Type: " + devType + ", Status: " + status);

		Assert.assertEquals("30100608", meterID);
		Assert.assertEquals("NZR", manufId);
		Assert.assertEquals("1", version);
		Assert.assertEquals("ELECTRICITY", devType);
		Assert.assertEquals("0", status);

		Assert.assertEquals((byte) 0x3f, response.manufacturerData[0]);

		// if (response.getManufacturerData() != null) {
		// System.out.println("Manufacturer specific data follows:");
		// int j = 0;
		// for (byte element : response.manufacturerData) {
		// System.out.printf("0x%02x ", element);
		// j++;
		// if (j % 10 == 0) {
		// System.out.println();
		// }
		// }
		// }

		mBusSap.close();
		System.out.println("Test case ends successfully.\n");
	}

	@Test(timeout = 5000)
	public void testCommP60() {
		String myMBusAdr = "p60";
		int baudRate = 2400;

		MBusSap mBusSap = new MBusSap(MBusCommSetup.myMBusPort);
		try {
			mBusSap.open(baudRate);
		} catch (IOException e2) {
			fail("Failed to open serial port: " + e2.getMessage());
		}

		mBusSap.setTimeout(2000);

		VariableDataResponse response = null;
		try {
			response = mBusSap.read(myMBusAdr);
		} catch (IOException e) {
			mBusSap.close();
			fail("Error reading meter: " + e.getMessage());
		} catch (TimeoutException e) {
			mBusSap.close();
			fail("Read attempt timed out");
		}

		String meterID = new String(response.getId().toString());
		String manufId = new String(response.getManufacturerId());
		String version = new String(Byte.toString(response.getVersion()));
		String devType = new String(response.getDeviceType().toString());
		String status = new String(Byte.toString(response.getStatus()));

		System.out.println("MeterID: " + meterID + ", ManufacturerID: " + manufId + ", Version: " + version
				+ ", Device Type: " + devType + ", Status: " + status);

		Assert.assertEquals("67329774", meterID);
		Assert.assertEquals("LUG", manufId);
		Assert.assertEquals("4", version);
		Assert.assertEquals("HEAT", devType);
		Assert.assertEquals("0", status);

		Assert.assertEquals((byte) 0x21, response.manufacturerData[0]);
		Assert.assertEquals((byte) 0x04, response.manufacturerData[1]);
		Assert.assertEquals((byte) 0x00, response.manufacturerData[2]);

		mBusSap.close();
		System.out.println("Test case ends successfully.\n");
	}

}
