import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.Util;
import org.openmuc.jmbus.internal.MBusLPdu;
import org.openmuc.jmbus.internal.MBusLSap;

public class DumpMeterResponse {
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out.println("Usage: DumpMeterResponse <device/device-file> <meter_address>");
			System.out
					.println("Example: \"DumpMeterResponse /dev/ttyUSB0 p1\" to address a meter with primary address 1");
			System.out.println("use sxxxxxxxx for secondary addressing (00000000 - 99999999)");
			System.exit(1);
		}

		String device = args[0];
		String address = args[1];

		MBusLSap mBusLSAP = new MBusLSap();
		mBusLSAP.configureSerialPort(device, "2400-8N1");

		try {
			if (address.charAt(0) == 'p') {
				mBusLSAP.sendShortMessage(Util.getAddress(address), 0x5b);
			}
			else {
				System.out.println(address);
				System.out.println(Util.getAddress(address));
				if (mBusLSAP.selectComponent(Util.getAddress(address), (short) 0xffff, (byte) 0xff, (byte) 0xff, 1000)) {
					System.out.println("Selected!");
					mBusLSAP.sendShortMessage(0xfd, 0x5b);
					// mBusLSAP.sendShortMessage(0xfd, 0x7b);
				}
				else {
					// select timeout
					System.out.println("Select failed!");
					throw new IOException("unable to select component");
				}
			}

			MBusLPdu lpdu = mBusLSAP.receiveMessage(1000);

			byte[] message = lpdu.getRawMessage();

			for (int i = 1; i <= message.length; i++) {
				System.out.printf("%02x", message[i - 1]);

				if ((i % 16 == 0) || (i == message.length)) {
					System.out.println();
				}
				else {
					System.out.print(" ");
				}
			}
		} catch (TimeoutException e) {
			System.err.println(e.getMessage());
		} finally {
			mBusLSAP.closeSerialPort();
		}

		System.exit(0);
	}
}
