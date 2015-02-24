/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.milestone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.milestone.CustomMilestoneManager;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.UserContextService;
import org.squashtest.tm.service.user.UserAccountService;

@Service("CustomMilestoneManager")
public class CustomMilestoneManagerServiceImpl implements CustomMilestoneManager {

	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private UserContextService userContextService;

	@Inject
	private UserAccountService userService;
	
	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Override
	public void addMilestone(Milestone milestone) {
		milestoneDao.checkLabelAvailability(milestone.getLabel());
		milestone.setOwner(userService.findCurrentUser());
		milestoneDao.persist(milestone);
	}

	@Override
	public List<Milestone> findAll() {
		return milestoneDao.findAll();
	}


	@Override
	public void removeMilestones(Collection<Long> ids) {
		for (final Long id : ids) {
			Milestone milestone = milestoneDao.findById(id);
			deleteMilestoneBinding(milestone);
			deleteMilestone(milestone);
		}
	}

	private void deleteMilestoneBinding(final Milestone milestone) {
		List<GenericProject> projects = milestone.getProjects();
		for (GenericProject project : projects) {
			project.unbindMilestone(milestone);
		}
	}

	private void deleteMilestone(final Milestone milestone) {

		milestoneDao.remove(milestone);
	}

	@Override
	public Milestone findById(long milestoneId) {
		return milestoneDao.findById(milestoneId);
	}

	@Override
	public void verifyCanEditMilestone(long milestoneId) {
		if (!canEditMilestone(milestoneId)) {
			throw new IllegalAccessError("What are you doing here ?! You are not allowed. Go away");
		}
	}

	private boolean isGlobal(Milestone milestone) {
		boolean isGlobal = false;
		if (milestone.getRange().equals(MilestoneRange.GLOBAL)) {
			isGlobal = true;
		}
		return isGlobal;
	}

	private boolean isCreatedBySelf(Milestone milestone) {
		boolean isCreatedBySelf = false;
		String myName = userContextService.getUsername();
		if (myName.equals(milestone.getOwner().getLogin())) {
			isCreatedBySelf = true;
		}
		return isCreatedBySelf;
	}

	@Override
	public void verifyCanEditMilestoneRange() {
		// only admin can edit range
		if (!permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			throw new IllegalAccessError("What are you doing here ?! You are not allowed. Go away");
		}

	}

	@Override
	public boolean canEditMilestone(long milestoneId) {
		Milestone milestone = milestoneDao.findById(milestoneId);
		// admin can edit all milestones
		if (!permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			// project manager can't edit global milestone or milestone they don't own
			if (isGlobal(milestone) || !isCreatedBySelf(milestone)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Long> findAllIdsOfEditableMilestone() {
		List<Milestone> milestones = findAll();
		List<Long> ids = new ArrayList<Long>();
		for (Milestone milestone : milestones) {
			if (canEditMilestone(milestone.getId())) {
				ids.add(milestone.getId());
			}
		}
		return ids;
	}

	@Override
	public List<Milestone> findAllICanSee() {

		List<Milestone> allMilestones = findAll();
		List<Milestone> milestones = new ArrayList<Milestone>();

		if (permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			milestones.addAll(allMilestones);
		} else {
			for (Milestone milestone : allMilestones) {
				if (isGlobal(milestone) || isCreatedBySelf(milestone)|| isInAProjetICanManage(milestone)){
					milestones.add(milestone);
				}
			}
		}
		return milestones;
	}

	private boolean isInAProjetICanManage(Milestone milestone) {
		boolean isInAProjetICanManage = false;
		List<GenericProject> perimeter = milestone.getPerimeter();
		
		for (GenericProject project : perimeter){
			if (permissionEvaluationService.hasRoleOrPermissionOnObject("ADMIN", "MANAGEMENT", project)){
				isInAProjetICanManage = true;
			}
		}	
		return isInAProjetICanManage;
	}

	@Override
	public boolean isBoundToATemplate(Long milestoneId) {
		Milestone milestone = findById(milestoneId);	
		return milestone.isBoundToATemplate();
	}
}
