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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
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
		LONG,
		DOUBLE,
		DATE,
		STRING,
		BCD,
		NONE;
	}

	/**
	 * Function coded in the DIB
	 * 
	 */
	public enum FunctionField {
		INST_VAL,
		MAX_VAL,
		MIN_VAL,
		ERROR_VAL;
	}

	/**
	 * Data description stored in the VIB
	 * 
	 */
	public enum Description {
		ENERGY,
		VOLUME,
		MASS,
		ON_TIME,
		OPERATING_TIME,
		POWER,
		VOLUME_FLOW,
		VOLUME_FLOW_EXT,
		MASS_FLOW,
		FLOW_TEMPERATURE,
		RETURN_TEMPERATURE,
		TEMPERATURE_DIFFERENCE,
		EXTERNAL_TEMPERATURE,
		PRESSURE,
		DATE,
		DATE_TIME,
		VOLTAGE,
		CURRENT,
		AVERAGING_DURATION,
		ACTUALITY_DURATION,
		FABRICATION_NO,
		MODEL_VERSION,
		PARAMETER_SET_ID,
		HARDWARE_VERSION,
		FIRMWARE_VERSION,
		ERROR_FLAGS,
		CUSTOMER,
		RESERVED,
		OPERATING_TIME_BATTERY,
		HCA,
		REACTIVE_ENERGY,
		TEMPERATURE_LIMIT,
		MAX_POWER,
		REACTIVE_POWER,
		REL_HUMIDITY,
		FREQUENCY,
		PHASE,
		EXTENDED_IDENTIFICATION,
		ADDRESS,
		NOT_SUPPORTED,
		USER_DEFINED;
	}

	// // Data Information Block that contains a DIF and optionally up to 10 DIFEs
	private byte[] dib;
	// // Value Information Block that contains a VIF and optionally up to 10 VIFEs
	private byte[] vib;

	private Object dataValue;
	private DataValueType dataValueType;

	// DIB fields:
	private FunctionField functionField;
	// private int dataField;
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

	int decode(byte[] buffer, int offset, int length) throws DecodingException {

		int i = offset;

		// decode DIB
		int ff = ((buffer[i] & 0x30) >> 4);
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

		int dataField = buffer[i] & 0x0f;
		storageNumber = (buffer[i] & 0x40) >> 6;

		subunit = 0;
		tariff = 0;

		while ((buffer[i++] & 0x80) == 0x80) {
			subunit += (((buffer[i] & 0x40) >> 6) << (i - 1));
			tariff += ((buffer[i] & 0x30) >> 4) << ((i - 1) * 2);
			storageNumber += ((buffer[i] & 0x0f) << (((i - 1) * 4) + 1));
		}

		multiplierExponent = 0;

		unit = null;

		dib = Arrays.copyOfRange(buffer, offset, i);

		// decode VIB

		int vif = buffer[i++] & 0xff;

		boolean decodeFurtherVifes = false;

		if (vif == 0xfb) {
			decodeAlternateExtendedVif(buffer[i]);
			if ((buffer[i] & 0x80) == 0x80) {
				decodeFurtherVifes = true;
			}
			i++;
		}
		else if ((vif & 0x7f) == 0x7c) {
			i += decodeUserDefinedVif(buffer, i);
			if ((vif & 0x80) == 0x80) {
				decodeFurtherVifes = true;
			}
		}
		else if (vif == 0xfd) {
			decodeMainExtendedVif(buffer[i]);
			if ((buffer[i] & 0x80) == 0x80) {
				decodeFurtherVifes = true;
			}
			i++;
		}
		else if ((vif & 0x7f) == 0x7e) {
			throw new DecodingException("VIF types 0x7E/FE not supported.");
		}
		else if ((vif & 0x7f) == 0x7f) {
			throw new DecodingException("VIF types 0x7F/FF not supported.");
		}
		else {
			decodeMainVif(vif);
			if ((vif & 0x80) == 0x80) {
				decodeFurtherVifes = true;
			}
		}

		if (decodeFurtherVifes) {
			while ((buffer[i++] & 0x80) == 0x80) {
				// TODO these vifes should not be ignored!
			}
		}

		vib = Arrays.copyOfRange(buffer, offset + dib.length, i);

		switch (dataField) {
		case 0x00:
			dataValue = null;
			dataValueType = DataValueType.NONE;
			break;
		case 0x01: /* INT8 */
			dataValue = new Long(buffer[i++]);
			dataValueType = DataValueType.LONG;
			break;
		case 0x02: /* INT16 */
			if (dateTypeG) {
				int day = (0x1f) & buffer[i];
				int year1 = ((0xe0) & buffer[i++]) >> 5;
				int month = (0x0f) & buffer[i];
				int year2 = ((0xf0) & buffer[i++]) >> 1;
				int year = (2000 + year1 + year2);

				Calendar calendar = Calendar.getInstance();

				calendar.set(year, month - 1, day, 0, 0, 0);

				dataValue = calendar.getTime();
				dataValueType = DataValueType.DATE;
			}
			else {
				dataValue = new Long((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8));
				dataValueType = DataValueType.LONG;
			}
			break;
		case 0x03: /* INT24 */
			if ((buffer[i + 2] & 0x80) == 0x80) {
				// negative
				dataValue = new Long(
						(buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16) | 0xff << 24);
			}
			else {
				dataValue = new Long((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16));
			}
			dataValueType = DataValueType.LONG;
			break;
		case 0x04: /* INT32 */
			if (dateTypeF) {
				int min = (buffer[i++] & 0x3f);
				int hour = (buffer[i] & 0x1f);
				int yearh = (0x60 & buffer[i++]) >> 5;
				int day = (buffer[i] & 0x1f);
				int year1 = (0xe0 & buffer[i++]) >> 5;
				int mon = (buffer[i] & 0x0f);
				int year2 = (0xf0 & buffer[i++]) >> 1;

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
				dataValue = new Long((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16)
						| ((buffer[i++] & 0xff) << 24));
				dataValueType = DataValueType.LONG;
			}
			break;
		case 0x05: /* FLOAT32 */
			Float doubleDatavalue = ByteBuffer.wrap(buffer, i, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			i += 4;
			dataValue = new Double(doubleDatavalue);
			dataValueType = DataValueType.DOUBLE;
			break;
		case 0x06: /* INT48 */
			if ((buffer[i + 2] & 0x80) == 0x80) {
				// negative
				dataValue = new Long((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16)
						| ((buffer[i++] & 0xff) << 24) | (((long) buffer[i++] & 0xff) << 32)
						| (((long) buffer[i++] & 0xff) << 40) | (0xffl << 48) | (0xffl << 56));
			}
			else {
				dataValue = new Long((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16)
						| ((buffer[i++] & 0xff) << 24) | (((long) buffer[i++] & 0xff) << 32)
						| (((long) buffer[i++] & 0xff) << 40));
			}
			dataValueType = DataValueType.LONG;
			break;
		case 0x07: /* INT64 */
			dataValue = new Long((buffer[i++] & 0xff) | ((buffer[i++] & 0xff) << 8) | ((buffer[i++] & 0xff) << 16)
					| ((buffer[i++] & 0xff) << 24) | (((long) buffer[i++] & 0xff) << 32)
					| (((long) buffer[i++] & 0xff) << 40) | (((long) buffer[i++] & 0xff) << 48)
					| (((long) buffer[i++] & 0xff) << 56));
			dataValueType = DataValueType.LONG;
			break;
		case 0x08: /* no data - selection for readout request */
			dataValue = null;
			dataValueType = DataValueType.NONE;
			break;
		case 0x09:
			dataValue = new Bcd(Arrays.copyOfRange(buffer, i, i + 1));
			dataValueType = DataValueType.BCD;
			i += 1;
			break;
		case 0x0a:
			dataValue = new Bcd(Arrays.copyOfRange(buffer, i, i + 2));
			dataValueType = DataValueType.BCD;
			i += 2;
			break;
		case 0x0b:
			dataValue = new Bcd(Arrays.copyOfRange(buffer, i, i + 3));
			dataValueType = DataValueType.BCD;
			i += 3;
			break;
		case 0x0c:
			dataValue = new Bcd(Arrays.copyOfRange(buffer, i, i + 4));
			dataValueType = DataValueType.BCD;
			i += 4;
			break;
		case 0x0e:
			dataValue = new Bcd(Arrays.copyOfRange(buffer, i, i + 6));
			dataValueType = DataValueType.BCD;
			i += 6;
			break;
		case 0x0d:

			int variableLength = buffer[i++] & 0xff;
			int dataLength;

			if (variableLength < 0xc0) {
				dataLength = variableLength;
			}
			else if ((variableLength >= 0xc0) && (variableLength <= 0xc9)) {
				dataLength = 2 * (variableLength - 0xc0);
			}
			else if ((variableLength >= 0xd0) && (variableLength <= 0xd9)) {
				dataLength = 2 * (variableLength - 0xd0);
			}
			else if ((variableLength >= 0xe0) && (variableLength <= 0xef)) {
				dataLength = variableLength - 0xe0;
			}
			else if (variableLength == 0xf8) {
				dataLength = 4;
			}
			else {
				throw new DecodingException("Unsupported LVAR Field: " + variableLength);
			}

			// TODO check this:
			// if (variableLength >= 0xc0) {
			// throw new DecodingException("Variable length (LVAR) field >= 0xc0: " + variableLength);
			// }

			char rawData[] = new char[dataLength];

			for (int j = 0; j < dataLength; j++) {
				rawData[j] = (char) buffer[i + dataLength - 1 - j];
			}
			i += dataLength;

			dataValue = new String(rawData);
			dataValueType = DataValueType.STRING;
			break;
		default:
			throw new DecodingException("Unknown Data Field in DIF: " + HexConverter.toHexString((byte) dataField));
		}

		return i;

	}

	int encode(byte[] buffer, int offset) {

		int i = offset;

		System.arraycopy(dib, 0, buffer, i, dib.length);

		i += dib.length;

		System.arraycopy(vib, 0, buffer, i, vib.length);

		i += vib.length;

		return i - offset;
	}

	/**
	 * Returns a byte array containing the DIB (i.e. the DIF and the DIFEs) contained in the data record.
	 * 
	 * @return a byte array containing the DIB
	 */
	public byte[] getDib() {
		return dib;
	}

	/**
	 * Returns a byte array containing the VIB (i.e. the VIF and the VIFEs) contained in the data record.
	 * 
	 * @return a byte array containing the VIB
	 */
	public byte[] getVib() {
		return vib;
	}

	/**
	 * Returns the decoded data field of the data record as an Object. The Object is of one of the four types Long,
	 * Double, String or Date depending on information coded in the DIB/VIB. The DataType can be checked using
	 * getDataValueType().
	 * 
	 * @return the data value
	 */
	public Object getDataValue() {
		return dataValue;
	}

	public DataValueType getDataValueType() {
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
		return functionField;
	}

	public long getStorageNumber() {
		return storageNumber;
	}

	public int getTariff() {
		return tariff;
	}

	public short getSubunit() {
		return subunit;
	}

	public Description getDescription() {
		return description;
	}

	public String getUserDefinedDescription() {
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
		return multiplierExponent;
	}

	public DlmsUnit getUnit() {
		return unit;
	}

	private void decodeTimeUnit(int vif) {
		if ((vif & 0x02) == 0) {
			if ((vif & 0x01) == 0) {
				unit = DlmsUnit.SECOND;
			}
			else {
				unit = DlmsUnit.MIN;
			}
		}
		else {
			if ((vif & 0x01) == 0) {
				unit = DlmsUnit.HOUR;
			}
			else {
				unit = DlmsUnit.DAY;
			}
		}
	}

	private int decodeUserDefinedVif(byte[] buffer, int offset) throws DecodingException {

		int length = buffer[offset];
		StringBuilder sb = new StringBuilder();
		for (int i = offset + length; i > offset; i--) {
			sb.append((char) buffer[i]);
		}

		description = Description.USER_DEFINED;
		userDefinedDescription = sb.toString();

		return length + 1;

	}

	private void decodeMainVif(int vif) {
		description = Description.NOT_SUPPORTED;

		if ((vif & 0x40) == 0) {
			// E0
			if ((vif & 0x20) == 0) {
				// E00
				if ((vif & 0x10) == 0) {
					// E000
					if ((vif & 0x08) == 0) {
						// E000 0
						description = Description.ENERGY;
						multiplierExponent = (vif & 0x07) - 3;
						unit = DlmsUnit.WATT_HOUR;
					}
					else {
						// E000 1
						description = Description.ENERGY;
						multiplierExponent = vif & 0x07;
						unit = DlmsUnit.JOULE;
					}
				}
				else {
					// E001
					if ((vif & 0x08) == 0) {
						// E001 0
						description = Description.VOLUME;
						multiplierExponent = (vif & 0x07) - 6;
						unit = DlmsUnit.CUBIC_METRE;
					}
					else {
						// E001 1
						description = Description.MASS;
						multiplierExponent = (vif & 0x07) - 3;
						unit = DlmsUnit.KILOGRAM;
					}
				}
			}
			else {
				// E01
				if ((vif & 0x10) == 0) {
					// E010
					if ((vif & 0x08) == 0) {
						// E010 0
						if ((vif & 0x04) == 0) {
							// E010 00
							description = Description.ON_TIME;
						}
						else {
							// E010 01
							description = Description.OPERATING_TIME;
						}
						decodeTimeUnit(vif);
					}
					else {
						// E010 1
						description = Description.POWER;
						multiplierExponent = (vif & 0x07) - 3;
						unit = DlmsUnit.WATT;
					}
				}
				else {
					// E011
					if ((vif & 0x08) == 0) {
						// E011 0
						description = Description.POWER;
						multiplierExponent = vif & 0x07;
						unit = DlmsUnit.JOULE_PER_HOUR;
					}
					else {
						// E011 1
						description = Description.VOLUME_FLOW;
						multiplierExponent = (vif & 0x07) - 6;
						unit = DlmsUnit.CUBIC_METRE_PER_HOUR;
					}
				}
			}
		}
		else {
			// E1
			if ((vif & 0x20) == 0) {
				// E10
				if ((vif & 0x10) == 0) {
					// E100
					if ((vif & 0x08) == 0) {
						// E100 0
						description = Description.VOLUME_FLOW_EXT;
						multiplierExponent = (vif & 0x07) - 7;
						unit = DlmsUnit.CUBIC_METRE_PER_MINUTE;
					}
					else {
						// E100 1
						description = Description.VOLUME_FLOW_EXT;
						multiplierExponent = (vif & 0x07) - 9;
						unit = DlmsUnit.CUBIC_METRE_PER_SECOND;
					}
				}
				else {
					// E101
					if ((vif & 0x08) == 0) {
						// E101 0
						description = Description.MASS_FLOW;
						multiplierExponent = (vif & 0x07) - 3;
						unit = DlmsUnit.KILOGRAM_PER_HOUR;
					}
					else {
						// E101 1
						if ((vif & 0x04) == 0) {
							// E101 10
							description = Description.FLOW_TEMPERATURE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
						else {
							// E101 11
							description = Description.RETURN_TEMPERATURE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
					}
				}
			}
			else {
				// E11
				if ((vif & 0x10) == 0) {
					// E110
					if ((vif & 0x08) == 0) {
						// E110 0
						if ((vif & 0x04) == 0) {
							// E110 00
							description = Description.TEMPERATURE_DIFFERENCE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.KELVIN;
						}
						else {
							// E110 01
							description = Description.EXTERNAL_TEMPERATURE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
					}
					else {
						// E110 1
						if ((vif & 0x04) == 0) {
							// E110 10
							description = Description.PRESSURE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.BAR;
						}
						else {
							// E110 11
							if ((vif & 0x02) == 0) {
								// E110 110
								if ((vif & 0x01) == 0) {
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
								if ((vif & 0x01) == 0) {
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
					if ((vif & 0x08) == 0) {
						// E111 0
						if ((vif & 0x04) == 0) {
							description = Description.AVERAGING_DURATION;
						}
						else {
							description = Description.ACTUALITY_DURATION;
						}
						decodeTimeUnit(vif);
					}
					else {
						// E111 1
						if ((vif & 0x04) == 0) {
							// E111 10
							if ((vif & 0x02) == 0) {
								// E111 100
								if ((vif & 0x01) == 0) {
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
								if ((vif & 0x01) == 0) {
									description = Description.ADDRESS;
								}
								else {
									// E111 1011
									// Codes used with extension indicator 0xFB (table 29 of DIN EN 13757-3:2011)
									throw new IllegalArgumentException(
											"Trying to decode a mainVIF even though it is an alternate extended vif");
								}
							}
						}
						else {
							// E111 11
							if ((vif & 0x02) == 0) {
								// E111 110
								if ((vif & 0x01) == 0) {
									// E111 1100
									// Extension indicator 0xFC: VIF is given in following string
									description = Description.NOT_SUPPORTED;
								}
								else {
									// E111 1101
									// Extension indicator 0xFD: main VIFE-code extension table (table 28 of DIN EN
									// 13757-3:2011)
									throw new IllegalArgumentException(
											"Trying to decode a mainVIF even though it is a main extended vif");

								}
							}

						}

					}
				}
			}

		}

	}

	// implements table 28 of DIN EN 13757-3:2011
	private void decodeMainExtendedVif(byte vif) {
		if ((vif & 0x70) == 0x40) {
			description = Description.VOLTAGE;
			multiplierExponent = (vif & 0x0f) - 9;
			unit = DlmsUnit.VOLT;
		}
		else if ((vif & 0x70) == 0x50) {
			description = Description.CURRENT;
			multiplierExponent = (vif & 0x0f) - 12;
			unit = DlmsUnit.AMPERE;
		}
		else if ((vif & 0x7c) == 0x6c) {
			description = Description.OPERATING_TIME_BATTERY;
			switch (vif & 0x03) {
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
		else if ((vif & 0x7f) == 0x0b) {
			description = Description.PARAMETER_SET_ID;
		}
		else if ((vif & 0x7f) == 0x0c) {
			description = Description.MODEL_VERSION;
		}
		else if ((vif & 0x7f) == 0x0d) {
			description = Description.HARDWARE_VERSION;
		}
		else if ((vif & 0x7f) == 0x0e) {
			description = Description.FIRMWARE_VERSION;
		}
		else if ((vif & 0x7f) == 0x11) {
			description = Description.CUSTOMER;
		}
		else if ((vif & 0x7f) == 0x17) {
			description = Description.ERROR_FLAGS;
		}
		else if ((vif & 0x7f) >= 0x77) {
			description = Description.RESERVED;
		}
		else {
			description = Description.NOT_SUPPORTED;
		}
	}

	// implements table 29 of DIN EN 13757-3:2011
	private void decodeAlternateExtendedVif(byte vif) {
		description = Description.NOT_SUPPORTED; // default value

		if ((vif & 0x40) == 0) {
			// E0
			if ((vif & 0x20) == 0) {
				// E00
				if ((vif & 0x10) == 0) {
					// E000
					if ((vif & 0x08) == 0) {
						// E000 0
						if ((vif & 0x04) == 0) {
							// E000 00
							if ((vif & 0x02) == 0) {
								// E000 000
								description = Description.ENERGY;
								multiplierExponent = 5 + (vif & 0x01);
								unit = DlmsUnit.WATT_HOUR;
							}
							else {
								// E000 001
								description = Description.REACTIVE_ENERGY;
								multiplierExponent = 3 + (vif & 0x01);
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
						if ((vif & 0x04) == 0) {
							// E000 10
							if ((vif & 0x02) == 0) {
								// E000 100
								description = Description.ENERGY;
								multiplierExponent = 8 + (vif & 0x01);
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
							multiplierExponent = 5 + (vif & 0x03);
							unit = DlmsUnit.CALORIFIC_VALUE;
						}
					}
				}
				else {
					// E001
					if ((vif & 0x08) == 0) {
						// E001 0
						if ((vif & 0x04) == 0) {
							// E001 00
							if ((vif & 0x02) == 0) {
								// E001 000
								description = Description.VOLUME;
								multiplierExponent = 2 + (vif & 0x01);
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
							multiplierExponent = (vif & 0x03);
							unit = DlmsUnit.VAR;
						}
					}
					else {
						// E001 1
						if ((vif & 0x04) == 0) {
							// E001 10
							if ((vif & 0x02) == 0) {
								// E001 100
								description = Description.MASS;
								multiplierExponent = 5 + (vif & 0x01);
								unit = DlmsUnit.KILOGRAM;
							}
							else {
								// E001 101
								description = Description.REL_HUMIDITY;
								multiplierExponent = -1 + (vif & 0x01);
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
				if ((vif & 0x10) == 0) {
					// E010
					if ((vif & 0x08) == 0) {
						// E010 0
						if ((vif & 0x04) == 0) {
							// E010 00
							if ((vif & 0x02) == 0) {
								// E010 000
								if ((vif & 0x01) == 0) {
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
								multiplierExponent = -1 + (vif & 0x01);
								unit = DlmsUnit.US_GALLON;
							}
						}
						else {
							// E010 01
							if ((vif & 0x02) == 0) {
								// E010 010
								if ((vif & 0x01) == 0) {
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
								if ((vif & 0x01) == 0) {
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
						if ((vif & 0x04) == 0) {
							// E010 10
							if ((vif & 0x02) == 0) {
								// E010 100
								description = Description.POWER;
								multiplierExponent = 5 + (vif & 0x01);
								unit = DlmsUnit.WATT;
							}
							else {
								if ((vif & 0x01) == 0) {
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
							multiplierExponent = -3 + (vif & 0x03);
							unit = DlmsUnit.HERTZ;
						}
					}
				}
				else {
					// E011
					if ((vif & 0x08) == 0) {
						// E011 0
						if ((vif & 0x04) == 0) {
							// E011 00
							if ((vif & 0x02) == 0) {
								// E011 000
								description = Description.POWER;
								multiplierExponent = 8 + (vif & 0x01);
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
			if ((vif & 0x20) == 0) {
				// E10
				if ((vif & 0x10) == 0) {
					// E100
					description = Description.NOT_SUPPORTED;
				}
				else {
					// E101
					if ((vif & 0x08) == 0) {
						// E101 0
						description = Description.NOT_SUPPORTED;
					}
					else {
						// E101 1
						if ((vif & 0x04) == 0) {
							// E101 10
							// outdated value !
							description = Description.FLOW_TEMPERATURE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
						else {
							// E101 11
							// outdated value !
							description = Description.RETURN_TEMPERATURE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
					}
				}
			}
			else {
				// E11
				if ((vif & 0x10) == 0) {
					// E110
					if ((vif & 0x08) == 0) {
						// E110 0
						if ((vif & 0x04) == 0) {
							// E110 00
							// outdated value !
							description = Description.TEMPERATURE_DIFFERENCE;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
						else {
							// E110 01
							// outdated value !
							description = Description.FLOW_TEMPERATURE;
							multiplierExponent = (vif & 0x03) - 3;
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
					if ((vif & 0x08) == 0) {
						// E111 0
						if ((vif & 0x04) == 0) {
							// E111 00
							// outdated value !
							description = Description.TEMPERATURE_LIMIT;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_FAHRENHEIT;
						}
						else {
							// E111 01
							description = Description.TEMPERATURE_LIMIT;
							multiplierExponent = (vif & 0x03) - 3;
							unit = DlmsUnit.DEGREE_CELSIUS;
						}
					}
					else {
						// E111 1
						description = Description.MAX_POWER;
						multiplierExponent = (vif & 0x07) - 3;
						unit = DlmsUnit.WATT;
					}
				}
			}

		}

	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();

		builder.append("DIB:").append(HexConverter.toHexString(dib));
		builder.append(", VIB:").append(HexConverter.toHexString(vib));

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

}
