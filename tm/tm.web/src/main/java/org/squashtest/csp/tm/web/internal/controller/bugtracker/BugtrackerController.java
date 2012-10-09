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
package org.squashtest.csp.tm.web.internal.controller.bugtracker;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.core.domain.Identified;
import org.squashtest.csp.core.domain.IdentifiedUtil;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.IssueDetector;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.domain.campaign.Campaign;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.TestSuite;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStep;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.BugTrackerFinderService;
import org.squashtest.csp.tm.service.BugTrackersLocalService;
import org.squashtest.csp.tm.service.CampaignFinder;
import org.squashtest.csp.tm.service.ExecutionFinder;
import org.squashtest.csp.tm.service.IterationFinder;
import org.squashtest.csp.tm.service.ProjectFinder;
import org.squashtest.csp.tm.service.TestCaseFinder;
import org.squashtest.csp.tm.service.TestSuiteFinder;
import org.squashtest.csp.tm.web.internal.controller.bugtracker.BugtrackerControllerHelper.ExecutionIssuesTableModel;
import org.squashtest.csp.tm.web.internal.controller.bugtracker.BugtrackerControllerHelper.IterationIssuesTableModel;
import org.squashtest.csp.tm.web.internal.controller.bugtracker.BugtrackerControllerHelper.StepIssuesTableModel;
import org.squashtest.csp.tm.web.internal.controller.bugtracker.BugtrackerControllerHelper.TestCaseIssuesTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;

