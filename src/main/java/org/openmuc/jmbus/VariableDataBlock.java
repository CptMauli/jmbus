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

import java.text.ParseException;
import java.util.Calendar;
import org.openmuc.jmbus.DLMSUnit;

/**
 * Representation of a Variable Data Block.
 * 
 * A variable data block is the basic data entity in the M-Bus application
 * layer. They form parts of different types of M-Bus messages (e.g. a
 * VariableDataResponse - RESP-UD). It encodes information on the data type,
 * status, meaning, unit and value of a date. Data type and status are encoded
 * in the DIB (Data Information Block) whilst meaning and unit are encoded in
 * the VIB (Value Information Block).
 * 
 */
public class VariableDataBlock {

	public enum FunctionField {
		INST_VAL, MAX_VAL, MIN_VAL, ERROR_VAL;	
	}

	// Data descriptions stored in the VIF
	public enum Description {
		ENERGY, VOLUME, MASS, ON_TIME, OPERATING_TIME, POWER, VOLUME_FLOW, VOLUME_FLOW_EXT, MASS_FLOW, FLOW_TEMPERATURE, RETURN_TEMPERATURE, TEMPERATURE_DIFFERENCE, EXTERNAL_TEMPERATURE, PRESSURE, DATE, DATE_TIME, VOLTAGE, CURRENT, FABRICATION_NO, MODEL_VERSION, PARAMETER_SET_ID, HARDWARE_VERSION, FIRMWARE_VERSION;
	}

	// Data Information Block that contains a DIF and optionally up to 10 DIFEs
	private byte[] dib;
	// Value Information Block that contains a VIF and optionally up to 10 VIFEs
	private byte[] vib;
	private byte[] dataBytes;
	private Integer lvar;

	boolean parsed;

	public Object data;

	// DIB Fields:
	FunctionField functionField;
	byte dataField;
	long storageNumber; // max is 41 bits
	int tariff; // max 20 bits
	short deviceUnit; // max 10 bits

	// VIB Fields:
	Description description;
	byte scaler;
	int unit;

	public VariableDataBlock(byte[] dib, byte[] vib, byte[] data, Integer lvar) {
		this.dib = dib;
		this.vib = vib;
		this.lvar = lvar;
		this.dataBytes = data;
		parsed = false;
	}

	/*
	 * Returns a byte array containing the DIF and the DIFEs contained in the
	 * Variable Data Block
	 */
	public byte[] getDIB() {
		return dib;
	}

	/*
	 * Returns a byte array containing the VIF and the VIFEs contained in the
	 * Variable Data Block
	 */
	public byte[] getVIB() {
		return vib;
	}

	public byte[] getDataBytes() {
		return dataBytes;
	}

