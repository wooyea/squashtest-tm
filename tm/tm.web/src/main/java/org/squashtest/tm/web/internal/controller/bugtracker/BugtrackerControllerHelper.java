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
package org.squashtest.tm.web.internal.controller.bugtracker;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.tm.domain.bugtracker.BTIssueDecorator;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

public final class BugtrackerControllerHelper {
	private BugtrackerControllerHelper() {

	}

	/**
	 * Will build a string that shows all steps before the bugged step + the bugged step itself.<br>
	 * The string will look like this : <br/>
	 * <br>
	 * <em>
	 * 	=============================================<br>
	 *  |    Step 1/N<br>
	 *  =============================================<br>
	 * 	-------------------Action---------------------<br>
	 * 	action description<br>
	 * 	<br>
	 * 	----------------Expected Result---------------<br>
	 * 	expected result description<br>
	 * 	<br>
	 * 	<br>
	 * 	=============================================<br>
	 * 	|    Step 2/N<br>
	 * 	=============================================<br>
	 * 	...<br>
	 * 	<br></em>
	 * 
	 * @param buggedStep
	 *            the bugged step where the issue will be declared
	 * @param locale
	 * @param messageSource
	 * @return the built string as described
	 */
	public static String getDefaultAdditionalInformations(ExecutionStep buggedStep, Locale locale,
			MessageSource messageSource) {
		Execution execution = buggedStep.getExecution();
		List<ExecutionStep> steps = execution.getSteps();
		int totalStepNumber = steps.size();
		StringBuilder builder = new StringBuilder();
		for (ExecutionStep step : steps) {
			String actionText = HTMLCleanupUtils.htmlToText(step.getAction());
			String expectedResult = HTMLCleanupUtils.htmlToText(step.getExpectedResult());
			appendStepTitle(locale, messageSource, totalStepNumber, builder, step);
			builder.append(messageSource.getMessage("issue.default.additionalInformation.action", null, locale));
			builder.append(actionText);
			builder.append(messageSource.getMessage("issue.default.additionalInformation.expectedResult", null, locale));
			builder.append(expectedResult);
			builder.append("\n\n\n");
			if (step.getId().equals(buggedStep.getId())) {
				break;
			}
		}
		return builder.toString();
	}

	private static void appendStepTitle(Locale locale, MessageSource messageSource, int totalStepNumber,
			StringBuilder builder, ExecutionStep step) {
		builder.append("=============================================\n|    ");
		builder.append(messageSource.getMessage("issue.default.additionalInformation.step", null, locale));
		builder.append(" ");
		builder.append(step.getExecutionStepOrder() + 1);
		builder.append("/");
		builder.append(totalStepNumber);
		builder.append("\n=============================================\n");
	}

	/**
	 * Will build a default description String that will look like this : <br/>
	 * <br/>
	 * <em># Test Case : [Reference] test case name <br/>
	 * # Execution : execution link <br/>
	 * <br/>
	 * # Issue description :<br/></em>
	 * 
	 * @param execution
	 *            an execution where the issue will be declared
	 * @param locale
	 * @param messageSource
	 * @return the description string
	 */
	public static String getDefaultDescription(Execution execution, Locale locale, MessageSource messageSource,
			String executionUrl) {
		StringBuffer description = new StringBuffer();
		appendTestCaseDesc(execution.getReferencedTestCase(), description, locale, messageSource);
		appendExecutionDesc(description, locale, messageSource, executionUrl);
		appendDescHeader(description, locale, messageSource);
		return description.toString();
	}

	/**
	 * Will build a default description String that will look like this : <br/>
	 * <br/>
	 * <em># Test Case : [Reference] test case name <br/>
	 * # Execution : execution link <br/>
	 * # Concerned Step : step n�/total step nb<br/>
	 * <br/>
	 * # Issue description :<br/></em>
	 * 
	 * @param step
	 *            an execution step where the issue will be declared
	 * @param locale
	 * @param messageSource
	 * @param executionUrl
	 * @return the string built as described
	 */
	public static String getDefaultDescription(ExecutionStep step, Locale locale, MessageSource messageSource,
			String executionUrl) {
		StringBuffer description = new StringBuffer();
		appendTestCaseDesc(step.getExecution().getReferencedTestCase(), description, locale, messageSource);
		appendExecutionDesc(description, locale, messageSource, executionUrl);
		appendStepDesc(step, description, locale, messageSource);
		appendDescHeader(description, locale, messageSource);
		return description.toString();
	}

