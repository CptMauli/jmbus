import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.DlmsUnit;
import org.openmuc.jmbus.Util;
import org.openmuc.jmbus.VariableDataBlock;
import org.openmuc.jmbus.VariableDataResponse;
import org.openmuc.jmbus.internal.MBusLPdu;

public class ParseResponseDump {

	public static void main(String[] args) throws IOException, TimeoutException {

		boolean showVDBConstructor = false;

		if (args.length > 0) {
			if (args[0].equals("-t")) {
				showVDBConstructor = true;
			}
		}

		StringBuffer hexString = new StringBuffer(200);

		InputStream in = System.in;

		char c;

		while (in.available() > 0) {
			c = (char) in.read();
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
				hexString.append(c);
			}
		}

		in.close();

		byte[] message = Util.createByteArrayFromString(hexString.toString());

		MBusLPdu lpdu = new MBusLPdu(message);

		try {
			lpdu.parse();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		VariableDataResponse vdr = new VariableDataResponse();

		try {
			vdr.parse(lpdu.getAPDU());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		for (VariableDataBlock vdb : vdr.getVariableDataBlocks()) {

			if (showVDBConstructor) {

				// VariableDataBlock vdb = new VariableDataBlock(
				// new byte[] {(byte) 0x0e},
				// new byte[] {(byte) 0x04},
				// new byte[] {(byte) 0x10, (byte) 0x17, 0x00, 0x00, 0x00 , 0x00},
				// null);

				System.out.println("VariableDataBlock vdb = new VariableDataBlock(");

				// print DIB
				System.out.print("\tnew byte[] {");
				byte[] dib = vdb.getDIB();
				for (int i = 0; i < dib.length; i++) {
					if (i != dib.length - 1) {
						System.out.printf("(byte) 0x%02x, ", dib[i]);
					}
					else {
						System.out.printf("(byte) 0x%02x", dib[i]);
					}
				}
				System.out.println("},");

				// print VIB
				System.out.print("\tnew byte[] {");
				byte[] vib = vdb.getVIB();
				for (int i = 0; i < vib.length; i++) {
					if (i != vib.length - 1) {
						System.out.printf("(byte) 0x%02x, ", vib[i]);
					}
					else {
						System.out.printf("(byte) 0x%02x", vib[i]);
					}
				}
				System.out.println("},");

				// print data
				System.out.print("\tnew byte[] {");
				byte[] data = vdb.getDataBytes();
				for (int i = 0; i < data.length; i++) {
					if (i != data.length - 1) {
						System.out.printf("(byte) 0x%02x, ", data[i]);
					}
					else {
						System.out.printf("(byte) 0x%02x", data[i]);
					}
				}
				System.out.println("},");

				if (vdb.getLvar() == null) {
					System.out.println("\tnull);");
				}
				else {
					System.out.printf("\t%d);\n", vdb.getLvar());
				}

			}
			else {
				String difVif = Util.composeHexStringFromByteArray(vdb.getDIB()) + "/"
						+ Util.composeHexStringFromByteArray(vdb.getVIB());

				System.out.print(difVif + " ");

				try {
					vdb.parse();

					if (vdb.getDescription() != null) {
						System.out.print(vdb.getDescription().toString());
					}

					if (vdb.getData() != null) {
						System.out.print(" Value: " + vdb.getData().toString());
					}

					System.out.print(" Scaler: " + vdb.getMultiplier());

					System.out.print(" Unit: " + DlmsUnit.getString(vdb.getUnit()));

					System.out.println();
				} catch (ParseException e) {
					System.out.println(e.getMessage());
				}
			}
		}

		if (vdr.hasManufacturerData()) {
			System.out.print("Manufacturer specific data: ");
			System.out.println(Util.composeHexStringFromByteArray(vdr.getManufacturerData()));
		}
	}
}
