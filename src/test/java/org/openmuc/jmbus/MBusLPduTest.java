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

public class MBusLPduTest {

	@Test
	public void testParser() throws IOException, DecodingException {
		byte[] msg = MessagesTest.testMsg1;

		MBusMessage mBusMessage = new MBusMessage(msg);
		mBusMessage.decode();

		Assert.assertEquals(1, mBusMessage.getAddressField());

	}

	@Test
	public void testParser2() throws IOException, DecodingException {
		byte[] msg = MessagesTest.testMsg4;

		MBusMessage mBusMessage = new MBusMessage(msg);
		mBusMessage.decode();

		Assert.assertEquals(0, mBusMessage.getAddressField());

	}
}
