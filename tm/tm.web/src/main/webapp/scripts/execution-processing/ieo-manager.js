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

/**
 * conf : must be available as config for the main module.
 * 
 * {
 * 	completeTitle : "title for the completion message dialog",
 *  completeTestMessage : "content for the completion message dialog"
 *  completeSuiteMessage : "content for the completion of the whole suite message dialog"
 * 
 * }
 * 
 * + the rest described as in the 'state' variable below
 * 
 * comment[1] [Issue 1126] Had to use directly "refreshParent" instead of "iframe.unload(refreshParent)" beacause the latest do not work with IE 8  
 * 
 */



define(["jquery", "jquery.squash.messagedialog"], function($){
	

	
	/* this is a constructor */
	return function(settings) {

		// ***************** init function **********************

		
		this.state = $.extend({
				
				optimized : undefined,
				lastTestCase : undefined,
				testSuiteMode : undefined,
				prologue : undefined,
				
				baseStepUrl : undefined,
				nextTestCaseUrl : undefined,
				
				currentExecutionId : undefined,
				currentStepId : undefined,
				
				firstStepIndex : undefined,
				lastStepIndex : undefined,
				currentStepIndex : undefined,
				
				currentStepStatus : undefined
				
				
		}, settings);
		


		// ***************** private stuffs ****************
		
		var _updateState = $.proxy(function(newState){
			this.state = newState;
		}, this);
		
		
		var getJson = $.proxy(function(url){
			return $.get(url, null, null, "json")			
		}, this);
		

		var refreshParent = $.proxy(function(){
			window.opener.location.href = window.opener.location.href;
			if (window.opener.progressWindow) {
				window.opener.progressWindow.close();
			}
		}, this);
		
		
		var testComplete = $.proxy(function(){
			if (! this.state.testSuiteMode){
				$.squash.openMessage(settings.completeTitle, settings.completeTestMessage ).done(function() {
					refreshParent();// see "comment[1]"
					window.close();
				});
			} else if (canNavigateNextTestCase()){
				this.navigateNextTestCase();	
				refreshParent();
			}
			else{
				$.squash.openMessage(settings.completeTitle, settings.completeSuiteMessage).done(function() {
					refreshParent();// see "comment[1]"
					window.close();
	 			});				
			}
			
		}, this);
		
		var navigateLeftPanel = $.proxy(function(url){
			parent.frameleft.document.location.href = url;
		}, this);
		
		//************ public functions ****************
		
		this.fillRightFrame = function(url){
			this.rightFrame.attr('src', url);
		};
		
		this.navigateNext = function(){
			var state = this.state;
			
			if (! isLastStep()){
				var nextStep = state.currentStepIndex + 1;
				this.navigateRandom(nextStep);
			}
			else{
				testComplete();
			}
		};
		
		this.navigatePrevious = function(){
			var state = this.state;
			
			if (! isPrologue()){
				var prevStep = state.currentStepIndex - 1;
				this.navigateRandom(prevStep);
			}			
		};
		
		this.navigatePrologue = function(){
			var state = this.state;
			var url = state.baseStepUrl+"prologue?optimized=true&suitemode="+state.testSuiteMode;
			navigateLeftPanel(url);
			state.currentStepIndex = 0;
			this.control.ieoControl("navigateRandom", 0);		
		};
		
		
		this.navigateRandom = function(newStepIndex){
			var state = this.state;
			var control = this.control;
			
			if (newStepIndex === 0){
				this.navigatePrologue();
			}
			else{			
				var zeroBasedIndex = newStepIndex -1;	
				var nextUrl = state.baseStepUrl+"/"+zeroBasedIndex+"?optimized=true&suitemode="+state.testSuiteMode;
				
				getJson(nextUrl)
				.success(function(json){
					
					state.currentStepStatus = json.currentStepStatus;
					state.currentStepId = json.currentStepId;
					
					var frameLeftUrl = state.baseStepUrl+zeroBasedIndex+"?optimized=true&suitemode="+state.testSuiteMode;
					navigateLeftPanel(frameLeftUrl);	

					state.currentStepIndex = newStepIndex;
					control.ieoControl("navigateRandom", newStepIndex);		

				});				
			}
		};
		
		this.navigateNextTestCase = function(){			
			getJson(nextTestCaseUrl)
			.success(function(json){
				_updateState(json);
				this.control.ieoControl("navigateToNewTestCase");
			});
		};
		
		this.closeWindow = function(){
			window.close();
		};


		this.getState = function(){
			return this.state;
		};
		
		// ********************** predicates ************************
		
		var canNavigateNextTestCase = $.proxy(function(){
			var state = this.state;
			return ((state.testSuiteMode) &&  (! state.lastTestCase) && (this._isLastStep()));			
		}, this);
		
		var isLastStep = $.proxy(function(){
			return (this.state.currentStepIndex===this.state.lastStepIndex);			
		}, this);
		
		var isPrologue = $.proxy(function(){
			return (this.state.currentStepIndex===this.state.firstStepIndex);
		}, this);
		
		
		// *********** setters etc *********************

		
		this.setControl = function(control){
			
			var self = this;
			
			this.control = control;			
			control.ieoControl("setManager",this);
			
			var nextButton = control.ieoControl("getNextStepButton");
			var prevButton = control.ieoControl("getPreviousStepButton");
			var stopButton = control.ieoControl("getStopButton");
			var succButton = control.ieoControl("getSuccessButton");
			var failButton = control.ieoControl("getFailedButton");
			var mvTCButton = control.ieoControl("getNextTestCaseButton");
			var statusCombo = control.ieoControl("getStatusCombo");
			
			nextButton.click(function(){
				self.navigateNext();
			});
			
			prevButton.click(function(){
				self.navigatePrevious();
			});
			
			mvTCButton.click(function(){
				self.navigateNextTestCase();		
			});	
			
			stopButton.click(function(){
				window.close();
			});
			
			statusCombo.change(function(){
				var self = this;
				$.post(this.state.statusUrl, {
					executionStatus : $(self).val()
				});
			});
			
			succButton.click(function(){
				$.post(this.state.statusUrl,{
					executionStatus : "SUCCESS"
				})
				.success(function(){
					self.control.ieoControl("setSuccess");
				});
			});
			
			failButton.click(function(){
				$.post(this.state.statusUrl,{
					executionStatus : "FAILURE"
				})
				.success(function(){
					self.control.ieoControl("setFailure");
				});			
			});
		
		};
		
		this.setRightFrame = function(rightFrame){
			this.rightFrame = rightFrame;
		};
		
	}
	
});