@Controller
@RequestMapping("/bugtracker")
public class BugtrackerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugtrackerController.class);

	private BugTrackersLocalService bugTrackersLocalService;
	private CampaignFinder campaignFinder;
	private IterationFinder iterationFinder;
	private TestSuiteFinder testSuiteFinder;
	private ExecutionFinder executionFinder;
	private TestCaseFinder testCaseFinder;
	private ProjectFinder projectFinder;
	private BugTrackerFinderService bugTrackerFinderService;

	private static final String EXECUTION_STEP_TYPE = "execution-step";
	private static final String EXECUTION_TYPE = "execution";
	private static final String ITERATION_TYPE = "iteration";
	private static final String CAMPAIGN_TYPE = "campaign";
	private static final String TEST_SUITE_TYPE = "test-suite";
	private static final String TEST_CASE_TYPE = "test-case";
	private static final String BUGTRACKER_ID = "bugTrackerId";
	private static final String EMPTY_BUGTRACKER_MAV = "fragment/issues/bugtracker-panel-empty";
	
	private static final String STYLE = "style";
	private static final String TOGGLE = "toggle";

	@Inject
	private MessageSource messageSource;
	
	@ServiceReference
	public void setProjectFinder(ProjectFinder projectFinder){
		this.projectFinder = projectFinder;
	}
	@ServiceReference
	public void setCampaignFinder(CampaignFinder campaignFinder) {
		this.campaignFinder = campaignFinder;
	}

	@ServiceReference
	public void setIterationFinder(IterationFinder iterationFinder) {
		this.iterationFinder = iterationFinder;
	}

	@ServiceReference
	public void setTestSuiteFinder(TestSuiteFinder testSuiteFinder) {
		this.testSuiteFinder = testSuiteFinder;
	}

	@ServiceReference
	public void setExecutionFinder(ExecutionFinder executionFinder) {
		this.executionFinder = executionFinder;
	}

	@ServiceReference
	public void setTestCaseFinder(TestCaseFinder testCaseFinder) {
		this.testCaseFinder = testCaseFinder;
	}

	@ServiceReference
	public void setBugTrackersLocalService(BugTrackersLocalService bugTrackersLocalService) {
		if (bugTrackersLocalService == null) {
			throw new IllegalArgumentException("BugTrackerController : no service provided");
		}
		this.bugTrackersLocalService = bugTrackersLocalService;
	}

	@ServiceReference
	public void setBugTrackerFinderService(BugTrackerFinderService bugTrackerFinderService) {
		this.bugTrackerFinderService = bugTrackerFinderService;
	}

	/* **************************************************************************************************************
	 * *
	 * Navigation button * *
	 * ***********************************************************************************************************
	 */
	@RequestMapping(value = "workspace-button", method = RequestMethod.GET)
	public ModelAndView getNavButton(Locale locale) {
		List<Project> projects = projectFinder.findAllReadable();
		List<Long> projectsIds = IdentifiedUtil.extractIds(projects); 
		List<BugTracker> readableBugTrackers = bugTrackerFinderService.findDistinctBugTrackersForProjects(projectsIds);
		 if (readableBugTrackers.isEmpty()) {
			LOGGER.trace("no bugtracker");
			return new ModelAndView("fragment/issues/bugtracker-panel-empty");
		 } else {
			 LOGGER.trace("return bugtracker nav button");
			 ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-nav-button");
			 mav.addObject("bugtrackers", readableBugTrackers);
			 return mav;
		 }
	}

	@RequestMapping(value = "{bugtrackerId}/workspace", method = RequestMethod.GET)
	public ModelAndView showWorkspace(@PathVariable Long bugtrackerId) {
		BugTracker bugTracker = bugTrackerFinderService.findById(bugtrackerId);
		ModelAndView mav = new ModelAndView("page/bugtracker-workspace");
		 mav.addObject("bugtrackerUrl", bugTracker.getUrl().toString());
		return mav;
	}

	/* **************************************************************************************************************
	 * *
	 * ExecutionStep level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that execution step and the stub for the report form. Remember
	 * that the report bug dialog will be populated later.
	 * 
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}", method = RequestMethod.GET)
	public ModelAndView getExecStepIssuePanel(@PathVariable Long stepId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		ExecutionStep step = executionFinder.findExecutionStepById(stepId);
		return makeIssuePanel(step, EXECUTION_STEP_TYPE, locale, panelStyle, step.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getExecStepKnownIssuesData(@PathVariable("stepId") Long stepId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = new IssueCollectionSorting(params);
		try {

			filteredCollection = bugTrackersLocalService.findSortedIssueOwnerShipsForExecutionStep(stepId, sorter);
		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_STEP_TYPE, stepId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_STEP_TYPE, stepId, npException);
		}

		return new StepIssuesTableModel(bugTrackersLocalService).buildDataModel(filteredCollection, sorter.getFirstItemIndex() + 1,
				params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution step. The returned json infos will populate the form.
	 * 
	 * @param stepId
	 * @return
	 */

	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-issue")
	@ResponseBody
	public BTIssue getExecStepReportStub(@PathVariable Long stepId, Locale locale, HttpServletRequest request) {

		ExecutionStep step = executionFinder.findExecutionStepById(stepId);

		String executionUrl = BugtrackerControllerHelper.buildExecutionUrl(request, step.getExecution());

		return makeReportIssueModel(step, locale, executionUrl);
	}

	/**
	 * gets the data of a new issue to be reported
	 * 
	 */
	@RequestMapping(value = EXECUTION_STEP_TYPE + "/{stepId}/new-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecStepIssueReport(@PathVariable("stepId") Long stepId, @RequestBody BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + stepId);

		IssueDetector entity = executionFinder.findExecutionStepById(stepId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/* **************************************************************************************************************
	 * *
	 * Execution level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that execution and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param stepId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}", method = RequestMethod.GET)
	public ModelAndView getExecIssuePanel(@PathVariable Long execId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		Execution bugged = executionFinder.findById(execId);
		return makeIssuePanel(bugged, EXECUTION_TYPE, locale, panelStyle, bugged.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getExecKnownIssuesData(@PathVariable("execId") Long execId, final DataTableDrawParameters params,
			final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;

		CollectionSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsforExecution(execId, sorter);

		}

		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_TYPE, execId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(EXECUTION_TYPE, execId, npException);
		}

		return new ExecutionIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/**
	 * will prepare a bug report for an execution. The returned json infos will populate the form.
	 * 
	 * @param execId
	 * @return
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue")
	@ResponseBody
	public BTIssue getExecReportStub(@PathVariable Long execId, Locale locale, HttpServletRequest request) {
		Execution execution = executionFinder.findById(execId);
		String executionUrl = BugtrackerControllerHelper.buildExecutionUrl(request, execution);
		return makeReportIssueModel(execution, locale, executionUrl);
	}

	/**
	 * gets the data of a new issue to be reported
	 * 
	 */
	@RequestMapping(value = EXECUTION_TYPE + "/{execId}/new-issue", method = RequestMethod.POST)
	@ResponseBody
	public Object postExecIssueReport(@PathVariable("execId") Long execId, @RequestBody BTIssue jsonIssue) {
		LOGGER.trace("BugTrackerController: posting a new issue for execution-step " + execId);

		Execution entity = executionFinder.findById(execId);

		if (jsonIssue.hasBlankId()) {
			return processIssue(jsonIssue, entity);
		} else {
			return attachIssue(jsonIssue, entity);
		}
	}

	/* **************************************************************************************************************
	 * *
	 * TestCase level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that testCase and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param tcId
	 * @return
	 */
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}", method = RequestMethod.GET)
	public ModelAndView getTestCaseIssuePanel(@PathVariable Long tcId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		TestCase testCase = testCaseFinder.findById(tcId);
		return makeIssuePanel(testCase, TEST_CASE_TYPE, locale, panelStyle, testCase.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_CASE_TYPE + "/{tcId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestCaseKnownIssuesData(@PathVariable("tcId") Long tcId, final DataTableDrawParameters params,
			final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForTestCase(tcId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_CASE_TYPE, tcId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_CASE_TYPE, tcId, npException);
		}
		return new TestCaseIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/* **************************************************************************************************************
	 * *
	 * Iteration level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that iteration and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param iterId
	 * @return
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}", method = RequestMethod.GET)
	public ModelAndView getIterationIssuePanel(@PathVariable Long iterId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		Iteration iteration = iterationFinder.findById(iterId);
		return makeIssuePanel(iteration, ITERATION_TYPE, locale, panelStyle, iteration.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = ITERATION_TYPE + "/{iterId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getIterationKnownIssuesData(@PathVariable("iterId") Long iterId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipForIteration(iterId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(ITERATION_TYPE, iterId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(ITERATION_TYPE, iterId, npException);
		}
		return new IterationIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/* **************************************************************************************************************
	 * *
	 * Campaign level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that campaign and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param iterId
	 * @return
	 */
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}", method = RequestMethod.GET)
	public ModelAndView getCampaignIssuePanel(@PathVariable Long campId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		Campaign campaign = campaignFinder.findById(campId);
		return makeIssuePanel(campaign, CAMPAIGN_TYPE, locale, panelStyle, campaign.getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = CAMPAIGN_TYPE + "/{campId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getCampaignKnownIssuesData(@PathVariable("campId") Long campId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForCampaigns(campId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(CAMPAIGN_TYPE, campId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(CAMPAIGN_TYPE, campId, npException);
		}
		return new IterationIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());
	}

	/* **************************************************************************************************************
	 * *
	 * TestSuite level section * *
	 * ***********************************************************************************************************
	 */

	/**
	 * returns the panel displaying the current bugs of that test-suite and the stub for the report form. Remember that
	 * the report bug dialog will be populated later.
	 * 
	 * @param testSuiteId
	 * @return
	 */
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}", method = RequestMethod.GET)
	public ModelAndView getTestSuiteIssuePanel(@PathVariable Long testSuiteId, Locale locale,
			@RequestParam(value = STYLE, required = false, defaultValue = TOGGLE) String panelStyle) {

		TestSuite testSuite = testSuiteFinder.findById(testSuiteId);
		return makeIssuePanel(testSuite, TEST_SUITE_TYPE, locale, panelStyle, testSuite.getIteration().getProject());

	}

	/**
	 * json Data for the known issues table.
	 */
	@RequestMapping(value = TEST_SUITE_TYPE + "/{testSuiteId}/known-issues", method = RequestMethod.GET)
	public @ResponseBody
	DataTableModel getTestSuiteKnownIssuesData(@PathVariable("testSuiteId") Long testSuiteId,
			final DataTableDrawParameters params, final Locale locale) {

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> filteredCollection;
		CollectionSorting sorter = new IssueCollectionSorting(params);
		try {
			filteredCollection = bugTrackersLocalService.findSortedIssueOwnershipsForTestSuite(testSuiteId, sorter);
		}
		// no credentials exception are okay, the rest is to be treated as usual
		catch (BugTrackerNoCredentialsException noCrdsException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_SUITE_TYPE, testSuiteId, noCrdsException);
		} catch (NullArgumentException npException) {
			filteredCollection = makeEmptyCollectionHolder(TEST_SUITE_TYPE, testSuiteId, npException);
		}
		return new IterationIssuesTableModel(bugTrackersLocalService, messageSource, locale).buildDataModel(filteredCollection,
				sorter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/* ************************* Generic code section ************************** */

	@RequestMapping(value = "/find-issue/{remoteKey}", method = RequestMethod.GET, params = { BUGTRACKER_ID })
	@ResponseBody
	public BTIssue findIssue(@PathVariable("remoteKey") String remoteKey, @RequestParam(BUGTRACKER_ID) long bugTrackerId) {
		BugTracker bugTracker = bugTrackerFinderService.findById(bugTrackerId);
		return bugTrackersLocalService.getIssue(remoteKey, bugTracker);
	}

	@RequestMapping(value = "/credentials", method = RequestMethod.POST, params = { "login", "password", BUGTRACKER_ID })
	public @ResponseBody
	Map<String, String> setCredendials(@RequestParam("login") String login, @RequestParam("password") String password,
			@RequestParam(BUGTRACKER_ID) long bugTrackerId) {
		BugTracker bugTracker = bugTrackerFinderService.findById(bugTrackerId);
		bugTrackersLocalService.setCredentials(login, password, bugTracker);

		Map<String, String> map = new HashMap<String, String>();
		map.put("status", "ok");
		return map;

	}

	@RequestMapping(value = "/status", method = RequestMethod.GET, params={"projectId"} )
	public @ResponseBody
	Object getBugTrackerStatus(@RequestParam("projectId")Long projectId) {
		String strStatus = null;

		BugTrackerStatus status = checkStatus(projectId);

		if (status == BugTrackerStatus.BUGTRACKER_READY) {
			strStatus = "ready";
		} else if (status == BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS) {
			strStatus = "needs_credentials";
		} else {
			strStatus = "bt_undefined";
		}

		Map<String, String> result = new HashMap<String, String>();
		result.put("status", strStatus);
		return result;
	}

	// FIXME : check first if a bugtracker is defined and if the credentials are set
	private Map<String, String> processIssue(BTIssue issue, IssueDetector entity) {
		final BTIssue postedIssue = bugTrackersLocalService.createIssue(entity, issue);
		final URL issueUrl = bugTrackersLocalService.getIssueUrl(postedIssue.getId(), entity.getBugTracker());

		Map<String, String> result = new HashMap<String, String>();
		result.put("url", issueUrl.toString());
		result.put("issueId", postedIssue.getId());

		return result;
	}

	private Map<String, String> attachIssue(final BTIssue issue, IssueDetector entity) {

		bugTrackersLocalService.attachIssue(entity, issue.getId());
		final URL issueUrl = bugTrackersLocalService.getIssueUrl(issue.getId(), entity.getBugTracker());

		Map<String, String> result = new HashMap<String, String>();
		result.put("url", issueUrl.toString());
		result.put("issueId", issue.getId());

		return result;
	}

	/* ********* generates a json model for an issue ******* */

	private BTIssue makeReportIssueModel(Execution exec, Locale locale, String executionUrl) {
		String defaultDescription = BugtrackerControllerHelper.getDefaultDescription(exec, locale, messageSource,
				executionUrl);
		return makeReportIssueModel(exec, defaultDescription);
	}

	private BTIssue makeReportIssueModel(ExecutionStep step, Locale locale, String executionUrl) {
		String defaultDescription = BugtrackerControllerHelper.getDefaultDescription(step, locale, messageSource,
				executionUrl);
		String defaultAdditionalInformations = BugtrackerControllerHelper.getDefaultAdditionalInformations(step,
				locale, messageSource);
		return makeReportIssueModel(step, defaultDescription, defaultAdditionalInformations, locale);
	}

	private BTIssue makeReportIssueModel(ExecutionStep step, String defaultDescription,
			String defaultAdditionalInformations, Locale locale) {
		BTIssue emptyIssue = makeReportIssueModel(step, defaultDescription);
		String comment = BugtrackerControllerHelper.getDefaultAdditionalInformations(step, locale, messageSource);
		emptyIssue.setComment(comment);
		return emptyIssue;
	}

	private BTIssue makeReportIssueModel(IssueDetector entity, String defaultDescription) {
		String projectName = entity.getProject().getBugtrackerBinding().getProjectName();
		final BTProject project = bugTrackersLocalService.findRemoteProject(projectName, entity.getBugTracker());

		BTIssue emptyIssue = new BTIssue();
		emptyIssue.setProject(project);
		emptyIssue.setDescription(defaultDescription);

		return emptyIssue;
	}

	/*
	 * generates the ModelAndView for the bug section.
	 * 
	 * If the bugtracker isn'st defined no panel will be sent at all.
	 */
	private ModelAndView makeIssuePanel(Identified entity, String type, Locale locale, String panelStyle, Project project) {
		if (project.isBugtrackerConnected()) {
			BugTrackerStatus status = checkStatus(project.getId());
			// JSON STATUS TODO

			BugTrackerInterfaceDescriptor descriptor = bugTrackersLocalService.getInterfaceDescriptor(project
					.findBugTracker());
			descriptor.setLocale(locale);

			ModelAndView mav = new ModelAndView("fragment/issues/bugtracker-panel");
			mav.addObject("entity", entity);
			mav.addObject("entityType", type);
			mav.addObject("interfaceDescriptor", descriptor);
			mav.addObject("panelStyle", panelStyle);
			mav.addObject("bugTrackerStatus", status);
			mav.addObject("project", project);
			mav.addObject("bugTracker", project.findBugTracker());
			return mav;
		} else {
			return new ModelAndView(EMPTY_BUGTRACKER_MAV);
		}

	}

	/* ******************************* private methods ********************************************** */

	private BugTrackerStatus checkStatus(long projectId) {
		return bugTrackersLocalService.checkBugTrackerStatus(projectId);
	}

	
	private static final class IssueCollectionSorting implements CollectionSorting{
		
		private DataTableDrawParameters params;

		private IssueCollectionSorting(final DataTableDrawParameters params){
			this.params = params;
		}
		@Override
		public int getFirstItemIndex() {
			return params.getiDisplayStart();
		}

		@Override
		public String getSortingOrder() {
			return params.getsSortDir_0();
		}

		@Override
		public String getSortedAttribute() {
			return "Issue.id";
		}

		@Override
		public int getPageSize() {
			return params.getiDisplayLength();
		}
		
	}
	

	private FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> makeEmptyCollectionHolder(String entityName,
			Long entityId, Exception cause) {
		LOGGER.trace("BugTrackerController : fetching known issues for  " + entityName + " " + entityId
				+ " failed, exception : ", cause);
		List<IssueOwnership<BTIssue>> emptyList = new LinkedList<IssueOwnership<BTIssue>>();
		return new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(0, emptyList);
	}

	
	
}
