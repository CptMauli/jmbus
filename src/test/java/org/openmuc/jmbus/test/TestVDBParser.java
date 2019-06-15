package org.openmuc.jmbus.test;

import java.text.ParseException;

import org.openmuc.jmbus.OutOfRangeException;
import org.openmuc.jmbus.VariableDataBlock;
import org.openmuc.jmbus.BCD;

import junit.framework.TestCase;

public class TestVDBParser extends TestCase {

	
	public void testBCD12() {
		
		/* Test a small BCS value */
		VariableDataBlock vdb = new VariableDataBlock(
							new byte[] {(byte) 0x0e}, 
							new byte[] {(byte) 0x04}, 
							new byte[] {(byte) 0x10, (byte) 0x17, 0x00, 0x00, 0x00 , 0x00}, 
							null);
		
		
		try {
			vdb.parse();
			
			Object obj = vdb.getData();
			
			assertEquals(obj instanceof BCD, true);
			
			BCD bcd = (BCD) obj;
			
			assertEquals(bcd.toString(),"000000001710");
			
			try {
				bcd.toInteger();
				fail("Missing OutOfRangeException");
			} catch (OutOfRangeException e) {
			}
			
			assertEquals(bcd.longValue(), 1710);
			
			
			
		} catch (ParseException e) {
			fail("Unexpected exception");
		}
		
		
		/* Test the largest possible BCD value */
		vdb = new VariableDataBlock(
				new byte[] {(byte) 0x0e}, 
				new byte[] {(byte) 0x04}, 
				new byte[] {(byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99 , (byte) 0x99},
				null);
		
		try {
			vdb.parse();
			
			Object obj = vdb.getData();
			
			assertEquals(obj instanceof BCD, true);
			
			BCD bcd = (BCD) obj;
			
			assertEquals(bcd.toString(),"999999999999");
			
			try {
				bcd.toInteger();
				fail("Missing OutOfRangeException");
			} catch (OutOfRangeException e) {
			}
			
			assertEquals(bcd.longValue(), 999999999999l);
		} catch (ParseException e) {
			fail("Unexpected exception");
		}
	}
	
	public void testINT64() {		
		VariableDataBlock vdb = new VariableDataBlock(
				new byte[] {(byte) 0x07}, 
				new byte[] {(byte) 0x04}, 
				new byte[] {(byte) 0x12, (byte) 0x23, (byte) 0x34, (byte) 0x45, 
						(byte) 0x56 , (byte) 0x67, (byte) 0x78, (byte) 0x12}, null);
		
		try {
			vdb.parse();
			
			Object obj = vdb.getData();
			
			assertEquals(obj instanceof Long, true);
			
			Long val = (Long) obj;
			
			assertEquals(new Long(1330927310113874706l), val);

		} catch (ParseException e) {
			fail("Unexpected exception");
		}
		
		vdb = new VariableDataBlock(
				new byte[] {(byte) 0x07}, 
				new byte[] {(byte) 0x04}, 
				new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 
						(byte) 0xFF , (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, null);
		
		try {
			vdb.parse();
			
			Object obj = vdb.getData();
			
			assertEquals(obj instanceof Long, true);
			
			Long val = (Long) obj;
			
			assertEquals(new Long(-1l), val);
			
		} catch (ParseException e) {
			fail("Unexpected exception");
		}

	}
	
	public void testINT32() {
		
		byte[] data = new byte[] {(byte) 0xe4, (byte) 0x05, (byte) 0x00, (byte) 0x00};
		
		VariableDataBlock vdb = new VariableDataBlock(
				new byte[] {(byte) 0x04}, 
				new byte[] {(byte) 0x03}, 
				data, null);
		
		try {
			vdb.parse();
			
			Object obj = vdb.getData();
			
			assertEquals(true, obj instanceof Integer);
			
			Integer integer = (Integer) obj;
			
			assertEquals(new Integer(1508), integer);
			
		} catch (ParseException e) {
			fail("Failed to parse!");
		}
		
		data = new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
		
		vdb = new VariableDataBlock(
				new byte[] {(byte) 0x04}, 
				new byte[] {(byte) 0x03}, 
				data, null);
		
		try {
			vdb.parse();
			
			Object obj = vdb.getData();
			
			assertEquals(true, obj instanceof Integer);
			
			Integer integer = (Integer) obj;
			
			assertEquals(new Integer(-1), integer);
			
		} catch (ParseException e) {
			fail("Failed to parse!");
		}
		
	}
	
}