	/**
	 * build the url of the execution
	 * 
	 * @param request
	 * @param step
	 * @return <b>"http://</b>serverName<b>:</b>serverPort/contextPath<b>/executions/</b>executionId<b>/info"</b>
	 */
	public static String buildExecutionUrl(HttpServletRequest request, Execution execution) {
		StringBuffer requestUrl = new StringBuffer("http://");
		requestUrl.append(request.getServerName());
		requestUrl.append(':');
		requestUrl.append(request.getServerPort());
		requestUrl.append(request.getContextPath());
		requestUrl.append("/executions/");
		requestUrl.append(execution.getId());
		requestUrl.append("/info");
		return requestUrl.toString();
	}

	private static void appendDescHeader(StringBuffer description, Locale locale, MessageSource messageSource) {
		description.append("\n# ");
		description.append(messageSource.getMessage("issue.default.description.description", null, locale));
		description.append(" :\n");
	}

	private static void appendStepDesc(ExecutionStep step, StringBuffer description, Locale locale,
			MessageSource messageSource) {
		description.append("# ");
		description.append(messageSource.getMessage("issue.default.description.concernedStep", null, locale));
		description.append(": ");
		description.append(step.getExecutionStepOrder() + 1);
		description.append("/");
		description.append(step.getExecution().getSteps().size());
		description.append("\n");
	}

	private static void appendExecutionDesc(StringBuffer description, Locale locale, MessageSource messageSource,
			String executionUrl) {
		description.append("# ");
		description.append(messageSource.getMessage("issue.default.description.execution", null, locale));
		description.append(": ");
		description.append(executionUrl);
		description.append("\n");
	}

	private static void appendTestCaseDesc(TestCase testCase, StringBuffer description, Locale locale,
			MessageSource messageSource) {
		if (testCase != null) {
			description.append("# ");
			description.append(messageSource.getMessage("issue.default.description.testCase", null, locale));
			description.append(": [");
			description.append(testCase.getReference());
			description.append("] ");
			description.append(testCase.getName());
			description.append("\n");
		}
	}

	/**
	 * <p>
	 * the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the owning entity</li>
	 * </ul>
	 * </p>
	 */
	static final class IterationIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssueDecorator>> {

		private IssueOwnershipNameBuilder nameBuilder = new IterationModelOwnershipNamebuilder();
		private BugTrackersLocalService bugTrackersLocalService;

		public IterationIssuesTableModel(BugTrackersLocalService bugTrackersLocalService, MessageSource source,
				Locale locale) {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
			this.bugTrackersLocalService = bugTrackersLocalService;
		}

		@Override
		public Object[] buildItemData(IssueOwnership<BTIssueDecorator> ownership) {
			return new Object[] {
					bugTrackersLocalService.getIssueUrl(ownership.getIssue().getId(),
							ownership.getOwner().getBugTracker()).toExternalForm(), ownership.getIssue().getId(),
					ownership.getIssue().getSummary(), ownership.getIssue().getPriority().getName(),
					ownership.getIssue().getStatus().getName(), ownership.getIssue().getAssignee().getName(),
					nameBuilder.buildName(ownership.getOwner()) };
		}
	}

	/**
	 * <p>
	 * the DataTableModel for a TestCase will hold following informations :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the iteration name</li>
	 * </ul>
	 * </p>
	 */
	static final class TestCaseIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssueDecorator>> {

		private IssueOwnershipNameBuilder nameBuilder = new TestCaseModelOwnershipNamebuilder();
		private BugTrackersLocalService bugTrackersLocalService;

