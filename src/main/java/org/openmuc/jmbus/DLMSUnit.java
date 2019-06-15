package org.openmuc.jmbus;

/*
 * Copyright Fraunhofer ISE, 2010
 * Author(s): Michael Zillgith
 *            Stefan Feuerhahn
 *    
 * This file is part of jMBus.
 * For more information visit http://www.openmuc.org
 * 
 * jMBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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

public class DLMSUnit {
	// Zahlenwerte von DLMS-Unit-List, zu finden beispielsweise in IEC 62056-62.
	public static final int YEAR=1, MONTH=2, WEEK=3, DAY=4, HOUR=5, MIN=6, SECOND=7;
	public static final int DEGREE=8, DEGREE_CELSIUS=9, CURRENCY=10, METRE=11, METRE_PER_SECOND=12;
	public static final int CUBIC_METRE=13, CUBIC_METRE_CORRECTED=14, CUBIC_METRE_PER_HOUR = 15;
	public static final int CUBIC_METRE_PER_HOUR_CORRECTED = 16,CUBIC_METRE_PER_DAY = 17;
	public static final int CUBIC_METRE_PER_DAY_CORRECTED=18, LITRE=19, KILOGRAM=20, NEWTON=21;
	public static final int NEWTONMETER=22, PASCAL=23, BAR=24, JOULE=25, JOULE_PER_HOUR=26, WATT=27;
	public static final int VOLT_AMPERE=28, VAR=29, WATT_HOUR=30, VOLT_AMPERE_HOUR=31, VAR_HOUR=32;
	public static final int AMPERE=33, COULOMB=34, VOLT=35, VOLT_PER_METRE=36, FARAD=37, OHM=38;
	public static final int OHM_METRE=39, WEBER=40, TESLA=41, AMPERE_PER_METRE=42, HENRY=43, HERTZ=44;
	public static final int ACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE=45;
	public static final int REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE=46;
	public static final int APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE=47;
	public static final int VOLT_SQUARED_HOURS=48, AMPERE_SQUARED_HOURS=49, KILOGRAM_PER_SECOND=50;
	public static final int KELVIN=52, VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE=53;
	public static final int AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE=54;
	public static final int METER_CONSTANT_OR_PULSE_VALUE=55, PERCENTAGE=56, AMPERE_HOUR=57;
	public static final int ENERGY_PER_VOLUME=60, CALORIFIC_VALUE=61, MOLE_PERCENT=62, MASS_DENSITY=63;
	public static final int PASCAL_SECOND=64, RESERVED=253, OTHER_UNIT=254, COUNT=255;
	//not mentioned in 62056, added for MBus:
	public static final int CUBIC_METRE_PER_SECOND=150, CUBIC_METRE_PER_MINUTE=151, KILOGRAM_PER_HOUR=152;
	
	public static String getString(int unit) {
		switch (unit) {
		case WATT:
			return "W";
		case WATT_HOUR:
			return "Wh";
		case VOLT:
			return "V";
		case AMPERE:
			return "A";
		case VOLT_AMPERE:
			return "VA";
		case VAR:
			return "VAR";
		case HERTZ:
			return "Hz";
		case HOUR:
			return "h";
		case DEGREE_CELSIUS:
			return "°C";
		case DEGREE:
			return "°";
		case CUBIC_METRE:
			return "m^3";
		case CUBIC_METRE_PER_HOUR:
			return "m^3/h";
		case DAY:
			return "d";
		case PERCENTAGE:
			return "%";
		case MIN:
			return "min";
		case SECOND:
			return "s";
		case LITRE:
			return "l";
		case METRE_PER_SECOND:
			return "m/s";
		case CUBIC_METRE_PER_MINUTE:
			return "m^3/m";
		case CUBIC_METRE_PER_SECOND:
			return "m^3/s";
		default:
			return "(unkown)";
		}
	}
}
