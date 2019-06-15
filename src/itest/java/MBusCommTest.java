/*
 * Copyright 2010-14 Fraunhofer ISE
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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.MBusSap;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;

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
	public void testCommP5() throws DecodingException {
		int baudRate = 2400;
		int myMBusAdr = 5;

		MBusSap mBusSap = new MBusSap(MBusCommSetup.myMBusPort, baudRate);
		try {
			mBusSap.open();
		} catch (IOException e2) {
			fail("Failed to open serial port: " + e2.getMessage());
		}

		mBusSap.setTimeout(2000);

		VariableDataStructure response = null;
		try {
			response = mBusSap.read(myMBusAdr);
		} catch (IOException e) {
			mBusSap.close();
			fail("Error reading meter: " + e.getMessage());
		} catch (TimeoutException e) {
			mBusSap.close();
			fail("Read attempt timed out");
		}

		response.decodeDeep();

		SecondaryAddress secondaryAddress = response.getSecondaryAddress();

		Assert.assertEquals("30100608", secondaryAddress.getDeviceId().toString());
		Assert.assertEquals("NZR", secondaryAddress.getManufacturerId());
		Assert.assertEquals(1, secondaryAddress.getVersion());
		Assert.assertEquals(DeviceType.ELECTRICITY_METER, secondaryAddress.getDeviceType());
		Assert.assertEquals(0, response.getStatus());

		Assert.assertEquals((byte) 0x3f, response.getManufacturerData()[0]);

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
	public void testCommP60() throws DecodingException {
		int myMBusAdr = 60;
		int baudRate = 2400;

		MBusSap mBusSap = new MBusSap(MBusCommSetup.myMBusPort, baudRate);
		try {
			mBusSap.open();
		} catch (IOException e2) {
			fail("Failed to open serial port: " + e2.getMessage());
		}

		mBusSap.setTimeout(2000);

		VariableDataStructure response = null;
		try {
			response = mBusSap.read(myMBusAdr);
		} catch (IOException e) {
			mBusSap.close();
			fail("Error reading meter: " + e.getMessage());
		} catch (TimeoutException e) {
			mBusSap.close();
			fail("Read attempt timed out");
		}

		response.decodeDeep();

		SecondaryAddress secondaryAddress = response.getSecondaryAddress();

		Assert.assertEquals("67329774", secondaryAddress.getDeviceId().toString());
		Assert.assertEquals("LUG", secondaryAddress.getManufacturerId());
		Assert.assertEquals(4, secondaryAddress.getVersion());
		Assert.assertEquals(DeviceType.HEAT_METER, secondaryAddress.getDeviceType());
		Assert.assertEquals(0, response.getStatus());

		Assert.assertEquals((byte) 0x21, response.getManufacturerData()[0]);
		Assert.assertEquals((byte) 0x04, response.getManufacturerData()[1]);
		Assert.assertEquals((byte) 0x00, response.getManufacturerData()[2]);

		mBusSap.close();
		System.out.println("Test case ends successfully.\n");
	}

}
