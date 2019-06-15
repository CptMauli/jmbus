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

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * Representation of a data record (sometimes called variable data block).
 * 
 * A data record is the basic data entity of the M-Bus application layer. A variable data structure contains a list of
 * data records. Each data record represents a single data point. A data record consists of three fields: The data
 * information block (DIB), the value information block (VIB) and the data field.
 * 
 * The DIB codes the following parameters:
 * <ul>
 * <li>Storage number - a meter can have several storages e.g. to store historical time series data. The storage number
 * 0 signals an actual value.</li>
 * <li>Function - data can have the following four function types: instantaneous value, max value, min value, value
 * during error state.</li>
 * <li>Length and coding of the data field.</li>
 * <li>Tariff - indicates the tariff number of this data field. The data of tariff 0 is usually the sum of all other
 * tariffs.</li>
 * <li>Subunit - can be used by a slave to distinguish several subunits of the metering device</li>
 * </ul>
 * 
 * The VIB codes the following parameters:
 * <ul>
 * <li>Description - the meaning of the data value (e.g. "Energy", "Volume" etc.)</li>
 * <li>Unit - the unit of the data value.</li>
 * <li>Multiplier - a factor by which the data value coded in the data field has to be multiplied with.
 * <code>getScaledDataValue()</code> returns the result of the data value multiplied with the multiplier.</li>
 * </ul>
 * 
 * @author Stefan Feuerhahn
 * 
 */
public class DataRecord {

	/**
	 * The data value type
	 *
	 */
	public enum DataValueType {
		LONG, DOUBLE, DATE, STRING, BCD, NONE;
	}

	/**
	 * Function coded in the DIB
	 * 
	 */
	public enum FunctionField {
		INST_VAL, MAX_VAL, MIN_VAL, ERROR_VAL;
	}

	/**
	 * Data description stored in the VIB
	 * 
	 */
	public enum Description {
		ENERGY, VOLUME, MASS, ON_TIME, OPERATING_TIME, POWER, VOLUME_FLOW, VOLUME_FLOW_EXT, MASS_FLOW, FLOW_TEMPERATURE, RETURN_TEMPERATURE, TEMPERATURE_DIFFERENCE, EXTERNAL_TEMPERATURE, PRESSURE, DATE, DATE_TIME, VOLTAGE, CURRENT, AVERAGING_DURATION, ACTUALITY_DURATION, FABRICATION_NO, MODEL_VERSION, PARAMETER_SET_ID, HARDWARE_VERSION, FIRMWARE_VERSION, ERROR_FLAGS, CUSTOMER, RESERVED, OPERATING_TIME_BATTERY, HCA, REACTIVE_ENERGY, TEMPERATURE_LIMIT, MAX_POWER, REACTIVE_POWER, REL_HUMIDITY, FREQUENCY, PHASE, EXTENDED_IDENTIFICATION, ADDRESS, NOT_SUPPORTED, USER_DEFINED;
	}

	// Data Information Block that contains a DIF and optionally up to 10 DIFEs
	private final byte[] dib;
	// Value Information Block that contains a VIF and optionally up to 10 VIFEs
	private final byte[] vib;
	private final byte[] dataBytes;
	private final Integer lvar;

	private boolean decoded = false;

	private Object dataValue;
	private DataValueType dataValueType;

	// DIB fields:
	private FunctionField functionField;
	private byte dataField;
	private long storageNumber; // max is 41 bits
	private int tariff; // max 20 bits
	private short subunit; // max 10 bits

	// VIB fields:
	private Description description;
	private String userDefinedDescription;
	private int multiplierExponent = 0;
	private DlmsUnit unit;

	private boolean dateTypeF = false;
	private boolean dateTypeG = false;

	DataRecord(byte[] dib, byte[] vib, byte[] data, Integer lvar) {
		this.dib = dib;
		this.vib = vib;
		this.lvar = lvar;
		dataBytes = data;
	}

	public Integer getLvar() {
		return lvar;
	}

	/**
	 * Returns a byte array containing the DIB (i.e. the DIF and the DIFEs) contained in the data record.
	 * 
	 * @return a byte array containing the DIB
	 */
	public byte[] getDIB() {
		return dib;
	}

