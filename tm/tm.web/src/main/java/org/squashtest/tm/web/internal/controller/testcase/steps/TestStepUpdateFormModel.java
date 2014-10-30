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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.web.internal.model.customfield.RawValueModel;
import org.squashtest.tm.web.internal.model.customfield.RawValueModel.RawValueModelMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestStepUpdateFormModel {

	private String action;
	private String expectedResult;

	private RawValueModelMap customFields = new RawValueModelMap();

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public RawValueModelMap getCustomFields() {
		return customFields;
	}

	public void setCustomFields(RawValueModelMap customFields) {
		this.customFields = customFields;
	}


	@JsonIgnore
	public Map<Long, RawValue> getCufs(){
		Map<Long, RawValue> cufs = new HashMap<Long, RawValue>(customFields.size());
		for (Entry<Long, RawValueModel> entry : customFields.entrySet()){
			cufs.put(entry.getKey(), entry.getValue().toRawValue());
		}
		return cufs;
	}


}
