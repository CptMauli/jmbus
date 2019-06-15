/*
 * Copyright 2010-13 Fraunhofer ISE
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

Authors: Michael Zillgith & Stefan Feuerhahn

jMBus is a Java library implementing an M-Bus (Meter-Bus) master.  It
can be used to read metering data from M-Bus slaves such as gas,
water, heat, or electricity meters. The software has been tested under
Linux but should also work on Windows with little or no modifications.

The software has been tested with a few meters but does not support
all possible meters yet.  We are thankful for any code contributions.

-The library and its dependencies are found in the folders
 "build/libs/" and "dependencies"
-Javadocs can be found in the build/docs folder

For the latest release of this software visit http://www.openmuc.org .


Using the example:
------------------
For an example on how to use jMBus see src/sample/java/ReadMeter.java

You can create Eclipse project files as explained here:
http://www.openmuc.org/index.php?id=28 and run the sample from within
Eclipse or you can compile and execute it in a terminal. To compile
and execute the sample on Linux use something like this:
go to the folder src/sample/java/
>javac -cp "../../../build/libs/jmbus-<version>.jar" ReadMeter.java
>java -cp "../../../build/libs/jmbus-<version>.jar:../../../dependencies/rxtxcomm_api-2.2pre2-11_bundle.jar:./" ReadMeter /dev/ttyUSB0 p1

Note that jMBus depends on the Java Library RXTXcomm. This library in
turn depends on librxtxSerial.so under Linux.

You can activate debug information if the system property
org.openmuc.jmbus.debug exists.  
e.g. java -Dorg.openmuc.jmbus.debug -cp ... MyMBusApplication


Develop jMBus:
--------------
Please go to http://www.openmuc.org/index.php?id=28 for information on
how to rebuild the library after you have modified it and how to
generate Eclipse project files.

Please send us any code improvements so we can integrate them in our
distribution.


Information on M-Bus:
---------------------
For more information on M-Bus see
http://www.m-bus.com
DIN-EN-13757 parts 1-4
IEC-870-5 parts 1-2
