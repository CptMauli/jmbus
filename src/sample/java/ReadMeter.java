import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openmuc.jmbus.DLMSUnit;
import org.openmuc.jmbus.MBusASAP;
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
			System.out.println("Usage: ReadMeter <device/device-file> <meter_address>");
			System.out.println("Example: \"ReadMeter /dev/ttyUSB0 p1\" to address a meter with primary address 1");
			System.out.println("use sxxxxxxxx for secondary addressing (00000000 - 99999999)");
			System.exit(1);
		}

		String device = args[0];
		String address = args[1];

		MBusASAP connection = new MBusASAP(device, "2400-8N1");

		VariableDataResponse response = connection.readMeter(address);

		System.out.println("Vendor ID: " + Util.vendorID(response.getManufacturerID()));

		List<VariableDataBlock> blocks = response.getVariableDataBlocks();

		for (VariableDataBlock dataBlock : blocks) {
			System.out.print("DIF/VIF: " + Util.composeHexStringFromByteArray(dataBlock.getDIB()) + "/"
					+ Util.composeHexStringFromByteArray(dataBlock.getVIB()) + "  ");

			try {
				dataBlock.parse();
				if (dataBlock.getDescription() != null) {

					System.out.println(dataBlock.getDescription().toString() + " "
							+ dataBlock.getFunctionField().toString() + ": " + dataBlock.getData().toString() + " "
							+ DLMSUnit.getString(dataBlock.getUnit()));
				}
			} catch (ParseException e) {
				System.out.println("Failed to parse: " + e.getMessage());
			}

		}

		System.out.println();

		System.exit(0);

	}

}