		public TestCaseIssuesTableModel(BugTrackersLocalService bugTrackersLocalService, MessageSource source,
				Locale locale) {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
			this.bugTrackersLocalService = bugTrackersLocalService;
		}

		@Override
		public Object[] buildItemData(IssueOwnership<BTIssueDecorator> ownership) {
			BTIssue issue = ownership.getIssue();
			return new Object[] {
					bugTrackersLocalService.getIssueUrl(issue.getId(), ownership.getOwner().getBugTracker())
							.toExternalForm(), issue.getId(), issue.getSummary(), issue.getPriority().getName(),
					issue.getStatus().getName(), issue.getAssignee().getName(),
					nameBuilder.buildName(ownership.getOwner()), ownership.getExecution().getId() };
		}
	}

	/**
	 * <p>
	 * the DataTableModel for an execution will hold the same informations than IterationIssuesTableModel (for now) :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary</li>,
	 * <li>the priority,</li>
	 * <li>the status,</li>
	 * <li>the assignee,</li>
	 * <li>the owning entity</li>
	 * </ul>
	 * </p>
	 */
	static final class ExecutionIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssueDecorator>> {

		private IssueOwnershipNameBuilder nameBuilder = new ExecutionModelOwnershipNamebuilder();
		private BugTrackersLocalService bugTrackersLocalService;

		public ExecutionIssuesTableModel(BugTrackersLocalService bugTrackersLocalService, MessageSource source,
				Locale locale) {
			nameBuilder.setMessageSource(source);
			nameBuilder.setLocale(locale);
			this.bugTrackersLocalService = bugTrackersLocalService;
		}

		@Override
		public Map<String, Object> buildItemData(IssueOwnership<BTIssueDecorator> ownership) {
			
			BTIssueDecorator issue = ownership.getIssue();

			Map<String, Object> result = new HashMap<String, Object>(9);
			
			result.put("issue-url", bugTrackersLocalService.getIssueUrl(issue.getId(), ownership.getOwner().getBugTracker())
														    .toExternalForm());
			result.put("remote-id", issue.getId());
			result.put("summary", issue.getSummary());
			result.put("priority", issue.getPriority().getName());
			result.put("status", issue.getStatus().getName());
			result.put("assignee", issue.getAssignee().getName());
			result.put("owner", nameBuilder.buildName(ownership.getOwner()));
			result.put("empty-placeholder", "");
			result.put("local-id", issue.getIssueId());
			
			return result;
		}
	}

	/**
	 * <p>
	 * the DataTableModel will hold :
	 * <ul>
	 * <li>the url of that issue,</li>
	 * <li>the id,</li>
	 * <li>the summary,</li>
	 * <li>the priority</li>
	 * </ul>
	 * </p>
	 */
	static final class StepIssuesTableModel extends DataTableModelHelper<IssueOwnership<BTIssueDecorator>> {
		private BugTrackersLocalService bugTrackersLocalService;

		StepIssuesTableModel(BugTrackersLocalService bugTrackerLocalService) {
			this.bugTrackersLocalService = bugTrackerLocalService;
		}

		@Override
		public Map<String, Object> buildItemData(IssueOwnership<BTIssueDecorator> ownership) {
			
			BTIssueDecorator issue = ownership.getIssue();
			Map<String, Object> result = new HashMap<String, Object>();
			
			result.put("issue-url", bugTrackersLocalService.getIssueUrl(
									issue.getId(), ownership.getOwner().getBugTracker())
									.toExternalForm()
					   );
			
			result.put("remote-id", issue.getId());
			result.put("summary", issue.getSummary());
			result.put("priority", issue.getPriority().getName());
			result.put("empty-placeholder", "");
			result.put("local-id", issue.getIssueId());
			
			return result;
		}
	}

	/**
	 * 
	 * Build a different description String depending on IssueDetectorType.
	 *
	 */
	private interface IssueOwnershipNameBuilder {
		void setMessageSource(MessageSource source);

		void setLocale(Locale locale);

		String buildName(IssueDetector bugged);
	}
	
