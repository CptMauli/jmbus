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

import org.junit.Assert;
import org.junit.Test;
import org.openmuc.jmbus.internal.MBusLPdu;

public class MBusSapTest {
	public void testResponseParser() {

	}

	@Test
	public void testParser2() throws IOException {
		byte[] msg = MessagesTest.testMsg4;

		MBusLPdu lpdu = new MBusLPdu(msg);

		Assert.assertEquals(0, lpdu.getAField());

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			Assert.fail("IOException");
		}

		Assert.assertEquals(9, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.decode();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getDataValue() != null) {
					System.out.print(" Value: " + vdb.getDataValue().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Test
	public void testParser3() throws IOException {
		byte[] msg = MessagesTest.testMsg5;

		System.out.println("Test 3:");

		MBusLPdu lpdu = new MBusLPdu(msg);

		Assert.assertEquals(5, lpdu.getAField());

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			Assert.fail("IOException");
		}

		Assert.assertEquals(10, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.decode();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getDataValue() != null) {
					System.out.print(" Value: " + vdb.getDataValue().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testParser4() throws IOException {
		byte[] msg = MessagesTest.testMsg6;

		System.out.println("Test 4:");

		MBusLPdu lpdu = new MBusLPdu(msg);

		Assert.assertEquals(13, lpdu.getAField());

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			Assert.fail("IOException");
		}

		Assert.assertEquals(12, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.decode();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getDataValue() != null) {
					System.out.print(" Value: " + vdb.getDataValue().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testParser5() throws IOException {
		byte[] msg = MessagesTest.testMsg7;

		System.out.println("Test 5:\n-------");

		MBusLPdu lpdu = new MBusLPdu(msg);

		Assert.assertEquals(1, lpdu.getAField());

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			Assert.fail("IOException");
		}

		Assert.assertEquals(12, vdr.getVariableDataBlocks().size());

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {
			try {

				vdb.decode();

				if (vdb.getDescription() != null) {
					System.out.print(vdb.getDescription().toString());
				}

				if (vdb.getDataValue() != null) {
					System.out.print(" Value: " + vdb.getDataValue().toString());
				}

				System.out.print(" Unit: " + vdb.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
