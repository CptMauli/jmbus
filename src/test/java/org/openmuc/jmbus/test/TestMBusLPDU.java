package org.openmuc.jmbus.test;

import java.io.IOException;

import junit.framework.TestCase;

import org.openmuc.jmbus.internal.MBusLPdu;

public class TestMBusLPDU extends TestCase {
	public void testParser() {
		byte[] msg = TestMessages.testMsg1;

		MBusLPdu lpdu = new MBusLPdu(msg);

		try {
			lpdu.getAField();
			fail("Missing expected exception");
		} catch (RuntimeException re) {
		}

		try {
			lpdu.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(1, lpdu.getAField());

		System.out.println(lpdu.getAPDU().capacity());
	}

	public void testParser2() {
		byte[] msg = TestMessages.testMsg4;

		MBusLPdu lpdu = new MBusLPdu(msg);

		try {
			lpdu.getAField();
			fail("Missing expected exception");
		} catch (RuntimeException re) {
		}

		try {
			lpdu.parse();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(0, lpdu.getAField());

		System.out.println(lpdu.getAPDU().capacity());
	}
}
