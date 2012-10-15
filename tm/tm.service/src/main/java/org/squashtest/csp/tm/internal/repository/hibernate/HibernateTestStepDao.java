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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.List;
import java.util.ListIterator;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestStep;
import org.squashtest.csp.tm.internal.repository.TestStepDao;

@Repository
public class HibernateTestStepDao extends HibernateDao<TestStep> implements TestStepDao {
	
	/*
	 * Because of the -yet- unidirectional mapping : TestCase -- OneToMany --> TestStep, we can't remove the TestStep
	 * directly because it would violate the foreign key constraint.
	 *
	 * So we're going to locate the TestCase who contain it first, then remove the TestStep from the list.
	 */
	@Override
	public void removeById(Long testStepId) {

		TestCase testCase = findTestCaseByTestStepId(testStepId);

		if (testCase == null) {
			return;
		}

		ListIterator<TestStep> iterator = testCase.getSteps().listIterator();
		while (iterator.hasNext()) {
			TestStep ts = iterator.next();
			if (ts.getId().equals(testStepId)) {
				iterator.remove();
				break;
			}
		}

	}

	@Override
	public TestStep findById(Long testStepId) {
		return getEntity(testStepId);
	}

	@Override
	public void persist(TestStep testStep) {
		persistEntity(testStep);
		TestCase testCase = findTestCaseByTestStepId(testStep.getId());

	}


	/*
	 * Returns a TestCase if it contains a TestStep with the provided Id Returns null otherwise
	 *
	 * Note : as long as the relation between TestCase and TestStep is OneToMany, there will be either 0 either 1
	 * results, no more.
	 */
	@SuppressWarnings("unchecked")
	private TestCase findTestCaseByTestStepId(Long testStepId) {
		Session session = currentSession();

		List<TestCase> tcList = session.createCriteria(TestCase.class).createCriteria("steps")
		.add(Restrictions.eq("id", testStepId)).list();

		if (tcList.size() > 0) {
			TestCase tc = tcList.get(0);
			Hibernate.initialize(tc.getSteps());
			return tc;
		} else {
			return null;
		}
	}

	
	@Override
	public List<TestStep> findListById(final List<Long> testStepIds){
		SetQueryParametersCallback callback = new TestStepIdsQueryParametersCallback(testStepIds);
		
		return executeListNamedQuery("testStep.findOrderedListById", callback);
		
	}
	
	private static final class TestStepIdsQueryParametersCallback implements SetQueryParametersCallback  {
		
		private List<Long> testStepIds;
		private TestStepIdsQueryParametersCallback(List<Long> testStepIds) {
			this.testStepIds = testStepIds;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setParameterList("testStepIds", testStepIds, LongType.INSTANCE);
			
		}
		
	};


}
