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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SingleToMultiSortingAdapter;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignTestPlanItem;
import org.squashtest.tm.domain.campaign.TestPlanStatistics;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.service.campaign.IndexedCampaignTestPlanItem;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CampaignDao;

@Repository
public class HibernateCampaignDao extends HibernateEntityDao<Campaign> implements CampaignDao {

	private static final String CAMPAIGN_ID_PARAM_NAME = "campaignId";
	private static final String CONTAINER_ID = "containerId";
	private static final String PROJECT_FILTER = "projectFilter";
	private static final String REFERENCE_FILTER = "referenceFilter";
	private static final String TESTCASE_FILTER = "testcaseFilter";	
	private static final String USER_FILTER = "userFilter";
	private static final String WEIGHT_FILTER = "weightFilter";
	private static final String MODE_FILTER = "modeFilter";
	/*
	 * Because it is impossible to sort over the indices of ordered collection in a criteria query 
	 * we must then build an hql string which will let us do that. 
	 */
	private static final String HQL_INDEXED_TEST_PLAN = 
			"select index(CampaignTestPlanItem), CampaignTestPlanItem "+
			"from Campaign as Campaign inner join Campaign.testPlan as CampaignTestPlanItem "+
			"left outer join CampaignTestPlanItem.referencedTestCase as TestCase " +
			"left outer join TestCase.project as Project " + 
			"left outer join CampaignTestPlanItem.user as User "+
			"where Campaign.id = :campaignId ";
	
	private static final String HQL_INDEXED_TEST_PLAN_PROJECT_FILTER =
			"and Project.name like :projectFilter ";

	private static final String HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER =
			"and TestCase.reference like :referenceFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER =
			"and TestCase.name like :testcaseFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_USER_FILTER =
			"and CampaignTestPlanItem.user.id = :userFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER =
			"and TestCase.importance = :weightFilter ";
	
	private static final String HQL_INDEXED_TEST_PLAN_MODE_FILTER =
			"and TestCase.executionMode = :modeFilter ";
	
	@Override
	public Campaign findByIdWithInitializedIterations(long campaignId) {
		Campaign c = findById(campaignId);
		Hibernate.initialize(c.getIterations());
		return c;
	}

