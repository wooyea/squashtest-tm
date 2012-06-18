/*
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
var squashtm = squashtm || {};
/**
 * Controller for the report workspace page (report-workspace.html) Depends on : - jquery - jquery.squash.buttons.js
 */
squashtm.reportWorkspace = (function ($) {
	var settings = {
		expandSidebarLabel: ">>",
		collapseSidebarLabel: "<<"
	};
	
	function setEditReportNormalState() {
		$("#contextual-content").removeClass("expanded");
	}
	
	function setEditReportExpandState() {
		$("#contextual-content").addClass("expanded");
	}
	
	function toggleEditReportState() {
		$("#contextual-content").toggleClass("expanded");
	}
	
	function findId(name) {
		var idIndex = name.lastIndexOf("-") + 1;
		return name.substring(idIndex);		
	}

	function cleanContextual() {
		var contextualList = $(".is-contextual");

		if (contextualList.length == 0) {
			return;
		}

		$('.is-contextual').each(function () {
			$(this).dialog("destroy").remove();
		});
	}
	
	function loadContextualReport(reportItem) {
		cleanContextual();
		$("#contextual-content").html('');
		
		var reportUrl = $(reportItem).data('href');
		
		$("#contextual-content").load(reportUrl);
	}

	function setCategoryFrameNormalState() {
		$("#outer-category-frame").removeClass("expanded");
		$("#toggle-expand-category-frame-button").attr("value", settings.collapseSidebarLabel);
	}

	function setCategoryFrameExpandState() {
		$("#outer-category-frame").addClass("expanded");
		$("#toggle-expand-category-frame-button").attr("value", settings.expandSidebarLabel);
	}
	
	function toggleCategoryFrameState() {
		if ($("#outer-category-frame").hasClass("expanded")) {
			setCategoryFrameNormalState();
		} else {
			setCategoryFrameExpandState();
		}
	}
	
	function toggleReportWorkspaceState() {
		toggleCategoryFrameState();
		toggleEditReportState();							
	}
	
	/**
	 * initializes the workspace.
	 * 
	 * @returns
	 */
	function init(options) {
		var defaults = settings;
		settings = $.extend(defaults, options);
		
		$(".report-category").togglePanel({});

		$("#outer-category-frame .report-item").click(function () {
			loadContextualReport(this);
		});

		$("#category-frame-button").delegate("#toggle-expand-category-frame-button", "click", function () {
			toggleReportWorkspaceState();
		});

		/* decorate buttons */
		$.squash.decorateButtons();
	}

	return {
		/**
		 * the function regarding the category panel are defined in edit-report.jsp, because the button managing it is
		 * there too.
		 * 
		 * If that's too confusing consider refractoring some day.
		 */

		setReportWorkspaceNormalState : function () {
			setCategoryFrameNormalState();
			setEditReportNormalState();
		},
		
		setReportWorkspaceExpandState : function () {
			setCategoryFrameExpandState();
			setEditReportExpandState();
		},
		
		loadContextualReport : loadContextualReport,
		init : init
	};
})(jQuery);