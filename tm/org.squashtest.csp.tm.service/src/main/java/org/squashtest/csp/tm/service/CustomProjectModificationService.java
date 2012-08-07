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
package org.squashtest.csp.tm.service;

import java.util.List;

import org.squashtest.csp.core.security.acls.PermissionGroup;
import org.squashtest.csp.tm.domain.automatest.TestAutomationProject;
import org.squashtest.csp.tm.domain.project.AdministrableProject;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.users.User;
import org.squashtest.csp.tm.domain.users.UserProjectPermissionsBean;

public interface CustomProjectModificationService {
	Project findById(long projectId);

	void deleteProject(long projectId);

	AdministrableProject findAdministrableProjectById(long projectId);

	void addNewPermissionToProject(long userId, long projectId, String permission);

	void removeProjectPermission(long userId, long projectId);

	List<UserProjectPermissionsBean> findUserPermissionsBeansByProject(long projectId);

	List<PermissionGroup> findAllPossiblePermission();

	List<User> findUserWithoutPermissionByProject(long projectId);

	User findUserByLogin(String userLogin);

	/**
	 * WYSIWYG. Warning : the argument must be a persisted instance of {@link TestAutomationProject}
	 * 
	 * @param TMprojectId
	 * @param TAproject
	 */
	void bindTestAutomationProject(long TMprojectId, TestAutomationProject TAproject);
}
