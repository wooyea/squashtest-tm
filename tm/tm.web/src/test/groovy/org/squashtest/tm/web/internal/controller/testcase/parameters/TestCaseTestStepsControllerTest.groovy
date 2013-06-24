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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.util.List

import javax.inject.Provider
import javax.servlet.http.HttpServletRequest

import org.springframework.web.servlet.ModelAndView
import org.squashtest.csp.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.domain.attachment.AttachmentList
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestStep
import org.squashtest.tm.service.customfield.CustomFieldHelper;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.customfield.CustomFieldHelperService;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.controller.testcase.steps.TestCaseTestStepsController;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter
import org.squashtest.tm.web.internal.helper.LevelLabelFormatterWithoutOrder
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper

import spock.lang.Specification

class TestCaseTestStepsControllerTest extends Specification {
	TestCaseTestStepsController controller = new TestCaseTestStepsController()
	
	TestCaseModificationService testCaseModificationService = Mock()
	HttpServletRequest request = Mock()
	InternationalizationHelper messageSource = Mock()	
	CustomFieldHelperService cufHelperService = Mock()
	
	def setup() {
		controller.testCaseModificationService = testCaseModificationService
		request.getCharacterEncoding() >> "ISO-8859-1"
		controller.internationalizationHelper = messageSource
		controller.cufHelperService = cufHelperService
	}

	
	
	def "should build table model for test case steps"() {
		given:
		AttachmentList al = Mock()
		al.size() >> 1
		al.getId() >> 5

		and:
		ActionTestStep step1 = new ActionTestStep(action: "a1", expectedResult: "r1")
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step1, to: 1L
			ActionTestStep.set field: "attachmentList", of: step1, to: al
		}

		and:
		ActionTestStep step2 = new ActionTestStep(action: "a2", expectedResult: "r2")
		use(ReflectionCategory) {
			TestStep.set field: "id", of: step2, to: 2L
			ActionTestStep.set field: "attachmentList", of: step2, to: al
		}


		and:
		FilteredCollectionHolder<List<ActionTestStep>> holder = new FilteredCollectionHolder<List<ActionTestStep>>(2, [step1, step2])
		testCaseModificationService.findStepsByTestCaseIdFiltered(10, _) >> holder
		
		and:
		TestCase tc = Mock()
		tc.project >> Mock(Project)
		testCaseModificationService.findById(_) >> tc

		and:
		DataTableDrawParameters params = new DataTableDrawParameters();
		params.setiDisplayLength(10);
		params.setiDisplayStart(0)
		params.setsEcho("echo");

		and:
		CustomFieldHelper cufhelper = Mock() 
		cufhelper.getCustomFieldValues() >> []
		cufhelper.restrictToCommonFields() >> cufhelper
		cufhelper.setRenderingLocations(_) >> cufhelper
		
		cufHelperService.newStepsHelper(_, _) >> cufhelper

		when:
		def res = controller.getStepsTableModel(10, params, Locale.FRENCH)

		then:
		res.sEcho == "echo"
		res.aaData ==[
			[
				"step-id":1L,
				"empty-browse-holder":null,
				"customFields":[:],
				"nb-attachments":1,
				"empty-requirements-holder":null,
				"nb-requirements":0,
				"step-index":1, 
				"step-type":"action",
				"attach-list-id":5L,
				"step-result":"r1",
				"has-requirements":false,
				"called-tc-id":null,
				"empty-delete-holder":null,
				"step-action":"a1"
			],
			[
				"step-id":2L,
				"empty-browse-holder":null,
				"customFields":[:],
				"nb-attachments":1,
				"empty-requirements-holder":null,
				"nb-requirements":0,
				"step-index":2,
				"step-type":"action",
				"attach-list-id":5L,
				"step-result":"r2",
				"has-requirements":false,
				"called-tc-id":null,
				"empty-delete-holder":null,
				"step-action":"a2"
			]]


	}
	def "should change step index"() {
		given:


		when:
		controller.changeStepIndex(10, 1, 20)

		then:
		1 * testCaseModificationService.changeTestStepPosition(20, 10, 1)
	}


}
