/*
 * Copyright 2010-15 Fraunhofer ISE
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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmuc.jmbus.DataRecord.Description;

public class DataRecordParserTest {

	@Test
	public void testINT64() {
		DataRecord dataRecord = new DataRecord(new byte[] { (byte) 0x07 }, new byte[] { (byte) 0x04 },
				new byte[] { (byte) 0x12, (byte) 0x23, (byte) 0x34, (byte) 0x45, (byte) 0x56, (byte) 0x67, (byte) 0x78,
						(byte) 0x12 }, null);

		try {
			dataRecord.decode();

			Object obj = dataRecord.getDataValue();

			Assert.assertEquals(obj instanceof Long, true);

			Long val = (Long) obj;

			System.out.println(val);

			Assert.assertEquals(new Long(1330927310113874706l), val);

		} catch (DecodingException e) {
			Assert.fail("Unexpected exception");
		}

		dataRecord = new DataRecord(new byte[] { (byte) 0x07 }, new byte[] { (byte) 0x04 }, new byte[] { (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, null);

		try {
			dataRecord.decode();

			Object obj = dataRecord.getDataValue();

			Assert.assertEquals(obj instanceof Long, true);

			Long val = (Long) obj;

			Assert.assertEquals(new Long(-1l), val);

		} catch (DecodingException e) {
			Assert.fail("Unexpected exception");
		}

	}

	@Test
	public void testINT32() {

		byte[] data = new byte[] { (byte) 0xe4, (byte) 0x05, (byte) 0x00, (byte) 0x00 };

		DataRecord dataRecord = new DataRecord(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x03 }, data, null);

		try {
			dataRecord.decode();

			Object obj = dataRecord.getDataValue();

			Assert.assertEquals(true, obj instanceof Long);

			Long integer = (Long) obj;

			Assert.assertEquals(new Long(1508), integer);

		} catch (DecodingException e) {
			Assert.fail("Failed to parse!");
		}

		data = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };

		dataRecord = new DataRecord(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x03 }, data, null);

		try {
			dataRecord.decode();

			Object obj = dataRecord.getDataValue();

			Assert.assertEquals(true, obj instanceof Long);

			Long integer = (Long) obj;

			Assert.assertEquals(new Long(-1), integer);

		} catch (DecodingException e) {
			Assert.fail("Failed to parse!");
		}

	}

	private void assertParsingResults(DataRecord dataRecord, Description desc, DlmsUnit unit, byte scaler, Object data) {
		try {
			dataRecord.decode();
		} catch (DecodingException e) {
			Assert.fail("Failed to parse!");
		}

		Assert.assertEquals(desc, dataRecord.getDescription());
		Assert.assertEquals(unit, dataRecord.getUnit());
		Assert.assertEquals(scaler, dataRecord.getMultiplierExponent());
		Assert.assertEquals(data, dataRecord.getDataValue());
	}

	@Test
	public void testDataRecords() {
		/* e0000nnn Energy Wh */
		DataRecord dataRecord = new DataRecord(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x07 }, new byte[] {
				(byte) 0xc8, (byte) 0x1e, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(dataRecord, Description.ENERGY, DlmsUnit.WATT_HOUR, (byte) 4, new Long(7880));

		/* e0001nnn Energy J */

		/* e0010nnn Volume m^3 */
		dataRecord = new DataRecord(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x15 }, new byte[] { (byte) 0xfe,
				(byte) 0xbf, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(dataRecord, Description.VOLUME, DlmsUnit.CUBIC_METRE, (byte) -1, new Long(49150));

		dataRecord = new DataRecord(new byte[] { (byte) 0x84, (byte) 0x40 }, new byte[] { (byte) 0x15 }, new byte[] {
				(byte) 0xf8, (byte) 0xbf, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(dataRecord, Description.VOLUME, DlmsUnit.CUBIC_METRE, (byte) -1, new Long(49144));

		/* e0011nnn Mass kg */

		/* e01000nn On Time seconds/minutes/hours/days */
		dataRecord = new DataRecord(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x22 }, new byte[] { (byte) 0x38,
				(byte) 0x09, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(dataRecord, Description.ON_TIME, DlmsUnit.HOUR, (byte) 0, new Long(2360));

		/* e01001nn Operating Time seconds/minutes/hours/days */
		dataRecord = new DataRecord(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x26 }, new byte[] { (byte) 0x3d,
				(byte) 0x07, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(dataRecord, Description.OPERATING_TIME, DlmsUnit.HOUR, (byte) 0, new Long(1853));

		/* e10110nn Flow Temperature °C */
		dataRecord = new DataRecord(new byte[] { (byte) 0x02 }, new byte[] { (byte) 0x5a }, new byte[] { (byte) 0x79,
				(byte) 0x02 }, null);

		assertParsingResults(dataRecord, Description.FLOW_TEMPERATURE, DlmsUnit.DEGREE_CELSIUS, (byte) -1, new Long(
				(short) 633));

		/* e10111nn Return Temperature °C */
		dataRecord = new DataRecord(new byte[] { (byte) 0x02 }, new byte[] { (byte) 0x5e }, new byte[] { (byte) 0xa6,
				(byte) 0x01 }, null);

		assertParsingResults(dataRecord, Description.RETURN_TEMPERATURE, DlmsUnit.DEGREE_CELSIUS, (byte) -1, new Long(
				(short) 422));

		/* e11000nn Temperature Difference K */
		dataRecord = new DataRecord(new byte[] { (byte) 0x02 }, new byte[] { (byte) 0x62 }, new byte[] { (byte) 0xd3,
				(byte) 0x00 }, null);

		assertParsingResults(dataRecord, Description.TEMPERATURE_DIFFERENCE, DlmsUnit.KELVIN, (byte) -1, new Long(
				(short) 211));

		/* e1101101 Date and time - type F */
		dataRecord = new DataRecord(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x6d }, new byte[] { (byte) 0x2b,
				(byte) 0x11, (byte) 0x78, (byte) 0x11 }, null);

		try {
			dataRecord.decode();
			Date date = (Date) dataRecord.getDataValue();
			System.out.println(date.getTime());
		} catch (DecodingException e) {
			Assert.fail("Failed to parse!");
		}

		// assertParsingResults(vdb, Description.DATE_TIME, DLMSUnit.OTHER_UNIT, (byte) 0, new Date(1295887380276l));
	}

}
