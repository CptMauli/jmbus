package org.openmuc.jmbus.test;

import junit.framework.TestCase;

import org.openmuc.jmbus.Util;

public class UtilTest extends TestCase {

	public void testVendorId() {
		short id = 0x2324;

		assertEquals("HYD", Util.vendorID(id));
	}

	public void testByteArrayStringConversion() {

		String hexString = "A1B2C3D4E5F61001";
		byte[] byteArray = Util.createByteArrayFromString(hexString);

		assertEquals(8, byteArray.length);

		String createdHexString = Util.composeHexStringFromByteArray(byteArray);

		assertEquals("a1b2c3d4e5f61001", createdHexString);
	}

	public void testByteConversion() {

		System.out.println(new Long((short) ((((byte) 0xff) & 0xff) + ((0xff & 0xff) << 8))));

		short test2 = (short) ((short) (((byte) 0xff) & 0xff) | (((byte) 0xff & 0xff) << 8));
		System.out.println(new Long(test2));

		byte test = (byte) 0xff;
		System.out.println(test);

		System.out.println(0xff << 56);

	}
}
