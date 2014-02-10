/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.exception;

import org.squashtest.tm.core.foundation.exception.ActionException;

public class NoBugTrackerBindingException extends ActionException {

	/**
	 * 
	 */
	//FIXME add generated serial version UID , my eclipse couldn't do it (mpagnon)
	private static final long serialVersionUID = 1L;
	
	private final static String DEFAULT_MESSAGE = "No Bug-Tracker is bound to this Project";
	private final static String BUGTRACKER_NAME_EXISTS_MESSAGE_KEY = "squashtm.action.exception.bugtrackerBinding.notExist";

	public NoBugTrackerBindingException(Exception ex) {
		super(ex);
	}

	public NoBugTrackerBindingException(String message) {
		super(message);
	}

	public NoBugTrackerBindingException() {
		super(DEFAULT_MESSAGE);
	}

	@Override
	public String getI18nKey() {
		return BUGTRACKER_NAME_EXISTS_MESSAGE_KEY;
	}

}
