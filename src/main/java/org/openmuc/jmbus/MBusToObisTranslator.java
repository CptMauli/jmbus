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

import java.util.Iterator;
import java.util.Vector;

/**
 * Get the corresponding OBIS code for a given M-Bus VariableDataBlock.
 * 
 * Since this class is a singleton you have to get the instance with the static getInstance() method.
 * 
 * @author mzillgit
 */
public class MBusToObisTranslator {

	private static MBusToObisTranslator instance = null;

	// TODO Umskalierung kann fehlschlagen, wenn der Zwischenwert den int
	// Zahlenbreich ueberschreitet!
	// TODO Umskalierung auf die Grundeinheit ist schlecht, wenn der Wert als
	// Integer gespeichert wird
	class TranslationRule {
		byte[] difMask;
		byte[] difValue;
		byte[] vifMask;
		byte[] vifValue;
		String obisCode;
		byte scalingMask;
		byte medium;
		int baseExponent;
		int unit; /* According to DLMSUnit */

		/**
		 * @param medium
		 * @param difMask
		 * @param difValue
		 * @param vifMask
		 * @param vifValue
		 * @param obisCodeStr
		 * @param scalingMask
		 * @param baseExponent
		 * @param unit
		 */
		public TranslationRule(byte medium, byte[] difMask, byte[] difValue, byte[] vifMask, byte[] vifValue,
				String obisCodeStr, byte scalingMask, int baseExponent, int unit) {
			this.medium = medium;
			this.difMask = difMask;
			this.difValue = difValue;
			this.vifMask = vifMask;
			this.vifValue = vifValue;
			this.scalingMask = scalingMask;
			this.baseExponent = baseExponent;
			this.unit = unit;

			obisCode = obisCodeStr;
		}
	}

	private final Vector<TranslationRule> rules;

	private void populateRuleTable() {
		// TranslationRule rule;

		/* Current total active energy */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x00 }, "1-0:1.8.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT_HOUR));

		/*
		 * Current total active energy (for PadPuls) - removed conflicts with other rules
		 */
		// rules.add(new TranslationRule((byte) 2,
		// new byte[] {(byte) 0xf0, (byte) 0x00 }, new byte[] {(byte) 0x80,
		// (byte) 0x00},
		// new byte[] {(byte) 0xf8}, new byte[] {(byte) 0x00},
		// "1-0:1.8.0*255", (byte) 0x07, -3, DLMSUnit.WATT_HOUR));

