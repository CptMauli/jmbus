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
package org.openmuc.jmbus;

import java.io.IOException;
import java.text.ParseException;

import junit.framework.TestCase;

public class MBusSapTest extends TestCase {
	public void testResponseParser() {

	}

	public void testParser2() {
		byte[] msg = MessagesTest.testMsg4;

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

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			fail("IOException");
		}

		assertEquals(9, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.parse();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getData() != null) {
					System.out.print(" Value: " + vdb.getData().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void testParser3() {
		byte[] msg = MessagesTest.testMsg5;

		System.out.println("Test 3:");

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

		assertEquals(5, lpdu.getAField());

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			fail("IOException");
		}

		assertEquals(10, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.parse();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getData() != null) {
					System.out.print(" Value: " + vdb.getData().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void testParser4() {
		byte[] msg = MessagesTest.testMsg6;

		System.out.println("Test 4:");

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

		assertEquals(13, lpdu.getAField());

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			fail("IOException");
		}

		assertEquals(12, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.parse();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getData() != null) {
					System.out.print(" Value: " + vdb.getData().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void testParser5() {
		byte[] msg = MessagesTest.testMsg7;

		System.out.println("Test 5:\n-------");

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

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			fail("IOException");
		}

		assertEquals(12, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.parse();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getData() != null) {
					System.out.print(" Value: " + vdb.getData().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
