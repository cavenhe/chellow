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
package net.sf.chellow.billing;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.types.MonadObject;

public class InvoiceType extends MonadObject {
	public static final InvoiceType AMENDED;

	public static final InvoiceType FINAL;

	public static final InvoiceType NORMAL;

	public static final InvoiceType INTEREST;

	public static final InvoiceType RECONCILIATION;

	public static final InvoiceType PREPAID;

	public static final InvoiceType INFORMATION;

	public static final InvoiceType WITHDRAWAL;

	static {
		try {
			AMENDED = new InvoiceType(0);
			FINAL = new InvoiceType(1);
			NORMAL = new InvoiceType(2);
			INTEREST = new InvoiceType(3);
			RECONCILIATION = new InvoiceType(4);
			PREPAID = new InvoiceType(5);
			INFORMATION = new InvoiceType(6);
			WITHDRAWAL = new InvoiceType(7);
		} catch (HttpException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static InvoiceType getType(int intValue) throws HttpException,
			InternalException {
		switch (intValue) {
		case 0:
			return AMENDED;
		case 1:
			return FINAL;
		case 2:
			return NORMAL;
		case 3:
			return INTEREST;
		case 4:
			return RECONCILIATION;
		case 5:
			return PREPAID;
		case 6:
			return INFORMATION;
		case 7:
			return WITHDRAWAL;
		default:
			throw new UserException(
					"There isn't a read type with this int value.");
		}
	}

	public static String name(InvoiceType type) throws HttpException,
			InternalException {
		switch (type.getInt()) {
		case 0:
			return "amended";
		case 1:
			return "final";
		case 2:
			return "normal";
		case 3:
			return "interest";
		case 4:
			return "reconciliation";
		case 5:
			return "prepaid";
		case 6:
			return "information";
		case 7:
			return "withdrawal";
		default:
			throw new UserException("There isn't a Units with this int value.");
		}
	}

	public static InvoiceType getType(String name) throws HttpException,
			InternalException {
		name = name.trim().toLowerCase();
		if (name.equals("amended")) {
			return AMENDED;
		} else if (name.equals("final")) {
			return FINAL;
		} else if (name.equals("normal")) {
			return NORMAL;
		} else if (name.equals("interest")) {
			return INTEREST;
		} else if (name.equals("reconciliation")) {
			return RECONCILIATION;
		} else if (name.equals("prepaid")) {
			return PREPAID;
		} else if (name.equals("information")) {
			return INFORMATION;
		} else if (name.equals("withdrawal")) {
			return WITHDRAWAL;
		} else {
			throw new UserException("There isn't a read type with that name.");
		}
	}

	private int intValue;

	InvoiceType() {
	}

	private InvoiceType(int intValue) throws HttpException {
		if (intValue < 0) {
			throw new UserException("The int value can't be negative.");
		}
		if (intValue > 7) {
			throw new UserException("The int value can't be greater than 7.");
		}
		setInt(intValue);
	}

	int getInt() {
		return intValue;
	}

	void setInt(int intValue) {
		this.intValue = intValue;
	}

	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof InvoiceType) {
			InvoiceType toCompare = (InvoiceType) obj;
			if (toCompare.getInt() == getInt()) {
				isEqual = true;
			}
		}
		return isEqual;
	}

	@Override
	public Attr toXml(Document doc) throws HttpException {
		Attr attr = doc.createAttribute("unit");
		attr.setNodeValue(this.toString());
		return null;
	}
}
