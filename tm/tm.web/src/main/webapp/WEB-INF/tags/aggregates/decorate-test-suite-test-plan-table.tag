<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag body-content="empty" description="jqueryfies a campaign test case table" %>

<%@ attribute name="testSuite" required="true" type="java.lang.Object"  description="the base iteration url" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="executable" type="java.lang.Boolean" description="Right to execute. Default to false." %>


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>


<%-- =============== URLs and other variables ======================== --%>

<c:set var="batchRemoveButtonId" value="remove-test-suite-test-case-button" />
<c:set var="testCaseSingleRemovalPopupId" value="delete-test-suite-single-test-plan-dialog" />
<c:set var="testCaseMultipleRemovalPopupId" value="delete-test-suite-multiple-test-plan-dialog" />
<c:set var="testSuiteExecButtonsId" value="test-suite-execution-button" />


<s:url var="tableModelUrl"	value="/test-suites/{testSuiteId}/test-plan/table">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="removeTestPlansUrl" value="/test-suites/{testSuiteId}/{iterationId}/test-plan">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var ="showExecutionUrl" value="/executions"/>

<s:url var="testPlanExecutionsUrl" value="/test-suites/{testSuiteId}/{iterationId}/test-case-executions/">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>


<s:url var="updateTestPlanUrl"	value="/test-suites/{testSuiteId}/{iterationId}/test-plan/">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="assignableUsersUrl" value="/test-suites/{testSuiteId}/{iterationId}/assignable-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>


<s:url var="testSuiteExecButtonsUrl" value="/test-suites/{testSuiteId}/exec-button">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="baseIterationUrl" value="/iterations/{iterationId}">
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<%-- =============== /URLs and other variables ======================== --%>


<%-- ================== regionale and al. ============== --%>

<f:message var="statusUntestable" key="execution.execution-status.UNTESTABLE" />
<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
<f:message var="statusReady" key="execution.execution-status.READY" />
<f:message var="statusError" key="execution.execution-status.ERROR" />
<f:message var="statusWarning" key="execution.execution-status.WARNING" />

<f:message var="cannotCreateExecutionException" key="squashtm.action.exception.cannotcreateexecution.label" />
<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />


<%-- ================== /regionale and al. ============== --%>

