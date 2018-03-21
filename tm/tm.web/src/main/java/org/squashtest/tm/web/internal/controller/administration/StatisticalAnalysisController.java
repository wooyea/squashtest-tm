/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.administration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.web.internal.helper.JsonHelper;

import javax.inject.Inject;

@Controller
@RequestMapping("/administration/statistical-analysis")
public class StatisticalAnalysisController {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticalAnalysisController.class);

	@Inject
	AdministrationService administrationService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView administration() {
		ModelAndView mav = new ModelAndView("page/administration/statistical-analysis");
		mav.addObject("statsHistory", JsonHelper.serialize(administrationService.findAllAdministrationStatistics()));
		return mav;
	}

}