	@Override
	public List<CampaignTestPlanItem> findAllTestPlanByIdFiltered(final long campaignId, final PagingAndSorting filter) {

		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("campaignId", campaignId);
			}

		};
		
		return executeListNamedQuery("campaign.findTestPlanFiltered", callback, filter);

	}
	
	@Override
	public List<CampaignTestPlanItem> findTestPlan(long campaignId, PagingAndMultiSorting sorting) {
		List<Object[]> tuples = _findIndexedTestPlan(campaignId, sorting);
		return buildItems(tuples);
	}

	@Override
	public List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndMultiSorting sorting) {
		List<Object[]> tuples = _findIndexedTestPlan(campaignId, sorting);
		return buildIndexedItems(tuples);
	}

	@Override
	public List<IndexedCampaignTestPlanItem> findIndexedTestPlan(long campaignId, PagingAndSorting sorting) {
		return findIndexedTestPlan(campaignId, new SingleToMultiSortingAdapter(sorting));
	}
	
	private List<Object[]> _findIndexedTestPlan(final long campaignId, PagingAndMultiSorting sorting){
		
		StringBuilder hqlbuilder = new StringBuilder(HQL_INDEXED_TEST_PLAN);

		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper= new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);
		
		SortingUtils.addOrder(hqlbuilder, wrapper);
		
		Query query = currentSession().createQuery(hqlbuilder.toString());
		
		query.setParameter("campaignId", campaignId, LongType.INSTANCE);
		
		PagingUtils.addPaging(query, sorting);
		
		return query.list();
	}
	
	private List<Object[]> _findIndexedTestPlan(final long campaignId, PagingAndMultiSorting sorting, ColumnFiltering filtering){
		
		StringBuilder hqlbuilder = new StringBuilder(HQL_INDEXED_TEST_PLAN);
		if(filtering.hasFilter(0)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_PROJECT_FILTER);
		}
		if(filtering.hasFilter(1)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_REFERENCE_FILTER);
		}
		if(filtering.hasFilter(2)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_TESTCASE_FILTER);
		}
		if(filtering.hasFilter(3)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_USER_FILTER);
		}
		if(filtering.hasFilter(4)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_WEIGHT_FILTER);
		}
		if(filtering.hasFilter(5)){
			hqlbuilder.append(HQL_INDEXED_TEST_PLAN_MODE_FILTER);
		}
		
		// tune the sorting to make hql happy
		LevelImplementorSorter wrapper= new LevelImplementorSorter(sorting);
		wrapper.map("TestCase.importance", TestCaseImportance.class);
		
		SortingUtils.addOrder(hqlbuilder, wrapper);
		
		Query query = currentSession().createQuery(hqlbuilder.toString());
		
		query.setParameter("campaignId", campaignId, LongType.INSTANCE);
		
		if(filtering.hasFilter(0)){
			query.setParameter(PROJECT_FILTER, "%"+filtering.getFilter(0)+"%", StringType.INSTANCE);
		}
		if(filtering.hasFilter(1)){
			query.setParameter(REFERENCE_FILTER, "%"+filtering.getFilter(1)+"%", StringType.INSTANCE);
		} 
		if(filtering.hasFilter(2)){
			query.setParameter(TESTCASE_FILTER, "%"+filtering.getFilter(2)+"%", StringType.INSTANCE);
		}
		if(filtering.hasFilter(3)){
			query.setParameter(USER_FILTER, Long.parseLong(filtering.getFilter(3)), LongType.INSTANCE);
		}
		if(filtering.hasFilter(4)){
			query.setParameter(WEIGHT_FILTER, filtering.getFilter(4), StringType.INSTANCE);
		}
		if(filtering.hasFilter(5)){
			query.setParameter(MODE_FILTER, filtering.getFilter(5), StringType.INSTANCE);
		}

		PagingUtils.addPaging(query, sorting);
		
		return query.list();
	}
	
	@Override
	public long countTestPlanById(long campaignId) {
		return (Long) executeEntityNamedQuery("campaign.countTestCasesById", idParameter(campaignId));
	}

	private SetQueryParametersCallback idParameter(long campaignId) {
		return new SetIdParameter(CAMPAIGN_ID_PARAM_NAME, campaignId);
	}

	@Override
	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(folderId, nameStart);
		return executeListNamedQuery("campaign.findNamesInFolderStartingWith", newCallBack1);
	}

	@Override
	public List<String> findNamesInCampaignStartingWith(final long campaignId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(campaignId, nameStart);
		return executeListNamedQuery("campaign.findNamesInCampaignStartingWith", newCallBack1);
	}

	@Override
	public List<String> findAllNamesInCampaign(final long campaignId) {
		SetQueryParametersCallback newCallBack1 = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter(CONTAINER_ID, campaignId);
			}
		};
		return executeListNamedQuery("campaign.findAllNamesInCampaign", newCallBack1);
	}

	@Override
	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart) {
		SetQueryParametersCallback newCallBack1 = new ContainerIdNameStartParameterCallback(libraryId, nameStart);
		return executeListNamedQuery("campaign.findNamesInLibraryStartingWith", newCallBack1);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CampaignLibraryNode> findAllByNameContaining(final String tokenInName, boolean groupByProject) {
		Criteria criteria = currentSession().createCriteria(CampaignLibraryNode.class, "campaignLibraryNode")
				.createAlias("campaignLibraryNode.project", "project")
				.add(Restrictions.ilike("campaignLibraryNode.name", tokenInName, MatchMode.ANYWHERE));

		if (groupByProject) {
			criteria = criteria.addOrder(Order.asc("project.id"));
		}

		criteria = criteria.addOrder(Order.asc("campaignLibraryNode.name"));

		return criteria.list();
	}

	@Override
	public List<Execution> findAllExecutionsByCampaignId(Long campaignId) {
		SetQueryParametersCallback callback = idParameter(campaignId);
		return executeListNamedQuery("campaign.findAllExecutions", callback);
	}

	@Override
	public TestPlanStatistics findCampaignStatistics(long campaignId) {
		
			Map<String, Integer> statusMap = new HashMap<String, Integer>();

			fillStatusMapWithQueryResult(campaignId, statusMap);

			return new TestPlanStatistics(statusMap);
		}

		
	private void fillStatusMapWithQueryResult(final long campaignId, Map<String, Integer> statusMap) {
		//Add Total number of TestCases
		Integer nbTestPlans = countIterationsTestPlanItems(campaignId).intValue();
		statusMap.put(TestPlanStatistics.TOTAL_NUMBER_OF_TEST_CASE_KEY, nbTestPlans);
		
		//Add number of testCase for each ExecutionStatus
		SetQueryParametersCallback newCallBack = idParameter(campaignId);
		List<Object[]> result = executeListNamedQuery("campaign.countStatuses", newCallBack);
		for (Object[] objTab : result) {
			statusMap.put(((ExecutionStatus) objTab[0]).name(), ((Long) objTab[1]).intValue());
		}
	}

	private Long countIterationsTestPlanItems(long campaignId) {
		SetQueryParametersCallback callback = idParameter(campaignId);
		return (Long) executeEntityNamedQuery("campaign.countIterationsTestPlanItems", callback);
	}

	@Override
	public long countRunningOrDoneExecutions(long campaignId){
		return (Long) executeEntityNamedQuery("campaign.countRunningOrDoneExecutions", idParameter(campaignId));
	}

	// ******************** utils ***************************
	
	private List<CampaignTestPlanItem> buildItems(List<Object[]> tuples){
		
		List<CampaignTestPlanItem> items = new ArrayList<CampaignTestPlanItem>(tuples.size());
		
		for (Object[] tuple : tuples){
			CampaignTestPlanItem ctpi = (CampaignTestPlanItem) tuple[1];
			items.add(ctpi);
		}
		
		return items;
	}
	
	private List<IndexedCampaignTestPlanItem> buildIndexedItems(List<Object[]> tuples){
		List<IndexedCampaignTestPlanItem> indexedItems = new ArrayList<IndexedCampaignTestPlanItem>(tuples.size());
		
		for (Object[] tuple : tuples){
			Integer index = (Integer)tuple[0];
			CampaignTestPlanItem ctpi = (CampaignTestPlanItem) tuple[1];
			indexedItems.add(new IndexedCampaignTestPlanItem(index, ctpi));
		}
		
		return indexedItems;
	}

	@Override
	public List<IndexedCampaignTestPlanItem> findFilteredIndexedTestPlan(long campaignId, PagingAndMultiSorting sorting, ColumnFiltering filtering) {
		List<Object[]> tuples = _findIndexedTestPlan(campaignId, sorting, filtering);
		return buildIndexedItems(tuples);
	}

	@Override
	public long countFilteredTestPlanById(long campaignId, ColumnFiltering filtering) {
		return (Long) executeEntityNamedQuery("campaign.countTestCasesById", idParameter(campaignId));
	}
}
