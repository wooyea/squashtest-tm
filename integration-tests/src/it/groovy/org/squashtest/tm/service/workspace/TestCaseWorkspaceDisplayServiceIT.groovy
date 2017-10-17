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
package org.squashtest.tm.service.workspace

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.dto.PermissionWithMask
import org.squashtest.tm.service.internal.dto.UserDto
import org.squashtest.tm.service.internal.dto.json.JsTreeNode
import org.squashtest.tm.service.internal.testcase.TestCaseWorkspaceDisplayService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
class TestCaseWorkspaceDisplayServiceIT extends DbunitServiceSpecification {

	@Inject
	TestCaseWorkspaceDisplayService testCaseWorkspaceDisplayService

	private HashMap<Long, JsTreeNode> initEmptyJsTreeNodes() {
		Map<Long, JsTreeNode> jsTreeNodes = new HashMap<>()
		jsTreeNodes.put(-1L, new JsTreeNode())
		jsTreeNodes.put(-20L, new JsTreeNode())
		jsTreeNodes.put(-3L, new JsTreeNode())
		jsTreeNodes
	}

	private HashMap<Long, JsTreeNode> initNoWizardJsTreeNodes() {
		Map<Long, JsTreeNode> jsTreeNodes = initEmptyJsTreeNodes()
		jsTreeNodes.values().each { it.addAttr("wizards", [] as Set) }
		jsTreeNodes
	}

