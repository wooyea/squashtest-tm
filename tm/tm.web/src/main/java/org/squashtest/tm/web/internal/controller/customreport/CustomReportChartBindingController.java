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
package org.squashtest.tm.web.internal.controller.customreport;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportChartBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.web.internal.model.builder.JsonCustomReportChartBindingBuilder;
import org.squashtest.tm.web.internal.model.json.FormCustomReportChartBinding;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportChartBinding;
import org.squashtest.tm.web.internal.model.json.JsonCustomReportGridElement;


@Controller
public class CustomReportChartBindingController {

	@Inject
	CustomReportLibraryNodeService crlnservice;

	@Inject
	CustomReportDashboardService dashboardService;

	@Inject
	@Named("customReport.chartBindingBuilder")
	private Provider<JsonCustomReportChartBindingBuilder> builderProvider;

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value="/custom-report-chart-binding", method=RequestMethod.POST)
	public @ResponseBody JsonCustomReportChartBinding createCustomReportChartBinding(@RequestBody FormCustomReportChartBinding formChartBinding){
		CustomReportChartBinding crcb = formChartBinding.convertToEntity();

		//setting the nested entities.
		ChartDefinition chartDefinition = crlnservice.findChartDefinitionByNodeId(formChartBinding.getChartNodeId());
		CustomReportDashboard dashboard = crlnservice.findCustomReportDashboardById(formChartBinding.getDashboardNodeId());
		crcb.setChart(chartDefinition);
		crcb.setDashboard(dashboard);

		//do binding and return.
		dashboardService.bindChart(crcb);
		return  builderProvider.get().build(crcb);
	}

	@RequestMapping(value="/custom-report-chart-binding-replace-chart/{bindingId}/{chartNodeId}", method=RequestMethod.POST)
	public @ResponseBody JsonCustomReportChartBinding changeBindedChart(@PathVariable long bindingId,@PathVariable long chartNodeId){
		CustomReportChartBinding crcb = dashboardService.changeBindedChart(bindingId,chartNodeId);
		return  builderProvider.get().build(crcb);
	}

	@RequestMapping(value="/custom-report-chart-binding", method=RequestMethod.PUT)
	public @ResponseBody void updateGrid(@RequestBody JsonCustomReportGridElement[] gridElements){
		List<CustomReportChartBinding> bindings = new ArrayList<>();
		for (JsonCustomReportGridElement gridElement : gridElements) {
			bindings.add(gridElement.convertToEntity());
		}
		dashboardService.updateGridPosition(bindings);
	}

	@RequestMapping(value="/custom-report-chart-binding/{id}", method=RequestMethod.DELETE)
	public @ResponseBody void unbindChart(@PathVariable long id){
		dashboardService.unbindChart(id);
	}
}