<script type="text/javascript">
	var removeTestPlansUrl = "${removeTestPlansUrl}";
	var nonBelongingTestPlansUrl = "${nonBelongingTestPlansUrl}";
	var runnerUrl = ""; //URL used to run an execution. will be created dynamically
	
	var dryRunStart = function() {
		return $.ajax({
			url : runnerUrl,
			method : 'get',
			dataType : 'json',
			data : {
				'dry-run' : ''
			}
		});
	};
	

	function newExecutionClickHandler(){
		var url = $(this).attr('data-new-exec');
		
		$.ajax({type : 'POST', url : url, dataType : "json", data:{"mode":"manual"}})
		.success(function(id){
			document.location.href="${showExecutionUrl}/"+id;
		});
		return false; //return false to prevent navigation in page (# appears at the end of the URL)
	}
	
	function newAutoExecutionClickHandler() {
		var url = $(this).attr('data-new-exec');
		$.ajax({
			type : 'POST',
			url : url,
			data : {"mode":"auto"},
			dataType : "json"
		})
		.done(function(suiteView) {
			refreshTestPlans();
			if(suiteView.executions.length == 0){
				$.squash
				.openMessage("<f:message key='popup.title.Info' />",
						"<f:message	key='dialog.execution.auto.overview.error.none'/>");
			}else{
				squashtm.automatedSuiteOverviewDialog.open(suiteView);
			}
		});
		return false; //return false to prevent navigation in page (# appears at the end of the URL)
	}
	
	function bindMenuToExecutionShortCut(row, data){
		
		var tpId = data["entity-id"];
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";


		//if the testcase is manual
		if(data[3] === 'M'){
		
			$(".shortcut-exec",row).fgmenu({
				content : $("#shortcut-exec-man",row).html(),
				showSpeed : 0,
				width : 130
			});
	
		//if the testcase is automated		
		} else {
			$(".shortcut-exec",row).click(function(){
				$.ajax({
					type : 'POST',
					url : url,
					data : {"mode":"auto"},
					dataType : "json"
				}).done(function(suiteView){
					refreshTestPlans();
					if(suiteView.executions.length == 0){
						$.squash
						.openMessage("<f:message key='popup.title.Info' />",
								"<f:message	key='dialog.execution.auto.overview.error.none'/>");
					}else{
						squashtm.automatedSuiteOverviewDialog.open(suiteView);
					}
				});
			});
		} 
	}
	
	function launchClassicExe(tpId){
		
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";
	
		var startResumeClassic = function() {
			var url = runnerUrl;
			var data = {
				'optimized' : 'false'
			};
			var winDef = {
				name : "classicExecutionRunner",
				features : "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised"
			};
			$.open(url, data, winDef);
		};

		$.ajax({
			type : 'POST',
			url : url,
			data : {"mode":"manual"},
			dataType : "json"
		}).done(function(id){
			runnerUrl = "${showExecutionUrl}/"+id+"/runner";
			dryRunStart().done(startResumeClassic);
		});
	}
	
	function launchOptimizedExe(tpId){
		
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";

		var startResumeOptimized = function() {
			
			var url = runnerUrl;
			$('body form#start-optimized-form').remove();
			$('body').append('<form id="start-optimized-form" action="'+runnerUrl+'?optimized=true&suitemode=false" method="post" name="execute-test-case-form" target="optimized-execution-runner" class="not-displayed"> <input type="submit" value="true" name="optimized" id="start-optimized-button" /><input type="button" value="false" name="suitemode"  /></form>');
			
			$('#start-optimized-button').trigger('click');
		};
		
		$.ajax({
			type : 'POST',
			url : url,
			data : {"mode":"manual"},
			dataType : "json"
		}).done(function(id){
			runnerUrl = "${showExecutionUrl}/"+id+"/runner";
			dryRunStart().done(startResumeOptimized);
		});
	}


	<%--=========================--%>
	<%-- Refresh methods --%>
	<%--=========================--%>
	function refreshTestPlans() {
		var table = $('#test-suite-test-plans-table').squashTable();
		saveTableSelection(table, getTestPlansTableRowId);
		table.refresh();
	}
	
	
	function refreshExecButtons(){
		$('#${ testSuiteExecButtonsId }').load('${ testSuiteExecButtonsUrl }');
	}
	
	function refreshTestPlansWithoutSelection() {
		var table = $('#test-suite-test-plans-table').squashTable();
		table.refresh();
	}


	<%--=========================--%>
	<%-- Table methods  --%>
	<%--=========================--%>
	function testPlanTableDrawCallback() {
		
		var table = $("#test-plans-table").squashTable();
		
		<c:if test="${ editable }">
		enableTableDragAndDrop('test-suite-test-plans-table', getTestPlanTableRowIndex, testPlanDropHandler);
		decorateDeleteButtons($('.delete-test-suite-test-plan-button', this));
		</c:if>
		restoreTableSelection(this, getTestPlansTableRowId);
		convertExecutionStatus(this);
		
		bindToggleExpandIcon(table);
	}

	function getTestPlansTableRowId(rowData) {
		return rowData[0];	
	}
	function getTestPlanTableRowIndex(rowData){
		return rowData[1];
	}
	function isTestCaseDeleted(data){
		return (data[11]=="true");
	}
	
	function addTestSuiteTestPlanItemExecModeIcon(row, data) {
		var automationToolTips = {
				"M" : "",
				"A" : "<f:message key='label.automatedExecution' />"
		};
		var automationClass = {
				"M" : "manual",
				"A" : "automated"
		};

		var mode = data["exec-mode"];
		$(row).find("td.exec-mode")
			.text('')
			.addClass("exec-mode-" + automationClass[mode])
			.attr("title", automationToolTips[mode]);
	}			

	function testPlanTableRowCallback(row, data, displayIndex) {
		addIdtoTestPlanRow(row, data);
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getTestPlansTableRowId(data), 'delete-test-suite-test-plan-button');
		addClickHandlerToSelectHandle(row, $("#test-suite-test-plans-table"));
		addLoginListToTestPlan(row, data);
		</c:if>
		addHLinkToTestPlanName(row, data);
		addIconToTestPlanName(row, data);
		<c:if test="${executable}">
		addExecuteIconToTestPlan(row, data);
		</c:if>
		addStyleToDeletedTestCaseRows(row, data);
		addTestSuiteTestPlanItemExecModeIcon(row, data);
		return row;
	}
	
	function addIdtoTestPlanRow(nRow, aData){
		$(nRow).attr("id", "test-plan:" + getTestPlansTableRowId(aData));
	}

	function parseTestPlanIds(elements) {
		var ids = new Array();
		for(var i=0; i<elements.length; i++) {
			ids.push(parseTestPlanId(elements[i]));
		}
		return ids;
	}
	
	function parseTestPlanId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addHLinkToTestPlanName(row, data) {
		var url= 'javascript:void(0)';			
		addHLinkToCellText($( 'td:eq(4)', row ), url);
		$('td:eq(4) a', row).addClass('test-case-name-hlink');
	}
	
	function addIconToTestPlanName(row, data){
		$('td:eq(4)', row).prepend('<img src="${pageContext.servletContext.contextPath}/images/arrow_right.gif"/>');	
	}	

	function addExecuteIconToTestPlan(row, data) {
		
		var tpId = data[0];
		
		if(!isTestCaseDeleted(data)){
			$('td:eq(10)', row)
				.prepend('<input type="image" class="shortcut-exec" src="${pageContext.servletContext.contextPath}/images/execute.png"/><div id="shortcut-exec-man" style="display: none"><ul><li><a id="option1-'+tpId+'" href="#" onclick="launchClassicExe('+tpId+')"><f:message key="test-suite.execution.classic.label"/></a></li><li><a id="option2-'+tpId+'" href="#" onclick="launchOptimizedExe('+tpId+')"><f:message key="test-suite.execution.optimized.label"/></a></li></ul></div>');
		} else {
			$('td:eq(10)', row).prepend('<input type="image" class="disabled-shortcut-exec" src="${pageContext.servletContext.contextPath}/images/execute.png"/>');
			//TODO explain why this is done here and not in css file
			$('.disabled-shortcut-exec', row).css('opacity', 0.35);
		}

		bindMenuToExecutionShortCut(row, data);
	}

	function addLoginListToTestPlan(row, data){
		if (! isTestCaseDeleted(data)){
			var id = getTestPlansTableRowId(data);
			$('td:eq(8)', row).load("${assignableUsersUrl}" + "?testPlanId="+ id +"");
		}
	}

	
	function addStyleToDeletedTestCaseRows(row, data){
		if (isTestCaseDeleted(data)){
			$(row).addClass("test-case-deleted");
		}		
	}
	
	
	
	function convertExecutionStatus(dataTable){
		var factory = new squashtm.StatusFactory({
			untestable : "${statusUntestable}",
			blocked : "${statusBlocked}",
			failure : "${statusFailure}",
			success : "${statusSuccess}",
			running : "${statusRunning}",
			ready : "${statusReady}",
			error : "${statusError}",
			warning : "${statusWarning}"
		});
		
		var rows=dataTable.fnGetNodes();
		if (rows.length==0) return;
		
		$(rows).each(function(){
			var col=$("td:eq(7)", this);
			var oldContent=col.html();
			
			var newContent = factory.getHtmlFor(oldContent);	
			
			col.html(newContent);
			
		});		
	}
	
	function bindToggleExpandIcon(table){
		$('tbody td a.test-case-name-hlink', table).bind('click', function() {
			toggleExpandIcon(this);
			return false; //return false to prevent navigation in page (# appears at the end of the URL)
		});		
	}
	

	function toggleExpandIcon(testPlanHyperlink){
		
	
		var table =  $('#test-suite-test-plans-table').squashTable();
		var donnees = table.fnGetData(testPlanHyperlink.parentNode.parentNode);
		var image = $(testPlanHyperlink).parent().find("img");
		var ltr = testPlanHyperlink.parentNode.parentNode;
		
		if (!$(testPlanHyperlink).hasClass("opened"))
		{
			/* the row is closed - open it */
			var nTr = table.fnOpen(ltr, "      ", "");
			var url1 = "${testPlanExecutionsUrl}" + donnees[0];
			var jqnTr = $(nTr);
			
			var rowClass = ($(this).parent().parent().hasClass("odd")) ? "odd" : "even";
			jqnTr.addClass(rowClass);

			jqnTr.attr("style", "vertical-align:top;");
			image.attr("src", "${pageContext.servletContext.contextPath}/images/arrow_down.gif");
			

			jqnTr.load(url1, function(){				
				<c:if test="${ executable }">
				//apply the post processing on the content
				expandedRowCallback(jqnTr);
				</c:if>
			});
			
		}
		else
		{
			/* This row is already open - close it */
			table.fnClose( ltr );
			image.attr("src","${pageContext.servletContext.contextPath}/images/arrow_right.gif");
		};
		$(testPlanHyperlink).toggleClass("opened");		
	}
	
	
	
	/* ***************************** expanded line post processing *************** */

