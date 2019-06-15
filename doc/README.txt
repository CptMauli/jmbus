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

Authors: Stefan Feuerhahn, Peter Feith & Michael Zillgith

jMBus is a Java library implementing an M-Bus (Meter-Bus) master.  It
can be used to read metering data from M-Bus slaves such as gas,
water, heat, or electricity meters.

-The library and its dependencies are found in the folders
 "build/libsdeps/" and "dependencies"
-Javadocs can be found in the build/javadoc folder

For the latest release of this software visit http://www.openmuc.org .

Please send us any code improvements so we can integrate them in our
distribution.

For more information on M-Bus see
http://www.m-bus.com
DIN-EN-13757 parts 1-4
IEC-870-5 parts 1-2


ReadMeter & ScanForMeters Applications:
---------------------------------------

The library includes an application that reads a given meter and
prints the received data to stdout. This application can also be
used as an example on how to use the the library. You can find the
source code of ReadMeter.java in src/main/java/org/openmuc/jmbus/app/.

You can can execute ReadMeter using the scripts found in the
"runscripts" folder or from the console with the following command
(from this projects root directory):

Linux/Unix:
>java -cp "build/libs/jmbus-<version>.jar:dependencies/rxtxcomm_api-2.2pre2-11_bundle.jar" org.openmuc.jmbus.app.ReadMeter
Windows:
>java -cp "build\libs\jmbus-<version>.jar;dependencies\rxtxcomm_api-2.2pre2-11_bundle.jar" org.openmuc.jmbus.app.ReadMeter

Sometimes you might have to add a system property so that java finds
the jni libs. e.g.: -Djava.library.path=/usr/lib/jni

In addition to the library includes the
org.openmuc.jmbus.app.ScanForMeters application that can be used to
scan all 250 primary addresses for meters. Run it using the script in
the runscripts folder.

Instead of running the applications from the console you can create
Eclipse project files as explained here:
http://www.openmuc.org/index.php?id=28 and run them from within
Eclipse.

RXTX - Java library for serial communication
--------------------------------------------

For the most up to date information on how to get RXTX to work take a
look at the question on 'serial communication" in our FAQs:
http://www.openmuc.org/index.php?id=72

jMBus depends on the Java library RXTX (Copyright 1997-2004 by Trent
Jarvi) for accessing the serial port (UART).  RXTX is licensed under
LGPL(v2 or later) and is usually located in the dependencies folder of
our distributions.

The main RXTX website rxtx.qbang.org is currently down but the project
is still actively supported by the Debian package maintainer. The
source code can be obtained from
http://anonscm.debian.org/gitweb/?p=pkg-java/rxtx.git. RXTX has a Java
part and a native part:

RXTX Java part:

The Java part is the file 'rxtxcomm_api-XXXX_bundle.jar' that can be
found in the 'dependencies' folder inside a projects distribution
file. Beware there exist several different versions of
rxtxcomm-2.2pre2.  The version of RXTX we use was taken from Debian
and has many bug fixes compared to the latest version released on
rxtx.qbang.org.  We made this library a bundle using bnd: "java -jar
bnd/bnd-1.50.0.jar wrap RXTXcomm-2.2pre2.jar".

RXTX native part:

Every RXTX native binary is specific for a certain processor
architecture and OS. Before you can run an application that depends on
RXTX you have to install the correct native file for your OS. In
Debian based distributions all you have to do is install the package
'librxtx-java' using your package manager. This will install the
correct native library.

If you use another system (e.g. Windows) you need to copy the native
library of RXTX for your specific system to the folder that is in the
java.libary.path. To figure out the actual Java library path in your
system, you can write a Java program that prints
System.getProperty('java.library.path').

You can get the native library either from the last distribution
published by rxtx.qbang.org that is located in the
'dependencies/rxtx-2.2pre2-bins.zip' inside the distribution or you
can compile it on your own from the source code as explained next.

Compile RXTX:

You can use the source code from the 'dependencies' folder in the
distribution. This is a snaphot from
http://anonscm.debian.org/gitweb/?p=pkg-java/rxtx.git. Apply all the
source patches by Debian as explained in the Debian patch guide:
http://cs-people.bu.edu/doucette/xia/guides/debian-patch.txt.


Develop jMBus:
--------------

We use the Gradle build automation tool. The distribution contains a
fully functional gradle build file ("build.gradle"). Thus if you
changed code and want to rebuild a library you can do it easily with
Gradle. Also if you want to import our software into Eclipse you can
easily create Eclipse project files using Gradle. Just follow these
instructions (for the most up to date version of these instructions
visit http://www.openmuc.org/index.php?id=72#faq_gradle):

Install Gradle: 

* Download Gradle from the website: www.gradle.org

* Set the PATH variable: e.g. in Linux add to your .bashrc: export
  PATH=$PATH:/home/user/path/to/gradle-version/bin

* Gradle will automatically download some dependencies from Maven
  Central. Thererfore if you are behind a proxy you should set the
  proxy options in the gradle.properties file as explained here:
  http://www.gradle.org/docs/current/userguide/build_environment.html.

* We use OpenJDK 6 to compile our projects. If you have several JDKs
  installed you may want to set the org.gradle.java.home property in
  the gradle.properties file.

* Setting "org.gradle.daemon=true" in gradle.properties will speed up
  Gradle

Create Eclipse project files using Gradle:

* with the command "gradle eclipse" you can generate Eclipse project
  files

* It is important to add the GRADLE_USER_HOME variable in Eclipse:
  Window->Preferences->Java->Build Path->Classpath Variable. Set it to
  the path of the .gradle folder in your home directory
  (e.g. /home/someuser/.gradle (Unix) or C:/Documents and
  Settings/someuser/.gradle (Windows))

Rebuild a library:

* After you have modified the code you can completely rebuild the code
  using the command "gradle clean build" This will also execute the
  junit tests.

* You can also assemble a new distribution tar file: the command
  "gradle clean tar" will build everything and put a new distribution
  file in the folder "build/distribution".
