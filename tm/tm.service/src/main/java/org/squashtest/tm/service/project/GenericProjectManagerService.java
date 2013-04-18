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
package org.squashtest.tm.service.project;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.project.GenericProject;

/**
 * @author Gregory Fouquet
 *
 */
@Transactional
@DynamicManager(name="squashtest.tm.service.GenericProjectManagerService", entity = GenericProject.class)
public interface GenericProjectManagerService extends CustomGenericProjectManager, GenericProjectFinder {
	public static final String ADMIN_OR_PROJECT_MANAGER = "hasPermission(#arg0, 'org.squashtest.tm.domain.project.GenericProject', 'MANAGEMENT') or hasRole('ROLE_ADMIN')";

	@PreAuthorize(ADMIN_OR_PROJECT_MANAGER)
	void changeDescription(long projectId, String newDescription);

	@PreAuthorize(ADMIN_OR_PROJECT_MANAGER)
	void changeLabel(long projectId, String newLabel);

	@PreAuthorize(ADMIN_OR_PROJECT_MANAGER)
	void changeName(long projectId, String newName);

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	void changeActive(long projectId, boolean isActive);
	
	@PreAuthorize(ADMIN_OR_PROJECT_MANAGER)
	void changeTestAutomationEnabled(long projectId, boolean isEnabled);
}
