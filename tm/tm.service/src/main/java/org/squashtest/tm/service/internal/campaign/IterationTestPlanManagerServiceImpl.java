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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.IdentifiersOrderComparator;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.campaign.CustomIterationModificationService;
import org.squashtest.tm.service.campaign.IterationTestPlanManagerService;
import org.squashtest.tm.service.foundation.collection.CollectionSorting;
import org.squashtest.tm.service.foundation.collection.FilteredCollectionHolder;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.testcase.TestCaseNodeWalker;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;

@Service("squashtest.tm.service.IterationTestPlanManagerService")
@Transactional
public class IterationTestPlanManagerServiceImpl implements IterationTestPlanManagerService {

	/**
	 * 
	 */
	private static final String OR_HAS_ROLE_ADMIN = "or hasRole('ROLE_ADMIN')";

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private IterationTestPlanDao iterationTestPlanDao;

	@Inject
	private ObjectAclService aclService;

	@Inject
	private UserDao userDao;

	@Inject
	private DatasetDao datasetDao;

	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	@Qualifier("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	private ObjectIdentityRetrievalStrategy objIdRetrievalStrategy;

	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();

	}

	/*
	 * security note here : well what if we add test cases for which the user have no permissions on ? think of
	 * something better.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.service.IterationTestPlanManagerService#addTestCasesToIteration(java.util.List, long)
	 */

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void addTestCasesToIteration(final List<Long> objectsIds, long iterationId) {

		Iteration iteration = iterationDao.findById(iterationId);

		addTestPlanItemsToIteration(objectsIds, iteration);
	}

	@Override
	@PreAuthorize("hasPermission(#iteration, 'LINK') " + OR_HAS_ROLE_ADMIN)
	public List<IterationTestPlanItem> addTestPlanItemsToIteration(final List<Long> testNodesIds, Iteration iteration) {

		// nodes are returned unsorted
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(testNodesIds);

		// now we resort them according to the order in which the testcaseids were given
		IdentifiersOrderComparator comparator = new IdentifiersOrderComparator(testNodesIds);
		Collections.sort(nodes, comparator);

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);

		List<IterationTestPlanItem> testPlan = new LinkedList<IterationTestPlanItem>();

