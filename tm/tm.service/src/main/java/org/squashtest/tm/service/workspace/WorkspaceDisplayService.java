/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.service.workspace;

import org.apache.commons.collections.MultiMap;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.dto.json.JsonProject;

import java.util.Collection;
import java.util.List;

public interface WorkspaceDisplayService {

	/**
	 * Service responsible for building the workspace rootModel as a collection of {@link JsTreeNode}. Only libraries are returned, not their content
	 * @param readableProjectIds The ids of projects witch must be included
	 * @param currentUser The {@link UserDto} representing current user and somme of it's attributes
	 * @param expansionCandidates
	 * @return The list of libraries as {@link JsTreeNode}, correctly initialized to be rendered by JsTree
	 */
	Collection<JsTreeNode> findAllLibraries(List<Long> readableProjectIds, UserDto currentUser, MultiMap expansionCandidates, JsonMilestone activeMileston);

	Collection<JsonProject> findAllProjects(List<Long> readableProjectIds, UserDto currentUser);

}
