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
package org.squashtest.tm.service.testcase;

import java.util.List;

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.CyclicStepCallException;

public interface CallStepManagerService {

	TestCase findTestCase(long testCaseId);

	/**
	 * will add a call test step.
	 * 
	 * @param parentTestCaseId that calls a step
	 * @param calledTestCaseId being called
	 */
	void addCallTestStep(long parentTestCaseId, long calledTestCaseId);

	/**
	 * Used to check if the destination test case id is found in the calling tree of the pasted steps
	 * if so : a {@linkplain CyclicStepCallException} is thrown.
	 * 
	 * @param testCaseId
	 * @param copiedStepId
	 */
	@Deprecated
	void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, String[] pastedStepsIds);

	/**
	 * same as {@link #checkForCyclicStepCallBeforePaste(long, String[])} with a more comfortable signature
	 * 
	 * @param destinationTestCaseId
	 * @param pastedStepsIds
	 */
	void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, List<Long> pastedStepsIds);

	void checkForCyclicStepCallBeforePaste(Long destinationTestCaseId, Long calledTestCaseId);
}
