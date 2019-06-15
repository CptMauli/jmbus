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

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;
import org.openmuc.jmbus.VariableDataBlock.Description;

public class VdbParserTest extends TestCase {

	public void testINT64() {
		VariableDataBlock vdb = new VariableDataBlock(new byte[] { (byte) 0x07 }, new byte[] { (byte) 0x04 },
				new byte[] { (byte) 0x12, (byte) 0x23, (byte) 0x34, (byte) 0x45, (byte) 0x56, (byte) 0x67, (byte) 0x78,
						(byte) 0x12 }, null);

		try {
			vdb.parse();

			Object obj = vdb.getData();

			assertEquals(obj instanceof Long, true);

			Long val = (Long) obj;

			System.out.println(val);

			assertEquals(new Long(1330927310113874706l), val);

		} catch (ParseException e) {
			fail("Unexpected exception");
		}

		vdb = new VariableDataBlock(new byte[] { (byte) 0x07 }, new byte[] { (byte) 0x04 }, new byte[] { (byte) 0xFF,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF }, null);

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

		byte[] data = new byte[] { (byte) 0xe4, (byte) 0x05, (byte) 0x00, (byte) 0x00 };

		VariableDataBlock vdb = new VariableDataBlock(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x03 }, data,
				null);

		try {
			vdb.parse();

			Object obj = vdb.getData();

			assertEquals(true, obj instanceof Long);

			Long integer = (Long) obj;

			assertEquals(new Long(1508), integer);

		} catch (ParseException e) {
			fail("Failed to parse!");
		}

		data = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };

		vdb = new VariableDataBlock(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x03 }, data, null);

		try {
			vdb.parse();

			Object obj = vdb.getData();

			assertEquals(true, obj instanceof Long);

			Long integer = (Long) obj;

			assertEquals(new Long(-1), integer);

		} catch (ParseException e) {
			fail("Failed to parse!");
		}

	}

	private void assertParsingResults(VariableDataBlock vdb, Description desc, DlmsUnit unit, byte scaler, Object data) {
		try {
			vdb.parse();
		} catch (ParseException e) {
			fail("Failed to parse!");
		}

		assertEquals(desc, vdb.getDescription());
		assertEquals(unit, vdb.getUnit());
		assertEquals(scaler, vdb.getMultiplier());
		assertEquals(data, vdb.getData());
	}

	@Test
	public void testVDBs() {
		/* e0000nnn Energy Wh */
		VariableDataBlock vdb = new VariableDataBlock(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x07 },
				new byte[] { (byte) 0xc8, (byte) 0x1e, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(vdb, Description.ENERGY, DlmsUnit.WATT_HOUR, (byte) 4, new Long(7880));

		/* e0001nnn Energy J */

		/* e0010nnn Volume m^3 */
		vdb = new VariableDataBlock(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x15 }, new byte[] { (byte) 0xfe,
				(byte) 0xbf, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(vdb, Description.VOLUME, DlmsUnit.CUBIC_METRE, (byte) -1, new Long(49150));

		vdb = new VariableDataBlock(new byte[] { (byte) 0x84, (byte) 0x40 }, new byte[] { (byte) 0x15 }, new byte[] {
				(byte) 0xf8, (byte) 0xbf, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(vdb, Description.VOLUME, DlmsUnit.CUBIC_METRE, (byte) -1, new Long(49144));

		/* e0011nnn Mass kg */

		/* e01000nn On Time seconds/minutes/hours/days */
		vdb = new VariableDataBlock(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x22 }, new byte[] { (byte) 0x38,
				(byte) 0x09, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(vdb, Description.ON_TIME, DlmsUnit.HOUR, (byte) 0, new Long(2360));

		/* e01001nn Operating Time seconds/minutes/hours/days */
		vdb = new VariableDataBlock(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x26 }, new byte[] { (byte) 0x3d,
				(byte) 0x07, (byte) 0x00, (byte) 0x00 }, null);

		assertParsingResults(vdb, Description.OPERATING_TIME, DlmsUnit.HOUR, (byte) 0, new Long(1853));

		/* e10110nn Flow Temperature °C */
		vdb = new VariableDataBlock(new byte[] { (byte) 0x02 }, new byte[] { (byte) 0x5a }, new byte[] { (byte) 0x79,
				(byte) 0x02 }, null);

		assertParsingResults(vdb, Description.FLOW_TEMPERATURE, DlmsUnit.DEGREE_CELSIUS, (byte) -1, new Long(
				(short) 633));

		/* e10111nn Return Temperature °C */
		vdb = new VariableDataBlock(new byte[] { (byte) 0x02 }, new byte[] { (byte) 0x5e }, new byte[] { (byte) 0xa6,
				(byte) 0x01 }, null);

		assertParsingResults(vdb, Description.RETURN_TEMPERATURE, DlmsUnit.DEGREE_CELSIUS, (byte) -1, new Long(
				(short) 422));

		/* e11000nn Temperature Difference K */
		vdb = new VariableDataBlock(new byte[] { (byte) 0x02 }, new byte[] { (byte) 0x62 }, new byte[] { (byte) 0xd3,
				(byte) 0x00 }, null);

		assertParsingResults(vdb, Description.TEMPERATURE_DIFFERENCE, DlmsUnit.KELVIN, (byte) -1, new Long((short) 211));

		/* e1101101 Date and time - type F */
		vdb = new VariableDataBlock(new byte[] { (byte) 0x04 }, new byte[] { (byte) 0x6d }, new byte[] { (byte) 0x2b,
				(byte) 0x11, (byte) 0x78, (byte) 0x11 }, null);

		try {
			vdb.parse();
			Date date = (Date) vdb.getData();
			System.out.println(date.getTime());
		} catch (ParseException e) {
			fail("Failed to parse!");
		}

		// assertParsingResults(vdb, Description.DATE_TIME, DLMSUnit.OTHER_UNIT, (byte) 0, new Date(1295887380276l));
	}

}
