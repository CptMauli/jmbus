package org.openmuc.jmbus.test;

import java.io.IOException;
import java.text.ParseException;

import org.openmuc.jmbus.DLMSUnit;
import org.openmuc.jmbus.MBusLPDU;
import org.openmuc.jmbus.Util;
import org.openmuc.jmbus.VariableDataBlock;
import org.openmuc.jmbus.VariableDataResponse;

import junit.framework.TestCase;

public class TestMBusASAP extends TestCase {
	public void testResponseParser() {
		

	}
	
	public void testParser2() {
		byte[] msg = TestMessages.testMsg4;
		
		MBusLPDU lpdu = new MBusLPDU(msg);
		
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
		
		VariableDataResponse vdr = new VariableDataResponse();
		
		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			fail("IOException");
		}
		
		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {
				String difVif = Util.composeHexStringFromByteArray(vdb.getDIB()) + "/" +
						Util.composeHexStringFromByteArray(vdb.getVIB());
								
				vdb.parse();
				
				System.out.print(difVif + " ");
				if (vdb.getDescription() != null)
					System.out.print(vdb.getDescription().toString());
				
				if (vdb.getData() != null)
					System.out.print(" Value: " + vdb.getData().toString());
				
				System.out.print(" Unit: " + DLMSUnit.getString(vdb.getUnit()));
				
				System.out.println();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	public void testParser3() {
		byte[] msg = TestMessages.testMsg5;
		
		System.out.println("Test 3:");
		
		MBusLPDU lpdu = new MBusLPDU(msg);
		
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
		
		assertEquals(5, lpdu.getAField());
		
		VariableDataResponse vdr = new VariableDataResponse();
		
		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			fail("IOException");
		}
		
		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {
				String difVif = Util.composeHexStringFromByteArray(vdb.getDIB()) + "/" +
						Util.composeHexStringFromByteArray(vdb.getVIB());
								
				vdb.parse();
				
				System.out.print(difVif + " ");
				if (vdb.getDescription() != null)
					System.out.print(vdb.getDescription().toString());
				
				if (vdb.getData() != null)
					System.out.print(" Value: " + vdb.getData().toString());
				
				System.out.print(" Unit: " + DLMSUnit.getString(vdb.getUnit()));
				
				System.out.println();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
}
