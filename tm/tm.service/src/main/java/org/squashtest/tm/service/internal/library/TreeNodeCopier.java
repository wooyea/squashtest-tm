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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.library.Copiable;
import org.squashtest.tm.domain.library.Folder;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.NodeVisitor;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;
import org.squashtest.tm.service.internal.campaign.IterationTestPlanManager;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.CampaignDao;
import org.squashtest.tm.service.internal.repository.CampaignFolderDao;
import org.squashtest.tm.service.internal.repository.EntityDao;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.ItemTestPlanDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestSuiteDao;

@Component
@Scope("prototype")
public class TreeNodeCopier implements NodeVisitor {
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private RequirementFolderDao requirementFolderDao;
	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private CampaignDao campaignDao;
	@Inject
	private CampaignFolderDao campaignFolderDao;
	@Inject
	private IterationDao iterationDao;
	@Inject
	private TestSuiteDao testSuiteDao;
	@Inject
	private ItemTestPlanDao iterationTestPlanItemDao;
	@Inject
	private IterationTestPlanManager iterationTestPlanManager;
	@Inject
	private PrivateCustomFieldValueService customFieldValueManagerService;

	private Map<NodeContainer<TreeNode>, Collection<TreeNode>> nextsSourcesByDestination;
	private NodeContainer<? extends TreeNode> destination;
	private TreeNode copy;


