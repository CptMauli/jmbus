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
package org.openmuc.jmbus;

/**
 * The device type that is part of the data header of a Variable Data Response.
 * 
 * @author Stefan Feuerhahn
 * 
 */
public enum DeviceType {
	OTHER(0x00),
	OIL_METER(0x01),
	ELECTRICITY_METER(0x02),
	GAS_METER(0x03),
	HEAT_METER(0x04),
	STEAM_METER(0x05),
	WARM_WATER_METER(0x06),
	WATER_METER(0x07),
	HEAT_COST_ALLOCATOR(0x08),
	COMPRESSED_AIR(0x09),
	COOLING_METER_OUTLET(0x0a),
	COOLING_METER_INLET(0x0b),
	HEAT_METER_INLET(0x0c),
	HEAT_COOLING_METER(0x0d),
	BUS_SYSTEM_COMPONENT(0x0e),
	UNKNOWN(0x0f),
	RESERVED_FOR_METER_16(0x10),
	RESERVED_FOR_METER_17(0x11),
	RESERVED_FOR_METER_18(0x12),
	RESERVED_FOR_METER_19(0x13),
	CALORIFIC_VALUE(0x14),
	HOT_WATER_METER(0x15),
	COLD_WATER_METER(0x16),
	DUAL_REGISTER_WATER_METER(0x17),
	PRESSURE_METER(0x18),
	AD_CONVERTER(0x19),
	SMOKE_DETECTOR(0x1a),
	ROOM_SENSOR_TEMP_HUM(0x1b),
	GAS_DETECTOR(0x1c),
	RESERVED_FOR_SENSOR_0X1D(0x1d),
	RESERVED_FOR_SENSOR_0X1E(0x1e),
	RESERVED_FOR_SENSOR_0X1F(0x1f),
	BREAKER_ELEC(0x20),
	VALVE_GAS_OR_WATER(0x21),
	RESERVED_FOR_SWITCHING_DEVICE_0X22(0x22),
	RESERVED_FOR_SWITCHING_DEVICE_0X23(0x23),
	RESERVED_FOR_SWITCHING_DEVICE_0X24(0x24),
	CUSTOMER_UNIT_DISPLAY_DEVICE(0x25),
	RESERVED_FOR_CUSTOMER_UNIT_0X26(0x26),
	RESERVED_FOR_CUSTOMER_UNIT_0X27(0x27),
	WASTE_WATER_METER(0x28),
	GARBAGE(0x29),
	RESERVED_FOR_CO2(0x2a),
	RESERVED_FOR_ENV_METER_0X2B(0x2b),
	RESERVED_FOR_ENV_METER_0X2C(0x2c),
	RESERVED_FOR_ENV_METER_0X2D(0x2d),
	RESERVED_FOR_ENV_METER_0X2E(0x2e),
	RESERVED_FOR_ENV_METER_0X2F(0x2f),
	RESERVED_FOR_SYSTEM_DEVICES_0X30(0x30),
	COM_CONTROLLER(0x31),
	UNIDIRECTION_REPEATER(0x32),
	BIDIRECTION_REPEATER(0x33),
	RESERVED_FOR_SYSTEM_DEVICES_0X34(0x34),
	RESERVED_FOR_SYSTEM_DEVICES_0X35(0x35),
	RADIO_CONVERTER_SYSTEM_SIDE(0x36),
	RADIO_CONVERTER_METER_SIDE(0x37),
	RESERVED(0xff);
	private final int code;