<c:if test="${ executable }">
	
	function expandedRowCallback(jqnTr) {
		initDeleteButtonsToFunctions(jqnTr);
		initNewExecButtons(jqnTr);
	};
	
	
	
	function initNewExecButtons(jqnTr){		
		var newExecButton = $('a.new-exec', jqnTr);
		newExecButton.button().on('click', newExecutionClickHandler);
		var newExecAutoButton = $('a.new-auto-exec', jqnTr);
		newExecAutoButton.button().on('click', newAutoExecutionClickHandler);		
	}
	

	function initDeleteButtonsToFunctions(jqnTr) {
		
		decorateDeleteButtons($(".delete-execution-table-button", jqnTr));
		
		var execOffset = "delete-execution-table-button-";
		
		$(".delete-execution-table-button", jqnTr)
		.click(function() {
			//console.log("delete execution #"+idExec);
			var execId = $(this).attr("id");
			var idExec = execId.substring(execOffset.length);
			var execRow = $(this).closest("tr");
			var testPlanHyperlink = $(this).closest("tr")
										   .closest("tr")
										   .prev()
										   .find("a.test-case-name-hlink");

			confirmeDeleteExecution(idExec, testPlanHyperlink, execRow);
		});

	}

	function confirmeDeleteExecution(idExec, testPlanHyperlink, execRow) {
		oneShotConfirm("<f:message key='dialog.delete-execution.title'/>",
				"<f:message key='dialog.delete-execution.message'/>",
				"<f:message key='label.Confirm'/>",
				"<f:message key='label.Cancel'/>").done(
				function() {
					$.ajax({
						'url' : "${showExecutionUrl}/" + idExec,
						type : 'DELETE',
						data : [],
						dataType : "json"
					}).done(function(data){
						refreshTable(testPlanHyperlink, execRow, data);
					});
				});
	}

	
	function refreshTable(testPlanHyperlink, execRow, data) {
		refreshTestPlans();		
		refreshTestSuiteInfos();
		refreshStatistics();
		refreshExecButtons();

	}
	
