/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.interceptor.openedentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.execution.Execution;

/**
 * 
 * @author mpagnon
 * 
 */
public class ExecutionViewInterceptor extends ObjectViewsInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionViewInterceptor.class);

	@Override
	public void preHandle(WebRequest request) {
		// NOOP
	}

	@Override
	public void postHandle(WebRequest request, ModelMap model) {
		// check model is not null in case we are intercepting an ajax request on the page
		if (model != null) {
			Identified identified = (Identified) model.get("execution");
			if (identified != null) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Execution request  description {}", request.getDescription(true));
				}
				boolean otherViewers = super.addViewerToEntity(Execution.class.getSimpleName(), identified,
						request.getRemoteUser());
				model.addAttribute("otherViewers", otherViewers);
			}
		}
	}

	@Override
	public void afterCompletion(WebRequest request, Exception ex) {
		// NOOP
	}

}