		/*
		 * Current accumulated active energy (forward - consumption?) added for KAM WMBUS 382
		 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 }, new byte[] {
				(byte) 0xf8, (byte) 0xff }, new byte[] { (byte) 0x80, (byte) 0x3b }, "1-0:1.8.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT_HOUR));

		/*
		 * Current accumulated active energy (backward - delivery?) added for KAM WMBUS 382
		 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 }, new byte[] {
				(byte) 0xf8, (byte) 0xff }, new byte[] { (byte) 0x80, (byte) 0x3c }, "1-0:2.8.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT_HOUR));

		/*
		 * Current active power (W) (forward - consumption?) added for KAM WMBUS 382
		 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 }, new byte[] {
				(byte) 0xf8, (byte) 0xff }, new byte[] { (byte) 0xa8, (byte) 0x3b }, "1-0:1.7.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT));

		/*
		 * Current active power (W) (backward - delivery?) added for KAM WMBUS 382
		 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 }, new byte[] {
				(byte) 0xf8, (byte) 0xff }, new byte[] { (byte) 0xa8, (byte) 0x3c }, "1-0:2.7.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT));

		/* Current total active power */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x28 }, "1-0:1.7.0*255", (byte) 0x07, -3, DlmsUnit.WATT));

		/* Maximum total active power */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x10 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x28 }, "1-0:1.7.6*255", (byte) 0x07, -3, DlmsUnit.WATT));

		/* Current active energy tariff 1 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0, (byte) 0xbf }, new byte[] { (byte) 0x80,
				(byte) 0x10 }, new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x00 }, "1-0:1.8.1*255", (byte) 0x07,
				-3, DlmsUnit.WATT_HOUR));

		/* Current active energy tariff 2 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0, (byte) 0xbf }, new byte[] { (byte) 0x80,
				(byte) 0x20 }, new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x00 }, "1-0:1.8.2*255", (byte) 0x07,
				-3, DlmsUnit.WATT_HOUR));

		/* Current voltage phase L1 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 }, new byte[] {
				(byte) 0xff, (byte) 0xf0 }, new byte[] { (byte) 0xfd, (byte) 0x40 }, "1-0:32.7.0*255", (byte) 0x0f, -9,
				DlmsUnit.VOLT));

		/* Current current phase L1 */
		rules.add(new TranslationRule((byte) 2, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 }, new byte[] {
				(byte) 0xff, (byte) 0xf0 }, new byte[] { (byte) 0xfd, (byte) 0x50 }, "1-0:31.7.0*255", (byte) 0x0f,
				-12, DlmsUnit.AMPERE));

		/***********************/
		/* Heat */
		/***********************/

		/* Heat: total energy - current value */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x00 }, "6-0:1.0.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT_HOUR));

		/* Heat: total energy - set date value */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x40 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x00 }, "6-0:1.2.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT_HOUR));

		/* Heat: power (energy flow) - average, current value [W] */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x28 }, "6-0:8.0.0*255", (byte) 0x07, -3, DlmsUnit.WATT));

		/* Heat: flow rate - average, current value [m^3/h] */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x38 }, "6-0:9.0.0*255", (byte) 0x07, -6,
				DlmsUnit.CUBIC_METRE_PER_HOUR));

		/* Heat: flow temperature [°C] */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xfc }, new byte[] { (byte) 0x58 }, "6-0:10.0.0*255", (byte) 0x03, -3,
				DlmsUnit.DEGREE_CELSIUS));

		/* Heat: return temperature [°C] */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xfc }, new byte[] { (byte) 0x5c }, "6-0:11.0.0*255", (byte) 0x03, -3,
				DlmsUnit.DEGREE_CELSIUS));

		/* Heat: temperature difference [°C] */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xfc }, new byte[] { (byte) 0x60 }, "6-0:12.0.0*255", (byte) 0x03, -3,
				DlmsUnit.DEGREE_CELSIUS));

		/* Heat: Volume [m^3] */
		rules.add(new TranslationRule((byte) 4, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x10 }, "8-0:1.0.0*255", (byte) 0x07, -6,
				DlmsUnit.CUBIC_METRE));

		/**************************************/
		/* Heat (volume measured at flow temp */
		/**************************************/

		/* Heat: total energy - current value */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x00 }, "6-0:1.0.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT_HOUR));

		/* Heat: total energy - set date value */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x40 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x00 }, "6-0:1.2.0*255", (byte) 0x07, -3,
				DlmsUnit.WATT_HOUR));

		/* Heat: power (energy flow) - average, current value [W] */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x28 }, "6-0:8.0.0*255", (byte) 0x07, -3, DlmsUnit.WATT));

		/* Heat: flow rate - average, current value [m^3/h] */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x38 }, "6-0:9.0.0*255", (byte) 0x07, -6,
				DlmsUnit.CUBIC_METRE_PER_HOUR));

		/* Heat: flow temperature [°C] */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xfc }, new byte[] { (byte) 0x58 }, "6-0:10.0.0*255", (byte) 0x03, -3,
				DlmsUnit.DEGREE_CELSIUS));

		/* Heat: return temperature [°C] */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xfc }, new byte[] { (byte) 0x5c }, "6-0:11.0.0*255", (byte) 0x03, -3,
				DlmsUnit.DEGREE_CELSIUS));

		/* Heat: temperature difference [°C] */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xfc }, new byte[] { (byte) 0x60 }, "6-0:12.0.0*255", (byte) 0x03, -3,
				DlmsUnit.DEGREE_CELSIUS));

		/* Heat: Volume [m^3] */
		rules.add(new TranslationRule((byte) 12, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x10 }, "8-0:1.0.0*255", (byte) 0x07, -6,
				DlmsUnit.CUBIC_METRE));

		/*************************
		 * Water *
		 **************************/
		/* Water: Volume [m^3] total current value */
		rules.add(new TranslationRule((byte) 7, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x10 }, "8-0:1.0.0*255", (byte) 0x07, -6,
				DlmsUnit.CUBIC_METRE));

		/* Water: Volume [m^3] total current value */
		rules.add(new TranslationRule((byte) 7, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x40 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x10 }, "8-0:1.2.0*255", (byte) 0x07, -6,
				DlmsUnit.CUBIC_METRE));

		/* Water: Volume flow [m^3/h] total current value */
		rules.add(new TranslationRule((byte) 7, new byte[] { (byte) 0xf0 }, new byte[] { (byte) 0x00 },
				new byte[] { (byte) 0xf8 }, new byte[] { (byte) 0x38 }, "8-0:2.0.0*255", (byte) 0x07, -6,
				DlmsUnit.CUBIC_METRE_PER_HOUR));

	}

	private boolean checkRule(byte medium, VariableDataBlock vdb, TranslationRule rule) {
		/* Check if medium matches */
		if (medium != rule.medium) {
			return false;
		}

		/* check if difs match */
		if (vdb.getDIB().length != rule.difMask.length) {
			return false;
		}

		for (int i = 0; i < rule.difMask.length; i++) {
			if ((vdb.getDIB()[i] & rule.difMask[i]) != rule.difValue[i]) {
				return false;
			}
		}

		/* check if vifs match */
		if (vdb.getVIB().length != rule.vifMask.length) {
			return false;
		}

		for (int i = 0; i < rule.vifMask.length; i++) {
			if ((vdb.getVIB()[i] & rule.vifMask[i]) != rule.vifValue[i]) {
				return false;
			}
		}

		return true;
	}

	public String getOBISCode(byte medium, VariableDataBlock vdb) {
		boolean found = false;
		Iterator<TranslationRule> it;
		TranslationRule rule;

		it = rules.iterator();

		while (it.hasNext() && (found == false)) {
			rule = it.next();
			if (checkRule(medium, vdb, rule) == true) {
				return (rule.obisCode);
			}
		}

		return null;
	}

	public MBusToObisTranslator() {
		rules = new Vector<TranslationRule>();

		populateRuleTable();
	}

	public static MBusToObisTranslator getInstance() {
		if (instance == null) {
			instance = new MBusToObisTranslator();
		}

		return instance;
	}
}
