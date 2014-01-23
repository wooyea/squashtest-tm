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
package org.squashtest.tm.web.internal.controller.report;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.web.internal.report.ReportsRegistry;

@Controller
@RequestMapping("/report-workspace")
public class ReportWorkspaceController {
	@Inject
	private ReportsRegistry reportsRegistry;

	@RequestMapping(method = RequestMethod.GET)
	public String showReportWorkspace(Model model) {
		model.addAttribute("categories", reportsRegistry.getSortedCategories());
		model.addAttribute("reports", reportsRegistry.getSortedReportsByCategory());

		return "report-workspace.html";
	}
	
	@ModelAttribute("hilightedWorkspace")
	String getHighlightedWorkspace() {
		return "report";
	}
}
