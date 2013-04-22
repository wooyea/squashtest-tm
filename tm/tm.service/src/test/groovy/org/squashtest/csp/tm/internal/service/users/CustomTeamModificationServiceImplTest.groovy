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
package org.squashtest.csp.tm.internal.service.users

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.domain.users.Team
import org.squashtest.tm.exception.customfield.NameAlreadyInUseException
import org.squashtest.tm.service.internal.repository.TeamDao
import org.squashtest.tm.service.internal.user.CustomTeamModificationServiceImpl
import org.squashtest.tm.service.security.acls.model.ObjectAclService

import spock.lang.Specification

class CustomTeamModificationServiceImplTest extends Specification {
	
	CustomTeamModificationServiceImpl service = new CustomTeamModificationServiceImpl()
	TeamDao teamDao = Mock()
	ObjectAclService aclService = Mock()
	
	def setup(){
		service.teamDao = teamDao
		service.aclService = aclService
	}

	def "should persist a new team"(){
		given : Team team = new Team()
		team.name ="team1"
		teamDao.findAllByName(_)>> Collections.emptyList()
		
		
		when: service.persist(team)
	
		
		then : 1* teamDao.persist(team)
	}
	
	def "should not persist team because name already in use"(){
		given : Team team =  new Team()
		team.name = "team1"
		Team team2 = Mock()
		teamDao.findAllByName(_)>> [team2]
		
		when : service.persist(team)
		
		then:
		0* teamDao.persist(team)
		thrown(NameAlreadyInUseException)
	}
	
	def "should delete a team and delete acls"(){
		given : Team team = Mock()
		team.getId()>> 1L
		teamDao.findById(1L)>> team
		when: service.deleteTeam(1L)
		then :
		1* aclService.removeAllResponsibilitiesForParty(1L)
		1* teamDao.delete(team)
	}
	
}
