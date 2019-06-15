/*
 * Copyright 2011-13 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openmuc.jmbus;

public final class DecodingException extends Exception {

	private static final long serialVersionUID = 1735527302166708223L;

	public DecodingException() {
		super();
	}

	public DecodingException(String s) {
		super(s);
	}

	public DecodingException(Throwable cause) {
		super(cause);
	}

	public DecodingException(String s, Throwable cause) {
		super(s, cause);
	}

}
