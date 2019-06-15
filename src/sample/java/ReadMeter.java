import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.DlmsUnit;
import org.openmuc.jmbus.MBusSap;
import org.openmuc.jmbus.Util;
import org.openmuc.jmbus.VariableDataBlock;
import org.openmuc.jmbus.VariableDataResponse;

public class ReadMeter {

	/**
	 * @param args
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, TimeoutException {
		if (args.length < 2) {
			System.out.println("Usage: ReadMeter <comPort> <meter_address>");
			System.out
					.println("Example: \"ReadMeter /dev/ttyS0 p1\" to address a meter with primary address 1 in Unix");
			System.out.println("use sxxxxxxxx for secondary addressing (00000000 - 99999999)");
			System.exit(1);
		}

		String device = args[0];
		String address = args[1];

		MBusSap connection = new MBusSap(device, "2400/8E1", 1000);

		VariableDataResponse response;
		try {
			response = connection.readMeter(address);
		} catch (TimeoutException e) {
			System.err.println("Timeout reading meter. Will quit!");
			connection.closeSerialPort();
			return;
		}

		System.out.println("Vendor ID: " + Util.vendorID(response.getManufacturerID()));

		List<VariableDataBlock> blocks = response.getVariableDataBlocks();

		for (VariableDataBlock dataBlock : blocks) {
			System.out.print("DIB:" + Util.composeHexStringFromByteArray(dataBlock.getDIB()) + ", VIB:"
					+ Util.composeHexStringFromByteArray(dataBlock.getVIB()));

			try {
				dataBlock.parse();
			} catch (ParseException e) {
				System.out.println(", failed to parse: " + e.getMessage());
				continue;
			}
			if (dataBlock.getDescription() != null) {

				System.out.print(", descr:" + dataBlock.getDescription() + ", function:" + dataBlock.getFunctionField()
						+ ", data type:" + dataBlock.getDataType() + ", value:");

				switch (dataBlock.getDataType()) {
				case DATE:
					System.out.println(dataBlock.getData());
				case STRING:
					System.out.println((String) dataBlock.getData());
					break;
				case DOUBLE:
					System.out.println(dataBlock.getData() + ", multiplier:" + dataBlock.getMultiplier()
							+ ", scaled data:" + dataBlock.getScaledValue() + " unit:"
							+ DlmsUnit.getString(dataBlock.getUnit()));
					break;
				case LONG:
					System.out.println(dataBlock.getData() + ", multiplier:" + dataBlock.getMultiplier()
							+ ", scaled data:" + dataBlock.getScaledValue() + " unit:"
							+ DlmsUnit.getString(dataBlock.getUnit()));
					break;

				}
			}

		}

		if (response.hasManufacturerData()) {
			System.out.println("Manufacturer specific data follows:");
			int j = 0;
			for (byte element : response.manufacturerData) {
				System.out.printf("0x%02x ", element);
				j++;
				if (j % 10 == 0) {
					System.out.println();
				}
			}
		}

		System.out.println();

		connection.closeSerialPort();

		System.exit(0);

	}

}
