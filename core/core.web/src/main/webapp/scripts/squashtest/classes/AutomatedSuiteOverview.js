/*
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
function AutomatedSuiteOverviewDialog(settings){
	
			//------------------------public -----------------------------
	this.open = openDialog;
	this.popup = $("#execute-auto-dialog");
		
	//------------------------initialize---------------------------------
	var self = this;
	initialize();
	var automatedSuiteBaseUrl = settings.automatedSuiteBaseUrl;
		var executionRowTemplate = $("#execution-info-template .display-table-row");
		
		function initialize(){
			self.popup.bind("dialogclose", function(event, ui) {
				clearInterval(autoUpdate);
				executionAutoInfos.empty();
				$("#execution-auto-progress-bar").progressbar("value", 0);
				$("#execution-auto-progress-amount").text(0 + "/" + 0);
			});
		}
		
	//---------------------------private -----------------------------------------
		
		var autoUpdate ; 
		var suiteId ; 
		
		function openDialog(suiteView) {
			suiteId = suiteView.suiteId;
			//update progress bar values
			updateProgress(suiteView);
			//fill execution-info content
			fillContent(suiteView);
			
			self.popup.dialog('open');
			
			if (suiteView.percentage < 100) {
				autoUpdate = setInterval(function() {
					refreshContent();
				}, 2000);
			}
		}
		
		function updateProgress(suiteView) {
			var executions = suiteView.executions;
			var progress = suiteView.percentage;
			var executionTerminated = progress / 100 * executions.length
			$("#execution-auto-progress-bar").progressbar("value", progress);
			$("#execution-auto-progress-amount").text(
					executionTerminated + "/" + executions.length);
		}
		
		
		
		function fillContent(suiteView){
			var executionAutoInfos = $("#executions-auto-infos");
			
			var executions = suiteView.executions;
			
			var template = executionRowTemplate.clone()
			for (i = 0; i < executions.length; i++) {
				var execution = executions[i];
				var executionHtml = template.clone();
				
				executionHtml.attr('id', "execution-info" + execution.id);
				executionHtml.find(".executionName").html(execution.name);
				//TODO change this for the use of statusFactory
				var executionStatus = executionHtml.find(".executionStatus");
				executionStatus.html(execution.localizedStatus);
				executionStatus.addClass('executions-status-'
						+ execution.status.toLowerCase() + '-icon');
				//end TODO
				
				executionAutoInfos.append(executionHtml);
				
			}
		}
		
		function refreshContent() {
		$.ajax({
			type : 'GET',
			url : automatedSuiteBaseUrl+"/" + suiteId + "/executions",
			dataType : "json"
		}).done(
				function(suiteView) {
					//find executions in json
					var executions = suiteView.executions;
					for (i = 0; i < executions.length; i++) {
						//update info for each execution in popup
						var execution = executions[i];
						var execInfo = $("#execution-info-" + execution.id);
						//TODO change this with use of StatusFactory
						var newExecStatus = 
								executionRowTemplate.find(" .executionStatus")
								.clone();
								
						var execStatus = execInfo.find(".executionStatus");
						
						newExecStatus.text(execution.localizedStatus);
						newExecStatus.addClass('executions-status-'
								+ execution.status.toLowerCase() + '-icon');
						
						execStatus.replaceWith(newExecStatus);
						//end TODO

					}
					updateProgress(suiteView)
					if (suiteView.percentage == 100) {
						clearInterval(autoUpdate);
					}
				});
	}
		
}