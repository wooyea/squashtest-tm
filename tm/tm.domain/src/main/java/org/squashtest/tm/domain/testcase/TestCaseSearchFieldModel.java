/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.domain.testcase;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.PROPERTY,  
	    property = "type")  
	@JsonSubTypes({  
	    @Type(value = TestCaseSearchListFieldModel.class, name = "LIST"),  
	    @Type(value = TestCaseSearchSingleFieldModel.class, name = "SINGLE"),
	    @Type(value = TestCaseSearchTextFieldModel.class, name = "TEXT"),
	    @Type(value = TestCaseSearchRangeFieldModel.class, name = "RANGE"),
	    @Type(value = TestCaseSearchTimeIntervalFieldModel.class, name = "TIME_INTERVAL")})  
public interface TestCaseSearchFieldModel {

	static final String SINGLE = "SINGLE";
	static final String LIST = "LIST";
	static final String TEXT = "TEXT";	
	static final String TIME_INTERVAL = "TIME_INTERVAL";	
	static final String RANGE = "RANGE";
	
	String getType();

}