	DeviceType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static DeviceType newDevice(int code) {
		switch (code) {
		case 0x00:
			return DeviceType.OTHER;
		case 0x01:
			return DeviceType.OIL_METER;
		case 0x02:
			return DeviceType.ELECTRICITY_METER;
		case 0x03:
			return DeviceType.GAS_METER;
		case 0x04:
			return DeviceType.HEAT_METER;
		case 0x05:
			return DeviceType.STEAM_METER;
		case 0x06:
			return DeviceType.WARM_WATER_METER;
		case 0x07:
			return DeviceType.WATER_METER;
		case 0x08:
			return DeviceType.HEAT_COST_ALLOCATOR;
		case 0x09:
			return DeviceType.COMPRESSED_AIR;
		case 0x0a:
			return DeviceType.COOLING_METER_OUTLET;
		case 0x0b:
			return DeviceType.COOLING_METER_INLET;
		case 0x0c:
			return DeviceType.HEAT_METER_INLET;
		case 0x0d:
			return DeviceType.HEAT_COOLING_METER;
		case 0x0e:
			return DeviceType.BUS_SYSTEM_COMPONENT;
		case 0x0f:
			return DeviceType.UNKNOWN;
		case 0x10:
			return DeviceType.RESERVED_FOR_METER_16;
		case 0x11:
			return DeviceType.RESERVED_FOR_METER_17;
		case 0x12:
			return DeviceType.RESERVED_FOR_METER_18;
		case 0x13:
			return DeviceType.RESERVED_FOR_METER_19;
		case 0x14:
			return DeviceType.CALORIFIC_VALUE;
		case 0x15:
			return DeviceType.HOT_WATER_METER;
		case 0x16:
			return DeviceType.COLD_WATER_METER;
		case 0x17:
			return DeviceType.DUAL_REGISTER_WATER_METER;
		case 0x18:
			return DeviceType.PRESSURE_METER;
		case 0x19:
			return DeviceType.AD_CONVERTER;
		case 0x1a:
			return DeviceType.SMOKE_DETECTOR;
		case 0x1b:
			return DeviceType.ROOM_SENSOR_TEMP_HUM;
		case 0x1c:
			return DeviceType.GAS_DETECTOR;
		case 0x1d:
			return DeviceType.RESERVED_FOR_SENSOR_0X1D;
		case 0x1e:
			return DeviceType.RESERVED_FOR_SENSOR_0X1E;
		case 0x1f:
			return DeviceType.RESERVED_FOR_SENSOR_0X1F;
		case 0x20:
			return DeviceType.BREAKER_ELEC;
		case 0x21:
			return DeviceType.VALVE_GAS_OR_WATER;
		case 0x22:
			return DeviceType.RESERVED_FOR_SWITCHING_DEVICE_0X22;
		case 0x23:
			return DeviceType.RESERVED_FOR_SWITCHING_DEVICE_0X23;
		case 0x24:
			return DeviceType.RESERVED_FOR_SWITCHING_DEVICE_0X24;
		case 0x25:
			return DeviceType.CUSTOMER_UNIT_DISPLAY_DEVICE;
		case 0x26:
			return DeviceType.RESERVED_FOR_CUSTOMER_UNIT_0X26;
		case 0x27:
			return DeviceType.RESERVED_FOR_CUSTOMER_UNIT_0X27;
		case 0x28:
			return DeviceType.WASTE_WATER_METER;
		case 0x29:
			return DeviceType.GARBAGE;
		case 0x2a:
			return DeviceType.RESERVED_FOR_CO2;
		case 0x2b:
			return DeviceType.RESERVED_FOR_ENV_METER_0X2B;
		case 0x2c:
			return DeviceType.RESERVED_FOR_ENV_METER_0X2C;
		case 0x2d:
			return DeviceType.RESERVED_FOR_ENV_METER_0X2D;
		case 0x2e:
			return DeviceType.RESERVED_FOR_ENV_METER_0X2E;
		case 0x2f:
			return DeviceType.RESERVED_FOR_ENV_METER_0X2F;
		case 0x30:
			return DeviceType.RESERVED_FOR_SYSTEM_DEVICES_0X30;
		case 0x31:
			return DeviceType.COM_CONTROLLER;
		case 0x32:
			return DeviceType.UNIDIRECTION_REPEATER;
		case 0x33:
			return DeviceType.BIDIRECTION_REPEATER;
		case 0x34:
			return DeviceType.RESERVED_FOR_SYSTEM_DEVICES_0X34;
		case 0x35:
			return DeviceType.RESERVED_FOR_SYSTEM_DEVICES_0X35;
		case 0x36:
			return DeviceType.RADIO_CONVERTER_SYSTEM_SIDE;
		case 0x37:
			return DeviceType.RADIO_CONVERTER_METER_SIDE;
		default:
			return DeviceType.RESERVED;
		}
	}

}
