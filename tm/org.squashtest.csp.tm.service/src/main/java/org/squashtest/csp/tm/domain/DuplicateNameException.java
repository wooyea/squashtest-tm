/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.domain;

public class DuplicateNameException extends DomainException {
	private static final long serialVersionUID = 2815263509542519285L;
	private String i18nkey = "squashtm.domain.exception.duplicate.name";
	private String name = "";
	// note : this exception always reports an error on the name, hence the setField("name");

	public DuplicateNameException(String oldName, String newName) {
		this("Cannot create/rename " + oldName + " : " + newName + " already exists within the same container");
	}

	public DuplicateNameException() {
		super();
		setField("name");
	}

	public DuplicateNameException(String message) {
		super(message);
		setField("name");
	}
	
	public DuplicateNameException(String message, String name, String i18key) {
		this(message);
		this.name = name;
		this.i18nkey = i18key;
	}

	@Override
	public String getI18nKey() {
		return i18nkey;
	}

}