		for (TestCase testCase : testCases) {
			addTestCaseToTestPlan(testCase, testPlan);
		}
		addTestPlanToIteration(testPlan, iteration.getId());
		return testPlan;
	}

	private void addTestCaseToTestPlan(TestCase testCase, List<IterationTestPlanItem> testPlan) {
		List<Dataset> datasets = datasetDao.findAllDatasetsByTestCase(testCase.getId());

		if (datasets != null && !datasets.isEmpty()) {
			testPlan.addAll(IterationTestPlanItem.createTestPlanItems(testCase, datasets));
		} else {
			// TODO somewhat useless, above "if" branch could handle both cases
			testPlan.add(IterationTestPlanItem.createUnparameterizedTestPlanItem(testCase));
		}
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void addTestPlanToIteration(List<IterationTestPlanItem> testPlan, long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		for (IterationTestPlanItem itp : testPlan) {
			iterationTestPlanDao.persist(itp);
			iteration.addTestPlan(itp);
		}
	}

	

	/**
	 * @see CustomIterationModificationService#changeTestPlanPosition(long, int, List)
	 */
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public void changeTestPlanPosition(long iterationId, int newPosition, List<Long> itemIds) {
		Iteration iteration = iterationDao.findById(iterationId);
		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByIds(itemIds);

		iteration.moveTestPlans(newPosition, items);
	}
	
	
	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public boolean removeTestPlansFromIteration(List<Long> testPlanIds, long iterationId) {
		boolean unauthorizedDeletion = false;
		Iteration it = iterationDao.findById(iterationId);

		unauthorizedDeletion = removeTestPlansFromIterationObj(testPlanIds, it);

		return unauthorizedDeletion;
	}

	@Override
	@PreAuthorize("hasPermission(#iteration, 'LINK') " + OR_HAS_ROLE_ADMIN)
	public boolean removeTestPlansFromIterationObj(List<Long> testPlanIds, Iteration iteration) {
		boolean unauthorizedDeletion = false;

		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByIds(testPlanIds);

		for (IterationTestPlanItem item : items) {
			// We do not allow deletion if there are execution
			if (item.getExecutions().isEmpty()) {
				iteration.removeItemFromTestPlan(item);
				iterationTestPlanDao.remove(item);
			} else {
				unauthorizedDeletion = true;
			}
		}

		return unauthorizedDeletion;
	}

	@Override
	@PreAuthorize("hasPermission(#testPlanItemId, 'org.squashtest.tm.domain.campaign.IterationTestPlanItem', 'LINK') "
			+ OR_HAS_ROLE_ADMIN)
	public boolean removeTestPlanFromIteration(long testPlanItemId) {
		boolean unauthorizedDeletion = false;
		IterationTestPlanItem item = iterationTestPlanDao.findById(testPlanItemId);
		Iteration it = item.getIteration();

		// We do not allow deletion if there are execution
		if (item.getExecutions().isEmpty()) {
			it.removeItemFromTestPlan(item);
			iterationTestPlanDao.remove(item);
		} else {
			unauthorizedDeletion = true;
		}

		return unauthorizedDeletion;

	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public List<TestCase> findPlannedTestCases(Long iterationId) {
		Iteration iteration = iterationDao.findById(iterationId);
		return iteration.getPlannedTestCase();
	}

	// FIXME : security
	@Override
	@Deprecated
	public void updateTestCaseLastExecutedByAndOn(IterationTestPlanItem givenTestPlan, Date lastExecutedOn,
			String lastExecutedBy) {
		givenTestPlan.setLastExecutedBy(lastExecutedBy);
		givenTestPlan.setLastExecutedOn(lastExecutedOn);
		givenTestPlan.setUser(userDao.findUserByLogin(lastExecutedBy));

	}

	@Override
	// FIXME : security. Note that the user should either have the right to execute executions, has role admin, or has
	// role ta_server
	public void updateExecutionMetadata(IterationTestPlanItem item) {
		Execution execution = item.getLatestExecution();
		if (execution != null) {
			item.setLastExecutedBy(execution.getLastExecutedBy());
			item.setLastExecutedOn(execution.getLastExecutedOn());
			item.setUser(userDao.findUserByLogin(execution.getLastExecutedBy()));
		}
	}

	@Override
	@PreAuthorize("hasPermission(#iterationId, 'org.squashtest.tm.domain.campaign.Iteration', 'READ') "
			+ OR_HAS_ROLE_ADMIN)
	public FilteredCollectionHolder<List<IterationTestPlanItem>> findTestPlan(long iterationId, CollectionSorting filter) {
		List<IterationTestPlanItem> testPlan = iterationDao.findTestPlan(iterationId, filter);
		long count = iterationDao.countTestPlans(iterationId);
		return new FilteredCollectionHolder<List<IterationTestPlanItem>>(count, testPlan);
	}

	@Override
	public List<User> findAssignableUserForTestPlan(long iterationId) {

		Iteration iteration = iterationDao.findById(iterationId);

		List<ObjectIdentity> entityRefs = new ArrayList<ObjectIdentity>();

		ObjectIdentity oid = objIdRetrievalStrategy.getObjectIdentity(iteration);
		entityRefs.add(oid);

		List<String> loginList = aclService.findUsersWithExecutePermission(entityRefs);
		List<User> usersList = userDao.findUsersByLoginList(loginList);

		return usersList;

	}

	/**
	 * 
	 * @see org.squashtest.tm.service.campaign.IterationTestPlanManagerService#assignUserToTestPlanItem(long, long)
	 */
	// FIXME : security execute
	@Override
	public void assignUserToTestPlanItem(long testPlanItemId, long userId) {
		User user = (userId == 0) ? null : userDao.findById(userId);

		IterationTestPlanItem itp = iterationTestPlanDao.findTestPlanItem(testPlanItemId);
		if (!itp.isTestCaseDeleted()) {
			itp.setUser(user);
		}
	}

	/**
	 * 
	 * @see org.squashtest.tm.service.campaign.IterationTestPlanManagerService#assignUserToTestPlanItems(java.util.List,
	 *      long)
	 */
	// FIXME : security execute
	@Override
	public void assignUserToTestPlanItems(List<Long> testPlanIds, long userId) {
		List<IterationTestPlanItem> items = iterationTestPlanDao.findAllByIds(testPlanIds);

		User user = (userId == 0) ? null : userDao.findById(userId);

		for (IterationTestPlanItem item : items) {
			if (!item.isTestCaseDeleted()) {
				item.setUser(user);
			}
		}
	}

	/**
	 * @deprecated method used only in Integration tests should be removed
	 * @param iterationId
	 * @param testCaseId
	 * @return
	 */
	@Deprecated
	@Override
	public IterationTestPlanItem findTestPlanItemByTestCaseId(long iterationId, long testCaseId) {
		Iteration iteration = iterationDao.findById(iterationId);
		for (IterationTestPlanItem item : iteration.getTestPlans()) {
			if (!item.isTestCaseDeleted() && item.getReferencedTestCase().getId() == testCaseId) {
				return item;
			}
		}
		return null;
	}

	// FIXME : security
	@Override
	public IterationTestPlanItem findTestPlanItem(long itemTestPlanId) {
		return iterationTestPlanDao.findTestPlanItem(itemTestPlanId);
	}

	@Override
	public List<ExecutionStatus> getExecutionStatusList() {

		List<ExecutionStatus> statusList = new LinkedList<ExecutionStatus>();
		statusList.addAll(ExecutionStatus.getCanonicalStatusSet());
		return statusList;
	}

	@Override
	public void assignExecutionStatusToTestPlanItem(long iterationTestPlanItemId, String statusName) {

		IterationTestPlanItem testPlanItem = findTestPlanItem(iterationTestPlanItemId);
		testPlanItem.setExecutionStatus(ExecutionStatus.valueOf(statusName));
	}

	/**
	 * Creates a fragment of test plan, containing either :
	 * <ul>
	 * <li>a unique item when the test case is not parameterized</li>
	 * <li>one item per dataset when the test case is parameterized</li>
	 * </ul>
	 * 
	 * <strong>Note :</strong> The returned test plan fragment is in a transient state.
	 * 
	 * @param referenced
	 * @param assignee
	 * @return
	 */
	private Collection<IterationTestPlanItem> createTestPlanFragment(TestCase testCase) {
		List<IterationTestPlanItem> fragment = new ArrayList<IterationTestPlanItem>();
		addTestCaseToTestPlan(testCase, fragment);
		return fragment;
	}

	/**
	 * @see org.squashtest.tm.service.campaign.IterationTestPlanManagerService#createTestPlanFragment(org.squashtest.tm.domain.testcase.TestCase,
	 *      org.squashtest.tm.domain.users.User)
	 */
	@Override
	public Collection<IterationTestPlanItem> createTestPlanFragment(TestCase testCase, User assignee) {
		Collection<IterationTestPlanItem> fragment = createTestPlanFragment(testCase);

		for (IterationTestPlanItem item : fragment) {
			item.setUser(assignee);
		}

		return fragment;
	}
}
