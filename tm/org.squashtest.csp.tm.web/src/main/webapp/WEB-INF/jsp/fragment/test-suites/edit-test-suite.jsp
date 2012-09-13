<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>

<f:message var="squashlocale" key="squashtm.locale" />

<comp:rich-jeditable-header />
<comp:datepicker-manager locale="${squashlocale}" />

<c:url var="workspaceUrl" value="/campaign-workspace/#" />
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />

<s:url var="testSuiteUrl" value="/test-suites/{testSuiteId}">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="duplicateTestSuiteUrl"
	value="/iterations/{iterationId}/duplicateTestSuite/{testSuiteId}">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testSuiteInfoUrl" value="/test-suites/{testSuiteId}/general">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testSuiteStatisticsUrl"
	value="/test-suites/{testSuiteId}/stats">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testSuiteExecButtonsUrl"
	value="/test-suites/{testSuiteId}/exec-button">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testSuiteTestPlanUrl"
	value="/test-suites/{testSuiteId}/test-plan/table">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="removeTestCaseUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/test-plan/remove">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="updateTestCaseUrl"
	value="/test-suites/{testSuiteId}/test-case/">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="assignableUsersUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/assignable-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="batchAssignableUsersUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/batch-assignable-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="assignTestCasesUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/batch-assign-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testPlanManagerUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/test-plan-manager">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="nonBelongingTestCasesUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/non-belonging-test-cases/remove">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testCaseExecutionsUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/test-case-executions/">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<c:url var="testCaseDetailsBaseUrl"
	value="/test-case-libraries/1/test-cases" />

<s:url var="confirmDeletionUrl"
	value="/iterations/{iterationId}/test-suites/delete">
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>
<s:url var="btEntityUrl" value="/bugtracker/test-suite/{id}">
	<s:param name="id" value="${testSuite.id}" />
</s:url>

<%-- ----------------------------------- Authorization ----------------------------------------------%>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="SMALL_EDIT"
	domainObject="${ testSuite }">
	<c:set var="smallEditable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE"
	domainObject="${ testSuite }">
	<c:set var="deletable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE"
	domainObject="${ testSuite }">
	<c:set var="creatable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"
	domainObject="${ testSuite }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
	domainObject="${ testSuite }">
	<c:set var="executable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>

<script type="text/javascript">
	
	/* Bind any changeable element to this handler to refresh the general informations */	
	function refreshTestSuiteInfos(){
		$('#general-informations-panel').load('${testSuiteInfoUrl}');	
	}
	
	/* display the iteration name. Used for extern calls (like from the page who will include this fragment)
	*  will refresh the general informations as well*/
	function nodeSetname(name){
		$('#test-suite-name').html(name);
		refreshTestSuiteInfos();
	}
	
	/*post a request to duplicate the test suite*/
	function duplicateTestSuite(){
		return $.ajax({
			'url' : '${duplicateTestSuiteUrl}',
			type : 'POST',
			data : [],
			dataType : 'json'
		});
	};
	
	/*duplication sucess handler*/
	function duplicateTestSuiteSuccess(idOfDuplicate){
		<c:choose>
			<%-- if we were in a sub page context. We need to navigate back to the workspace. --%>
			<c:when test="${param.isInfoPage}" >	
			$.squash.openMessage('<f:message key="test-suite.duplicate.success.title" />', '<f:message key="test-suite.duplicate.success.message" />');
			</c:when>
			<c:otherwise>
				var destination = new SquashEventObject(${testSuite.iteration.id}, "iterations");
				var duplicate = new SquashEventObject( idOfDuplicate, "test-suites");
				var source = new SquashEventObject(${testSuite.id}, "test-suites");
				var evt = new EventDuplicate(destination, duplicate, source);
				squashtm.contextualContent.fire(this, evt);
			</c:otherwise>
		</c:choose>
		
	}
	
	/* renaming success handler */
	function renameTestSuiteSuccess(data){
		nodeSetname(data.newName);
		//change the name in the tree
		updateTreeDisplayedName(data.newName);
		//change also the node name attribute
		if(typeof updateSelectedNodeName == 'function'){
			updateSelectedNodeName(data.newName);
		}
						
		$( '#rename-test-suite-dialog' ).dialog( 'close' );
	}
	
	function updateTreeDisplayedName(name){
		//update the name
		if (typeof renameSelectedNreeNode == 'function'){
			renameSelectedNreeNode(name);
		}
	}
	
	/* renaming failure handler */
	function renameTestSuiteFailure(xhr){
		$('#rename-test-suite-dialog .popup-label-error')
		.html(xhr.statusText);		
	}
	
	/* deletion success handler */
	function deleteTestSuiteSuccess(){		
		<c:choose>
		<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
		<c:when test="${param.isInfoPage}" >		
			document.location.href="${workspaceUrl}" ;
		</c:when>
		<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
		<c:otherwise>
		location.reload(true);
		</c:otherwise>
		</c:choose>		
	}
	/* deletion failure handler */
	function deleteTestSuiteFailure(xhr){
		oneShotDialog("<f:message key='popup.title.error' />", xhr.statusText);
	}
