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
package org.openmuc.jmbus;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class MBusSapTest {
	public void testResponseParser() {

	}

	@Test
	public void testParser2() throws IOException, DecodingException {
		byte[] msg = MessagesTest.testMsg4;

		MBusMessage mBusMessage = new MBusMessage(msg);
		mBusMessage.decodeDeep();

		Assert.assertEquals(0, mBusMessage.getAddressField());

		VariableDataStructure vdr = mBusMessage.getVariableDataResponse();

		Assert.assertEquals(9, vdr.getDataRecords().size());

		for (DataRecord dataRecord : vdr.getDataRecords()) {
			try {

				dataRecord.decode();

				if (dataRecord.getDescription() != null) {
					System.out.print(dataRecord.getDescription().toString());
				}

				if (dataRecord.getDataValue() != null) {
					System.out.print(" Value: " + dataRecord.getDataValue().toString());
				}

				System.out.print(" Unit: " + dataRecord.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Test
	public void testParser3() throws IOException, DecodingException {
		byte[] msg = MessagesTest.testMsg5;

		MBusMessage lpdu = new MBusMessage(msg);
		lpdu.decodeDeep();

		Assert.assertEquals(5, lpdu.getAddressField());

		VariableDataStructure vds = lpdu.getVariableDataResponse();

		Assert.assertEquals(10, vds.getDataRecords().size());

		for (DataRecord dataRecord : vds.getDataRecords()) {
			try {

				dataRecord.decode();

				if (dataRecord.getDescription() != null) {
					System.out.print(dataRecord.getDescription().toString());
				}

				if (dataRecord.getDataValue() != null) {
					System.out.print(" Value: " + dataRecord.getDataValue().toString());
				}

				System.out.print(" Unit: " + dataRecord.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testParser4() throws IOException, DecodingException {
		byte[] msg = MessagesTest.testMsg6;

		MBusMessage lpdu = new MBusMessage(msg);
		lpdu.decodeDeep();

		Assert.assertEquals(13, lpdu.getAddressField());

		VariableDataStructure vdr = lpdu.getVariableDataResponse();

		Assert.assertEquals(12, vdr.getDataRecords().size());

		for (DataRecord dataRecord : vdr.getDataRecords()) {
			try {

				dataRecord.decode();

				if (dataRecord.getDescription() != null) {
					System.out.print(dataRecord.getDescription().toString());
				}

				if (dataRecord.getDataValue() != null) {
					System.out.print(" Value: " + dataRecord.getDataValue().toString());
				}

				System.out.print(" Unit: " + dataRecord.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testParser5() throws IOException, DecodingException {
		byte[] msg = MessagesTest.testMsg7;

		MBusMessage lpdu = new MBusMessage(msg);
		lpdu.decodeDeep();

		Assert.assertEquals(1, lpdu.getAddressField());

		VariableDataStructure vdr = lpdu.getVariableDataResponse();

		Assert.assertEquals(12, vdr.getDataRecords().size());

		for (DataRecord dataRecord : vdr.getDataRecords()) {
			try {

				dataRecord.decode();

				if (dataRecord.getDescription() != null) {
					System.out.print(dataRecord.getDescription().toString());
				}

				if (dataRecord.getDataValue() != null) {
					System.out.print(" Value: " + dataRecord.getDataValue().toString());
				}

				System.out.print(" Unit: " + dataRecord.getUnit());

				System.out.println();
			} catch (DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
