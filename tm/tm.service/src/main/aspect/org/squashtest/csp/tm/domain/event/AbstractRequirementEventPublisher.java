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
package org.squashtest.csp.tm.domain.event;

import javax.inject.Inject;

import org.squashtest.csp.core.service.security.UserContextService;
import org.squashtest.csp.tm.internal.service.event.RequirementAuditor;

/**
 * Superclass for requirement event publisher aspects. Mainly offers common convenience methods
 * 
 * @author Gregory Fouquet
 * 
 */
public abstract class AbstractRequirementEventPublisher {
	@Inject
	private RequirementAuditor auditor;

	@Inject
	private UserContextService userContext;

	/**
	 * The aspect is enabled if it can publish events to an auditor.
	 * 
	 * @return <code>true</code> if the aspect is enabled.
	 */
	protected final boolean aspectIsEnabled() {
		return auditor != null;
	}

	/**
	 * 
	 * @return the current user's username, 'unknown' if there is no user context.
	 */
	protected final String currentUser() {
		if (userContext != null) {
			return userContext.getUsername();
		} else {
			return "unknown";
		}
	}

	protected final void publish(RequirementAuditEvent event) {
		auditor.notify(event);
	}
}
