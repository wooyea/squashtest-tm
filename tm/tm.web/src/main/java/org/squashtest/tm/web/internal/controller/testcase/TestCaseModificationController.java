/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.DefaultPaging;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestCaseType;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService;
import org.squashtest.tm.service.execution.ExecutionFinder;
import org.squashtest.tm.service.foundation.collection.CollectionSorting;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.controller.testcase.ActionStepFormModel.ActionStepFormModelValidator;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatterWithoutOrder;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.combo.OptionTag;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperCollectionSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTablePagedFilter;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.tm.web.internal.service.CustomFieldHelperService;


@Controller
@RequestMapping("/test-cases/{testCaseId}")
public class TestCaseModificationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseModificationController.class);
	
	private static final String NAME_KEY = "name";
	private static final String S_ECHO_PARAM = "sEcho";
	private static final String TEST_CASE_ = "test case ";
	private static final String COPIED_STEP_ID_PARAM = "copiedStepId[]";

	private final DataTableMapper verifiedReqMapper = new DataTableMapper("verified-requirement",
			RequirementVersion.class, Project.class).initMapping(9)
			.mapAttribute(Project.class, 1, NAME_KEY, String.class)
			.mapAttribute(RequirementVersion.class, 2, "id", Long.class)
			.mapAttribute(RequirementVersion.class, 3, "reference", String.class)
			.mapAttribute(RequirementVersion.class, 4, NAME_KEY, String.class)
			.mapAttribute(RequirementVersion.class, 5, "versionNumber", Integer.class)
			.mapAttribute(RequirementVersion.class, 6, "criticality", RequirementCriticality.class)
			.mapAttribute(RequirementVersion.class, 7, "category", RequirementCategory.class);

	private final DataTableMapper referencingTestCaseMapper = new DataTableMapper("referencing-test-cases",
			TestCase.class, Project.class).initMapping(6).mapAttribute(Project.class, 2, NAME_KEY, String.class)
			.mapAttribute(TestCase.class, 3, "reference", String.class)
			.mapAttribute(TestCase.class, 4, NAME_KEY, String.class)
			.mapAttribute(TestCase.class, 5, "executionMode", TestCaseExecutionMode.class);

	private final DataTableMapper execsTableMapper = new DataTableMapper("executions", Execution.class, Project.class, Campaign.class, Iteration.class, TestSuite.class)
			.initMapping(11).mapAttribute(Project.class, 1, NAME_KEY, String.class)
			.mapAttribute(Campaign.class, 2, NAME_KEY, String.class)
			.mapAttribute(Iteration.class, 3, NAME_KEY, String.class)
			.mapAttribute(Execution.class, 4, NAME_KEY, String.class)
			.mapAttribute(Execution.class, 5, "executionMode", TestCaseExecutionMode.class)
			.mapAttribute(TestSuite.class, 6, NAME_KEY, String.class)
			.mapAttribute(Execution.class, 8, "executionStatus", ExecutionStatus.class)
			.mapAttribute(Execution.class, 9, "lastExecutedBy", String.class)
			.mapAttribute(Execution.class, 10, "lastExecutedOn", Date.class);

	private TestCaseModificationService testCaseModificationService;

	private ExecutionFinder executionFinder;
	
	@Inject
	private CallStepManagerService callStepManager;
	
	
	@Inject
	private InternationalizationHelper internationalizationHelper;
	
	@Inject
	private Provider<TestCaseImportanceJeditableComboDataBuilder> importanceComboBuilderProvider;

	@Inject
	private Provider<TestCaseNatureJeditableComboDataBuilder> natureComboBuilderProvider;
	
	@Inject
	private Provider<TestCaseTypeJeditableComboDataBuilder> typeComboBuilderProvider;

	
	// ****** custom field services ******************

	@Inject
	private CustomFieldHelperService cufHelperService;

	// ****** /custom field services ******************
	
	@Inject
	private Provider<TestCaseStatusJeditableComboDataBuilder> statusComboBuilderProvider;
	
	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatterProvider;

	@Inject
	private Provider<LevelLabelFormatterWithoutOrder> levelLabelFormatterWithoutOrderProvider;
	
	@ServiceReference
	public void setTestCaseModificationService(TestCaseModificationService testCaseModificationService) {
		this.testCaseModificationService = testCaseModificationService;
	}
	
	@InitBinder("add-test-step")
	public void addTestCaseBinder(WebDataBinder binder){
		ActionStepFormModelValidator validator = new ActionStepFormModelValidator();
		validator.setMessageSource(internationalizationHelper);
		binder.setValidator(validator);
	}
	

	@RequestMapping(method = RequestMethod.GET)
	public final ModelAndView showTestCase(@PathVariable long testCaseId, Locale locale) {
		ModelAndView mav = new ModelAndView("fragment/test-cases/edit-test-case");

		TestCase testCase = testCaseModificationService.findById(testCaseId);
		populateModelWithTestCaseEditionData(mav, testCase, locale);

		return mav;
	}


	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showTestCaseInfo(@PathVariable long testCaseId, Locale locale) {

		LOGGER.trace("TestCaseModificationController : getting infos");

		ModelAndView mav = new ModelAndView("page/test-case-libraries/show-test-case");

		TestCase testCase = testCaseModificationService.findTestCaseWithSteps(testCaseId);
		populateModelWithTestCaseEditionData(mav, testCase, locale);

		return mav;
	}

	private void populateModelWithTestCaseEditionData(ModelAndView mav, TestCase testCase, Locale locale) {
		

		boolean hasCUF = cufHelperService.hasCustomFields(testCase);
		
		// Convert execution mode with local parameter
		List<OptionTag> executionModes = new ArrayList<OptionTag>();
		for (TestCaseExecutionMode executionMode : TestCaseExecutionMode.values()) {
			OptionTag ot = new OptionTag();
			ot.setLabel(formatExecutionMode(executionMode, locale));
			ot.setValue(executionMode.toString());
			executionModes.add(ot);
		}
		mav.addObject("testCase", testCase);
		mav.addObject("executionModes", executionModes);
		mav.addObject("testCaseImportanceComboJson", buildImportanceComboData(testCase, locale));
		mav.addObject("testCaseImportanceLabel", formatImportance(testCase.getImportance(), locale));
		mav.addObject("testCaseNatureComboJson", buildNatureComboData(testCase, locale));
		mav.addObject("testCaseNatureLabel", formatNature(testCase.getNature(), locale));
		mav.addObject("testCaseTypeComboJson", buildTypeComboData(testCase, locale));
		mav.addObject("testCaseTypeLabel", formatType(testCase.getType(), locale));
		mav.addObject("testCaseStatusComboJson", buildStatusComboData(testCase, locale));
		mav.addObject("testCaseStatusLabel", formatStatus(testCase.getStatus(), locale));
		mav.addObject("hasCUF", hasCUF);
	}

	private String buildImportanceComboData(TestCase testCase, Locale locale) {
		return importanceComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}

	private String buildNatureComboData(TestCase testCase, Locale locale) {
		return natureComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	
	private String buildTypeComboData(TestCase testCase, Locale locale) {
		return typeComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	
	private String buildStatusComboData(TestCase testCase, Locale locale) {
		return statusComboBuilderProvider.get().useLocale(locale).buildMarshalled();
	}
	
	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return internationalizationHelper.internationalize(mode, locale);
	}

	
	@RequestMapping(value="/steps/panel")
	public String getTestStepsPanel(@PathVariable("testCaseId") long testCaseId, Model model, Locale locale){
		
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		List<TestStep> steps = testCase.getSteps().subList(0, Math.min(10, testCase.getSteps().size()));	
		
		List<CustomField> cufDefinitions = cufHelperService.findCustomFieldsBoundTo(testCase.getProject().getId(), BindableEntity.TEST_STEP);
		List<CustomFieldValue> cufValues = cufHelperService.findCustomFieldValuesForTestSteps(steps);
			
		
		TestStepsTableModelBuilder builder = new TestStepsTableModelBuilder(internationalizationHelper, locale);
		builder.usingCustomFields(cufValues);
		List<Map<?,?>>  stepsData = builder.buildAllData(steps);
		

		model.addAttribute("testCase", testCase);
		model.addAttribute("stepsData", stepsData);
		model.addAttribute("cufDefinitions", cufDefinitions);
		
		return "test-cases-tabs/test-steps-tab.html";
		
	}
	
	@RequestMapping(value = "/steps-table", params = S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getStepsTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
			Locale locale) {

		LOGGER.trace("TestCaseModificationController: getStepsTableModel called ");

		Paging filter = createPaging(params);

		FilteredCollectionHolder<List<TestStep>> holder = testCaseModificationService.findStepsByTestCaseIdFiltered(
				testCaseId, filter);

		return new TestStepsTableModelBuilder(internationalizationHelper, locale).buildDataModel(holder,
				filter.getFirstItemIndex() + 1, params.getsEcho());

	}

	@RequestMapping(value = "/steps/add", method = RequestMethod.POST, params = { "action", "expectedResult" })
	@ResponseBody
	public void addActionTestStep(@Valid @ModelAttribute("add-test-step") ActionStepFormModel stepModel,
			@PathVariable long testCaseId) {

		ActionTestStep step = stepModel.getActionTestStep();
		
		Map<Long, String> customFieldValues = stepModel.getCustomFields();
		
		testCaseModificationService.addActionTestStep(testCaseId, step, customFieldValues);

		LOGGER.trace(TEST_CASE_ + testCaseId + ": step added, action : " + step.getAction() + ", expected result : "
				+ step.getExpectedResult());
	}
	
	

	@RequestMapping(value = "/steps/paste", method = RequestMethod.POST, params = { COPIED_STEP_ID_PARAM })
	@ResponseBody
	public void pasteStep(@RequestParam(COPIED_STEP_ID_PARAM) String[] copiedStepId,
			@RequestParam(value = "indexToCopy", required = true) Long positionId, @PathVariable long testCaseId) {

		callStepManager.checkForCyclicStepCallBeforePaste(testCaseId, copiedStepId);

		for (int i = copiedStepId.length - 1; i >= 0; i--) {
			String id = copiedStepId[i];
			testCaseModificationService.pasteCopiedTestStep(testCaseId, positionId, Long.parseLong(id));
		}
		LOGGER.trace("test case copied some Steps");
	}

	@RequestMapping(value = "/steps/paste-last-index", method = RequestMethod.POST, params = { COPIED_STEP_ID_PARAM })
	@ResponseBody
	public void pasteStepLastIndex(@RequestParam(COPIED_STEP_ID_PARAM) String[] copiedStepId, @PathVariable long testCaseId) {

		callStepManager.checkForCyclicStepCallBeforePaste(testCaseId, copiedStepId);

		for (int i = 0; i < copiedStepId.length; i++) {
			String id = copiedStepId[i];
			testCaseModificationService.pasteCopiedTestStepToLastIndex(testCaseId, Long.parseLong(id));
		}
		LOGGER.trace("test case copied some Steps");
	}

	@RequestMapping(value = "/steps/{stepId}", method = RequestMethod.POST, params = "newIndex")
	@ResponseBody
	public void changeStepIndex(@PathVariable long stepId, @RequestParam int newIndex, @PathVariable long testCaseId) {

		testCaseModificationService.changeTestStepPosition(testCaseId, stepId, newIndex);
		LOGGER.trace(TEST_CASE_ + testCaseId + ": step " + stepId + " moved to " + newIndex);

	}

	@RequestMapping(value = "/steps/move", method = RequestMethod.POST, params = { "newIndex", "itemIds[]" })
	@ResponseBody
	public void changeStepsIndex(@RequestParam("itemIds[]") List<Long> itemIds, @RequestParam("newIndex") int newIndex,
			@PathVariable long testCaseId) {

		
		testCaseModificationService.changeTestStepsPosition(testCaseId, newIndex, itemIds);

	}

	@RequestMapping(value = "/steps/{stepIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteSteps(@PathVariable("stepIds") List<Long> stepIds, @PathVariable long testCaseId) {
		testCaseModificationService.removeListOfSteps(testCaseId, stepIds);
	}

	@RequestMapping(value = "/steps/{stepId}/action", method = RequestMethod.POST, params = { "id", VALUE })
	@ResponseBody
	public String changeStepAction(@PathVariable long stepId, @RequestParam(VALUE) String newAction) {
		testCaseModificationService.updateTestStepAction(stepId, newAction);
		LOGGER.trace("TestCaseModificationController : updated action for step {}", stepId);
		return newAction;
	}

	@RequestMapping(value = "/steps/{stepId}/result", method = RequestMethod.POST, params = { "id", VALUE })
	@ResponseBody
	public String changeStepDescription(@PathVariable long stepId, @RequestParam(VALUE) String newResult) {
		testCaseModificationService.updateTestStepExpectedResult(stepId, newResult);
		LOGGER.trace("TestCaseModificationController : updated action for step {}", stepId);
		return newResult;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-description", VALUE })
	@ResponseBody
	public String changeDescription(@RequestParam(VALUE) String testCaseDescription, @PathVariable long testCaseId) {

		testCaseModificationService.changeDescription(testCaseId, testCaseDescription);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated description to " + testCaseDescription);
		}

		return testCaseDescription;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-reference", VALUE })
	@ResponseBody
	public String changeReference(@RequestParam(VALUE) String testCaseReference, @PathVariable long testCaseId) {

		testCaseModificationService.changeReference(testCaseId, testCaseReference);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated reference to " + testCaseReference);
		}

		return testCaseReference;
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-importance", VALUE })
	public String changeImportance(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseImportance importance,
			Locale locale) {
		testCaseModificationService.changeImportance(testCaseId, importance);

		return formatImportance(importance, locale);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-nature", VALUE })
	public String changeNature(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseNature nature,
			Locale locale) {
		testCaseModificationService.changeNature(testCaseId, nature);

		return formatNature(nature, locale);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-type", VALUE })
	public String changeType(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseType type,
			Locale locale) {
		testCaseModificationService.changeType(testCaseId, type);

		return formatType(type, locale);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-status", VALUE })
	public String changeStatus(@PathVariable long testCaseId, @RequestParam(VALUE) TestCaseStatus status,
			Locale locale) {
		testCaseModificationService.changeStatus(testCaseId, status);

		return formatStatus(status, locale);
	}
	
	@RequestMapping(value = "/importanceAuto", method = RequestMethod.POST, params = { "importanceAuto" })
	@ResponseBody
	public String changeImportanceAuto(@PathVariable long testCaseId,
			@RequestParam(value = "importanceAuto") boolean auto, Locale locale) {
		testCaseModificationService.changeImportanceAuto(testCaseId, auto);
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		return formatImportance(testCase.getImportance(), locale);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=test-case-prerequisite", VALUE })
	@ResponseBody
	public String changePrerequisite(@RequestParam(VALUE) String testCasePrerequisite, @PathVariable long testCaseId) {

		testCaseModificationService.changePrerequisite(testCaseId, testCasePrerequisite);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(TEST_CASE_ + testCaseId + ": updated prerequisite to " + testCasePrerequisite);
		}

		return testCasePrerequisite;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(HttpServletResponse response, @PathVariable long testCaseId, @RequestParam String newName) {

		testCaseModificationService.rename(testCaseId, newName);
		LOGGER.info("TestCaseModificationController : renaming {} as {}", testCaseId, newName);
		
		return new RenameModel(newName);

	}

	@ResponseBody
	@RequestMapping(value = "/importance", method = RequestMethod.GET)
	public String getImportance(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseImportance importance = testCase.getImportance();
		return formatImportance(importance, locale);
	}
	
	@ResponseBody
	@RequestMapping(value = "/nature", method = RequestMethod.GET)
	public String getNature(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseNature nature = testCase.getNature();
		return formatNature(nature, locale);
	}

	@ResponseBody
	@RequestMapping(value = "/type", method = RequestMethod.GET)
	public String getType(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseType type = testCase.getType();
		return formatType(type, locale);
	}
	
	@ResponseBody
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public String getStatus(@PathVariable long testCaseId, Locale locale) {
		TestCase testCase = testCaseModificationService.findById(testCaseId);
		TestCaseStatus status = testCase.getStatus();
		return formatStatus(status, locale);
	}
	
	private String formatImportance(TestCaseImportance importance, Locale locale) {
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(importance);
	}

	private String formatNature(TestCaseNature nature, Locale locale) {
		return levelLabelFormatterWithoutOrderProvider.get().useLocale(locale).formatLabel(nature);
	}
	
	private String formatType(TestCaseType type, Locale locale) {
		return levelLabelFormatterWithoutOrderProvider.get().useLocale(locale).formatLabel(type);
	}
	
	private String formatStatus(TestCaseStatus status, Locale locale) {
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(status);
	}
	
	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long testCaseId) {

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		TestCase testCase = testCaseModificationService.findById(testCaseId);

		if (testCase == null) {
			testCase = createNotFoundTestCase();
		}
		mav.addObject("auditableEntity", testCase);
		// context-absolute url of this entity
		mav.addObject("entityContextUrl", "/test-cases/" + testCaseId);

		return mav;
	}

	// FIXME : a not found test case is an exception, now that we have a decent Exception manager we should remove that
	// workaround.
	@Deprecated
	private TestCase createNotFoundTestCase() {
		TestCase testCase;
		testCase = new TestCase();
		testCase.setName("NotFound");
		testCase.setDescription("This requirement either do not exists, or was removed");
		return testCase;
	}

	@RequestMapping(value = "/all-verified-requirements-table", params = S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAllVerifiedRequirementsTableModel(@PathVariable long testCaseId,
			final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting pas = createPagingAndSorting(params, verifiedReqMapper);

		PagedCollectionHolder<List<VerifiedRequirement>> holder = testCaseModificationService
				.findAllVerifiedRequirementsByTestCaseId(testCaseId, pas);

		return new DataTableModelHelper<VerifiedRequirement>() {
			@Override
			public Object[] buildItemData(VerifiedRequirement item) {
				return new Object[] { getCurrentIndex(), item.getProject().getName(), item.getId(), 
						item.getReference(), item.getName(), item.getDecoratedRequirement().getVersionNumber(),
						internationalizationHelper.internationalize(item.getCriticality(), locale), 
						internationalizationHelper.internationalize(item.getCategory(), locale), "",
						item.getDecoratedRequirement().getStatus().name(), item.isDirectVerification() };
			}
		}.buildDataModel(holder, params.getsEcho());

	}

	private PagingAndSorting createPagingAndSorting(DataTableDrawParameters params, DataTableMapper mapper) {
		return new DataTableMapperPagingAndSortingAdapter(params, mapper);
	}

	@RequestMapping(value = "/calling-test-case-table", params = S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getCallingTestCaseTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
			final Locale locale) {

		LOGGER.trace("TestCaseModificationController: getCallingTestCaseTableModel called ");

		CollectionSorting filter = createPaging(params, referencingTestCaseMapper);

		FilteredCollectionHolder<List<TestCase>> holder = testCaseModificationService.findCallingTestCases(testCaseId,
				filter);

		return new DataTableModelHelper<TestCase>() {
			@Override
			public Object[] buildItemData(TestCase item) {
				return new Object[] { item.getId(), getCurrentIndex(), item.getProject().getName(), item.getReference(),
						item.getName(), internationalizationHelper.internationalize(item.getExecutionMode(), locale) };
			}
		}.buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());

	}

	private CollectionSorting createPaging(final DataTableDrawParameters params, final DataTableMapper dtMapper) {
		return new DataTableMapperCollectionSortingAdapter(params, dtMapper);
	}

	private Paging createPaging(final DataTableDrawParameters params) {
		return new DataTablePagedFilter(params);
	}

	/* ********************************** localization stuffs ****************************** */
	/**
	 * Returns the
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/executions", method = RequestMethod.GET, params = "tab")
	public String getExecutionsTab(@PathVariable long testCaseId, Model model) {
		Paging paging = DefaultPaging.FIRST_PAGE;

		List<Execution> executions = executionFinder.findAllByTestCaseIdOrderByRunDate(testCaseId, paging);

		model.addAttribute("executionsPageSize", paging.getPageSize());
		model.addAttribute("testCaseId", testCaseId);
		model.addAttribute("execs", executions);

		return "test-case-executions-tab.html";
	}

	/**
	 * @param executionFinder
	 *            the executionFinder to set
	 */
	@ServiceReference
	public void setExecutionFinder(ExecutionFinder executionFinder) {
		this.executionFinder = executionFinder;
	}

	@RequestMapping(value = "/executions", params = S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getExecutionsTableModel(@PathVariable long testCaseId, DataTableDrawParameters params,
			Locale locale) {
		PagingAndSorting pas = createPagingAndSorting(params);

		PagedCollectionHolder<List<Execution>> executions = executionFinder.findAllByTestCaseId(testCaseId, pas);

		return new ExecutionsTableModelBuilder(locale, internationalizationHelper).buildDataModel(executions, params.getsEcho());
	}

	private PagingAndSorting createPagingAndSorting(DataTableDrawParameters params) {
		return new DataTableMapperPagingAndSortingAdapter(params, execsTableMapper);
	}
}