	@DataSet("WorkspaceDisplayService.sandbox.no.filter.xml")
	def "should find test Case Libraries as JsTreeNode"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = testCaseWorkspaceDisplayService.doFindLibraries(readableProjectIds, user, new ArrayList<>(), new HashMap<>())

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds   || expectedLibrariesIds 	| expectedProjectsNames | expectedLibraryFullId
		[]                   || [] 						| [] 					| []
		[-1L, -2L, -3L, -4L] || [-1L, -20L, -3L] 		| ["foo", "bar", "baz"] | ["TestCaseLibrary-1", "TestCaseLibrary-20", "TestCaseLibrary-3"]
	}

	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find test Case Libraries as JsTreeNode with filter"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = testCaseWorkspaceDisplayService.doFindLibraries(readableProjectIds, user, new ArrayList<>(), new HashMap<>())

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == expectedLibrariesIds.sort() as Set
		jsTreeNodes.values().collect { it -> it.getTitle() }.sort() as Set == expectedProjectsNames.sort() as Set

		where:
		readableProjectIds   || expectedLibrariesIds 	| expectedProjectsNames | expectedLibraryFullId
		[]                   || [] 						| [] 					| []
		[-1L, -2L, -3L, -4L] || [-1L, -20L] 			| ["foo", "bar"] 		| ["TestCaseLibrary-1", "TestCaseLibrary-20"]
	}

	@DataSet("WorkspaceDisplayService.sandbox.no.filter.xml")
	def "should find test Case Libraries as JsTreeNode with all perm for admin"() {
		given:
		UserDto user = new UserDto("robert", -2L, [], true)

		and:
		def readableProjectIds = [-1L, -2L, -3L, -4L]

		when:
		def jsTreeNodes = testCaseWorkspaceDisplayService.doFindLibraries(readableProjectIds, user, new ArrayList<>(), new HashMap<>())

		then:
		jsTreeNodes.values().collect { it -> it.getAttr().get("resId") }.sort() as Set == [-1L, -20L, -3L].sort() as Set
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.READ.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.EXECUTE.getQuality()) == null } //execute is only for campaign
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true) }
		jsTreeNodes.values().collect { it -> it.getAttr().get(PermissionWithMask.MANAGEMENT.getQuality()) == null } //management is only for projects
	}


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find permission masks for standard user"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)
		HashMap<Long, JsTreeNode> jsTreeNodes = initEmptyJsTreeNodes()


		when:
		testCaseWorkspaceDisplayService.findPermissionMap(user, jsTreeNodes)

		then:
		jsTreeNodes.keySet().sort() == [-1L, -20L, -3L].sort()

		def lib20Attr = jsTreeNodes.get(-20L).getAttr()
		lib20Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib20Attr.get(PermissionWithMask.WRITE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.CREATE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.DELETE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib20Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null
		lib20Attr.get(PermissionWithMask.EXPORT.getQuality()) == null
		lib20Attr.get(PermissionWithMask.LINK.getQuality()) == null
		lib20Attr.get(PermissionWithMask.ATTACH.getQuality()) == null
		lib20Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null

		def lib1Attr = jsTreeNodes.get(-1L).getAttr()
		lib1Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.CREATE.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.DELETE.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.IMPORT.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null //execute is for campaign workspace
		lib1Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.ATTACH.getQuality()) == String.valueOf(true)
		lib1Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null //we can't manager libraries, we manage projects...

		def lib3Attr = jsTreeNodes.get(-3L).getAttr()
		lib3Attr.get(PermissionWithMask.READ.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.WRITE.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.CREATE.getQuality()) == null
		lib3Attr.get(PermissionWithMask.DELETE.getQuality()) == null
		lib3Attr.get(PermissionWithMask.IMPORT.getQuality()) == null
		lib3Attr.get(PermissionWithMask.EXECUTE.getQuality()) == null
		lib3Attr.get(PermissionWithMask.EXPORT.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.LINK.getQuality()) == String.valueOf(true)
		lib3Attr.get(PermissionWithMask.ATTACH.getQuality()) == null
		lib3Attr.get(PermissionWithMask.MANAGEMENT.getQuality()) == null
	}


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find wizards for test case library"() {
		given:
		def jsTreeNodes = initNoWizardJsTreeNodes()

		when:
		testCaseWorkspaceDisplayService.findWizards([-1L, -2L, -3L, -4L], jsTreeNodes)

		then:
		jsTreeNodes.size() == 3
		jsTreeNodes.get(-1L).getAttr().get("wizards") == ["JiraAgile"] as Set
		jsTreeNodes.get(-20L).getAttr().get("wizards") == ["JiraAgain", "JiraAgile", "JiraForSquash"] as Set
		jsTreeNodes.get(-3L).getAttr().get("wizards") == [] as Set

	}


	@DataSet("WorkspaceDisplayService.sandbox.xml")
	def "should find projects models"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsonProjects = testCaseWorkspaceDisplayService.findAllProjects([-1L, -2L, -3L, -4L], user)

		then:
		jsonProjects.size() == 3
		jsonProjects.collect { it.name }.sort() == ["bar", "baz", "foo"]

		def jsonProject1 = jsonProjects.getAt(2)
		jsonProject1.getId() == -1L
		jsonProject1.getName().equals("foo")
		jsonProject1.getRequirementCategories().id == -1L
		jsonProject1.getTestCaseNatures().id == -2L
		jsonProject1.getTestCaseTypes().id == -4L

		def customFieldBindings = jsonProject1.getCustomFieldBindings()
		customFieldBindings.size() == 8
		def customFieldBindingModels = customFieldBindings.get("REQUIREMENT_VERSION")
		customFieldBindingModels.size() == 2
		customFieldBindingModels.collect { it.id }.sort() == [-3L, -2L]
		customFieldBindingModels.collect { it.customField.id }.sort() == [-3L, -1L]
		customFieldBindingModels.collect { it.customField.name }.sort() == ["Liste 2", "Lot"]

		def customFieldBindingModels2 = customFieldBindings.get("TEST_STEP")
		customFieldBindingModels2.size() == 2
		customFieldBindingModels2.collect { it.customField.id }.sort() == [-3L, -1L]
		def customFieldBindingModel = customFieldBindingModels2.get(0)
		customFieldBindingModel.getRenderingLocations().size() == 2
		customFieldBindingModel.getRenderingLocations().collect { it.enumName }.sort() == ["STEP_TABLE", "TEST_PLAN"]

		def jsonMilestones = jsonProject1.getMilestones()
		jsonMilestones.size() == 3
		jsonMilestones.collect { it.label }.sort() == ["My milestone", "My milestone 2", "My milestone 3"]
	}

	@DataSet("TestCaseDisplayService.sandbox.xml")
	def "should build open test Case folders with all their children"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsTreeNodes = testCaseWorkspaceDisplayService.FindExpandedJsTreeNodes(user, [-5L, -7L])

		then:
		def JsTreeNode parentFolder = jsTreeNodes.get(-5L);

		def List<JsTreeNode> children = parentFolder.getChildren();

		def List<JsTreeNode> grandChildren = children.get(1).getChildren();

		children.size() == 3
		children.collect { it.getAttr().get("resId") }.sort() == [-11L, -7L, -6L]
		children.collect { it.getState() }.sort() == ["closed", "leaf", "open"]
		children.collect { it.getTitle() }.sort() == ["folder 2", "folder 3", "test case 1"]

		grandChildren.size() == 1
		grandChildren.collect { it.getAttr().get("resId") } == [-13L];
		grandChildren.collect { it.getState() }.sort() == ["leaf"]
		grandChildren.collect { it.getTitle() }.sort() == ["test case 3"]
	}

	@DataSet("TestCaseDisplayService.sandbox.xml")
	def "should build test Case libraries with all their children"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		and:
		def readableProjectIds = [-1L, -2L, -3L, -4L]

		when:
		def expandedNode = new JsTreeNode()
		expandedNode.setTitle("Coucou")
		expandedNode.setState(JsTreeNode.State.open);

		Map<Long, JsTreeNode> expandedJsTreeNodes = new HashMap<>();
		expandedJsTreeNodes.put(-5L,expandedNode)

		def jsTreeNodes = testCaseWorkspaceDisplayService.doFindLibraries(readableProjectIds, user, [-1L], expandedJsTreeNodes)
		then:
		def JsTreeNode parentFolder = jsTreeNodes.get(-5L);

		def List<JsTreeNode> children = parentFolder.getChildren();

		def List<JsTreeNode> grandChildren = children.get(1).getChildren();

		children.size() == 3
		children.collect { it.getAttr().get("resId") }.sort() == [-11L, -7L, -6L]
		children.collect { it.getState() }.sort() == ["closed", "leaf", "open"]
		children.collect { it.getTitle() }.sort() == ["folder 2", "folder 3", "test case 1"]

		grandChildren.size() == 1
		grandChildren.collect { it.getAttr().get("resId") } == [-13L];
		grandChildren.collect { it.getState() }.sort() == ["leaf"]
		grandChildren.collect { it.getTitle() }.sort() == ["test case 3"]
	}
}
