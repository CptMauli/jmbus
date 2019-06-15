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
 * The device type that is part of the data header of a Variable Data Response.
 * 
 */
public enum DeviceType {
	OTHER(0), OIL(1), ELECTRICITY(2), GAS(3), HEAT(4), STEAM(5), WARM_WATER(6), WATER(7), HEAT_COST_ALLOCATOR(8), COMPRESSED_AIR(
			9), COOLING_LOAD_METER_OUTLET(10), COOLING_LOAD_METER_INLET(11), HEAT_INLET(12), HEAT_COOLING_LOAD_METER(13), BUS_SYSTEM_COMPONENT(
			14), UNKNOWN_MEDIUM(15), HOT_WATER(21), COLD_WATER(22), DUAL_REGISTER_WATER_METER(23), PRESSURE(24), AD_CONVERTER(
			25), RESERVED_FOR_VALVE(33);
	private final int code;

	DeviceType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static DeviceType newDevice(int code) {
		switch (code) {
		case 0:
			return DeviceType.OTHER;
		case 1:
			return DeviceType.OIL;
		case 2:
			return DeviceType.ELECTRICITY;
		case 3:
			return DeviceType.GAS;
		case 4:
			return DeviceType.HEAT;
		case 5:
			return DeviceType.STEAM;
		case 6:
			return DeviceType.WARM_WATER;
		case 7:
			return DeviceType.WATER;
		case 8:
			return DeviceType.HEAT_COST_ALLOCATOR;
		case 9:
			return DeviceType.COMPRESSED_AIR;
		case 10:
			return DeviceType.COOLING_LOAD_METER_OUTLET;
		case 11:
			return DeviceType.COOLING_LOAD_METER_INLET;
		case 12:
			return DeviceType.HEAT_INLET;
		case 13:
			return DeviceType.HEAT_COOLING_LOAD_METER;
		case 14:
			return DeviceType.BUS_SYSTEM_COMPONENT;
		case 15:
			return DeviceType.UNKNOWN_MEDIUM;
		case 21:
			return DeviceType.HOT_WATER;
		case 22:
			return DeviceType.COLD_WATER;
		case 23:
			return DeviceType.DUAL_REGISTER_WATER_METER;
		case 24:
			return DeviceType.PRESSURE;
		case 25:
			return DeviceType.AD_CONVERTER;
		case 33:
			return DeviceType.RESERVED_FOR_VALVE;
		default:
			throw new IllegalArgumentException("Unknown Device Type code: " + code);
		}
	}

}