	/*
	 * Returns the data as an Object. The Object could be a Byte, Short,
	 * Integer, ... depending on the DataField information
	 */
	public Object getData() {
		if (parsed) {
			return data;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public FunctionField getFunctionField() {
		if (parsed) {
			return functionField;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public byte getDataField() {
		if (parsed) {
			return dataField;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public long getStorageNumber() {
		if (parsed) {
			return storageNumber;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public int getTariff() {
		if (parsed) {
			return tariff;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public short getDeviceUnit() {
		if (parsed) {
			return deviceUnit;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public Description getDescription() {
		if (parsed) {
			return description;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public byte getScaler() {
		if (parsed) {
			return scaler;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
	}

	public int getUnit() {
		if (parsed) {
			return unit;
		} else {
			throw new RuntimeException("Variable Data Block was not parsed.");
		}
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

		scaler = 0;

		// decode VIB

		// if extension bit not set
		if ((vib[0] & 0x80) == 0) {
			if ((vib[0] & 0x40) == 0) {
				if ((vib[0] & 0x20) == 0) {
					if ((vib[0] & 0x10) == 0) {
						if ((vib[0] & 0x08) == 0) {
							description = Description.ENERGY;
							scaler = (byte) ((vib[0] & 0x07) - 3);
							unit = DLMSUnit.WATT_HOUR;
						} else {
							description = Description.ENERGY;
							scaler = (byte) (vib[0] & 0x07);
							unit = DLMSUnit.JOULE;
						}
					} else {
						if ((vib[0] & 0x08) == 0) {
							description = Description.VOLUME;
							scaler = (byte) ((vib[0] & 0x07) - 6);
							unit = DLMSUnit.CUBIC_METRE;
						} else {
							description = Description.MASS;
							scaler = (byte) ((vib[0] & 0x07) - 3);
							unit = DLMSUnit.KILOGRAM;
						}
					}
				} else {
					if ((vib[0] & 0x10) == 0) {
						if ((vib[0] & 0x08) == 0) {
							if ((vib[0] & 0x04) == 0) {
								description = Description.ON_TIME;
							} else {
								description = Description.OPERATING_TIME;
							}
							if ((vib[0] & 0x02) == 0) {
								if ((vib[0] & 0x01) == 0) {
									unit = DLMSUnit.SECOND;
								} else {
									unit = DLMSUnit.MIN;
								}
							} else {
								if ((vib[0] & 0x01) == 0) {
									unit = DLMSUnit.HOUR;
								} else {
									unit = DLMSUnit.DAY;
								}
							}
						} else {
							description = Description.POWER;
							scaler = (byte) ((vib[0] & 0x07) - 3);
							unit = DLMSUnit.WATT;
						}
					} else {
						if ((vib[0] & 0x08) == 0) {
							description = Description.POWER;
							scaler = (byte) (vib[0] & 0x07);
							unit = DLMSUnit.JOULE_PER_HOUR;
						} else {
							description = Description.VOLUME_FLOW;
							scaler = (byte) ((vib[0] & 0x07) - 6);
							unit = DLMSUnit.CUBIC_METRE_PER_HOUR;
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
							scaler = (byte) ((vib[0] & 0x07) - 7);
							unit = DLMSUnit.CUBIC_METRE_PER_MINUTE;
						} else {
							description = Description.VOLUME_FLOW_EXT;
							scaler = (byte) ((vib[0] & 0x07) - 9);
							unit = DLMSUnit.CUBIC_METRE_PER_SECOND;
						}
					} else {
						if ((vib[0] & 0x08) == 0) {
							description = Description.MASS_FLOW;
							scaler = (byte) ((vib[0] & 0x07) - 3);
							unit = DLMSUnit.KILOGRAM_PER_HOUR;
						} else {
							if ((vib[0] & 0x04) == 0) {
								description = Description.FLOW_TEMPERATURE;
								scaler = (byte) ((vib[0] & 0x03) - 3);
								unit = DLMSUnit.DEGREE_CELSIUS;
							} else {
								description = Description.RETURN_TEMPERATURE;
								scaler = (byte) ((vib[0] & 0x03) - 3);
								unit = DLMSUnit.DEGREE_CELSIUS;
							}
						}
					}
				} else { /* 011 ... */
					if ((vib[0] & 0x10) == 0) {
						if ((vib[0] & 0x08) == 0) {
							if ((vib[0] & 0x04) == 0) {
								description = Description.TEMPERATURE_DIFFERENCE;
								scaler = (byte) ((vib[0] & 0x03) - 3);
								unit = DLMSUnit.KELVIN;
							} else {
								description = Description.EXTERNAL_TEMPERATURE;
								scaler = (byte) ((vib[0] & 0x03) - 3);
								unit = DLMSUnit.DEGREE_CELSIUS;
							}
						} else { /* 01101 ... */
							if ((vib[0] & 0x04) == 0) {
								description = Description.PRESSURE;
								scaler = (byte) ((vib[0] & 0x03) - 3);
								unit = DLMSUnit.BAR;
							}
							else { /* 011011 .. */
								if ((vib[0] & 0x02) == 0) {
									if ((vib[0] & 0x01) == 0) {
										description = Description.DATE;
										dateTypeG = true;
									}
									else {
										description = Description.DATE_TIME;
										dateTypeF = true;
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
		} else if (vib[0] == (byte) 0xFD) { /* Main VIFE-code extension table */
			if ((vib[1] & 0x70) == 0x40) {
				description = Description.VOLTAGE;
				scaler = (byte) ((vib[1] & 0x0f) - 9);
				unit = DLMSUnit.VOLT;
			} else if ((vib[1] & 0x70) == 0x50) {
				description = Description.CURRENT;
				scaler = (byte) ((vib[1] & 0x0f) - 12);
				unit = DLMSUnit.AMPERE;
			} else if ((vib[1] & 0x7f) == 0x0b) {
				description = Description.PARAMETER_SET_ID;
			} else if ((vib[1] & 0x7f) == 0x0c) {
				description = Description.MODEL_VERSION;
			} else if ((vib[1] & 0x7f) == 0x0d) {
				description = Description.HARDWARE_VERSION;
			} else if ((vib[1] & 0x7f) == 0x0e) {
				description = Description.FIRMWARE_VERSION;
			}
		} else {
			throw new ParseException("Parsing Error: unable to parse VIB", 0);
		}

		// decode dataField

		switch (dataField) {
		case 0x01: /* INT8 */
			data = new Byte(dataBytes[0]);
			break;
		case 0x02: /* INT16 */
			if (dateTypeG) {
				int day = (0x1f) & (int) dataBytes[0];
				int month = (0x0f) & (int) dataBytes[1];
				int year1 = ((0xe0) & (int) dataBytes[0]) >> 5;
				int year2 = ((0xf0) & (int) dataBytes[1]) >> 1;
				int year = (2000 + year1 + year2);
				
				Calendar calendar = Calendar.getInstance();
				
				calendar.set(year, month -1, day, 0, 0, 0);
				
				data = calendar.getTime();
			}
			else {
				data = new Short((short) ((dataBytes[0] & 0xff) + ((dataBytes[1] & 0xff) << 8)));
			}
			break;
		case 0x03: /* INT24 */
			data = new Integer((dataBytes[0] & 0xff) + ((dataBytes[1] & 0xff) << 8) + ((dataBytes[2] & 0xff) << 16));
			break;
		case 0x04: /* INT32 */
			if (dateTypeF) {
				int min = (dataBytes[0] & 0x3f);
				int hour = (dataBytes[1] & 0x1f);
				int day = (dataBytes[2] & 0x1f);
				int mon = (dataBytes[3] & 0x0f);
				int year1 = (0xe0 & (int) dataBytes[2]) >> 5;
				int year2 = (0xf0 & (int) dataBytes[3]) >> 1;
				int yearh = (0x60 & (int) dataBytes[1]) >> 5;
				
				if (yearh == 0) yearh = 1;
			
				int year = 1900 + 100*yearh + year1 + year2;
				
				Calendar calendar = Calendar.getInstance();
				
				calendar.set(year, mon -1, day, hour, min, 0);
				
				data = calendar.getTime();
			}
			else { 
				data = new Integer((dataBytes[0] & 0xff) + ((dataBytes[1] & 0xff) << 8) + ((dataBytes[2] & 0xff) << 16)
					+ ((dataBytes[3] & 0xff) << 24));
			}
			break;
		case 0x06: /* INT48 */
			data = new Long((dataBytes[0] & 0xff) + ((dataBytes[1] & 0xff) << 8) + ((dataBytes[2] & 0xff) << 16)
					+ ((dataBytes[3] & 0xff) << 24) + (((long) dataBytes[4] & 0xff) << 32)
					+ (((long) dataBytes[5] & 0xff) << 40));
			break;
		case 0x07: /* INT64 */
			data = new Long((dataBytes[0] & 0xff) + ((dataBytes[1] & 0xff) << 8) + ((dataBytes[2] & 0xff) << 16)
					+ (((long) dataBytes[3] & 0xff) << 24) + (((long) dataBytes[4] & 0xff) << 32)
					+ (((long) dataBytes[5] & 0xff) << 40) + (((long) dataBytes[6] & 0xff) << 48)
					+ (((long) dataBytes[7] & 0xff) << 56));
			break;
		case 0x0a:
		case 0x0b:
		case 0x0c:
			data = new BCD(dataBytes);
			break;
		case 0x0d: /* variable length - LVAR */
			if (lvar < 0xc0) {
				 char rawData[] = new char[dataBytes.length];
				 
				 for (int i = 0; i < dataBytes.length; i++) {
					 rawData[i] = (char) dataBytes[dataBytes.length - 1 - i];
				 }
				 
				 data = new String(rawData);
			}
			
			break;
		case 0x0e:
			data = new BCD(dataBytes);
			break;
		default:
			throw new ParseException("Unknown Data Field in DIF", 0);
		}

		parsed = true;

	} /* parse() */

} /* class VariableDataBlock */
