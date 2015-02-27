/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.ExportTestStepData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.library.LibraryNavigationService;
import org.squashtest.tm.service.milestone.MilestoneFinderService;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatisticsBundle;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.LibraryNavigationController;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseFormModel.TestCaseFormModelValidator;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.model.builder.DriveNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.JsTreeNodeListBuilder;
import org.squashtest.tm.web.internal.model.builder.TestCaseLibraryTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

@Controller
@RequestMapping("/test-case-browser")
public class TestCaseLibraryNavigationController extends
LibraryNavigationController<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> {
	public static final Logger LOGGER = LoggerFactory.getLogger(TestCaseLibraryNavigationController.class);

	@Inject
	private Provider<TestCaseLibraryTreeNodeBuilder> testCaseLibraryTreeNodeBuilder;

	@Inject
	@Named("testCase.driveNodeBuilder")
	private Provider<DriveNodeBuilder<TestCaseLibraryNode>> driveNodeBuilder;

	@Inject
	private TestCaseLibraryNavigationService testCaseLibraryNavigationService;

	@Inject
	private MilestoneFinderService milestoneFinder;

	private static final String JASPER_EXPORT_FILE = "/WEB-INF/reports/test-case-export.jasper";
	private static final String ADD_TEST_CASE = "add-test-case";
	private static final String FILENAME = "filename";
	private static final String LIBRARIES = "libraries";
	private static final String NODES = "nodes";
	private static final String CALLS = "calls";



	@Override
	protected LibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode> getLibraryNavigationService() {
		return testCaseLibraryNavigationService;
	}

	@Override
	protected JsTreeNode createTreeNodeFromLibraryNode(TestCaseLibraryNode node, List<Long> milestoneIds) {

		TestCaseLibraryTreeNodeBuilder builder = testCaseLibraryTreeNodeBuilder.get();

		if (!milestoneIds.isEmpty()){
			builder.filterByMilestone(milestoneFinder.findById(milestoneIds.get(0)));
		}

		return builder.setNode(node).build();
	}


	/*
	 * The former validation system doesn't work anymore because it kicks in before our json form is filled.
	 * So we're doing it manually now.
	 */
	@RequestMapping(value = "/drives/{libraryId}/content/new-test-case", method = RequestMethod.POST, consumes="application/json")
	public @ResponseBody JsTreeNode addNewTestCaseToLibraryRootContent(
			@PathVariable long libraryId,
			@RequestBody TestCaseFormModel testCaseModel,
			@CookieValue(value="milestones", required=false, defaultValue="") List<Long> milestoneIds) throws BindException{

		BindingResult validation = new BeanPropertyBindingResult(testCaseModel, ADD_TEST_CASE);
		TestCaseFormModelValidator validator = new TestCaseFormModelValidator(getMessageSource());
		validator.validate(testCaseModel, validation);

		if (validation.hasErrors()){
			throw new BindException(validation);
		}

		TestCase testCase = testCaseModel.getTestCase();

		Map<Long, RawValue> customFieldValues = testCaseModel.getCufs();

		testCaseLibraryNavigationService.addTestCaseToLibrary(libraryId, testCase, customFieldValues, null, milestoneIds);

		return createTreeNodeFromLibraryNode(testCase, milestoneIds);
	}


	@RequestMapping(value = "/folders/{folderId}/content/new-test-case", method = RequestMethod.POST, consumes="application/json")
	public @ResponseBody JsTreeNode addNewTestCaseToFolder(
			@PathVariable long folderId,
			@RequestBody TestCaseFormModel testCaseModel,
			@CookieValue(value="milestones", required=false, defaultValue="") List<Long> milestoneIds) throws BindException {

		BindingResult validation = new BeanPropertyBindingResult(testCaseModel, ADD_TEST_CASE);
		TestCaseFormModelValidator validator = new TestCaseFormModelValidator(getMessageSource());
		validator.validate(testCaseModel, validation);

		if (validation.hasErrors()){
			throw new BindException(validation);
		}

		TestCase testCase = testCaseModel.getTestCase();

		Map<Long, RawValue> customFieldValues = testCaseModel.getCufs();

		testCaseLibraryNavigationService.addTestCaseToFolder(folderId, testCase, customFieldValues, null, milestoneIds);

		return createTreeNodeFromLibraryNode(testCase, milestoneIds);
	}

	@RequestMapping(value = "/drives", method = RequestMethod.GET, params = { "linkables" })
	public @ResponseBody List<JsTreeNode> getLinkablesRootModel() {
		List<TestCaseLibrary> linkableLibraries = testCaseLibraryNavigationService.findLinkableTestCaseLibraries();
		return createLinkableLibrariesModel(linkableLibraries);
	}

	private List<JsTreeNode> createLinkableLibrariesModel(List<TestCaseLibrary> linkableLibraries) {
		JsTreeNodeListBuilder<TestCaseLibrary> listBuilder = new JsTreeNodeListBuilder<TestCaseLibrary>(
				driveNodeBuilder.get());

		return listBuilder.setModel(linkableLibraries).build();
	}

	@RequestMapping(value = "/content/csv", produces = "application/octet-stream", method = RequestMethod.GET, params = {
			FILENAME, LIBRARIES, NODES, CALLS, RequestParams.RTEFORMAT })
	@ResponseBody
	public void exportAsCsv(Locale locale, @RequestParam(FILENAME) String filename,
			@RequestParam(LIBRARIES) List<Long> libraryIds, @RequestParam(NODES) List<Long> nodeIds,
			@RequestParam(CALLS) Boolean includeCalledTests, @RequestParam(RequestParams.RTEFORMAT) Boolean keepRteFormat,
			HttpServletResponse response) throws FileNotFoundException {

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");

		List<ExportTestCaseData> dataSource = testCaseLibraryNavigationService.findTestCasesToExport(libraryIds,
				nodeIds, includeCalledTests);

		if (!keepRteFormat) {
			escapePrerequisiteAndSteps(dataSource);
		}

		printExport(dataSource, filename, JASPER_EXPORT_FILE, response, locale, "csv", keepRteFormat);
	}

	@RequestMapping(value = "/content/xls", produces = "application/octet-stream", method = RequestMethod.GET, params = {
			FILENAME, LIBRARIES, NODES, CALLS, RequestParams.RTEFORMAT })
	@ResponseBody
	public FileSystemResource exportAsExcel(@RequestParam(FILENAME) String filename,
			@RequestParam(LIBRARIES) List<Long> libraryIds, @RequestParam(NODES) List<Long> nodeIds,
			@RequestParam(CALLS) Boolean includeCalledTests, @RequestParam(RequestParams.RTEFORMAT) Boolean keepRteFormat,
			HttpServletResponse response) throws FileNotFoundException {

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");

		File export = testCaseLibraryNavigationService.exportTestCaseAsExcel(libraryIds, nodeIds, includeCalledTests,
				keepRteFormat);
		return new FileSystemResource(export);

	}

	private void escapePrerequisiteAndSteps(List<ExportTestCaseData> dataSource) {
		for (ExportTestCaseData data : dataSource) {
			// escape prerequisite
			String htmlPrerequisite = data.getPrerequisite();
			String prerequisite = HTMLCleanupUtils.htmlToText(htmlPrerequisite);
			data.setPrerequisite(prerequisite);

			// escape first step
			String htmlFirstAction = data.getFirstAction();
			String firstAction = HTMLCleanupUtils.htmlToText(htmlFirstAction);
			data.setFirstAction(firstAction);

			String htmlFirstResult = data.getFirstExpectedResult();
			String firstResult = HTMLCleanupUtils.htmlToText(htmlFirstResult);
			data.setFirstExpectedResult(firstResult);

			// escape other steps
			for (ExportTestStepData step : data.getSteps()) {
				String htmlAction = step.getAction();
				String action = HTMLCleanupUtils.htmlToText(htmlAction);
				step.setAction(action);

				String htmlExpectedResult = step.getExpectedResult();
				String expectedResult = HTMLCleanupUtils.htmlToText(htmlExpectedResult);
				step.setExpectedResult(expectedResult);
			}

		}
	}

	// ****************************** statistics section *******************************

	@RequestMapping(value = "/statistics", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON, params = {
			LIBRARIES, NODES })
	public @ResponseBody TestCaseStatisticsBundle getStatisticsAsJson(
			@RequestParam(value = LIBRARIES, defaultValue = "") Collection<Long> libraryIds,
			@RequestParam(value = NODES, defaultValue = "") Collection<Long> nodeIds) {

		return testCaseLibraryNavigationService.getStatisticsForSelection(libraryIds, nodeIds);
	}

	@RequestMapping(value = "/dashboard", method = RequestMethod.GET, produces = ContentTypes.TEXT_HTML, params = {
			LIBRARIES, NODES })
	public String getDashboard(Model model, @RequestParam(LIBRARIES) Collection<Long> libraryIds,
			@RequestParam(NODES) Collection<Long> nodeIds) {

		TestCaseStatisticsBundle stats = testCaseLibraryNavigationService
				.getStatisticsForSelection(libraryIds, nodeIds);

		model.addAttribute("statistics", stats);

		return "fragment/test-cases/test-cases-dashboard";
	}

}
