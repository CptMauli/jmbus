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

package org.openmuc.jmbus;

import java.text.ParseException;
import java.util.Calendar;

/**
 * Representation of a Variable Data Block.
 * 
 * A variable data block is the basic data entity in the M-Bus application layer. They form parts of different types of
 * M-Bus messages (e.g. a VariableDataResponse - RESP-UD). It encodes information on the data type, status, meaning,
 * unit and value of a date. Data type and status are encoded in the DIB (Data Information Block) whilst meaning and
 * unit are encoded in the VIB (Value Information Block).
 * 
 */
public class VariableDataBlock {

	public enum DataType {
		LONG, DOUBLE, DATE, STRING;
	}

	public enum FunctionField {
		INST_VAL, MAX_VAL, MIN_VAL, ERROR_VAL;
	}

	// Data descriptions stored in the VIF
	public enum Description {
		ENERGY, VOLUME, MASS, ON_TIME, OPERATING_TIME, POWER, VOLUME_FLOW, VOLUME_FLOW_EXT, MASS_FLOW, FLOW_TEMPERATURE, RETURN_TEMPERATURE, TEMPERATURE_DIFFERENCE, EXTERNAL_TEMPERATURE, PRESSURE, DATE, DATE_TIME, VOLTAGE, CURRENT, FABRICATION_NO, MODEL_VERSION, PARAMETER_SET_ID, HARDWARE_VERSION, FIRMWARE_VERSION, ERROR_FLAGS, CUSTOMER, RESERVED, OPERATING_TIME_BATTERY, HCA;
	}

	// Data Information Block that contains a DIF and optionally up to 10 DIFEs
	private final byte[] dib;
	// Value Information Block that contains a VIF and optionally up to 10 VIFEs
	private final byte[] vib;
	private final byte[] dataBytes;
	private final Integer lvar;

	boolean parsed;

	private Object data;
	private DataType dataType;

	// DIB Fields:
	private FunctionField functionField;
	private byte dataField;
	private long storageNumber; // max is 41 bits
	private int tariff; // max 20 bits
	private short deviceUnit; // max 10 bits

	// VIB Fields:
	private Description description;
	private byte multiplier;
	private int unit;

	public VariableDataBlock(byte[] dib, byte[] vib, byte[] data, Integer lvar) {
		this.dib = dib;
		this.vib = vib;
		this.lvar = lvar;
		dataBytes = data;
		parsed = false;
	}

	public Integer getLvar() {
		return lvar;
	}

	/*
	 * Returns a byte array containing the DIF and the DIFEs contained in the Variable Data Block
	 */
	public byte[] getDIB() {
		return dib;
	}

	/*
	 * Returns a byte array containing the VIF and the VIFEs contained in the Variable Data Block
	 */
	public byte[] getVIB() {
		return vib;
	}

	public byte[] getDataBytes() {
		return dataBytes;
	}