</script>

<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header">
	<div style="float: left; height: 100%;">
		<h2>
			<span><f:message key="test-suite.header.title" />&nbsp;:&nbsp;</span><a
				id="test-suite-name" href="${ testSuiteUrl }/info"><c:out
					value="${ testSuite.name }" escapeXml="true" /> </a>
		</h2>
	</div>

	<div style="clear: both;"></div>
	<c:if test="${ smallEditable }">
		<comp:popup id="rename-test-suite-dialog"
			titleKey="dialog.testsuites.rename.title" isContextual="true"
			openedBy="rename-test-suite-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.testsuites.rename.title" />
				'${ label }': function() {
					var url = "${ testSuiteUrl }";
					<jq:ajaxcall url="url" dataType="json" httpMethod="POST"
					useData="true" successHandler="renameTestSuiteSuccess">				
						<jq:params-bindings newName="#rename-test-suite-name" />
					</jq:ajaxcall>					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<script type="text/javascript">
				$( "#rename-test-suite-dialog" ).bind( "dialogopen", function(event, ui) {
					var name = $('#test-suite-name').text();
					$("#rename-test-suite-name").val(name);
					
				});
				</script>			
				<label><f:message key="dialog.rename.label" />
				</label>
				<input type="text" id="rename-test-suite-name" maxlength="255" />
				<br />
				<comp:error-message forField="name" />	
		
			</jsp:body>
		</comp:popup>
	</c:if>
</div>




<div id="test-suite-toolbar" class="toolbar-class ui-corner-all ">
	<div class="toolbar-information-panel">
		<div id="general-informations-panel">
			<comp:general-information-panel auditableEntity="${testSuite}" />
		</div>
	</div>
	<div class="toolbar-button-panel">
		<c:if test="${ executable }">
			<div id="test-suite-execution-button" style="display: inline-block;">
				<comp:test-suite-execution-button testSuiteId="${ testSuite.id }"
					statisticsEntity="${ statistics }" />
			</div>
			<c:if test="${ testSuite.iteration.project.testAutomationEnabled }">
			<comp:execute-auto-button url="${ testSuiteUrl }" testPlanTableId="test-suite-test-plans-table"/>
			</c:if>
		</c:if>
		<c:if test="${ smallEditable }">
			<input type="button"
				value="<f:message key='test-suite.button.rename.label' />"
				id="rename-test-suite-button" class="button"
				style="display: inline-block;" />
		</c:if>
		<c:if test="${ deletable }">
			<input type="button"
				value="<f:message key='test-suite.button.remove.label' />"
				id="delete-test-suite-button" class="button"
				style="display: inline-block;" />
		</c:if>
		<c:if test="${ creatable }">
			<input type="button"
				value="<f:message key='test-suite.button.duplicate.label' />"
				id="duplicate-test-suite-button" class="button"
				style="display: inline-block;" />
		</c:if>
	</div>
	<div style="clear: both;"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }"
			objectUrl="${ testSuiteUrl }" isContextual="${ ! param.isInfoPage }" />
	</c:if>
