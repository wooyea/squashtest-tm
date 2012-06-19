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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BTIssue;
import org.squashtest.csp.core.bugtracker.domain.BTProject;
import org.squashtest.csp.core.bugtracker.domain.Priority;
import org.squashtest.csp.core.bugtracker.service.BugTrackerService;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.csp.tm.domain.bugtracker.BugTrackerStatus;
import org.squashtest.csp.tm.domain.bugtracker.Bugged;
import org.squashtest.csp.tm.domain.bugtracker.Issue;
import org.squashtest.csp.tm.domain.bugtracker.IssueList;
import org.squashtest.csp.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.IssueDao;
import org.squashtest.csp.tm.service.BugTrackerLocalService;

@Service("squashtest.tm.service.BugTrackerLocalService")
@Transactional
public class BugTrackerLocalServiceImpl implements BugTrackerLocalService {

	@Inject
	private IssueDao issueDao;

	@Inject
	private BugTrackerService remoteBugTrackerService;

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")
	public <X extends Bugged> X findBuggedEntity(Long entityId, Class<X> entityClass) {
		return issueDao.findBuggedEntity(entityId, entityClass);
	}

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor() {
		return remoteBugTrackerService.getInterfaceDescriptor();
	}

	@Override
	public BugTrackerStatus checkBugTrackerStatus() {
		BugTrackerStatus status;

		if (!remoteBugTrackerService.isBugTrackerDefined()) {
			status = BugTrackerStatus.BUGTRACKER_UNDEFINED;
		} else if (remoteBugTrackerService.isCredentialsNeeded()) {
			status = BugTrackerStatus.BUGTRACKER_NEEDS_CREDENTIALS;
		} else {
			status = BugTrackerStatus.BUGTRACKER_READY;
		}
		return status;
	}

	@Override
	@PreAuthorize("hasPermission(#entity, 'EXECUTE') or hasRole('ROLE_ADMIN')")
	public BTIssue createIssue(Bugged entity, BTIssue issue) {

		String btName = remoteBugTrackerService.getBugTrackerName();
		issue.setBugtracker(btName);
		
		BTIssue createdIssue = remoteBugTrackerService.createIssue(issue);
		createdIssue.setBugtracker(btName);

		// if success we set the bug in Squash TM database
		// a success being : we reach this code with no exceptions

		Issue sqIssue = new Issue();
		sqIssue.setRemoteIssueId(createdIssue.getId());
		sqIssue.setBugtrackerName(btName);

		IssueList list = entity.getIssueList();

		list.addIssue(sqIssue);

		issueDao.persist(sqIssue);

		return createdIssue;
	}

	

	@Override
	public BTIssue getIssue(String issueKey) {
		return remoteBugTrackerService.getIssue(issueKey);
	}
	
	@Override
	public List<BTIssue> getIssues(List<String> issueKeyList) {
		return remoteBugTrackerService.getIssues(issueKeyList);
	}

	@Override
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<Issue>>> findSquashIssues(Bugged buggedEntity,
			CollectionSorting sorter) {

		String btName = remoteBugTrackerService.getBugTrackerName();
		
		List<Long> issueListIds = buggedEntity.getAllIssueListId();

		List<IssueOwnership<Issue>> pairedIssues = issueDao.findIssuesWithOwner(buggedEntity, sorter, btName);

		Integer totalIssues = issueDao.countIssuesfromIssueList(issueListIds, btName);

		FilteredCollectionHolder<List<IssueOwnership<Issue>>> result = new FilteredCollectionHolder<List<IssueOwnership<Issue>>>(
				totalIssues, pairedIssues);

		return result;

	}

	// TODO
	/*
	 * refactor that code, it's too hard to understand (and maintain)
	 */

	@Override
	@PreAuthorize("hasPermission(#buggedEntity, 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> findBugTrackerIssues(Bugged buggedEntity,
			CollectionSorting sorter) {

		FilteredCollectionHolder<List<IssueOwnership<Issue>>> filteredIssues = findSquashIssues(buggedEntity, sorter);

		// collect the ids of the bugs
		List<String> issuesIds = new LinkedList<String>();

		for (IssueOwnership<Issue> ownership : filteredIssues.getFilteredCollection()) {
			issuesIds.add(ownership.getIssue().getRemoteIssueId());
		}

		// get them
		List<BTIssue> remoteIssues = getIssues(issuesIds);

		// now return a new FilteredCollectionHolder containing the BTIssues paired with their owner 
		List<IssueOwnership<BTIssue>> btOwnership = buildSortedBTIssueOwnership(remoteIssues,
				filteredIssues.getFilteredCollection());

		FilteredCollectionHolder<List<IssueOwnership<BTIssue>>> toReturn = new FilteredCollectionHolder<List<IssueOwnership<BTIssue>>>(
				filteredIssues.getUnfilteredResultCount(), btOwnership);

		return toReturn;

	}

	/* ************** delegate methods ************* */

	@Override
	public BTProject findRemoteProject(String name) {
		return remoteBugTrackerService.findProject(name);

	}

	@Override
	public List<Priority> getRemotePriorities() {
		return remoteBugTrackerService.getPriorities();

	}

	@Override
	public void setCredentials(String username, String password) {
		remoteBugTrackerService.setCredentials(username, password);
	}

	@Override
	public URL getIssueUrl(String btIssueId) {
		return remoteBugTrackerService.getViewIssueUrl(btIssueId);
	}

	/* ******************* private stuffs ****************************** */

	private List<IssueOwnership<BTIssue>> buildSortedBTIssueOwnership(List<BTIssue> remoteIssues,
			List<IssueOwnership<Issue>> localIssues) {

		List<IssueOwnership<BTIssue>> remoteOwnerships = new LinkedList<IssueOwnership<BTIssue>>();
		
		for (IssueOwnership<Issue> localOwner : localIssues){
			
			ListIterator<BTIssue> remoteIterator = remoteIssues.listIterator();
			
			while (remoteIterator.hasNext()){
				
				BTIssue remoteIssue = remoteIterator.next();
				if (remoteIssue.getId().equals(localOwner.getIssue().getRemoteIssueId())){
					IssueOwnership<BTIssue> remoteOwnership = new IssueOwnership<BTIssue>(remoteIssue, localOwner.getOwner());
					remoteOwnerships.add(remoteOwnership);
					remoteIterator.remove();
					break;
				}				
			}
			
		}

		return remoteOwnerships;

	}

	@Override
	public URL getBugtrackerUrl() {
		return remoteBugTrackerService.getBugTrackerUrl();
	}
	
	@Override
	public Boolean getBugtrackerIframeFriendly() {
		return new Boolean(remoteBugTrackerService.isIframeFriendly());
	}

}
