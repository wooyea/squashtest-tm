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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;

@DynamicDao(entity = TestCase.class)
public interface TestCaseDao extends CustomTestCaseDao {
	TestCase findByIdWithInitializedSteps(long testCaseId);

	/**
	 * Counts the calling test steps of a test case
	 * 
	 * @param testCaseId
	 * @return
	 */
	long countCallingTestSteps(long testCaseId);

	List<Long> findAllDistinctTestCasesIdsCalledByTestCase(long testCaseId);

	List<Long> findAllDistinctTestCasesIdsCallingTestCase(long testCaseId);

	List<Long> findAllTestCaseIdsByLibraries(@QueryParam("libraryIds") Collection<Long> libraryIds);

	
}
