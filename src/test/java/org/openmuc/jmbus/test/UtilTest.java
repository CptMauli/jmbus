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
	
	
}
