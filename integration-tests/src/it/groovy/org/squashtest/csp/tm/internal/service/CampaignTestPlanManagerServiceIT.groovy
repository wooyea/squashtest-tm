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
package org.squashtest.csp.tm.internal.service;

import org.springframework.transaction.annotation.Transactional;
import spock.unitils.UnitilsSupport;
import javax.inject.Inject;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.service.CampaignTestPlanManagerService;
import org.unitils.dbunit.annotation.DataSet;

@UnitilsSupport
@Transactional
class CampaignTestPlanManagerServiceIT extends DbunitServiceSpecification {
	@Inject CampaignTestPlanManagerService service

	@DataSet("CampaignTestPlanManagerServiceImplIT.should add to test plan a test case already in test plan.xml")
	def "should add to test plan a test case already in test plan"() {
		when:
		service.addTestCasesToCampaignTestPlan([100L], 10)

		then:
		notThrown(HibernateException)
	}
}