	/*
	 * Returns the data as an Object. The Object could be a Long, Double or Date depending on the DataField information.
	 * The DataType can be checked using getDataType().
	 */
	public Object getData() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return data;
	}

	public DataType getDataType() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return dataType;
	}

	/**
	 * Returns the data (value) multiplied by the multiplier as a Double. If the data is not a number than null is
	 * returned.
	 * 
	 * @return the data (value) multiplied by the multiplier as a Double
	 */
	public Double getScaledValue() {
		try {
			return ((Number) data).doubleValue() * Math.pow(10, multiplier);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public FunctionField getFunctionField() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return functionField;
	}

	public byte getDataField() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return dataField;
	}

	public long getStorageNumber() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return storageNumber;
	}

	public int getTariff() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return tariff;
	}

	public short getDeviceUnit() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return deviceUnit;
	}

	public Description getDescription() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return description;
	}

	public byte getMultiplier() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return multiplier;
	}

	public int getUnit() {
		if (!parsed) {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
		return unit;
	}

	public void parse() throws ParseException {

		parsed = false;

		boolean dateTypeF = false;
		boolean dateTypeG = false;

		// decode DIB
		int ff = ((dib[0] & 0x30) >> 4);
		switch (ff) {
		case 0:
			functionField = FunctionField.INST_VAL;
			break;
		case 1:
			functionField = FunctionField.MAX_VAL;
			break;
		case 2:
			functionField = FunctionField.MIN_VAL;
			break;
		case 3:
			functionField = FunctionField.ERROR_VAL;
		}

		dataField = (byte) (dib[0] & 0x0f);
		storageNumber = (dib[0] & 0x40) >> 6;

		deviceUnit = 0;
		tariff = 0;

		for (int i = 1; i < dib.length; i++) {
			deviceUnit += (((dib[i] & 0x40) >> 6) << (i - 1));
			tariff += ((dib[i] & 0x30) >> 4) << ((i - 1) * 2);
			storageNumber += ((dib[i] & 0x0f) << (((i - 1) * 4) + 1));
		}

		multiplier = 0;

		unit = DlmsUnit.OTHER_UNIT;

		// decode VIB

		// if extension bit not set
		if (vib[0] == (byte) 0xFD) { /* Main VIFE-code extension table */
			if ((vib[1] & 0x70) == 0x40) {
				description = Description.VOLTAGE;
				multiplier = (byte) ((vib[1] & 0x0f) - 9);
				unit = DlmsUnit.VOLT;
			}
			else if ((vib[1] & 0x70) == 0x50) {
				description = Description.CURRENT;
				multiplier = (byte) ((vib[1] & 0x0f) - 12);
				unit = DlmsUnit.AMPERE;
			}
			else if ((vib[1] & 0x7c) == 0x6c) {
				description = Description.OPERATING_TIME_BATTERY;
				switch (vib[1] & 0x03) {
				case 0:
					unit = DlmsUnit.SECOND;
					break;
				case 1:
					unit = DlmsUnit.MIN;
					break;
				case 2:
					unit = DlmsUnit.HOUR;
					break;
				case 3:
					unit = DlmsUnit.DAY;
				}
			}
			else if ((vib[1] & 0x7f) == 0x0b) {
				description = Description.PARAMETER_SET_ID;
			}
			else if ((vib[1] & 0x7f) == 0x0c) {
				description = Description.MODEL_VERSION;
			}
			else if ((vib[1] & 0x7f) == 0x0d) {
				description = Description.HARDWARE_VERSION;
			}
			else if ((vib[1] & 0x7f) == 0x0e) {
				description = Description.FIRMWARE_VERSION;
			}
			else if ((vib[1] & 0x7f) == 0x17) {
				description = Description.ERROR_FLAGS;
			}
			else if ((vib[1] & 0x7f) == 0x11) {
				description = Description.CUSTOMER;
			}
			else if ((vib[1] & 0x7f) > 0x71) {
				description = Description.RESERVED;
			}
		}
		else if (vib[0] == (byte) 0xFB) { /*
										 * Codes used with extension indicator 0xFB
										 */
			if ((vib[1] & 0x7E) == 0) {
				description = Description.ENERGY;
				multiplier = (byte) (6 - ((vib[1] & 0x01) - 1));
				unit = DlmsUnit.WATT_HOUR;
			}
			else if ((vib[1] & 0x7E) == 8) {
				description = Description.ENERGY;
				multiplier = (byte) (9 - ((vib[1] & 0x01) - 1));
				unit = DlmsUnit.JOULE;
			}
			else if ((vib[1] & 0x7E) == 16) {
				description = Description.VOLUME;
				multiplier = (byte) ((vib[1] & 0x01) + 2);
				unit = DlmsUnit.CUBIC_METRE;
			}
			else if ((vib[1] & 0x7E) == 24) {
				description = Description.MASS;
				multiplier = (byte) (3 + ((vib[1] & 0x01) + 2));
				unit = DlmsUnit.KILOGRAM;
			}
			else if ((vib[1] & 0x7E) == 40) {
				description = Description.POWER;
				multiplier = (byte) (6 + ((vib[1] & 0x01) - 1));
				unit = DlmsUnit.WATT;
			}
			else if ((vib[1] & 0x7E) == 48) {
				description = Description.POWER;
				multiplier = (byte) (9 + ((vib[1] & 0x01) - 1));
				unit = DlmsUnit.JOULE_PER_HOUR;
			}

		}
		else {
			if ((vib[0] & 0x40) == 0) {
				if ((vib[0] & 0x20) == 0) {
					if ((vib[0] & 0x10) == 0) {
						if ((vib[0] & 0x08) == 0) {
							description = Description.ENERGY;
							multiplier = (byte) ((vib[0] & 0x07) - 3);
							unit = DlmsUnit.WATT_HOUR;
						}
						else {
							description = Description.ENERGY;
							multiplier = (byte) (vib[0] & 0x07);
							unit = DlmsUnit.JOULE;
						}
					}
					else {
						if ((vib[0] & 0x08) == 0) {
							description = Description.VOLUME;
							multiplier = (byte) ((vib[0] & 0x07) - 6);
							unit = DlmsUnit.CUBIC_METRE;
						}
						else {
							description = Description.MASS;
							multiplier = (byte) ((vib[0] & 0x07) - 3);
							unit = DlmsUnit.KILOGRAM;
						}
					}
				}
				else {
					if ((vib[0] & 0x10) == 0) {
						if ((vib[0] & 0x08) == 0) {
							if ((vib[0] & 0x04) == 0) {
								description = Description.ON_TIME;
							}
							else {
								description = Description.OPERATING_TIME;
							}
							if ((vib[0] & 0x02) == 0) {
								if ((vib[0] & 0x01) == 0) {
									unit = DlmsUnit.SECOND;
								}
								else {
									unit = DlmsUnit.MIN;
								}
							}
							else {
								if ((vib[0] & 0x01) == 0) {
									unit = DlmsUnit.HOUR;
								}
								else {
									unit = DlmsUnit.DAY;
								}
							}
						}
						else {
							description = Description.POWER;
							multiplier = (byte) ((vib[0] & 0x07) - 3);
							unit = DlmsUnit.WATT;
						}
					}
					else {
						if ((vib[0] & 0x08) == 0) {
							description = Description.POWER;
							multiplier = (byte) (vib[0] & 0x07);
							unit = DlmsUnit.JOULE_PER_HOUR;
						}
						else {
							description = Description.VOLUME_FLOW;
							multiplier = (byte) ((vib[0] & 0x07) - 6);
							unit = DlmsUnit.CUBIC_METRE_PER_HOUR;
						}
					}
				}
			}
			// second bit is 1
			else {
				if ((vib[0] & 0x20) == 0) {
					if ((vib[0] & 0x10) == 0) {
						if ((vib[0] & 0x08) == 0) {
							description = Description.VOLUME_FLOW_EXT;
							multiplier = (byte) ((vib[0] & 0x07) - 7);
							unit = DlmsUnit.CUBIC_METRE_PER_MINUTE;
						}
						else {
							description = Description.VOLUME_FLOW_EXT;
							multiplier = (byte) ((vib[0] & 0x07) - 9);
							unit = DlmsUnit.CUBIC_METRE_PER_SECOND;
						}
					}
					else {
						if ((vib[0] & 0x08) == 0) {
							description = Description.MASS_FLOW;
							multiplier = (byte) ((vib[0] & 0x07) - 3);
							unit = DlmsUnit.KILOGRAM_PER_HOUR;
						}
						else {
							if ((vib[0] & 0x04) == 0) {
								description = Description.FLOW_TEMPERATURE;
								multiplier = (byte) ((vib[0] & 0x03) - 3);
								unit = DlmsUnit.DEGREE_CELSIUS;
							}
							else {
								description = Description.RETURN_TEMPERATURE;
								multiplier = (byte) ((vib[0] & 0x03) - 3);
								unit = DlmsUnit.DEGREE_CELSIUS;
							}
						}
					}
				}
				else { /* 011 ... */
					if ((vib[0] & 0x10) == 0) {
						if ((vib[0] & 0x08) == 0) {
							if ((vib[0] & 0x04) == 0) {
								description = Description.TEMPERATURE_DIFFERENCE;
								multiplier = (byte) ((vib[0] & 0x03) - 3);
								unit = DlmsUnit.KELVIN;
							}
							else {
								description = Description.EXTERNAL_TEMPERATURE;
								multiplier = (byte) ((vib[0] & 0x03) - 3);
								unit = DlmsUnit.DEGREE_CELSIUS;
							}
						}
						else { /* 01101 ... */
							if ((vib[0] & 0x04) == 0) {
								description = Description.PRESSURE;
								multiplier = (byte) ((vib[0] & 0x03) - 3);
								unit = DlmsUnit.BAR;
							}
							else { /* 011011 .. */
								if ((vib[0] & 0x02) == 0) { /* 0110110 ... */
									if ((vib[0] & 0x01) == 0) {
										description = Description.DATE;
										dateTypeG = true;
									}
									else {
										description = Description.DATE_TIME;
										dateTypeF = true;
									}
								}
								else { /* 0110111 ... */
									if ((vib[0] & 0x01) == 0) { /* 01101110 */
										description = Description.HCA;
										unit = DlmsUnit.RESERVED;
									}

								}

							}
						}
					}
					else { /* 0111 ... */
						if ((vib[0] & 0x08) == 0) {
							/* nop */
						}
						else { /* 01111... */
							if ((vib[0] & 0x04) == 0) {
								if ((vib[0] & 0x02) == 0) {
									if ((vib[0] & 0x01) == 0) {
										description = Description.FABRICATION_NO;
									}
								}
							}

						}
					}
				}

			}
			// extension bit is set
		}

		// decode dataField

		switch (dataField) {
		case 0x01: /* INT8 */
			data = new Long(dataBytes[0]);
			dataType = DataType.LONG;
			break;
		case 0x02: /* INT16 */
			if (dateTypeG) {
				int day = (0x1f) & dataBytes[0];
				int month = (0x0f) & dataBytes[1];
				int year1 = ((0xe0) & dataBytes[0]) >> 5;
				int year2 = ((0xf0) & dataBytes[1]) >> 1;
				int year = (2000 + year1 + year2);

				Calendar calendar = Calendar.getInstance();

				calendar.set(year, month - 1, day, 0, 0, 0);

				data = calendar.getTime();
				dataType = DataType.DATE;
			}
			else {
				data = new Long((short) ((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8)));
				dataType = DataType.LONG;
			}
			break;
		case 0x03: /* INT24 */
			if ((dataBytes[2] & 0x80) == 0x80) {
				// negative
				data = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8) | ((dataBytes[2] & 0xff) << 16)
						| 0xff << 24);
				dataType = DataType.LONG;
			}
			else {
				data = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8) | ((dataBytes[2] & 0xff) << 16));
				dataType = DataType.LONG;
			}
			break;
		case 0x04: /* INT32 */
			if (dateTypeF) {
				int min = (dataBytes[0] & 0x3f);
				int hour = (dataBytes[1] & 0x1f);
				int day = (dataBytes[2] & 0x1f);
				int mon = (dataBytes[3] & 0x0f);
				int year1 = (0xe0 & dataBytes[2]) >> 5;
				int year2 = (0xf0 & dataBytes[3]) >> 1;
				int yearh = (0x60 & dataBytes[1]) >> 5;

				if (yearh == 0) {
					yearh = 1;
				}

				int year = 1900 + 100 * yearh + year1 + year2;

				Calendar calendar = Calendar.getInstance();

				calendar.set(year, mon - 1, day, hour, min, 0);

				data = calendar.getTime();
				dataType = DataType.DATE;
			}
			else {
				data = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8) | ((dataBytes[2] & 0xff) << 16)
						| ((dataBytes[3] & 0xff) << 24));
				dataType = DataType.LONG;
			}
			break;
		case 0x06: /* INT48 */
			if ((dataBytes[2] & 0x80) == 0x80) {
				// negative
				data = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8) | ((dataBytes[2] & 0xff) << 16)
						| ((dataBytes[3] & 0xff) << 24) | ((dataBytes[4] & 0xff) << 32) | ((dataBytes[5] & 0xff) << 40)
						| (0xffl << 48) | (0xffl << 56));
			}
			else {
				data = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8) | ((dataBytes[2] & 0xff) << 16)
						| ((dataBytes[3] & 0xff) << 24) | (((long) dataBytes[4] & 0xff) << 32)
						| (((long) dataBytes[5] & 0xff) << 40));
			}
			dataType = DataType.LONG;
			break;
		case 0x07: /* INT64 */
			data = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8) | ((dataBytes[2] & 0xff) << 16)
					| ((dataBytes[3] & 0xff) << 24) | (((long) dataBytes[4] & 0xff) << 32)
					| (((long) dataBytes[5] & 0xff) << 40) | (((long) dataBytes[6] & 0xff) << 48)
					| (((long) dataBytes[7] & 0xff) << 56));
			dataType = DataType.LONG;
			break;
		case 0x0a:
		case 0x0b:
		case 0x0c:
		case 0x0e:
			data = new Long(bcdToLong(dataBytes));
			dataType = DataType.LONG;
			break;
		case 0x0d: /* variable length - LVAR */
			if (lvar < 0xc0) {
				char rawData[] = new char[dataBytes.length];

				for (int i = 0; i < dataBytes.length; i++) {
					rawData[i] = (char) dataBytes[dataBytes.length - 1 - i];
				}

				data = new String(rawData);
				dataType = DataType.STRING;
			}
			throw new ParseException("Unknown Data Field in DIF", 0);
		default:
			throw new ParseException("Unknown Data Field in DIF", 0);
		}

		parsed = true;

	}

	private static long bcdToLong(byte[] bcd) {
		long result = 0l;
		long factor = 1;

		for (byte element : bcd) {
			result += (element & 0x0f) * factor;
			factor = factor * 10;
			result += ((element >> 4) & 0x0f) * factor;
			factor = factor * 10;
		}

		return result;
	}

}
