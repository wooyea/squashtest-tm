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

package org.squashtest.csp.tm.internal.service

import org.junit.runner.RunWith;
import org.spockframework.runtime.Sputnik;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.tm.service.CustomRequirementModificationService;
import org.squashtest.csp.tm.service.RequirementModificationService;
import org.squashtest.csp.tm.domain.DuplicateNameException;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;

import spock.unitils.UnitilsSupport;

import org.unitils.dbunit.annotation.DataSet;




import javax.inject.Inject;

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class RequirementModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	RequirementModificationService modService;

	long requirementId = 10

	@DataSet("RequirementModificationServiceIT.should successfully rename a requirement.xml")
	def "should successfully rename a requirement"(){
		when :
		modService.rename(requirementId, "new req");
		def rereq = modService.findById(requirementId);


		then :
		rereq != null
		rereq.id != null
		rereq.name == "new req"

		
	}

	@DataSet("RequirementModificationServiceIT.should change requirement criticality.xml")
	def "should change requirement criticality"(){
		when:
		modService.changeCriticality (requirementId, RequirementCriticality.CRITICAL)
		def requirement = modService.findById(requirementId)

		then:
		requirement.criticality == RequirementCriticality.CRITICAL
	}

	@DataSet("RequirementModificationServiceIT.should change requirement reference.xml")
	def "should change requirement reference"(){
		given:
		def reference = "something"

		when:
		modService.changeReference(requirementId, reference)
		def requirement = modService.findById(requirementId)

		then:
		requirement.reference == reference
	}

	// Execute this one last
	@DataSet("RequirementModificationServiceIT.should fail to rename a requirement because duplicated name.xml")
	def "should fail to rename a requirement because duplicated name"(){
		when :
		modService.rename(requirementId, "req 2")
		then :
		thrown(DuplicateNameException)
	}
}
