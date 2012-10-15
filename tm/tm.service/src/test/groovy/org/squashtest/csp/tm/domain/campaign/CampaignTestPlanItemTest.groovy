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
package org.squashtest.csp.tm.domain.campaign;

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.users.User;

import spock.lang.Specification;


class CampaignTestPlanItemTest extends Specification{
	def "should create a copy of test plan item not affected to any campaign"() {
		given:
		TestCase tc = Mock()
		Campaign c = Mock()
		User u = Mock()
		CampaignTestPlanItem item = new CampaignTestPlanItem(referencedTestCase: tc, campaign: c, user: u)

		when:
		def copy = item.createCampaignlessCopy()

		then:
		copy.referencedTestCase == tc
		copy.user == u
		copy.campaign == null
	}
}
