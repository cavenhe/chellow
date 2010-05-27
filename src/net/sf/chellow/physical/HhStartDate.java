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
package net.sf.chellow.physical;

import java.util.Calendar;
import java.util.Date;

import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.types.MonadDate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HhStartDate extends MonadDate {
	public static HhStartDate getNext(HhStartDate date) throws HttpException {
		return new HhStartDate(new Date(getNext(getCalendar(), date.getDate()
				.getTime())));
	}

	public static HhStartDate getPrevious(HhStartDate date)
			throws HttpException {
		return new HhStartDate(new Date(getPrevious(getCalendar(), date.getDate()
				.getTime())));
	}

	public static HhStartDate roundUp(Date date) throws HttpException {
		return new HhStartDate(new Date(roundUp(getCalendar(), date.getTime())));
	}

	public static long getNext(Calendar cal, long date) {
		return roundUp(cal, date + 1);
	}
	
	public static boolean isEqual(HhStartDate date1, HhStartDate date2) {
		if (date1 == null) {
			if (date2 == null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (date2 == null) {
				return false;
			} else {
				return date1.getDate().equals(date2.getDate());
			}
		}
	}

	public static boolean isAfter(HhStartDate date1, HhStartDate date2) {
		if (date1 == null) {
			if (date2 == null) {
				return false;
			} else {
				return true;
			}
		} else {
			return date1.after(date2);
		}
	}
	
	public static boolean isBefore(HhStartDate date1, HhStartDate date2) {
		if (date1 == null) {
			return false;
		} else {
			return date1.before(date2);
		}
	}

	protected static long roundUp(Calendar cal, long date) {
		cal.clear();
		cal.setTimeInMillis(date);
		if (cal.get(Calendar.MILLISECOND) > 0) {
			cal.set(Calendar.MILLISECOND, 0);
			cal.add(Calendar.SECOND, 1);
		}
		if (cal.get(Calendar.SECOND) > 0) {
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.MINUTE, 1);
		}
		int minute = cal.get(Calendar.MINUTE);
		if (minute > 0 && minute < 30) {
			cal.set(Calendar.MINUTE, 30);
		} else if (minute > 30) {
			cal.set(Calendar.MINUTE, 0);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		return cal.getTimeInMillis();
	}

	public static HhStartDate roundDown(Date date) throws HttpException {
		return new HhStartDate(new Date(roundDown(getCalendar(), date.getTime())));
	}

	public static long getPrevious(Calendar cal, long date) {
		return roundDown(cal, date - 1);
	}

	public static long roundDown(Calendar cal, long date) {
		cal.clear();
		cal.setTimeInMillis(date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		int minute = cal.get(Calendar.MINUTE);
		if (minute < 30) {
			cal.set(Calendar.MINUTE, 0);
		} else if (minute < 60) {
			cal.set(Calendar.MINUTE, 30);
		}
		return cal.getTimeInMillis();
	}

	HhStartDate() throws HttpException {
		super(new Date(0));
	}

	public HhStartDate(Date date) throws HttpException {
		super(date);
	}

	public HhStartDate(String label, String year, String month, String day)
			throws HttpException {
		super(label, year, month, day);
	}

	public HhStartDate(int year, int month, int day)
			throws HttpException {
		super(year, month, day);
	}

	public HhStartDate(String dateStr) throws HttpException {
		super(dateStr);
	}

	public HhStartDate(String label, String dateStr) throws HttpException {
		super(label, dateStr);
	}

	public void update(Date date) throws HttpException {
		if (date == null) {
			throw new InternalException("Date can't be null I'm afraid.");
		}
		Calendar cal = getCalendar();
		cal.clear();
		cal.setTime(date);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int milliSecond = cal.get(Calendar.MILLISECOND);
		if (minute != 0 && minute != 30) {
			throw new UserException("For the date " + date
					+ ", the minutes must be 0 or 30.");
		}
		if (second != 0) {
			throw new UserException("For the date " + date
					+ ", the seconds must be 0.");
		}
		if (milliSecond != 0) {
			throw new UserException("For the date " + date
					+ ", the milliseconds must be 0.");
		}
		super.update(cal.getTime());
	}

	public HhStartDate getPrevious() throws HttpException {
		return getPrevious(this);
	}

	public HhStartDate getNext() throws HttpException {
		return getNext(this);
	}

	public Element toXml(Document doc) throws InternalException {
		return toXML(getDate(), getLabel(), doc, "hh-start-date");
	}
    
	public boolean after(HhStartDate date) {
		if (date == null) {
			return false;
		} else {
			return getDate().after(date.getDate());
		}
	}
	
	public boolean before(HhStartDate date) {
		if (date == null) {
			return true;
		} else {
			return getDate().before(date.getDate());
		}
	}
}