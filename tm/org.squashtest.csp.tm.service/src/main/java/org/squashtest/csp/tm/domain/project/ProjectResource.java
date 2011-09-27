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
package org.squashtest.csp.tm.domain.project;

import org.squashtest.csp.core.security.annotation.AclConstrainedObject;

/**
 * Any resource associated to a project.
 *
 * @author Gregory Fouquet
 *
 */
public interface ProjectResource {
	/**
	 *
	 * @return The project which this resource belongs to. Should never be <code>null</code>.
	 */
	@AclConstrainedObject
	Project getProject();

	/**
	 * Notifies this resource now belongs to the given project. {@kink ProjectResource#getProject()} should
	 * return this project afterwards.
	 *
	 * @param project
	 *            should not ne <code>null</code>
	 */
	void notifyAssociatedWithProject(Project project);

}