	/**
	 * 
	 * Holds generic code to differentiate IssueDetectorTypes
	 *
	 */
	private abstract static class IssueOwnershipAbstractNameBuilder implements IssueOwnershipNameBuilder {
		protected Locale locale;
		protected MessageSource messageSource;

		@Override
		public void setLocale(Locale locale) {
			this.locale = locale;
		}

		@Override
		public void setMessageSource(MessageSource source) {
			this.messageSource = source;
		}
		
		@Override
		public String buildName(IssueDetector bugged) {
			String name = "this is clearly a bug";

			if (bugged instanceof ExecutionStep) {
				ExecutionStep step = ((ExecutionStep) bugged);
				name = buildStepName(step);
			} else if (bugged instanceof Execution) {
				Execution exec = ((Execution) bugged);
				name = buildExecName(exec);
			}

			return name;
		}
		
		abstract String buildStepName(ExecutionStep executionStep);
		abstract String buildExecName(Execution execution) ;
		
	}
	/**
	 * 
	 * Implements builder for IssueDetector's description to display in Iteration's Issues table.
	 *
	 */
	private static final class IterationModelOwnershipNamebuilder extends IssueOwnershipAbstractNameBuilder {
		@Override
		String buildExecName(Execution bugged) {
			String suiteNameList = findTestSuiteNameList(bugged);
			if (suiteNameList.equals("")) {
				return messageSource.getMessage("squashtm.generic.hierarchy.execution.name.noSuite", new Object[] {
						bugged.getName(), bugged.getExecutionOrder() + 1 }, locale);
			} else {
				return messageSource.getMessage("squashtm.generic.hierarchy.execution.name",
						new Object[] { bugged.getName(), suiteNameList, bugged.getExecutionOrder() + 1 }, locale);
			}
		}

		@Override
		String buildStepName(ExecutionStep executionStep) {
			return buildExecName(executionStep.getExecution());
		}
	}
	/**
	 * 
	 * Implements builder for IssueDetector's description to display in Execution's Issues table.
	 *
	 */
	private static final class ExecutionModelOwnershipNamebuilder extends IssueOwnershipAbstractNameBuilder {
		@Override
		public String buildExecName(Execution bugged) {
			 return "";
		}
		
		@Override
		String buildStepName(ExecutionStep bugged) {
			Integer index = bugged.getExecutionStepOrder() + 1;
			return messageSource.getMessage("squashtm.generic.hierarchy.execution.step.name", new Object[] { index },
					locale);
		}

		

	}
	/**
	 * 
	 * Implements builder for IssueDetector's description to display in TestCase's Issues table.
	 *
	 */
	private static final class TestCaseModelOwnershipNamebuilder extends IssueOwnershipAbstractNameBuilder {

		String buildExecName(Execution execution) {
			String iterationName = findIterationName(execution);
			String suiteNameList = findTestSuiteNameList(execution);
			if (suiteNameList.equals("")) {
				return messageSource.getMessage("squashtm.test-case.hierarchy.execution.name.noSuite", new Object[] {
						iterationName, execution.getExecutionOrder() + 1 }, locale);
			} else {
				return messageSource.getMessage("squashtm.test-case.hierarchy.execution.name", new Object[] {
						iterationName, suiteNameList, execution.getExecutionOrder() + 1 }, locale);
			}
		}

		@Override
		String buildStepName(ExecutionStep executionStep) {
			return buildExecName(executionStep.getExecution());
		}

	}

	private static String findTestSuiteNameList(Execution execution) {
		List<TestSuite> buggedSuites = execution.getTestPlan().getTestSuites();
		String testSuiteNames = "";
		if (!buggedSuites.isEmpty()) {
			int i = 0;
			while (i < buggedSuites.size() - 1) {
				testSuiteNames += buggedSuites.get(i).getName() + ",";
			}
			testSuiteNames += buggedSuites.get(i).getName();
		}
		return testSuiteNames;
	}

	private static String findIterationName(Execution execution) {
		Iteration iteration = execution.getTestPlan().getIteration();
		String iterationName = "";
		if (iteration != null) {
			iterationName = iteration.getName();
		}
		return iterationName;
	}

}