</div>
<comp:fragment-tabs />
<div class="fragment-tabs fragment-body">
	<ul>
		<li><a href="#tabs-1"><f:message key="tabs.label.information" />
		</a>
		</li>
		<li><a href="#tabs-2"><f:message key="tabs.label.test-plan" />
		</a>
		</li>
		<li><a href="#tabs-3"><f:message key="tabs.label.attachments" />
				<c:if test="${ testSuite.attachmentList.notEmpty }">
					<span class="hasAttach">!</span>
				</c:if>
		</a>
		</li>
	</ul>
	<div id="tabs-1">
		<c:if test="${ smallEditable }">
			<comp:rich-jeditable targetUrl="${ testSuiteUrl }"
				componentId="test-suite-description"
				submitCallback="refreshTestSuiteInfos" />
		</c:if>

		<comp:toggle-panel id="test-suite-description-panel"
			titleKey="generics.description.title" isContextual="true"
			open="${ not empty testSuite.description }">
			<jsp:attribute name="body">
		<div id="test-suite-description">${ testSuite.description }</div>
	</jsp:attribute>
		</comp:toggle-panel>

		<%-- ------------------ statistiques --------------------------- --%>

		<comp:toggle-panel id="test-suite-statistics-toggle-panel"
			titleKey="test-suite.statistics.panel.title" open="true"
			isContextual="true">
			<jsp:attribute name="body">
		<div id="test-suite-statistics-panel">
			<comp:test-suite-statistics-panel statisticsEntity="${ statistics }" />
		</div>
	</jsp:attribute>
		</comp:toggle-panel>
	</div>
	<div id="tabs-2" class="table-tab">
		<%-- ------------------ test plan ------------------------------ --%>



		<div class="toolbar">
			<c:if test="${ linkable }">
				<f:message var="associateLabel"
					key="label.Add" />
				<f:message var="removeLabel"
					key="label.Remove" />
				<f:message var="assignLabel"
					key="label.Assign" />
				<input id="test-case-button" type="button" value="${associateLabel}"
					class="button" />
				<input id="remove-test-suite-test-case-button" type="button"
					value="${removeLabel}" class="button" />
				<input id="assign-test-case-button" type="button"
					value="${assignLabel}" class="button" />

			</c:if>
		</div>

		<div class="table-tab-wrap">

			<aggr:decorate-test-suite-test-plan-table
				tableModelUrl="${testSuiteTestPlanUrl}"
				testPlanDetailsBaseUrl="${testCaseDetailsBaseUrl}"
				removeTestPlansUrl="${removeTestCaseUrl}"
				batchRemoveButtonId="remove-test-suite-test-case-button"
				updateTestPlanUrl="${updateTestCaseUrl}"
				assignableUsersUrl="${assignableUsersUrl}"
				nonBelongingTestPlansUrl="${nonBelongingTestCasesUrl}"
				testPlanExecutionsUrl="${testCaseExecutionsUrl}"
				editable="${ linkable }"
				testCaseMultipleRemovalPopupId="delete-test-suite-multiple-test-plan-dialog"
				testCaseSingleRemovalPopupId="delete-test-suite-single-test-plan-dialog"
				testSuiteStatisticsId="test-suite-statistics-panel"
				testSuiteStatisticsUrl="${ testSuiteStatisticsUrl }"
				testSuiteExecButtonsId="test-suite-execution-button"
				testSuiteExecButtonsUrl="${ testSuiteExecButtonsUrl }" />
			<aggr:test-suite-test-plan-table />
		</div>


		<%--------------------------- Deletion confirmation popup for Test plan section ------------------------------------%>

		<pop:popup id="delete-test-suite-multiple-test-plan-dialog"
			openedBy="remove-test-suite-test-case-button"
			titleKey="dialog.remove-testcase-testsuite-associations.title">
			<jsp:attribute name="buttons">
		<f:message var="labelDelete" key="label.Yes" />
				'${ labelDelete }' : function(){						
						$("#delete-test-suite-multiple-test-plan-dialog").data("answer","delete");
						$("#delete-test-suite-multiple-test-plan-dialog").dialog("close");
				},
				
		<f:message var="labelDetach" key="label.No" />
				'${ labelDetach }' : function(){
						$("#delete-test-suite-multiple-test-plan-dialog").data("answer","detach");
						$("#delete-test-suite-multiple-test-plan-dialog").dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
			<jsp:attribute name="body">
		<f:message var="emptyMessage"
					key="message.EmptyTableSelection" />			
		<script type="text/javascript">
				$("#delete-test-suite-multiple-test-plan-dialog").bind( "dialogopen", function(event, ui){
					var table = $( '#test-suite-test-plans-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
	
					if (ids.length == 0) {
						$.squash.openMessage("<f:message key='popup.title.error' />", "${emptyMessage}");
						$(this).dialog('close');
					}
					
				});
			</script>
		<f:message key="dialog.remove-testcase-testsuite-associations.message" />
	</jsp:attribute>
		</pop:popup>

		<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
		<pop:popup id="delete-test-suite-single-test-plan-dialog"
			openedBy="test-suite-test-plans-table .delete-test-suite-test-plan-button"
			titleKey="dialog.remove-testcase-testsuite-association.title">
			<jsp:attribute name="buttons">
		<f:message var="labelDelete" key="label.Yes" />
				'${ labelDelete }' : function(){
						$("#delete-test-suite-single-test-plan-dialog").data("answer","delete");
						$("#delete-test-suite-single-test-plan-dialog").dialog("close");
				},
				
		<f:message var="labelDetach" key="label.No" />
				'${ labelDetach }' : function(){
						$("#delete-test-suite-single-test-plan-dialog").data("answer","detach");
						$("#delete-test-suite-single-test-plan-dialog").dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
			<jsp:attribute name="body">
		<f:message key="dialog.remove-testcase-testsuite-association.message" />
	</jsp:attribute>
		</pop:popup>

		<%-- ------------------------- /Deletion confirmation popup for Test plan section --------------------------------- --%>
	</div>

	<%------------------------------ Attachments bloc ------------------------------------------- --%>
	<comp:attachment-tab tabId="tabs-3" entity="${ testSuite }"
		editable="${ executable }" />
	<%-- ---------------------deletion popup------------------------------ --%>
	<c:if test="${ deletable }">
		<script>
		var testSuiteId = ${testSuite.id};
		$(function(){
			$('#delete-test-suite-button').click(function(){
				oneShotConfirm("<f:message key='dialog.delete-test-suite.title' />", 
						"<f:message key='dialog.delete-test-suite.message' />",
						"<f:message key='label.Confirm' />",  
						"<f:message key='label.Cancel' />",
						'500px').done(function(){confirmTestSuiteDeletion().done(deleteTestSuiteSuccess).fail(deleteTestSuiteFailure)});
			});
		});
		function confirmTestSuiteDeletion(){
			return $.ajax({
				'url' : '${confirmDeletionUrl}',
				type : 'POST',
				data : {"ids[]":[testSuiteId]},
				dataType : 'json'
			});
		}
		
		</script>


	</c:if>

	<%--------------------------- Assign User popup -------------------------------------%>
	<comp:popup id="batch-assign-test-case"
		titleKey="label.AssignUser" isContextual="true"
		openedBy="assign-test-case-button" closeOnSuccess="false">

		<jsp:attribute name="buttons">
		
			<f:message var="label" key="campaign.test-plan.assign.button.label" />
			'${ label }': function() {
				var url = "${assignTestCasesUrl}";
				var table = $( '#test-suite-test-plans-table' ).dataTable();
				var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
		
				var user = $(".comboLogin").val();
			
				$.post(url, { testPlanIds: ids, userId: user}, function(){
					refreshTestPlansWithoutSelection();
					$("#batch-assign-test-case").dialog('close');
				});
				
			},			
			<pop:cancel-button />
		</jsp:attribute>
		<jsp:body>
			<f:message var="confirmMessage"
				key="message.AssignTestCaseToUser" />
			<script type="text/javascript">
				$("#batch-assign-test-case").bind( "dialogopen", function(event, ui){
					var table = $( '#test-suite-test-plans-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);

					if (ids.length > 0) {
						var comboBox = $.get("${batchAssignableUsersUrl}", false, function(){
							$("#comboBox-div").html("${confirmMessage}");
							$("#comboBox-div").append(comboBox.responseText);
							$("#comboBox-div").show();
						});
					}
					else {
						$.squash.openMessage("<f:message key='popup.title.error' />", "${emptyMessage}");
						$(this).dialog('close');
					}
					
				});
			</script>
			<div id="comboBox-div">
			</div>
		</jsp:body>
	</comp:popup>
</div>
<%------------------------------------------automated suite overview --------------------------------------------%>
<c:if test="${ testSuite.iteration.project.testAutomationEnabled }">
	<comp:automated-suite-overview-popup />
	</c:if>
	<%------------------------------------------/automated suite overview --------------------------------------------%>
<%------------------------------ bugs section -------------------------------%>
<c:if test="${testSuite.iteration.project.bugtrackerConnected }">
	<comp:issues-tab btEntityUrl="${ btEntityUrl }" />
</c:if>

<%------------------------------ /bugs section -------------------------------%>
<comp:decorate-buttons />
<script type="text/javascript">
	$(function(){

		$('#test-case-button').click(function(){
			document.location.href="${testPlanManagerUrl}" ;	
		});		
		
		$('#duplicate-test-suite-button').click(function(){
			duplicateTestSuite().done(function(json){
				duplicateTestSuiteSuccess(json);
			});
		});
	});
</script>
