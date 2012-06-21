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
var squashtm = squashtm || {};
/**
 * Controller for the report panel
 * 
 * depends on : 
 * jquery
 * jquery ui
 * jquery.jeditable.js
 * jquery.jeditable.datepicker.js
 * jquery.squashtm.plugin.js
 * jquery.squashtm.linkabletree.js
 * 
 * @author Gregory Fouquet
 */
squashtm.report = (function ($) {
	var config = {
		contextPath: "",
		dateFormat: "dd/mm/yy", 
		noDateLabel: "---",
		okLabel: "OK",
		cancelLabel: "Cancel"
	};
	
	var formState = {};
	var selectedTab = false;

	var postDateFormat = $.datepicker.ATOM;
	var postNoDate = "--";
	
	function resetState() {
		formState = {};
		selectedTab = false;
	}

	function onSingleCheckboxChanged() {
		var option = this;
		var name = option.name;

		formState[name] = {
			value : option.value,
			selected : option.checked, 
			type: 'CHECKBOX'
		};
	}

	function onGroupedCheckboxesChanged() {
		var option = this;
		var name = option.name;
		var value = option.value;

		var groupState = formState[name] || [];
		formState[name] = groupState;

		var res = $(groupState).filter(function (index) {
			return this.value === value;
		});

		if (res[0]) {
			res[0].selected = this.checked;
		} else {
			groupState.push({
				value : value,
				selected : option.checked,
				type: 'CHECKBOXES_GROUP'
			});
		}
	}
	
	function onGroupedRadiosChanged() {
		var option = this;
		var name = option.name;
		var value = option.value;

		var res = $(formState[name]).each(function () {
			if (this.value === value) {
				this.selected = option.checked;
			} else {
				this.selected = false;
			}
		});
	}

	function onListItemSelected() {
		var dropdown = $(this);
		var options = dropdown.find("option");
		
		var state = $.map(options, function (item, index) {
			return { value: item.value, selected: item.selected, type: 'DROPDOWN_LIST'  };
		}); 
		
		formState[dropdown.attr('name')] = state;
	}
	
	function onTextBlurred() {
		formState[this.name] = {value: this.value, type: 'TEXT'};
	}

	function onDatepickerChanged(value) {
		var localizedDate = value;
		
		var postDate;
		
		if (config.noDateLabel === value) {
			postDate = postNoDate;
		} else {
			var date = $.datepicker.parseDate(config.dateFormat, localizedDate);
			postDate = $.datepicker.formatDate(postDateFormat, date);
		}
		
		formState[this.id] = {value: postDate, type: 'DATE'};
	}
	
	function initDropdowns(panel) {
		var dropdowns = panel.find('select');
		dropdowns.change(onListItemSelected);
		dropdowns.change();
	}
	
	function initTexts(panel) {
		var texts = panel.find("input:text");
		texts.blur(onTextBlurred);
		texts.blur();		
	}
	
	function initDatepickers(panel) {
		var dateSettings = {
			dateFormat: config.dateFormat
		};
		
		var datepickers = panel.find(".rpt-date-crit");
		datepickers.editable(function (value, settings) {
			var self = this;
			onDatepickerChanged.apply(self, [value]);
			
			return value;
		}, {
	        type      : 'datepicker',
	        tooltip   : "Click to edit...",
	        datepicker: dateSettings
		});
		
		datepickers.each(function () {
			var self = this;
			onDatepickerChanged.apply(self, [self.innerText]);
		});
	}
	
	function initRadios(panel) {
		var radios = panel.find("input:radio");
		radios.change(onGroupedRadiosChanged)
		.each(function () {
			var option = this;
			var name = option.name;
			
			formState[name] = formState[name] || [];
			formState[name].push({
				name: name,
				value : option.value,
				selected : option.checked,
				type: 'RADIO_BUTTONS_GROUP'
			});
		});
	}
	
	function initCheckboxes(panel) {
		var checkboxes = panel.find("input:checkbox");
		
		var groupedCheckboxes = checkboxes.filter(function (index) {
			var item = $(this);
			return item.data('grouped');
		});
		groupedCheckboxes.change(onGroupedCheckboxesChanged);
		groupedCheckboxes.change();
		
		var singleCheckboxes = checkboxes.filter(function (index) {
			var item = $(this);
			return !$(item).data('grouped');
		});
		singleCheckboxes.change(onSingleCheckboxChanged);
		singleCheckboxes.change();
	}
	
	function buildViewUrl(index, format) {
		return 'http://' + document.location.host + config.reportUrl + "/views/" + index + "/formats/" + format;
	}
	
	function loadTab(tab) {
		var url = buildViewUrl(tab.index, "html");	
		$.ajax({
			type: 'post', 
			url: url, 
			data: JSON.stringify(formState),  
			contentType: "application/json"
		}).done(function (html) {
			tab.panel.innerHTML = html;
		});		
	}
	
	function generateView() {
		var tabPanel = $("#view-tabed-panel");
		if (!selectedTab) {
			tabPanel.tabs('select', 0);
		} else {
			loadTab(selectedTab);
		}
		$("#view-tabed-panel:hidden").show('blind', {}, 500);
	}
	
	function onViewTabSelected(event, ui) {
		selectedTab = ui;
		var tabs = $(this);
		tabs.find(".view-format-cmb").addClass('not-displayed');
		tabs.find("#view-format-cmb-" + ui.index).removeClass('not-displayed');
		
		loadTab(ui);
	}
	
	function doExport() {
		var viewIndex = selectedTab.index;
		var format = $("#view-format-cmb-" + viewIndex).val();

		var url = buildViewUrl(viewIndex, format);	
		var data = JSON.stringify(formState).replace(/"/g, '&quot;');
		
		$.open(url, {data: data}, {name: '_blank'});
	}
 
	function initViewTabs() {
		$("#view-tabed-panel").tabs({
			selected: -1,
			select: onViewTabSelected
		});
	}
	/**
	 * Converts a NODE_TYPE into a workspace-type
	 */
	function nodeTypeToWorkspaceType(nodeType) {
		return nodeType.toLowerCase().replace(/_/g, "-");
	}
	/**
	 * Fetches the workspace type from the given jquery object pointing to a treepicker
	 */
	function getWorkspaceType(treePicker) {
		return nodeTypeToWorkspaceType(treePicker.data("node-type"));
	}
	
	function setTreeState(tree, nodes) {
		var name = tree.attr('id');
		
		formState[name] = [];
		
		if (nodes && nodes.length === 0) {
			formState[name].push({value: "", nodeType: "", type: 'TREE_PICKER'});
			return;
		} 
		
		$(nodes).each(function () {
			var node = $(this);
			formState[name].push({value: node.attr("resid"), nodeType: node.attr("restype"), type: 'TREE_PICKER'});
		});
	}

	function initTreePickerCallback() {
		var tree = $(this);
		var workspaceType = getWorkspaceType(tree);
		

		$.get(config.contextPath + "/" + workspaceType + "-browser/drives", "linkables", "json")
		.done(function (data) {
			var settings = $.extend({}, config);
			settings.workspaceType = workspaceType;
			settings.jsonData = data;
			tree.linkableTree(settings);
		});
	
		setTreeState(tree, []);
	}
	
	function onConfirmTreePickerDialog() {
		var self = $(this);
		self.dialog("close");	
		
		var tree = self.find('.rpt-tree-crit');
		var nodes = tree.jstree('get_selected');
		
		setTreeState(tree, nodes);
	}

	function onCancelTreePickerDialog() {
		$(this).dialog('close');
	}

	function initTreePickerDialogCallback() {
		var dialog = $(this);
		
		dialog.createPopup({
			height: 500,
			buttons: [{
				text: config.okLabel, 
				click: onConfirmTreePickerDialog
			}, {
				text: config.cancelLabel, 
				click: onCancelTreePickerDialog
			}]
		});	
	}

	function initTreePickers(panel, settings) {
		panel.find('.rpt-tree-crit-open').click(function () {
			console.log($(this));
			var dialogId = $(this).data('id-opened');
			console.log(dialogId);
			$("#" + dialogId).dialog('open');
		});
		
		panel.find('.rpt-tree-crit').each(initTreePickerCallback);
					
		panel.find(".rpt-tree-crit-dialog").each(initTreePickerDialogCallback);
	}

	function init(settings) {
		resetState();
		config = $.extend(config, settings);		
		
		var panel = $("#report-criteria-panel");

		initCheckboxes(panel);
		initRadios(panel);
		initDropdowns(panel);
		initTexts(panel);
		initDatepickers(panel);		
		initTreePickers(panel);
		initViewTabs();

		$('#generate-view').click(generateView);
		$('#export').click(doExport);
	}

	return {
		init : init
	};
})(jQuery);
/*
(function ($) {
	$.fn.extend({
		projectPicker: function () {
			//We initiate the popup only once, this is the flag
			var isPopupFilled = false;
			//Total number of project
			var numberOfProject = 0;
			
			function loadProjectList(){
				//Check if the popup wasn't already filled
				if(!isPopupFilled){
					$.get("${projectFilterUrl}",populateReportProjectList,"json");
					isPopupFilled = true;
				}
			}
			
			//Fill the popup with project list
			function populateReportProjectList(jsonData){
				
				var cssClass="odd";
				var i=0;
				for (i=0;i<jsonData.projectData.length;i++){
					appendProjectReportItem("report-project-list",jsonData.projectData[i], cssClass);
					cssClass=swapCssClass(cssClass);
				}
				//total number of project
				numberOfProject = jsonData.projectData.length;
						
			}
			
			//Alternate class
			function swapCssClass(cssClass){
				if (cssClass=="odd") return "even";
				return "odd";
			}
			

			//Set the html and css project list attributes
			function appendProjectReportItem(containerId, projectItemData, cssClass){
				var jqNewItem = $("#report-select-project-popup .project-report-list-template .project-item").clone();
				jqNewItem.addClass(cssClass);
				
				var jqChkBx = jqNewItem.find(".project-report-checkbox");
				jqChkBx.attr('id','project-report-checkbox-'+parseInt(projectItemData[0]));
				
				var jqName = jqNewItem.find(".project-name");
				jqName.html(projectItemData[1]);
				
				$("#"+containerId).append(jqNewItem);
			}
			
			//Get selected ids
			function getSelectedReportProjectIds(){
		 		var selectedBoxes = $("#report-project-list .project-report-checkbox:checked");
		 		var zeids = new Array();
		 		var i;
		 		
		 		for (i=0;i<selectedBoxes.length;i++){
		 			var jqBox = $(selectedBoxes[i]);
		 			zeids.push(extractProjectId(jqBox.attr('id')));
		 		}
		 		
		 		return zeids;
		 	}
			
			//Get the project id
			function extractProjectId(strDomId){
		 		var idTemplate = "project-report-checkbox-";	
		 		var templateLength = idTemplate.length;
		 		var extractedId = parseInt(strDomId.substring(templateLength));
		 		return extractedId;
		 	}
			
			function selectAllReportProjects(){
				var boxes = $("#report-project-list .project-report-checkbox");
				
				if (boxes.length==0) return;
				
				$(boxes).each(function(){
					setCheckBox($(this), true);
				});
			}
			
			function deselectAllReportProjects(){
				var boxes = $("#report-project-list .project-report-checkbox");
				
				if (boxes.length==0) return;
				
				$(boxes).each(function(){
					setCheckBox($(this), false);
				});
			}
			
			function invertAllReportProjects(){
				var boxes = $("#report-project-list .project-report-checkbox");
				
				if (boxes.length==0) return;
				
				$(boxes).each(function(){
					setCheckBox($(this), ! $(this).is(":checked"));
				});		
			}

			$("#dialog-settings-project-selectall").click(function(){
				selectAllReportProjects();
			});
			
			$("#dialog-settings-project-deselectall").click(function(){
				deselectAllReportProjects();
			});
			
			$("#dialog-settings-project-invertselect").click(function(){
				invertAllReportProjects();
			});
			
			return this;
		}
	});
})(jQuery);
*/