	public TreeNode copy(TreeNode source, NodeContainer<TreeNode> destination,
			Map<NodeContainer<TreeNode>, Collection<TreeNode>> nextsSourcesByDestination) {
		this.nextsSourcesByDestination = nextsSourcesByDestination;
		this.destination = destination;
		copy = null;
		source.accept(this);
		return copy;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void visit(Folder source, FolderDao dao) {
		Folder<?> copyFolder = (Folder<?>) source.createCopy();
		persistCopy(copyFolder, dao);
		// XXX try to put this before binding the copy to the source's content so that we dont need to remove stuff from nextsSourcesByDestination afterwards
		saveNextToCopy((NodeContainer<TreeNode>) source, copyFolder);
	}

	@Override
	public void visit(Campaign source) {
		Campaign copyCampaign = source.createCopy();
		persistCopy(copyCampaign, campaignDao);
		copyCustomFields(source, copyCampaign);
		// XXX try to put this before binding the copy to the source's content so that we dont need to remove stuff from nextsSourcesByDestination afterwards
		saveNextToCopy(source, copyCampaign);
	}

	@Override
	public void visit(Iteration source) {
		Iteration copyIteration = source.createCopy();
		persitIteration(copyIteration);
		copyCustomFields(source, copyIteration);
		copyIterationTestSuites(source, copyIteration);
		// Why not doing "saveNextToCopy" ?
		// ===============================
		// Because, because the requirements for "copying an test-suite alone" and
		// "copying test-suites of a copied iteration" are different.
		// 1/ when copying a test-suite alone all test-plan item of the concerned test-suite will be added to the
		// iteration even if there were already contained by the iteration.
		// 2/ when copying an interaction, it's copied test-suite should be bound to the already copied
		// iteration-test-plan-items.
	}

	@Override
	public void visit(TestSuite source) {		
			TestSuite copyTestSuite = source.createCopy();
			persistCopy(copyTestSuite, testSuiteDao);
			copyCustomFields(source, copyTestSuite);			
			copyTestSuiteTestPlanToDestinationIteration(source, copyTestSuite);		
	}

	private void copyTestSuiteTestPlanToDestinationIteration(TestSuite source, TestSuite copy) {
		Iteration iteration = (Iteration) destination;
		List<IterationTestPlanItem> copyOfTestPlan = source.createPastableCopyOfTestPlan();
		for (IterationTestPlanItem itp : copyOfTestPlan) {
			iterationTestPlanItemDao.persist(itp);
			iteration.addTestPlan(itp);
		}
		copy.bindTestPlanItems(copyOfTestPlan);
	}



	@Override
	public void visit(Requirement source) {
		Requirement copyRequirement = source.createCopy();
		TreeMap<RequirementVersion, RequirementVersion> previousVersionsCopiesBySources = source
				.addPreviousVersionsCopiesToCopy(copyRequirement);
		persistCopy(copyRequirement, requirementDao);
		copyCustomFields(source.getCurrentVersion(), copyRequirement.getCurrentVersion());
		for (Entry<RequirementVersion, RequirementVersion> previousVersionCopyBySource : previousVersionsCopiesBySources
				.entrySet()) {
			RequirementVersion sourceVersion = previousVersionCopyBySource.getKey();
			RequirementVersion copyVersion = previousVersionCopyBySource.getValue();
			copyCustomFields(sourceVersion, copyVersion);
		}
	}

	@Override
	public void visit(TestCase source) {
		TestCase copyTestCase = source.createCopy();
		persistTestCase(copyTestCase);
		copyCustomFields(source, copyTestCase);
	}

	@Override
	public void visit(CampaignFolder campaignFolder) {
		visit(campaignFolder, campaignFolderDao);
	}

	@Override
	public void visit(RequirementFolder requirementFolder) {
		visit(requirementFolder, requirementFolderDao);
	}

	@Override
	public void visit(TestCaseFolder testCaseFolder) {
		visit(testCaseFolder, testCaseFolderDao);

	}

	/**************************************************** PRIVATE **********************************************************/

	private void copyIterationTestSuites(Iteration originalIteration, Iteration iterationCopy) {
		Map<TestSuite, List<Integer>> testSuitesPastableCopiesMap = originalIteration.createTestSuitesPastableCopy();
		for (Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry : testSuitesPastableCopiesMap.entrySet()) {
			TestSuite testSuiteCopy = testSuitePastableCopyEntry.getKey();
			iterationTestPlanManager.addTestSuite(iterationCopy, testSuiteCopy);
			bindTestPlanOfCopiedTestSuite(iterationCopy, testSuitePastableCopyEntry, testSuiteCopy);
		}
	}

	private void bindTestPlanOfCopiedTestSuite(Iteration iterationCopy,
			Entry<TestSuite, List<Integer>> testSuitePastableCopyEntry, TestSuite testSuiteCopy) {
		List<Integer> testSuiteTpiIndexesInIterationList = testSuitePastableCopyEntry.getValue();
		List<IterationTestPlanItem> testPlanItemsToBind = new ArrayList<IterationTestPlanItem>();
		List<IterationTestPlanItem> iterationTestPlanCopy = iterationCopy.getTestPlans();
		for (Integer testSuiteTpiIndexInIterationList : testSuiteTpiIndexesInIterationList) {
			IterationTestPlanItem testPlanItemToBind = iterationTestPlanCopy.get(testSuiteTpiIndexInIterationList);
			testPlanItemsToBind.add(testPlanItemToBind);
		}
		testSuiteCopy.bindTestPlanItems(testPlanItemsToBind);
	}
	
	
	/**
	 * @see PrivateCustomFieldValueService#copyCustomFieldValues(BoundEntity, BoundEntity)
	 */
	private void copyCustomFields(BoundEntity source, BoundEntity copy) {
		customFieldValueManagerService.copyCustomFieldValues(source, copy);
	}
	
	
	private void copyCustomFields(TestCase source, TestCase copy) {
		customFieldValueManagerService.copyCustomFieldValues(source, copy);
		//do the same for the steps if any
		int total=copy.getSteps().size();
		for (int i=0;i<total;i++){
			TestStep copyStep = copy.getSteps().get(i);
			TestStep sourceStep = source.getSteps().get(i);
			copyStep.accept(new TestStepCufCopier(customFieldValueManagerService, sourceStep));
		}
	}
	
	private final class TestStepCufCopier implements TestStepVisitor{
		private PrivateCustomFieldValueService customFieldValueManagerService;
		private TestStep sourceStep;

		private TestStepCufCopier(PrivateCustomFieldValueService customFieldValueManagerService, TestStep sourceStep){
			this.customFieldValueManagerService = customFieldValueManagerService;
			this.sourceStep = sourceStep;
		}

		@Override
		public void visit(ActionTestStep visited) {
			customFieldValueManagerService.copyCustomFieldValues((ActionTestStep) sourceStep, visited);
			
		}

		@Override
		public void visit(CallTestStep visited) {
			// do nothing
			
		}
		
	}
	@SuppressWarnings("unchecked")
	private void saveNextToCopy(NodeContainer<? extends TreeNode> source, NodeContainer<? extends TreeNode> copy) {
		if (source.hasContent()) {
			Set<TreeNode> sourceContent = new HashSet<TreeNode>(source.getContent()); 
			nextsSourcesByDestination.put((NodeContainer<TreeNode>) copy, sourceContent);
		}
	}

	
	@SuppressWarnings("unchecked")
	private <T extends TreeNode>void persistCopy(T copyParam, EntityDao<T> dao) {
		renameIfNeeded((Copiable) copyParam);
		dao.persist(copyParam);
		((NodeContainer<T>)destination).addContent(copyParam);
		this.copy = copyParam;
	}
	
	
	@SuppressWarnings("unchecked")
	private void persistTestCase(TestCase testCase){
		renameIfNeeded(testCase);
		testCaseDao.persistTestCaseAndSteps(testCase);
		((NodeContainer<TestCase>)destination).addContent(testCase);
		this.copy = testCase;		
	}

	@SuppressWarnings("unchecked")
	private void persitIteration(Iteration copyParam) {
		renameIfNeeded((Copiable) copyParam);
		iterationDao.persistIterationAndTestPlan(copyParam);
		((NodeContainer<Iteration>)destination).addContent(copyParam);
		this.copy = copyParam;
	}

	private <T extends Copiable> void renameIfNeeded(T copyParam) {
		if (!destination.isContentNameAvailable(copyParam.getName())) {
			String newName = LibraryUtils.generateUniqueCopyName(destination.getContentNames(), copyParam.getName());
			copyParam.setName(newName);
		}
	}
	

}
