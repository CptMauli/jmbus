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
import static org.junit.Assert.fail;

import java.io.IOException;

public class MBusCommSetup {
	static Process procTTY;
	static Process procMBus;
	static String userDir;
	static String nativeLibPath = "";
	static String pathSeperator;
	static String mBusServerStr = "SimpleMBusServer";
	static String mBusSrvPort = "/dev/ttyS99";
	public static String myMBusPort = "/dev/ttyS100";
	static String myMBusAdr = "p5";
	private static String OS = System.getProperty("os.name").toLowerCase();
	private static String DM = System.getProperty("sun.arch.data.model").toLowerCase();

	public static void setUpMBusTest() {
		System.out.println("Set up MBus integration test now!");
		try {
			// set up some useful paths
			pathSeperator = System.getProperty("path.separator");

			// path to jmbus base folder
			userDir = System.getProperty("user.dir");
			System.out.println("user.dir: " + userDir);

			// path to itest
			nativeLibPath = userDir + "/src/itest";

			String libPaths = System.getProperty("java.library.path");
			System.out.println("\nYour current 'java.library.path' is: " + libPaths + "\n");

		} catch (Exception e) {
			fail("" + e);
		}

		// start tty0tty
		startTty0tty();

	}

	public static void shutDownMBusTest() {
		System.out.println("Shut down MBus integration test now!");

		try {
			System.out.println("Stop 'tty0tty" + DM + "' now!");
			procTTY.destroy();
		} catch (Exception e) {
			System.err.println("Error during jUnit testcase setup: " + e);
		}

	}

	private static void startTty0tty() {
		System.out.println("Start serial port tunnel app 'tty0tty" + DM + "' now!");
		try {

			// kill old instances of 'tty0tty'
			Process proc = Runtime.getRuntime().exec("killall tty0tty" + DM);
			try {
				proc.waitFor();
			} catch (Exception e) {
			}

			// start tty0tty
			procTTY = Runtime.getRuntime().exec(
					userDir + "/src/itest/tty0tty/tty0tty" + DM + " " + mBusSrvPort + " " + myMBusPort);

		} catch (IOException e) {
			System.err.println("These commands may be needed:");
			System.err.println("sudo chown root:$USER /dev");
			System.err.println("sudo chmod 775 /dev");
			fail("" + e);
		}
	}

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isLinux() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

}
