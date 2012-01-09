/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.campaign;

import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.users.User

import spock.lang.Specification;

public class IterationTestPlanItemTest extends Specification {
	IterationTestPlanItem copySource = new IterationTestPlanItem(iteration : Mock(Iteration), executionStatus: ExecutionStatus.FAILURE, label: "copy source")

	def setup() {
		copySource.referencedTestCase= new TestCase()

		Execution exec = new Execution()
		copySource.addExecution(exec)
	}


	def "copy of a test plan item should be in ready state"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.executionStatus == ExecutionStatus.READY
	}

	def "copy of a test plan item should have no execution"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.executions.isEmpty()
	}
	
	def "copy of a test plan item should reference the same test case"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.referencedTestCase == copySource.referencedTestCase
	}

	def "copy of a test plan item should have the same label"() {
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.label == copySource.label
	}
	def "copy of a test plan item should copy the assigned user"() {
		given: 
		User user = new User()
		copySource.setUser(user)
		when:
		IterationTestPlanItem copy = copySource.createCopy()

		then:
		copy.user == copySource.getUser();
	}

	def "should copying a  test plan item should not modify the source"() {
		given:
		IterationTestPlanItem source = new IterationTestPlanItem(iteration : Mock(Iteration), executionStatus: ExecutionStatus.FAILURE, label: "label")
		TestCase referencedTestCase = new TestCase()
		source.referencedTestCase= referencedTestCase

		Execution exec = new Execution()
		source.addExecution(exec)

		when:
		source.createCopy()

		then:
		source.label == "label"
		source.referencedTestCase == referencedTestCase
		source.executions == [exec]
	}
}
