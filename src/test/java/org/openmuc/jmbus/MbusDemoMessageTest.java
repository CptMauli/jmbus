/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.openmuc.jmbus;

import org.junit.Test;

public class MbusDemoMessageTest {

    @Test
    public void testQundis_WTT16_msg1() throws Exception {
        byte[] msgByte = new byte[] { (byte) 0x68, (byte) 0x40, (byte) 0x40, (byte) 0x68, (byte) 0x08, (byte) 0x00,
                (byte) 0x72, (byte) 0x71, (byte) 0x22, (byte) 0x23, (byte) 0x10, (byte) 0x65, (byte) 0x32, (byte) 0x18,
                (byte) 0x0E, (byte) 0x17, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x22, (byte) 0x22,
                (byte) 0x37, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x6D, (byte) 0x30, (byte) 0x10, (byte) 0xDA,
                (byte) 0x19, (byte) 0x06, (byte) 0xFD, (byte) 0x0C, (byte) 0x18, (byte) 0x00, (byte) 0x0E, (byte) 0x00,
                (byte) 0x22, (byte) 0x03, (byte) 0x0D, (byte) 0xFD, (byte) 0x0B, (byte) 0x05, (byte) 0x36, (byte) 0x31,
                (byte) 0x54, (byte) 0x54, (byte) 0x57, (byte) 0x32, (byte) 0x6C, (byte) 0xFF, (byte) 0xFF, (byte) 0x02,
                (byte) 0xFA, (byte) 0x3D, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x7C, (byte) 0x06, (byte) 0x54,
                (byte) 0x54, (byte) 0x41, (byte) 0x42, (byte) 0x20, (byte) 0x25, (byte) 0x61, (byte) 0x43,
                (byte) 0x16 };
        MBusMessage.decode(msgByte, msgByte.length);
    }

    @Test
    public void testQundis_WTT16_msg2() throws Exception {
        byte[] msgByte = new byte[] { (byte) 0x68, (byte) 0x40, (byte) 0x40, (byte) 0x68, (byte) 0x08, (byte) 0x00,
                (byte) 0x72, (byte) 0x71, (byte) 0x22, (byte) 0x23, (byte) 0x10, (byte) 0x65, (byte) 0x32, (byte) 0x18,
                (byte) 0x0E, (byte) 0x1E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x22, (byte) 0x23,
                (byte) 0x37, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x6D, (byte) 0x36, (byte) 0x11, (byte) 0xDA,
                (byte) 0x19, (byte) 0x06, (byte) 0xFD, (byte) 0x0C, (byte) 0x18, (byte) 0x00, (byte) 0x0E, (byte) 0x00,
                (byte) 0x22, (byte) 0x03, (byte) 0x0D, (byte) 0xFD, (byte) 0x0B, (byte) 0x05, (byte) 0x36, (byte) 0x31,
                (byte) 0x54, (byte) 0x54, (byte) 0x57, (byte) 0x32, (byte) 0x6C, (byte) 0xFF, (byte) 0xFF, (byte) 0x02,
                (byte) 0xFA, (byte) 0x3D, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x7C, (byte) 0x06, (byte) 0x54,
                (byte) 0x54, (byte) 0x41, (byte) 0x42, (byte) 0x20, (byte) 0x25, (byte) 0x61, (byte) 0x52,
                (byte) 0x16 };
        MBusMessage.decode(msgByte, msgByte.length);
    }

    @Test
    public void testQundis_WTT16_msg3() throws Exception {
        byte[] msgByte = new byte[] { (byte) 0x68, (byte) 0x40, (byte) 0x40, (byte) 0x68, (byte) 0x08, (byte) 0x00,
                (byte) 0x72, (byte) 0x71, (byte) 0x22, (byte) 0x23, (byte) 0x10, (byte) 0x65, (byte) 0x32, (byte) 0x18,
                (byte) 0x0E, (byte) 0x1F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x22, (byte) 0x23,
                (byte) 0x37, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x6D, (byte) 0x0D, (byte) 0x12, (byte) 0xDA,
                (byte) 0x19, (byte) 0x06, (byte) 0xFD, (byte) 0x0C, (byte) 0x18, (byte) 0x00, (byte) 0x0E, (byte) 0x00,
                (byte) 0x22, (byte) 0x03, (byte) 0x0D, (byte) 0xFD, (byte) 0x0B, (byte) 0x05, (byte) 0x36, (byte) 0x31,
                (byte) 0x54, (byte) 0x54, (byte) 0x57, (byte) 0x32, (byte) 0x6C, (byte) 0xFF, (byte) 0xFF, (byte) 0x02,
                (byte) 0xFA, (byte) 0x3D, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x7C, (byte) 0x06, (byte) 0x54,
                (byte) 0x54, (byte) 0x41, (byte) 0x42, (byte) 0x20, (byte) 0x25, (byte) 0x61, (byte) 0x2B,
                (byte) 0x16 };
        MBusMessage.decode(msgByte, msgByte.length);
    }
}