</c:if>
	
</script>

<script type="text/javascript">

$(function() {
	
	/* ************************** various event handlers ******************* */
	
	//This function checks the response and inform the user if a deletion was impossible
	function checkForbiddenDeletion(data){
		if(data=="true"){
			squashtm.notification.showInfo('${ unauthorizedDeletion }');
		}
	}
	
	
	var refreshAll = function(){
		refreshTestPlans();
		refreshStatistics();
		refreshExecButtons();		
	}
	

	var postItemRemoval(removalType){
		var selectedIds = $("#test-suite-test-plans-table").squashTable().getSelectedIds().join(',');
		var url = "${removeTestPlansUrl}/"+selectedIds+"/"+removalType;
		
		$.ajax({
			type : 'POST',
			url : url,
			dataType : 'text'
		}).success(function(data){
			checkForbiddenDeletion(data);
			refreshAll();
		});
	} 
	
	
	<%--=========================--%>
	<%-- single test-plan removal --%>
	<%--=========================--%>
	
	$("#${ testCaseSingleRemovalPopupId }").bind('dialogclose', function() {
			var answer = $("#${ testCaseSingleRemovalPopupId }").data("answer");
			if ( (answer != "delete") && (answer != "detach") ) {
				return; //should throw an exception instead. Should not happen anyway.
			}
			
			postItemRemoval(answer);
			
		});
	
	<%--=========================--%>
	<%-- multiple test-plan removal --%>
	<%--=========================--%>
	//multiple deletion
	$("#${ testCaseMultipleRemovalPopupId }").bind('dialogclose', function() {
			var answer = $("#${ testCaseMultipleRemovalPopupId }").data("answer");
			if ( (answer != "delete") && (answer != "detach") ) {
				return;
			}

			postItemRemoval(answer);
		
		});

	/* ************************** datatable settings ********************* */
	
	var tableSettings = {
			"oLanguage": {
				"sUrl": "<c:url value='/datatables/messages' />"
			},
			"sAjaxSource" : "${tableModelUrl}", 
			"fnRowCallback" : testPlanTableRowCallback,
			"fnDrawCallback" : testPlanTableDrawCallback,
			"aoColumnDefs": [
				{'bSortable': false, 'bVisible': false, 'aTargets': [0], 'mDataProp' : 'entity-id'},
				{'bSortable': false, 'sClass': 'centered ui-state-default drag-handle select-handle', 'aTargets': [1], 'mDataProp' : 'entity-index'},
				{'bSortable': false, 'aTargets': [2], 'mDataProp' : 'project-name'},
				{'bSortable': false, 'aTargets': [3], 'mDataProp' : 'exec-mode', 'sWidth': '2em', 'sClass' : "exec-mode"},
				{'bSortable': false, 'aTargets': [4], 'mDataProp' : 'reference'},
				{'bSortable': false, 'aTargets': [5], 'mDataProp' : 'tc-name'},
				{'bSortable': false, 'aTargets': [6], 'mDataProp' : 'importance'},
				{'bSortable': false, 'sWidth': '10%', 'aTargets': [7], 'mDataProp' : 'type'},
				{'bSortable': false, 'sWidth': '10%', 'sClass': 'has-status status-combo', 'aTargets': [8], 'mDataProp' : 'status'},
				{'bSortable': false, 'sWidth': '10%', 'sClass': 'assignable-combo', 'aTargets': [9], 'mDataProp' : 'last-exec-by'},
				{'bSortable': false, 'sWidth': '10%', 'aTargets': [10], 'mDataProp' : 'last-exec-on'},
				{'bSortable': false, 'bVisible': false, 'aTargets': [11], 'mDataProp' : 'is-tc-deleted'},
				{'bSortable': false, 'sWidth': '2em', 'sClass': 'centered execute-button', 'aTargets': [12], 'mDataProp' : 'empty-execute-holder'}, 
				{'bSortable': false, 'sWidth': '2em', 'sClass': 'centered delete-button', 'aTargets': [13], 'mDataProp' : 'empty-delete-holder'} 
				]
		};		
	
		var squashSettings = {
				
			enableHover : true,
			executionStatus : {
				untestable : "${statusUntestable}",
				blocked : "${statusBlocked}",
				failure : "${statusFailure}",
				success : "${statusSuccess}",
				running : "${statusRunning}",
				ready : "${statusReady}",
				error : "${statusError}",
				warning : "${statusWarning}",
			},
			confirmPopup : {
				oklabel : '<f:message key="label.Yes" />',
				cancellabel : '<f:message key="label.Cancel" />'
			}
			
		};
		
		<c:if test="${editable}">
		squashSettings.enableDnD = true;

			
		squashSettings.functions = {
			dropHandler : function(dropData){
				var itemIds = dropData.itemIds;
				var newIndex = dropData.newIndex;
				var url = "${updateTestPlanUrl}/"+itemIds.join(',')+"/position/"+newIndex;
				$.post(url,function(){
					$("#test-suite-test-plans-table").squashTable().refresh();
				});
			}
		};
		</c:if>
				
		$("#test-suite-test-plans-table").squashTable(tableSettings, squashSettings);
});

</script>