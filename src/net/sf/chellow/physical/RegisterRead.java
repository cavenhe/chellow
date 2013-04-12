/*******************************************************************************
 * 
 *  Copyright (c) 2005-2013 Wessex Water Services Limited
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
package net.sf.chellow.physical;

import java.math.BigDecimal;
import java.net.URI;

import net.sf.chellow.billing.Bill;
import net.sf.chellow.monad.Hiber;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.types.MonadUri;
import net.sf.chellow.ui.GeneralImport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RegisterRead extends PersistentEntity {

	public static void generalImport(String action, String[] values,
			Element csvElement) throws HttpException {
		if (action.equals("insert")) {
		} else if (action.equals("update")) {
			String readIdStr = GeneralImport.addField(csvElement, "Chellow Id",
					values, 0);
			RegisterRead read = RegisterRead.getRegisterRead(Long
					.parseLong(readIdStr));

			String tprStr = GeneralImport
					.addField(csvElement, "TPR", values, 1);
			Tpr tpr = null;
			if (tprStr.equals(GeneralImport.NO_CHANGE)) {
				tpr = read.getTpr();
			} else {
				tpr = Tpr.getTpr(tprStr);
			}

			String coefficientStr = GeneralImport.addField(csvElement,
					"Coefficient", values, 2);
			BigDecimal coefficient = null;
			if (tprStr.equals(GeneralImport.NO_CHANGE)) {
				coefficient = read.getCoefficient();
			} else {
				coefficient = new BigDecimal(coefficientStr);
			}

			Units units = null;
			String unitsStr = GeneralImport.addField(csvElement, "Units",
					values, 3);
			if (unitsStr.equals(GeneralImport.NO_CHANGE)) {
				units = read.getUnits();
			} else {
				units = Units.getUnits(unitsStr);
			}

			String msn = GeneralImport.addField(csvElement,
					"Meter Serial Number", values, 4);
			if (msn.equals(GeneralImport.NO_CHANGE)) {
				msn = read.getMeterSerialNumber();
			}

			String mpanStr = GeneralImport.addField(csvElement, "MPAN", values,
					5);
			if (mpanStr.equals(GeneralImport.NO_CHANGE)) {
				mpanStr = read.getMpanStr();
			}

			String previousDateStr = GeneralImport.addField(csvElement,
					"Previous Date", values, 6);
			HhStartDate previousDate = null;
			if (previousDateStr.equals(GeneralImport.NO_CHANGE)) {
				previousDate = read.getPreviousDate();
			} else {
				previousDate = new HhStartDate(previousDateStr);
			}

			String previousValueStr = GeneralImport.addField(csvElement,
					"Previous Value", values, 7);
			BigDecimal previousValue = null;
			if (previousValueStr.equals(GeneralImport.NO_CHANGE)) {
				previousValue = read.getPreviousValue();
			} else {
				previousValue = new BigDecimal(previousValueStr);
			}

			ReadType previousType = null;
			String previousTypeStr = GeneralImport.addField(csvElement,
					"Previous Type", values, 8);
			if (previousTypeStr.equals(GeneralImport.NO_CHANGE)) {
				previousType = read.getPreviousType();
			} else {
				previousType = ReadType.getReadType(previousTypeStr);
			}

			String presentDateStr = GeneralImport.addField(csvElement,
					"Present Date", values, 9);
			HhStartDate presentDate = null;
			if (presentDateStr.equals(GeneralImport.NO_CHANGE)) {
				presentDate = read.getPresentDate();
			} else {
				presentDate = new HhStartDate(presentDateStr);
			}

			String presentValueStr = GeneralImport.addField(csvElement,
					"Present Value", values, 10);
			BigDecimal presentValue = null;
			if (presentValueStr.equals(GeneralImport.NO_CHANGE)) {
				presentValue = read.getPresentValue();
			} else {
				presentValue = new BigDecimal(presentValueStr);
			}

			String presentTypeStr = GeneralImport.addField(csvElement,
					"Present Type", values, 11);
			ReadType presentType = null;
			if (presentTypeStr.equals(GeneralImport.NO_CHANGE)) {
				presentType = read.getPresentType();
			} else {
				presentType = ReadType.getReadType(presentTypeStr);
			}

			read.update(tpr, coefficient, units, msn, mpanStr, previousDate,
					previousValue, previousType, presentDate, presentValue,
					presentType);
		}
	}

	public static RegisterRead getRegisterRead(Long id) throws HttpException {
		RegisterRead read = (RegisterRead) Hiber.session().get(
				RegisterRead.class, id);
		if (read == null) {
			throw new UserException("There isn't a register read with that id.");
		}
		return read;
	}

	private Bill bill;

	private String meterSerialNumber;

	private String mpanStr;

	private BigDecimal coefficient;

	private Units units;

	private Tpr tpr;

	private HhStartDate previousDate;

	private BigDecimal previousValue;

	private ReadType previousType;

	private HhStartDate presentDate;

	private BigDecimal presentValue;

	private ReadType presentType;

	RegisterRead() {
	}

	public RegisterRead(Bill bill, Tpr tpr, BigDecimal coefficient,
			Units units, String meterSerialNumber, String mpanStr,
			HhStartDate previousDate, BigDecimal previousValue,
			ReadType previousType, HhStartDate presentDate,
			BigDecimal presentValue, ReadType presentType) throws HttpException {
		if (bill == null) {
			throw new InternalException("The bill must not be null.");
		}
		setBill(bill);
		update(tpr, coefficient, units, meterSerialNumber, mpanStr,
				previousDate, previousValue, previousType, presentDate,
				presentValue, presentType);
	}

	public void update(Tpr tpr, BigDecimal coefficient, Units units,
			String meterSerialNumber, String mpanStr, HhStartDate previousDate,
			BigDecimal previousValue, ReadType previousType,
			HhStartDate presentDate, BigDecimal presentValue,
			ReadType presentType) throws HttpException {
		if (tpr == null && units.equals(Units.KWH)) {
			throw new UserException(
					"If a register read is measuring kWh, there must be a TPR.");
		}
		setTpr(tpr);
		setCoefficient(coefficient);
		setUnits(units);
		setPreviousDate(previousDate);
		setPreviousValue(previousValue);
		setPreviousType(previousType);
		setPresentDate(presentDate);
		setPresentValue(presentValue);
		setPresentType(presentType);
		setMeterSerialNumber(meterSerialNumber);
		setMpanStr(mpanStr);
	}

	public String getMpanStr() {
		return mpanStr;
	}

	void setMpanStr(String mpanStr) {
		this.mpanStr = mpanStr;
	}

	public String getMeterSerialNumber() {
		return meterSerialNumber;
	}

	void setMeterSerialNumber(String meterSerialNumber) {
		this.meterSerialNumber = meterSerialNumber;
	}

	public Bill getBill() {
		return bill;
	}

	void setBill(Bill bill) {
		this.bill = bill;
	}

	public BigDecimal getCoefficient() {
		return coefficient;
	}

	void setCoefficient(BigDecimal coefficient) {
		this.coefficient = coefficient;
	}

	public Units getUnits() {
		return units;
	}

	void setUnits(Units units) {
		this.units = units;
	}

	public Tpr getTpr() {
		return tpr;
	}

	void setTpr(Tpr tpr) {
		this.tpr = tpr;
	}

	public HhStartDate getPreviousDate() {
		return previousDate;
	}

	void setPreviousDate(HhStartDate previousDate) {
		this.previousDate = previousDate;
	}

	public BigDecimal getPreviousValue() {
		return previousValue;
	}

	void setPreviousValue(BigDecimal previousValue) {
		this.previousValue = previousValue;
	}

	public ReadType getPreviousType() {
		return previousType;
	}

	void setPreviousType(ReadType previousType) {
		this.previousType = previousType;
	}

	public HhStartDate getPresentDate() {
		return presentDate;
	}

	void setPresentDate(HhStartDate presentDate) {
		this.presentDate = presentDate;
	}

	public BigDecimal getPresentValue() {
		return presentValue;
	}

	void setPresentValue(BigDecimal presentValue) {
		this.presentValue = presentValue;
	}

	public ReadType getPresentType() {
		return presentType;
	}

	void setPresentType(ReadType presentType) {
		this.presentType = presentType;
	}
	
	public void attach() {
    }

	@Override
	public MonadUri getEditUri() throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getViewUri() throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node toXml(Document doc) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}
}