	/**
	 * Returns a byte array containing the VIB (i.e. the VIF and the VIFEs) contained in the data record.
	 * 
	 * @return a byte array containing the VIB
	 */
	public byte[] getVIB() {
		return vib;
	}

	/**
	 * Returns the data field of the data record as a byte array.
	 * 
	 * @return Returns the Data field of the data record as a byte array
	 */
	public byte[] getDataValueBytes() {
		return dataBytes;
	}

	/**
	 * Returns the decoded data field of the data record as an Object. The Object is of one of the four types Long,
	 * Double, String or Date depending on information coded in the DIB/VIB. The DataType can be checked using
	 * getDataValueType().
	 * 
	 * @return the data value
	 */
	public Object getDataValue() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return dataValue;
	}

	public DataValueType getDataValueType() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return dataValueType;
	}

	/**
	 * Returns the data (value) multiplied by the multiplier as a Double. If the data is not a number than null is
	 * returned.
	 * 
	 * @return the data (value) multiplied by the multiplier as a Double
	 */
	public Double getScaledDataValue() {
		try {
			return ((Number) dataValue).doubleValue() * Math.pow(10, multiplierExponent);
		} catch (ClassCastException e) {
			return null;
		}
	}

	public FunctionField getFunctionField() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return functionField;
	}

	public byte getDataField() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return dataField;
	}

	public long getStorageNumber() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return storageNumber;
	}

	public int getTariff() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return tariff;
	}

	public short getSubunit() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return subunit;
	}

	public Description getDescription() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return description;
	}

	public String getUserDefinedDescription() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		if (description == Description.USER_DEFINED) {
			return userDefinedDescription;
		}
		else {
			return description.toString();
		}
	}

	/**
	 * The multiplier is coded in the VIF. Is always a power of 10. This function returns the exponent. The base is
	 * always 10.
	 * 
	 * @return the exponent of the multiplier.
	 */
	public int getMultiplierExponent() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return multiplierExponent;
	}

	public DlmsUnit getUnit() {
		if (!decoded) {
			throw new RuntimeException("Data record was not decoded.");
		}
		return unit;
	}

	public void decode() throws DecodingException {

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

		subunit = 0;
		tariff = 0;

		for (int i = 1; i < dib.length; i++) {
			subunit += (((dib[i] & 0x40) >> 6) << (i - 1));
			tariff += ((dib[i] & 0x30) >> 4) << ((i - 1) * 2);
			storageNumber += ((dib[i] & 0x0f) << (((i - 1) * 4) + 1));
		}

		multiplierExponent = 0;

		unit = null;

		// decode VIB
		if (vib[0] == (byte) 0xFB) {
			decodeAlternateExtendedVif();
		}
		else if ((vib[0] & 0x7F) == (byte) 0x7C) {
			/**
			 * VIF equal to "E111 1100", allows user definable VIF´s (in plain ASCII-String)
			 */
			decodeUserDefinedVif();
		}
		else if (vib[0] == (byte) 0xFD) {
			decodeMainExtendedVif();
		}
		else if ((vib[0] & 0x7F) == (byte) 0x7E) {
			throw new DecodingException("VIF types 0x7E/FE not supported.");
		}
		else if ((vib[0] & 0x7F) == (byte) 0x7F) {
			throw new DecodingException("VIF types 0x7F/FF not supported.");
		}
		else {
			// primary VIF
			decodeMainVif();
		}

		// decode dataField
		switch (dataField) {
		case 0x00:
			dataValue = null;
			dataValueType = DataValueType.NONE;
			break;
		case 0x01: /* INT8 */
			dataValue = new Long(dataBytes[0]);
			dataValueType = DataValueType.LONG;
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

				dataValue = calendar.getTime();
				dataValueType = DataValueType.DATE;
			}
			else {
				dataValue = new Long((short) ((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8)));
				dataValueType = DataValueType.LONG;
			}
			break;
		case 0x03: /* INT24 */
			if ((dataBytes[2] & 0x80) == 0x80) {
				// negative
				dataValue = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8)
						| ((dataBytes[2] & 0xff) << 16) | 0xff << 24);
			}
			else {
				dataValue = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8)
						| ((dataBytes[2] & 0xff) << 16));
			}
			dataValueType = DataValueType.LONG;
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

				dataValue = calendar.getTime();
				dataValueType = DataValueType.DATE;
			}
			else {
				dataValue = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8)
						| ((dataBytes[2] & 0xff) << 16) | ((dataBytes[3] & 0xff) << 24));
				dataValueType = DataValueType.LONG;
			}
			break;
		case 0x06: /* INT48 */
			if ((dataBytes[2] & 0x80) == 0x80) {
				// negative
				dataValue = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8)
						| ((dataBytes[2] & 0xff) << 16) | ((dataBytes[3] & 0xff) << 24) | ((dataBytes[4] & 0xff) << 32)
						| ((dataBytes[5] & 0xff) << 40) | (0xffl << 48) | (0xffl << 56));
			}
			else {
				dataValue = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8)
						| ((dataBytes[2] & 0xff) << 16) | ((dataBytes[3] & 0xff) << 24)
						| (((long) dataBytes[4] & 0xff) << 32) | (((long) dataBytes[5] & 0xff) << 40));
			}
			dataValueType = DataValueType.LONG;
			break;
		case 0x07: /* INT64 */
			dataValue = new Long((dataBytes[0] & 0xff) | ((dataBytes[1] & 0xff) << 8) | ((dataBytes[2] & 0xff) << 16)
					| ((dataBytes[3] & 0xff) << 24) | (((long) dataBytes[4] & 0xff) << 32)
					| (((long) dataBytes[5] & 0xff) << 40) | (((long) dataBytes[6] & 0xff) << 48)
					| (((long) dataBytes[7] & 0xff) << 56));
			dataValueType = DataValueType.LONG;
			break;
		case 0x08: /* no data - selection for readout request */
			dataValue = null;
			dataValueType = DataValueType.NONE;
			break;
		case 0x09:
		case 0x0a:
		case 0x0b:
		case 0x0c:
		case 0x0e:
			dataValue = new Bcd(dataBytes);
			dataValueType = DataValueType.BCD;
			break;
		case 0x0d: /* variable length - LVAR */
			if (lvar < 0xc0) {
				char rawData[] = new char[dataBytes.length];

				for (int i = 0; i < dataBytes.length; i++) {
					rawData[i] = (char) dataBytes[dataBytes.length - 1 - i];
				}

				dataValue = new String(rawData);
				dataValueType = DataValueType.STRING;
			}
			else {
				throw new DecodingException("LVAR field > 0xc0: " + lvar);
			}
			break;
		default:
			throw new DecodingException("Unknown Data Field in DIF: " + HexConverter.getHexStringFromByte(dataField));
		}

		decoded = true;

	}

	private void decodeTimeUnit() {
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

	/**
	 * Convert Hex to ASCII and reverse the letters
	 * 
	 * @throws DecodingException
	 */
	private void decodeUserDefinedVif() throws DecodingException {
		description = Description.USER_DEFINED;

		try {
			userDefinedDescription = new String(vib, "ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new DecodingException("User defined description is not valid ASCII string.");
		}

		// remove the first vib from string
		if (userDefinedDescription.length() > 0) {
			userDefinedDescription = userDefinedDescription.substring(1);
		}

		userDefinedDescription = new StringBuilder(userDefinedDescription).reverse().toString();
	}

	private void decodeMainVif() {
		description = Description.NOT_SUPPORTED;

		if ((vib[0] & 0x40) == 0) {
			// E0
			if ((vib[0] & 0x20) == 0) {
				// E00
				if ((vib[0] & 0x10) == 0) {
					// E000
					if ((vib[0] & 0x08) == 0) {
						// E000 0
						description = Description.ENERGY;
						multiplierExponent = (vib[0] & 0x07) - 3;
						unit = DlmsUnit.WATT_HOUR;
					}
					else {
						// E000 1
						description = Description.ENERGY;
						multiplierExponent = vib[0] & 0x07;
						unit = DlmsUnit.JOULE;
					}
				}
				else {
					// E001
					if ((vib[0] & 0x08) == 0) {
						// E001 0
						description = Description.VOLUME;
						multiplierExponent = (vib[0] & 0x07) - 6;
						unit = DlmsUnit.CUBIC_METRE;
					}
					else {
						// E001 1
						description = Description.MASS;
						multiplierExponent = (vib[0] & 0x07) - 3;
						unit = DlmsUnit.KILOGRAM;
					}
				}
			}
			else {
				// E01
				if ((vib[0] & 0x10) == 0) {
					// E010
					if ((vib[0] & 0x08) == 0) {
						// E010 0
						if ((vib[0] & 0x04) == 0) {
							// E010 00
							description = Description.ON_TIME;
						}
						else {
							// E010 01
							description = Description.OPERATING_TIME;
						}
						decodeTimeUnit();
					}
					else {
						// E010 1
						description = Description.POWER;
						multiplierExponent = (vib[0] & 0x07) - 3;
						unit = DlmsUnit.WATT;
					}
				}
				else {
					// E011
					if ((vib[0] & 0x08) == 0) {
						// E011 0
						description = Description.POWER;
						multiplierExponent = vib[0] & 0x07;
						unit = DlmsUnit.JOULE_PER_HOUR;
					}
					else {
						// E011 1
						description = Description.VOLUME_FLOW;
						multiplierExponent = (vib[0] & 0x07) - 6;
						unit = DlmsUnit.CUBIC_METRE_PER_HOUR;
					}
				}
			}
		}
		else {
			// E1
			if ((vib[0] & 0x20) == 0) {
				// E10
				if ((vib[0] & 0x10) == 0) {
					// E100
					if ((vib[0] & 0x08) == 0) {
						// E100 0
						description = Description.VOLUME_FLOW_EXT;
						multiplierExponent = (vib[0] & 0x07) - 7;
						unit = DlmsUnit.CUBIC_METRE_PER_MINUTE;
					}
					else {
						// E100 1
						description = Description.VOLUME_FLOW_EXT;
						multiplierExponent = (vib[0] & 0x07) - 9;
						unit = DlmsUnit.CUBIC_METRE_PER_SECOND;
					}
				}
				else {
					// E101
					if ((vib[0] & 0x08) == 0) {
						// E101 0
						description = Description.MASS_FLOW;
						multiplierExponent = (vib[0] & 0x07) - 3;
						unit = DlmsUnit.KILOGRAM_PER_HOUR;
					}
					else {
						// E101 1
						if ((vib[0] & 0x04) == 0) {
							// E101 10
							description = Description.FLOW_TEMPERATURE;
							multiplierExponent = (vib[0] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
						else {
							// E101 11
							description = Description.RETURN_TEMPERATURE;
							multiplierExponent = (vib[0] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
					}
				}
			}
			else {
				// E11
				if ((vib[0] & 0x10) == 0) {
					// E110
					if ((vib[0] & 0x08) == 0) {
						// E110 0
						if ((vib[0] & 0x04) == 0) {
							// E110 00
							description = Description.TEMPERATURE_DIFFERENCE;
							multiplierExponent = (vib[0] & 0x03) - 3;
							unit = DlmsUnit.KELVIN;
						}
						else {
							// E110 01
							description = Description.EXTERNAL_TEMPERATURE;
							multiplierExponent = (vib[0] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
					}
					else {
						// E110 1
						if ((vib[0] & 0x04) == 0) {
							// E110 10
							description = Description.PRESSURE;
							multiplierExponent = (vib[0] & 0x03) - 3;
							unit = DlmsUnit.BAR;
						}
						else {
							// E110 11
							if ((vib[0] & 0x02) == 0) {
								// E110 110
								if ((vib[0] & 0x01) == 0) {
									// E110 1100
									description = Description.DATE;
									dateTypeG = true;
								}
								else {
									// E110 1101
									description = Description.DATE_TIME;
									dateTypeF = true;
								}
							}
							else {
								// E110 111
								if ((vib[0] & 0x01) == 0) {
									// E110 1110
									description = Description.HCA;
									unit = DlmsUnit.RESERVED;
								}
								else {
									description = Description.NOT_SUPPORTED;
								}

							}

						}
					}
				}
				else {
					// E111
					if ((vib[0] & 0x08) == 0) {
						// E111 0
						if ((vib[0] & 0x04) == 0) {
							description = Description.AVERAGING_DURATION;
						}
						else {
							description = Description.ACTUALITY_DURATION;
						}
						decodeTimeUnit();
					}
					else {
						// E111 1
						if ((vib[0] & 0x04) == 0) {
							// E111 10
							if ((vib[0] & 0x02) == 0) {
								// E111 100
								if ((vib[0] & 0x01) == 0) {
									// E111 1000
									description = Description.FABRICATION_NO;
								}
								else {
									// E111 1001
									description = Description.EXTENDED_IDENTIFICATION;
								}
							}
							else {
								// E111 101
								if ((vib[0] & 0x01) == 0) {
									description = Description.ADDRESS;
								}
								else {
									// E111 1011
									// Codes used with extension indicator 0xFB (table 29 of DIN EN 13757-3:2011)
									decodeAlternateExtendedVif();
								}
							}
						}
						else {
							// E111 11
							if ((vib[0] & 0x02) == 0) {
								// E111 110
								if ((vib[0] & 0x01) == 0) {
									// E111 1100
									// Extension indicator 0xFC: VIF is given in following string
									description = Description.NOT_SUPPORTED;
								}
								else {
									// E111 1101
									// Extension indicator 0xFD: main VIFE-code extension table (table 28 of DIN EN
									// 13757-3:2011)
									decodeMainExtendedVif();

								}
							}

						}

					}
				}
			}

		}

	}

	// implements table 28 of DIN EN 13757-3:2011
	private void decodeMainExtendedVif() {
		if ((vib[1] & 0x70) == 0x40) {
			description = Description.VOLTAGE;
			multiplierExponent = (vib[1] & 0x0f) - 9;
			unit = DlmsUnit.VOLT;
		}
		else if ((vib[1] & 0x70) == 0x50) {
			description = Description.CURRENT;
			multiplierExponent = (vib[1] & 0x0f) - 12;
			unit = DlmsUnit.AMPERE;
		}
		else if ((vib[1] & 0x7c) == 0x6c) {
			description = Description.OPERATING_TIME_BATTERY;
			switch (vib[1] & 0x03) {
			case 0:
				unit = DlmsUnit.HOUR;
				break;
			case 1:
				unit = DlmsUnit.DAY;
				break;
			case 2:
				unit = DlmsUnit.MONTH;
				break;
			case 3:
				unit = DlmsUnit.YEAR;
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
		else if ((vib[1] & 0x7f) == 0x11) {
			description = Description.CUSTOMER;
		}
		else if ((vib[1] & 0x7f) == 0x17) {
			description = Description.ERROR_FLAGS;
		}
		else if ((vib[1] & 0x7f) >= 0x77) {
			description = Description.RESERVED;
		}
		else {
			description = Description.NOT_SUPPORTED;
		}
	}

	// implements table 29 of DIN EN 13757-3:2011
	private void decodeAlternateExtendedVif() {
		description = Description.NOT_SUPPORTED; // default value

		if ((vib[1] & 0x40) == 0) {
			// E0
			if ((vib[1] & 0x20) == 0) {
				// E00
				if ((vib[1] & 0x10) == 0) {
					// E000
					if ((vib[1] & 0x08) == 0) {
						// E000 0
						if ((vib[1] & 0x04) == 0) {
							// E000 00
							if ((vib[1] & 0x02) == 0) {
								// E000 000
								description = Description.ENERGY;
								multiplierExponent = 5 + (vib[1] & 0x01);
								unit = DlmsUnit.WATT_HOUR;
							}
							else {
								// E000 001
								description = Description.REACTIVE_ENERGY;
								multiplierExponent = 3 + (vib[1] & 0x01);
								unit = DlmsUnit.VAR_HOUR;
							}

						}
						else {
							// E000 01
							description = Description.NOT_SUPPORTED;
						}
					}
					else {
						// E000 1
						if ((vib[1] & 0x04) == 0) {
							// E000 10
							if ((vib[1] & 0x02) == 0) {
								// E000 100
								description = Description.ENERGY;
								multiplierExponent = 8 + (vib[1] & 0x01);
								unit = DlmsUnit.JOULE;
							}
							else {
								// E000 101
								description = Description.NOT_SUPPORTED;
							}

						}
						else {
							// E000 11
							description = Description.ENERGY;
							multiplierExponent = 5 + (vib[1] & 0x03);
							unit = DlmsUnit.CALORIFIC_VALUE;
						}
					}
				}
				else {
					// E001
					if ((vib[1] & 0x08) == 0) {
						// E001 0
						if ((vib[1] & 0x04) == 0) {
							// E001 00
							if ((vib[1] & 0x02) == 0) {
								// E001 000
								description = Description.VOLUME;
								multiplierExponent = 2 + (vib[1] & 0x01);
								unit = DlmsUnit.CUBIC_METRE;
							}
							else {
								// E001 001
								description = Description.NOT_SUPPORTED;
							}
						}
						else {
							// E001 01
							description = Description.REACTIVE_POWER;
							multiplierExponent = (vib[1] & 0x03);
							unit = DlmsUnit.VAR;
						}
					}
					else {
						// E001 1
						if ((vib[1] & 0x04) == 0) {
							// E001 10
							if ((vib[1] & 0x02) == 0) {
								// E001 100
								description = Description.MASS;
								multiplierExponent = 5 + (vib[1] & 0x01);
								unit = DlmsUnit.KILOGRAM;
							}
							else {
								// E001 101
								description = Description.REL_HUMIDITY;
								multiplierExponent = -1 + (vib[1] & 0x01);
								unit = DlmsUnit.PERCENTAGE;
							}

						}
						else {
							// E001 11
							description = Description.NOT_SUPPORTED;
						}
					}

				}
			}
			else {
				// E01
				if ((vib[1] & 0x10) == 0) {
					// E010
					if ((vib[1] & 0x08) == 0) {
						// E010 0
						if ((vib[1] & 0x04) == 0) {
							// E010 00
							if ((vib[1] & 0x02) == 0) {
								// E010 000
								if ((vib[1] & 0x01) == 0) {
									// E010 0000
									description = Description.VOLUME;
									multiplierExponent = 0;
									unit = DlmsUnit.CUBIC_FEET;
								}
								else {
									// E010 0001
									description = Description.VOLUME;
									multiplierExponent = -1;
									unit = DlmsUnit.CUBIC_FEET;
								}
							}
							else {
								// E010 001
								// outdated value !
								description = Description.VOLUME;
								multiplierExponent = -1 + (vib[1] & 0x01);
								unit = DlmsUnit.US_GALLON;
							}
						}
						else {
							// E010 01
							if ((vib[1] & 0x02) == 0) {
								// E010 010
								if ((vib[1] & 0x01) == 0) {
									// E010 0100
									// outdated value !
									description = Description.VOLUME_FLOW;
									multiplierExponent = -3;
									unit = DlmsUnit.US_GALLON_PER_MINUTE;
								}
								else {
									// E010 0101
									// outdated value !
									description = Description.VOLUME_FLOW;
									multiplierExponent = 0;
									unit = DlmsUnit.US_GALLON_PER_MINUTE;
								}
							}
							else {
								// E010 011
								if ((vib[1] & 0x01) == 0) {
									// E010 0110
									// outdated value !
									description = Description.VOLUME_FLOW;
									multiplierExponent = 0;
									unit = DlmsUnit.US_GALLON_PER_HOUR;
								}
								else {
									// E010 0111
									description = Description.NOT_SUPPORTED;
								}
							}

						}
					}
					else {
						// E010 1
						if ((vib[1] & 0x04) == 0) {
							// E010 10
							if ((vib[1] & 0x02) == 0) {
								// E010 100
								description = Description.POWER;
								multiplierExponent = 5 + (vib[1] & 0x01);
								unit = DlmsUnit.WATT;
							}
							else {
								if ((vib[1] & 0x01) == 0) {
									// E010 1010
									description = Description.PHASE;
									multiplierExponent = -1; // is -1 or 0 correct ??
									unit = DlmsUnit.DEGREE;
								}
								else {
									// E010 1011
									description = Description.PHASE;
									multiplierExponent = -1; // is -1 or 0 correct ??
									unit = DlmsUnit.DEGREE;
								}
							}
						}
						else {
							// E010 11
							description = Description.FREQUENCY;
							multiplierExponent = -3 + (vib[1] & 0x03);
							unit = DlmsUnit.HERTZ;
						}
					}
				}
				else {
					// E011
					if ((vib[1] & 0x08) == 0) {
						// E011 0
						if ((vib[1] & 0x04) == 0) {
							// E011 00
							if ((vib[1] & 0x02) == 0) {
								// E011 000
								description = Description.POWER;
								multiplierExponent = 8 + (vib[1] & 0x01);
								unit = DlmsUnit.JOULE_PER_HOUR;
							}
							else {
								// E011 001
								description = Description.NOT_SUPPORTED;
							}
						}
						else {
							// E011 01
							description = Description.NOT_SUPPORTED;
						}
					}
					else {
						// E011 1
						description = Description.NOT_SUPPORTED;
					}
				}
			}
		}
		else {
			// E1
			if ((vib[1] & 0x20) == 0) {
				// E10
				if ((vib[1] & 0x10) == 0) {
					// E100
					description = Description.NOT_SUPPORTED;
				}
				else {
					// E101
					if ((vib[1] & 0x08) == 0) {
						// E101 0
						description = Description.NOT_SUPPORTED;
					}
					else {
						// E101 1
						if ((vib[1] & 0x04) == 0) {
							// E101 10
							// outdated value !
							description = Description.FLOW_TEMPERATURE;
							multiplierExponent = (vib[1] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
						else {
							// E101 11
							// outdated value !
							description = Description.RETURN_TEMPERATURE;
							multiplierExponent = (vib[1] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
					}
				}
			}
			else {
				// E11
				if ((vib[1] & 0x10) == 0) {
					// E110
					if ((vib[1] & 0x08) == 0) {
						// E110 0
						if ((vib[1] & 0x04) == 0) {
							// E110 00
							// outdated value !
							description = Description.TEMPERATURE_DIFFERENCE;
							multiplierExponent = (vib[1] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
						else {
							// E110 01
							// outdated value !
							description = Description.FLOW_TEMPERATURE;
							multiplierExponent = (vib[1] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
					}
					else {
						// E110 1
						description = Description.NOT_SUPPORTED;
					}
				}
				else {
					// E111
					if ((vib[1] & 0x08) == 0) {
						// E111 0
						if ((vib[1] & 0x04) == 0) {
							// E111 00
							// outdated value !
							description = Description.TEMPERATURE_LIMIT;
							multiplierExponent = (vib[1] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
						else {
							// E111 01
							description = Description.TEMPERATURE_LIMIT;
							multiplierExponent = (vib[1] & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
					}
					else {
						// E111 1
						description = Description.MAX_POWER;
						multiplierExponent = (vib[1] & 0x07) - 3;
						unit = DlmsUnit.WATT;
					}
				}
			}

		}

	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();

		builder.append("DIB:").append(composeHexStringFromByteArray(dib));
		builder.append(", VIB:").append(composeHexStringFromByteArray(vib));

		if (!decoded) {
			builder.append(" -> DataRecord has not been decoded.");
			return builder.toString();
		}

		builder.append(" -> descr:").append(description);
		if (description == Description.USER_DEFINED) {
			builder.append(" :").append(getUserDefinedDescription());
		}
		builder.append(", function:").append(functionField);

		if (storageNumber > 0) {
			builder.append(", storage:").append(storageNumber);
		}

		if (tariff > 0) {
			builder.append(", tariff:").append(tariff);
		}

		if (subunit > 0) {
			builder.append(", subunit:").append(subunit);
		}

		switch (dataValueType) {
		case DATE:
			builder.append(", value:").append((dataValue).toString());
			break;
		case STRING:
			builder.append(", value:").append((dataValue).toString());
			break;
		case DOUBLE:
			builder.append(", scaled value:").append(getScaledDataValue());
			break;
		case LONG:
			if (multiplierExponent == 0) {
				builder.append(", value:").append(dataValue);
			}
			else {
				builder.append(", scaled value:").append(getScaledDataValue());
			}
			break;
		case BCD:
			if (multiplierExponent == 0) {
				builder.append(", value:").append((dataValue).toString());
			}
			else {
				builder.append(", scaled value:").append(getScaledDataValue());
			}
			break;
		case NONE:
			builder.append(", value: NONE");
			break;
		}

		if (unit != null) {
			builder.append(", unit:").append(unit);
		}

		return builder.toString();

	}

	private static String composeHexStringFromByteArray(byte[] data) {
		StringBuilder builder = new StringBuilder(data.length * 2);

		for (byte element : data) {
			builder.append(String.format("%02x", 0xff & element));
		}

		return builder.toString();
	}

}
