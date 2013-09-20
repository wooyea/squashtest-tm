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
package org.squashtest.tm.service.internal.campaign;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.user.UserAccountService;

@Service("squashtest.tm.service.TestSuiteExecutionProcessingService")
@Transactional
public class TestSuiteExecutionProcessingServiceImpl implements TestSuiteExecutionProcessingService {

	private static final String CAN_EXECUTE_BY_TESTSUITE_ID = "hasPermission(#testSuiteId, 'org.squashtest.tm.domain.campaign.TestSuite', 'EXECUTE') or hasRole('ROLE_ADMIN')";

	@Inject
	private TestSuiteDao suiteDao;
	@Inject
	private CampaignNodeDeletionHandler campaignDeletionHandler;
	@Inject
	private IterationTestPlanManager testPlanManager;
	@Inject
	private UserAccountService userService;
	@Inject
	private ProjectsPermissionFinder projectsPermissionFinder;

	/**
	 * @see org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService#startResume(long, long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_TESTSUITE_ID)
	public Execution startResume(long testSuiteId) {
		Execution execution = null;
		TestSuite suite = suiteDao.findById(testSuiteId);
		String testerLogin = findUserLoginIfTester(suite.getProject().getId());
		IterationTestPlanItem item = suite.findFirstExecutableTestPlanItem(testerLogin);
		execution = findUnexecutedOrCreateExecution(item);
		if (execution == null || execution.getSteps().isEmpty()) {
			startResumeNextExecution(testSuiteId, item.getId());
		}
		return execution;
	}

	/**
	 * if has executions: will return last execution if not terminated,<br>
	 * if has no execution and is not test-case deleted : will return new execution<br>
	 * else will return null
	 * 
	 * @param executions
	 * @param testPlanItem
	 * @return
	 * 
	 */
	private Execution findUnexecutedOrCreateExecution(IterationTestPlanItem testPlanItem) {
		Execution executionToReturn = null;
		if (testPlanItem.isExecutableThroughTestSuite()) {
			executionToReturn = testPlanItem.getLatestExecution();
			if (executionToReturn == null) {
				executionToReturn = testPlanManager.addExecution(testPlanItem);
			}
		}
		return executionToReturn;
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService#restart(long, long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_TESTSUITE_ID)
	public void deleteAllExecutions(long testSuiteId) {
		// getTest plan
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		List<IterationTestPlanItem> suiteTestPlan = testSuite.getTestPlan();
		if (!suiteTestPlan.isEmpty()) {
			// delete all executions
			deleteAllExecutionsOfTestPlan(suiteTestPlan, testSuite.getProject().getId());
		}

	}

	private void deleteAllExecutionsOfTestPlan(List<IterationTestPlanItem> suiteTestPlan, long projectId) {
		String testerLogin = findUserLoginIfTester(projectId);
		for (IterationTestPlanItem iterationTestPlanItem : suiteTestPlan) {
			if ( testerLogin == null || (iterationTestPlanItem.getUser() != null && iterationTestPlanItem.getUser().getLogin().equals(testerLogin))) {
				List<Execution> executions = iterationTestPlanItem.getExecutions();
				if (!executions.isEmpty()) {
					campaignDeletionHandler.deleteExecutions(executions);
				}
			}
		}
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService#hasMoreExecutableItems(long, long)
	 */
	@Override
	public boolean hasMoreExecutableItems(long testSuiteId, long testPlanItemId) {
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		String testerLogin = findUserLoginIfTester(testSuite.getProject().getId());
		return !testSuite.isLastExecutableTestPlanItem(testPlanItemId, testerLogin);
		
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService#hasPreviousExecutableItems(long,
	 *      long)
	 */
	@Override
	public boolean hasPreviousExecutableItems(long testSuiteId, long testPlanItemId) {
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		String testerLogin = findUserLoginIfTester(testSuite.getProject().getId());
		return !testSuite.isFirstExecutableTestPlanItem(testPlanItemId, testerLogin);
	}

	/**
	 * @see org.squashtest.tm.service.campaign.TestSuiteExecutionProcessingService#startNextExecution(long, long)
	 */
	@Override
	@PreAuthorize(CAN_EXECUTE_BY_TESTSUITE_ID)
	public Execution startResumeNextExecution(long testSuiteId, long testPlanItemId) {
		Execution execution = null;
		TestSuite testSuite = suiteDao.findById(testSuiteId);
		String testerLogin = findUserLoginIfTester(testSuite.getProject().getId());
		IterationTestPlanItem item = testSuite.findNextExecutableTestPlanItem(testPlanItemId, testerLogin);
		execution = findUnexecutedOrCreateExecution(item);
		while (execution == null || execution.getSteps().isEmpty()) {
			item = testSuite.findNextExecutableTestPlanItem(testPlanItemId);
			execution = findUnexecutedOrCreateExecution(item);
		}
		return execution;
	}

	private String findUserLoginIfTester(long projectId) {
		String userLogin = userService.findCurrentUser().getLogin();
		String testerLogin = null;
		if (projectsPermissionFinder.isInPermissionGroup(userLogin, projectId,
				"squashtest.acl.group.tm.TestRunner")) {
			testerLogin = userLogin;
		}
		return testerLogin;
	}

	

}
