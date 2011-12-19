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
package org.squashtest.csp.tm.domain.requirement

import spock.lang.Specification;
import org.apache.commons.lang.NullArgumentException;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.library.GenericLibraryNode;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.internal.service.RequirementLibraryNavigationServiceImpl;
import org.squashtest.csp.tm.service.RequirementLibraryNavigationService;
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory;

class RequirementFolderTest extends Specification {
	RequirementFolder folder = new RequirementFolder()

	def "folder should hold a name and description"() {
		when:
		folder.name = "my name"
		folder.description = "my desc"

		then:
		folder.name == "my name"
		folder.description == "my desc"
	}

	def "should add a folder in another folder"(){

		given:
		folder.setName("bar")
		RequirementFolder fooFolder = new RequirementFolder()
		fooFolder.setName("foo")

		when:
		folder.addContent(fooFolder)

		then:
		folder.getContent().contains(fooFolder)
	}

	def "should not add node with duplicate name"() {
		given:
		Requirement existing = Mock()
		existing.name >> "foo"
		folder.addContent(existing)
		
		and: 
		Requirement candidate = Mock()
		candidate.name >> "foo"

		when:
		folder.addContent(candidate)

		then:
		thrown DuplicateNameException
	}

	def "should set this folder's project as the project of new content"() {
		given:
		Project project = new Project()

		use(ReflectionCategory) {
			RequirementLibraryNode.set field: "project", of: folder, to: project
		}

		and:
		RequirementFolder newContent = new RequirementFolder()

		when:
		folder.addContent newContent

		then:
		newContent.project == project
	}

	def "should propagate this folder's project to its content"() {

		given:
		RequirementFolder content = new RequirementFolder()
		folder.addContent content

		and:
		Project project = new Project()

		when:
		folder.notifyAssociatedWithProject project

		then:
		content.project == project
	}
}
