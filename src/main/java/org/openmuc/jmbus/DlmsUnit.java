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

/**
 * The units as defined in IEC 62056-62.
 * 
 */
public enum DlmsUnit {
	// can be found in IEC 62056-62.
	YEAR(1), MONTH(2), WEEK(3), DAY(4), HOUR(5), MIN(6), SECOND(7), DEGREE(8), DEGREE_CELSIUS(9), CURRENCY(10), METRE(
			11), METRE_PER_SECOND(12), CUBIC_METRE(13), CUBIC_METRE_CORRECTED(14), CUBIC_METRE_PER_HOUR(15), CUBIC_METRE_PER_HOUR_CORRECTED(
			16), CUBIC_METRE_PER_DAY(17), CUBIC_METRE_PER_DAY_CORRECTED(18), LITRE(19), KILOGRAM(20), NEWTON(21), NEWTONMETER(
			22), PASCAL(23), BAR(24), JOULE(25), JOULE_PER_HOUR(26), WATT(27), VOLT_AMPERE(28), VAR(29), WATT_HOUR(30), VOLT_AMPERE_HOUR(
			31), VAR_HOUR(32), AMPERE(33), COULOMB(34), VOLT(35), VOLT_PER_METRE(36), FARAD(37), OHM(38), OHM_METRE(39), WEBER(
			40), TESLA(41), AMPERE_PER_METRE(42), HENRY(43), HERTZ(44), ACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE(45), REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE(
			46), APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE(47), VOLT_SQUARED_HOURS(48), AMPERE_SQUARED_HOURS(49), KILOGRAM_PER_SECOND(
			50), KELVIN(52), VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE(53), AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE(
			54), METER_CONSTANT_OR_PULSE_VALUE(55), PERCENTAGE(56), AMPERE_HOUR(57), ENERGY_PER_VOLUME(60), CALORIFIC_VALUE(
			61), MOLE_PERCENT(62), MASS_DENSITY(63), PASCAL_SECOND(64), RESERVED(253), OTHER_UNIT(254), COUNT(255),
	// not mentioned in 62056), added for MBus:
	CUBIC_METRE_PER_SECOND(150), CUBIC_METRE_PER_MINUTE(151), KILOGRAM_PER_HOUR(152);

	private final int code;

	DlmsUnit(int code) {
		this.code = code;
	}

	/**
	 * Returns the code of the DLMS unit as defined in IEC 62056-62
	 * 
	 * @return the code of the DLMS unit.
	 */
	public int getCode() {
		return code;
	}

	public String getAbbreviation() {
		switch (code) {
		case 27:
			return "W";
		case 30:
			return "Wh";
		case 35:
			return "V";
		case 33:
			return "A";
		case 28:
			return "VA";
		case 44:
			return "Hz";
		case 5:
			return "h";
		case 9:
			return "°C";
		case 8:
			return "°";
		case 13:
			return "m^3";
		case 15:
			return "m^3/h";
		case 4:
			return "d";
		case 56:
			return "%";
		case 6:
			return "min";
		case 7:
			return "s";
		case 19:
			return "l";
		case 12:
			return "m/s";
		case 151:
			return "m^3/m";
		case 150:
			return "m^3/s";
		default:
			return "unknown";
		}
	}
}
