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
package org.squashtest.tm.service.internal.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.customfield.NameAlreadyInUseException;
import org.squashtest.tm.service.internal.repository.TeamDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;
import org.squashtest.tm.service.user.CustomTeamModificationService;
@Service("CustomTeamModificationService")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class CustomTeamModificationServiceImpl implements CustomTeamModificationService {

	@Inject
	private TeamDao teamDao;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private ObjectAclService aclService;

	/**
	 * @see CustomTeamModificationService#persist(Team)
	 */
	@Override
	public void persist(Team team) {
		if(teamDao.findAllByName(team.getName()).isEmpty()){
		teamDao.persist(team);
		}else{
			throw new NameAlreadyInUseException("Team", team.getName());
		}
	}
	
	/**
	 * @see CustomTeamModificationService#deleteTeam(long)
	 */
	@Override
	public void deleteTeam(long teamId) {
		Team team = teamDao.findById(teamId);
		aclService.removeAllResponsibilities(teamId);
		teamDao.delete(team);
		
	}
	
	@Override
	public PagedCollectionHolder<List<Team>> findAllFiltered(PagingAndSorting paging, Filtering filtering) {
		List<Team> teams = teamDao.findSortedTeams(paging, filtering);
		long count = teamDao.count();
		return new PagingBackedPagedCollectionHolder<List<Team>>(paging, count, teams);
	}

	@Override
	public void changeName(long teamId, String name) {
		if(!teamDao.findAllByName(name).isEmpty()){			
				throw new NameAlreadyInUseException("Team", name);
		}
		Team team = teamDao.findById(teamId);
		team.setName(name);
		
	}

	@Override
	public void addMember(long teamId, String login) {
		addMembers(teamId, Arrays.asList(login));
	}
	
	@Override
	public void addMembers(long teamId, List<String> logins) {
		List<User> users = userDao.findUsersByLoginList(logins);
		Team team = teamDao.findById(teamId);
		team.addMembers(users);
		for (User user : users){
			aclService.updateDerivedPermissions(user.getId());
		}
	}
	
	@Override
	public List<User> findAllNonMemberUsers(long teamId) {
		return userDao.findAllNonTeamMembers(teamId);
	}
	
	
	@Override
	public void removeMember(long teamId, long memberId) {
		User user = userDao.findById(memberId);
		Team team = teamDao.findById(teamId);
		team.removeMember(user);
		aclService.updateDerivedPermissions(memberId);
	}
	
	@Override
	public void removeMembers(long teamId, List<Long> memberIds) {
		List<User> users = userDao.findAllByIds(memberIds);
		Team team = teamDao.findById(teamId);
		team.removeMember(users);
		for (Long id  : memberIds){
			aclService.updateDerivedPermissions(id);
		}		
	}

	@Override
	public PagedCollectionHolder<List<User>> findAllTeamMembers(long teamId, PagingAndSorting sorting, Filtering filtering) {
		List<User> teamMates = userDao.findAllTeamMembers(teamId, sorting, filtering);
		long allMates = userDao.countAllTeamMembers(teamId);
		return new PagingBackedPagedCollectionHolder<List<User>>(sorting, allMates, teamMates);
	}

	@Override
	public void removeMemberFromAllTeams(long memberId){
		User user = userDao.findById(memberId);
		List<Long> teamIds = new ArrayList<Long>();
		Set<Team> teams = user.getTeams();
		for(Team team : teams){
			teamIds.add(team.getId());
		}
		user.removeTeams(teamIds);
		
		aclService.updateDerivedPermissions(memberId);
	}
}
