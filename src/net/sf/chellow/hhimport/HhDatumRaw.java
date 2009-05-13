/*******************************************************************************
 * 
 *  Copyright (c) 2005, 2009 Wessex Water Services Limited
 *  
 *  This file is part of Chellow.
 * 
 *  Chellow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Chellow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Chellow.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
package net.sf.chellow.hhimport;

import java.math.BigDecimal;

import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.physical.HhDatum;
import net.sf.chellow.physical.HhEndDate;

public class HhDatumRaw {
	private String core;

	private boolean isImport;

	private boolean isKwh;

	private HhEndDate endDate;

	private BigDecimal value;

	private char status;

	/*
	 * public HhDatumRaw(String core, HhEndDate endDate, String isImport, String
	 * isKwh, String value, String statusStr) throws HttpException { Character
	 * status = null; if (statusStr != null) { statusStr = statusStr.trim(); int
	 * statusLen = statusStr.length(); if (statusLen == 1) { status =
	 * statusStr.charAt(0); } else if (statusLen > 1) { throw new UserException(
	 * "The status must be blank or a single character."); } } init(core,
	 * Boolean.parseBoolean(isImport), Boolean.parseBoolean(isKwh), new
	 * HhEndDate(endDateStr), Float.parseFloat(value), status); }
	 */

	public HhDatumRaw(String core, boolean isImport, boolean isKwh,
			HhEndDate endDate, BigDecimal value, char status)
			throws HttpException {
		this.core = core;
		this.isImport = isImport;
		this.isKwh = isKwh;
		if (endDate == null) {
			throw new InternalException("The value 'endDate' must not be null.");
		}
		this.endDate = endDate;
		this.value = value;
		if (status != HhDatum.ESTIMATE && status != HhDatum.ACTUAL) {
			throw new UserException(
					"The status character must be E, A or null.");
		}
		this.status = status;
	}

	public String getMpanCore() {
		return core;
	}

	public boolean getIsImport() {
		return isImport;
	}

	public boolean getIsKwh() {
		return isKwh;
	}

	public HhEndDate getEndDate() {
		return endDate;
	}

	public BigDecimal getValue() {
		return value;
	}

	public char getStatus() {
		return status;
	}

	public String toString() {
		return "MPAN core: " + core + ", Is import? " + isImport + ", Is Kwh? "
				+ isKwh + ", End date " + endDate + ", Value " + value
				+ ", Status " + status;
	}
}
