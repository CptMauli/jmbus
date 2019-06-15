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

public class MBusLPduTest {

	@Test
	public void testParser() throws IOException {
		byte[] msg = MessagesTest.testMsg1;

		MBusLPdu lpdu = new MBusLPdu(msg);

		Assert.assertEquals(1, lpdu.getAField());

		System.out.println(lpdu.getAPDU().capacity());
	}

	@Test
	public void testParser2() throws IOException {
		byte[] msg = MessagesTest.testMsg4;

		MBusLPdu lpdu = new MBusLPdu(msg);

		Assert.assertEquals(0, lpdu.getAField());

		System.out.println(lpdu.getAPDU().capacity());
	}
}
