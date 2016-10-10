/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.model.json;

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jthebault on 10/10/2016.
 */
public class JsonDynamicScope {

	//Test-case workspace
	private List<Long> testCaseLibraryIds;
	private List<Long> testCaseFolderIds;
	private List<Long> testCaseIds;


	public JsonDynamicScope() {
	}

	public List<Long> getTestCaseLibraryIds() {
		return testCaseLibraryIds;
	}

	public void setTestCaseLibraryIds(List<Long> testCaseLibraryIds) {
		this.testCaseLibraryIds = testCaseLibraryIds;
	}

	public List<Long> getTestCaseFolderIds() {
		return testCaseFolderIds;
	}

	public void setTestCaseFolderIds(List<Long> testCaseFolderIds) {
		this.testCaseFolderIds = testCaseFolderIds;
	}

	public List<Long> getTestCaseIds() {
		return testCaseIds;
	}

	public void setTestCaseIds(List<Long> testCaseIds) {
		this.testCaseIds = testCaseIds;
	}

	public List<EntityReference> convertToEntityReferences (){
		List<EntityReference> entityReferences = new ArrayList<>();
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.TEST_CASE_LIBRARY,this.testCaseLibraryIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.TEST_CASE_FOLDER,this.testCaseFolderIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.TEST_CASE,this.testCaseIds));
		return entityReferences;
	}

	private List<EntityReference> convertToEntityReferencesForOneType (EntityType type, List<Long> ids){
		List<EntityReference> entityReferences = new ArrayList<>();
		for (Long id : ids) {
			entityReferences.add(new EntityReference(type,id));
		}
		return entityReferences;
	}
}
