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
package org.squashtest.csp.tm.web.internal.controller.campaign;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.service.security.PermissionEvaluationService;
import org.squashtest.csp.tm.domain.campaign.Iteration;
import org.squashtest.csp.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.csp.tm.domain.execution.Execution;
import org.squashtest.csp.tm.domain.execution.ExecutionStatus;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.service.IterationModificationService;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableFilterSorter;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.csp.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.csp.tm.web.internal.model.viewmapper.DataTableMapper;
import org.squashtest.csp.tm.web.internal.utils.DateUtils;

@Controller
@RequestMapping("/iterations/{iterationId}")
public class IterationModificationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(IterationModificationController.class);

	private IterationModificationService iterationModService;

	@Inject
	private PermissionEvaluationService permissionService;

	@ServiceReference
	public void setIterationModificationService(IterationModificationService iterationModificationService) {
		this.iterationModService = iterationModificationService;
	}

	@Inject
	private MessageSource messageSource;

	private final DataTableMapper testPlanMapper = new DataTableMapper("unused", IterationTestPlanItem.class,
			TestCase.class, Project.class).initMapping(8).mapAttribute(Project.class, 2, "name", String.class)
			.mapAttribute(TestCase.class, 3, "name", String.class)
			.mapAttribute(TestCase.class, 4, "executionMode", TestCaseExecutionMode.class)
			.mapAttribute(IterationTestPlanItem.class, 5, "executionStatus", ExecutionStatus.class)
			.mapAttribute(IterationTestPlanItem.class, 6, "lastExecutedBy", String.class)
			.mapAttribute(IterationTestPlanItem.class, 7, "lastExecutedOn", Date.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showIteration(@PathVariable long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);

		ModelAndView mav = new ModelAndView("fragment/iterations/edit-iteration");
		mav.addObject("iteration", iteration);

		return mav;
	}

	// will return the iteration in a full page
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView showIterationInfo(@PathVariable long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);

		ModelAndView mav = new ModelAndView("page/campaign-libraries/show-iteration");

		if (iteration != null) {
			mav.addObject("iteration", iteration);
		} else {
			iteration = new Iteration();
			iteration.setName("Not found");
			iteration.setDescription("This iteration either do not exists, or was removed");
			mav.addObject("iteration", new Iteration());

		}
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=iteration-description", "value" })
	@ResponseBody
	public String updateDescription(@RequestParam("value") String newDescription, @PathVariable long iterationId) {

		iterationModService.changeDescription(iterationId, newDescription);
		LOGGER.trace("Iteration " + iterationId + ": updated description to " + newDescription);
		return newDescription;

	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object rename(HttpServletResponse response, @RequestParam("newName") String newName,
			@PathVariable long iterationId) {

		LOGGER.info("IterationModificationController : renaming " + iterationId + " as " + newName);
		iterationModService.rename(iterationId, newName);
		final String reNewName = newName;
		return new Object() {
			public String newName = reNewName;
		};

	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public String remove(@PathVariable long iterationId) {

		iterationModService.delete(iterationId);

		LOGGER.info("IterationModificationController : deleting " + iterationId);

		return "ok";

	}

	@RequestMapping(value = "/general", method = RequestMethod.GET)
	public ModelAndView refreshGeneralInfos(@PathVariable long iterationId) {

		Iteration iteration = iterationModService.findById(iterationId);

		ModelAndView mav = new ModelAndView("fragment/generics/general-information-fragment");

		mav.addObject("auditableEntity", iteration);
		mav.addObject("entityContextUrl", "/iterations/" + iterationId);

		return mav;
	}

	/* *************************************** planning ********************************* */

	/**
	 * returns null if the string is empty, or a date otherwise. No check regarding the actual content of strDate.
	 */
	private Date strToDate(String strDate) {
		return DateUtils.millisecondsToDate(strDate);
	}

	private String dateToStr(Date date) {
		return DateUtils.dateToMillisecondsAsString(date);
	}

	@RequestMapping(value = "/planning", params = { "scheduledStart" })
	public @ResponseBody
	String setScheduledStart(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "scheduledStart") String strDate) {

		Date newScheduledStart = strToDate(strDate);
		String toReturn = dateToStr(newScheduledStart);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newScheduledStart);

		iterationModService.changeScheduledStartDate(iterationId, newScheduledStart);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "scheduledEnd" })
	@ResponseBody
	public String setScheduledEnd(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "scheduledEnd") String strDate) {

		Date newScheduledEnd = strToDate(strDate);
		String toReturn = dateToStr(newScheduledEnd);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newScheduledEnd);

		iterationModService.changeScheduledEndDate(iterationId, newScheduledEnd);

		return toReturn;

	}

	/** the next functions may receive null arguments : empty string **/

	@RequestMapping(value = "/planning", params = { "actualStart" })
	@ResponseBody
	public String setActualStart(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "actualStart") String strDate) {

		Date newActualStart = strToDate(strDate);
		String toReturn = dateToStr(newActualStart);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newActualStart);

		iterationModService.changeActualStartDate(iterationId, newActualStart);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "actualEnd" })
	@ResponseBody
	public String setActualEnd(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "actualEnd") String strDate) {

		Date newActualEnd = strToDate(strDate);
		String toReturn = dateToStr(newActualEnd);

		LOGGER.info("IterationModificationController : setting scheduled start date for iteration " + iterationId
				+ ", new date : " + newActualEnd);

		iterationModService.changeActualEndDate(iterationId, newActualEnd);

		return toReturn;

	}

	@RequestMapping(value = "/planning", params = { "setActualStartAuto" })
	@ResponseBody
	public String setActualStartAuto(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "setActualStartAuto") boolean auto) {

		LOGGER.info("IterationModificationController : autosetting actual start date for iteration " + iterationId
				+ ", new value " + auto);

		iterationModService.changeActualStartAuto(iterationId, auto);
		Iteration iteration = iterationModService.findById(iterationId);

		String toreturn = dateToStr(iteration.getActualStartDate());

		return toreturn;
	}

	@RequestMapping(value = "/planning", params = { "setActualEndAuto" })
	@ResponseBody
	public String setActualEndAuto(HttpServletResponse response, @PathVariable long iterationId,
			@RequestParam(value = "setActualEndAuto") boolean auto) {
		LOGGER.info("IterationModificationController : autosetting actual end date for campaign " + iterationId
				+ ", new value " + auto);

		iterationModService.changeActualEndAuto(iterationId, auto);
		Iteration iteration = iterationModService.findById(iterationId);

		String toreturn = dateToStr(iteration.getActualEndDate());

		return toreturn;

	}

	/* *************************************** test plan ********************************* */

	/***
	 * Method called when you drag a test case and change its position in the selected iteration
	 * 
	 * @param testPlanId
	 *            the test case to move
	 * @param newIndex
	 *            the new test case position
	 * @param iterationId
	 *            the iteration at which the test case is attached
	 */
	@RequestMapping(value = "/test-case/{testPlanId}", method = RequestMethod.POST, params = "newIndex")
	@ResponseBody
	public void changeTestPlanIndex(@PathVariable long testPlanId, @RequestParam int newIndex,
			@PathVariable long iterationId) {
		iterationModService.changeTestPlanPosition(iterationId, testPlanId, newIndex);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("iteration: " + iterationId + " test plan: " + testPlanId + " moved to " + newIndex);
		}
	}

	// returns the ID of the newly created execution
	@RequestMapping(value = "/test-plan/{testPlanId}/new-execution", method = RequestMethod.POST)
	public @ResponseBody
	String addExecution(@PathVariable("testPlanId") Long testPlanId, @PathVariable("iterationId") Long iterationId) {
		iterationModService.addExecution(iterationId, testPlanId);
		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId, testPlanId);

		return executionList.get(executionList.size() - 1).getId().toString();

	}

	@RequestMapping(value = "/test-case-executions/{testPlanId}", method = RequestMethod.GET)
	public ModelAndView getExecutionsForTestPlan(@PathVariable Long iterationId, @PathVariable Long testPlanId) {

		List<Execution> executionList = iterationModService.findExecutionsByTestPlan(iterationId, testPlanId);
		// get the iteraction to check access rights
		Iteration iter = iterationModService.findById(iterationId);
		boolean editable = permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "WRITE", iter);

		ModelAndView mav = new ModelAndView("fragment/iterations/iteration-test-plan-row");

		mav.addObject("editableIteration", editable);
		mav.addObject("testPlanId", testPlanId);
		mav.addObject("iterationId", iterationId);
		mav.addObject("executions", executionList);

		return mav;

	}

	@RequestMapping(value = "/test-plan", params = "sEcho")
	public @ResponseBody
	DataTableModel getTestPlanModel(@PathVariable Long iterationId, final DataTableDrawParameters params,
			final Locale locale) {

		CollectionSorting filter = createCollectionSorting(params, testPlanMapper);

		FilteredCollectionHolder<List<IterationTestPlanItem>> holder = iterationModService.findIterationTestPlan(
				iterationId, filter);

		return new DataTableModelHelper<IterationTestPlanItem>() {
			@Override
			public Object[] buildItemData(IterationTestPlanItem item) {

				String projectName;
				String testCaseName;
				String testCaseExecutionMode;

				if (item.isTestCaseDeleted()) {
					projectName = formatNoData(locale);
					testCaseName = formatDeleted(locale);
					testCaseExecutionMode = formatNoData(locale);
				} else {
					projectName = item.getReferencedTestCase().getProject().getName();
					testCaseName = item.getReferencedTestCase().getName();
					testCaseExecutionMode = formatExecutionMode(item.getReferencedTestCase().getExecutionMode(), locale);
				}

				return new Object[] { item.getId(), getCurrentIndex(), projectName, testCaseName,
						testCaseExecutionMode, formatStatus(item.getExecutionStatus(), locale),
						formatString(item.getLastExecutedBy(), locale), formatDate(item.getLastExecutedOn(), locale),
						item.isTestCaseDeleted(), " "

				};
			}
		}.buildDataModel(holder, filter.getFirstItemIndex() + 1, params.getsEcho());

	}

	/* ************** private stuffs are below ********************** */

	private CollectionSorting createCollectionSorting(final DataTableDrawParameters params, DataTableMapper mapper) {
		CollectionSorting filter = new DataTableFilterSorter(params, mapper);
		return filter;
	}

	/* ***************** data formatter *************************** */

	private String formatString(String arg, Locale locale) {
		if (arg == null) {
			return formatNoData(locale);
		} else {
			return arg;
		}
	}

	private String formatDate(Date date, Locale locale) {
		try {
			String format = messageSource.getMessage("squashtm.dateformat", null, locale);
			return new SimpleDateFormat(format).format(date);
		} catch (Exception anyException) {
			return formatNoData(locale);
		}

	}

	private String formatNoData(Locale locale) {
		return messageSource.getMessage("squashtm.nodata", null, locale);
	}

	private String formatDeleted(Locale locale) {
		return messageSource.getMessage("squashtm.itemdeleted", null, locale);
	}

	private String formatExecutionMode(TestCaseExecutionMode mode, Locale locale) {
		return messageSource.getMessage(mode.getI18nKey(), null, locale);
	}

	private String formatStatus(ExecutionStatus status, Locale locale) {
		String toReturn;

		switch (status) {
		case BLOQUED:
			toReturn = messageSource.getMessage("execution.combo.BLOQUED.label", null, locale);
			break;

		case FAILURE:
			toReturn = messageSource.getMessage("execution.combo.FAILURE.label", null, locale);
			break;

		case SUCCESS:
			toReturn = messageSource.getMessage("execution.combo.SUCCESS.label", null, locale);
			break;

		case RUNNING:
			toReturn = messageSource.getMessage("execution.combo.RUNNING.label", null, locale);
			break;

		case READY:
			toReturn = messageSource.getMessage("execution.combo.READY.label", null, locale);
			break;

		default:
			toReturn = "unknown";
			break;
		}

		return toReturn;
	}

}
