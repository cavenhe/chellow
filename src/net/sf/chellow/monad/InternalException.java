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

package net.sf.chellow.monad;

import javax.servlet.http.HttpServletResponse;

public class InternalException extends HttpException {
	private static final long serialVersionUID = 1L;

	public InternalException(String message) throws InternalException {
		this(message, null);
	}

	public InternalException(Throwable cause) throws InternalException {
		this(null, cause);
	}
	
	public InternalException(String message, Throwable cause) throws InternalException {
	super(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, message,
				null, cause);
}
}
