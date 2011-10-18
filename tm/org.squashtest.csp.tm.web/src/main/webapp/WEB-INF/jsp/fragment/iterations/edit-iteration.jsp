<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<f:message var="squashlocale" key="squashtm.locale" />
<comp:decorate-toggle-panels />

<comp:rich-jeditable-header />
<comp:datepicker-manager locale="${squashlocale}"/>

<jq:execution-status-factory/> 



<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />

<s:url var="iterationUrl" value="/iterations/{iterId}">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="iterationInfoUrl" value="/iterations/{iterId}/general">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="iterationPlanningUrl" value="/iterations/{iterId}/planning">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="iterationTestPlanUrl" value="/iterations/{iterId}/test-plan">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="testPlanManagerUrl" value="/iterations/{iterId}/test-plan-manager">
		<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="testCasesUrl" value="/iterations/{iterId}/test-plan" >
		<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="nonBelongingTestCasesUrl" value="/iterations/{iterId}/non-belonging-test-cases" >
		<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="assignableUsersUrl" value="/iterations/{iterId}/assignable-user" >
		<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="batchAssignableUsersUrl" value="/iterations/{iterId}/batch-assignable-user" >
		<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="assignTestCasesUrl" value="/iterations/{iterId}/batch-assign-user" >
		<s:param name="iterId" value="${iteration.id}" />
</s:url>

<c:url var="testCaseDetailsBaseUrl" value="/test-case-libraries/1/test-cases" />

<c:url var="workspaceUrl" value="/campaign-workspace/#" />



<s:url var="testCaseExecutionsUrl" value="/iterations/{iterId}/test-case-executions/" >
	<s:param name="iterId" value="${iteration.id}"/>
</s:url>
<s:url var="updateTestCaseUrl" value="/iterations/{iterId}/test-case/">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>


<s:url var="simulateDeletionUrl" value="/campaign-browser/delete-iterations/simulate" />
<s:url var="confirmDeletionUrl" value="/campaign-browser/delete-iterations/confirm" />




<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ iteration }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>

<script type="text/javascript">

	/* Bind any changeable element to this handler to refresh the general informations */	
	function refreshIterationInfos(){
		$('#general-informations-panel').load('${iterationInfoUrl}');	
	}
	
	/* display the iteration name. Used for extern calls (like from the page who will include this fragment)
	*  will refresh the general informations as well*/
	function nodeSetname(name){
		$('#iteration-name').html(name);		
		refreshIterationInfos();
	}

	/* renaming success handler */
	function renameIterationSuccess(data){
		nodeSetname(data.newName);
		//change the name in the tree
		updateTreeDisplayedName(data.newName);
		//change also the node name attribute
		if(typeof updateSelectedNodeName == 'function'){
			updateSelectedNodeName(data.newName);
		}
						
		$( '#rename-iteration-dialog' ).dialog( 'close' );
	}
	
	function updateTreeDisplayedName(name){
		//compose name
		if (typeof getSelectedNodeIndex == 'function'){
			name = getSelectedNodeIndex() + " - " + name;
		}
		//update the name
		if (typeof renameSelectedNreeNode == 'function'){
			renameSelectedNreeNode(name);
		}
	}
	
	/* renaming failure handler */
	function renameIterationFailure(xhr){
		$('#rename-iteration-dialog .popup-label-error')
		.html(xhr.statusText);		
	}
	
	/* deletion success handler */
	function deleteIterationSuccess(){		
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
	function deleteIterationFailure(xhr){
		alert(xhr.statusText);		
	}


</script>



<div class="ui-widget-header ui-state-default ui-corner-all fragment-header">
	<div style="float:left;height:100%;">	
		<h2>
			<span><f:message key="iteration.header.title" />&nbsp;:&nbsp;</span><a id="iteration-name" href="${ iterationUrl }/info"><c:out value="${ iteration.name }" escapeXml="true"/></a>
		</h2>
	</div>
	
	<div style="clear:both;"></div>	
	<c:if test="${ editable }">
		<comp:popup id="rename-iteration-dialog" titleKey="dialog.rename-iteration.title" 
		            isContextual="true"   openedBy="rename-iteration-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.rename-iteration.title" />
				'${ label }': function() {
					var url = "${ iterationUrl }";
					<jq:ajaxcall 
						url="url"
						dataType="json"
						httpMethod="POST"
						useData="true"
						successHandler="renameIterationSuccess">				
						<jq:params-bindings newName="#rename-iteration-name" />
					</jq:ajaxcall>					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<script type="text/javascript">
				$( "#rename-iteration-dialog" ).bind( "dialogopen", function(event, ui) {
					var name = $('#iteration-name').text();
					$("#rename-iteration-name").val(name);
					
				});
				</script>			
				<label><f:message key="dialog.rename.label" /></label>
				<input type="text" id="rename-iteration-name" /><br/>
				<comp:error-message forField="name"/>	
		
			</jsp:body>
		</comp:popup>	
	</c:if>	
</div>

<div class="fragment-body">


<div id="iteration-toolbar" class="toolbar-class ui-corner-all " >
	<div  class="toolbar-information-panel">
		<div id="general-informations-panel">
			<comp:general-information-panel auditableEntity="${iteration}"/>
		</div>
	</div>
	<div class="toolbar-button-panel">
		<c:if test="${ editable }">
			<input type="button" value='<f:message key="iteration.button.rename.label" />' id="rename-iteration-button" class="button"/> 
			<input type="button" value='<f:message key="iteration.button.remove.label" />' id="delete-iteration-button" class="button"/>
		</c:if>
	</div>	
	<div style="clear:both;"></div>	
</div>



<c:if test="${ editable }">
<comp:rich-jeditable targetUrl="${ iterationUrl }" componentId="iteration-description"
					 submitCallback="refreshIterationInfos"
					 />
</c:if>



<comp:toggle-panel titleKey="generics.description.title" isContextual="true" open="true">
	<jsp:attribute name="body">
		<div id="iteration-description" >${ iteration.description }</div>
	</jsp:attribute>
</comp:toggle-panel>


<comp:toggle-panel titleKey="campaign.planning.panel.title" isContextual="true" open="true">
	<jsp:attribute name="body">
	<div class="datepicker-panel">


		<table class="datepicker-table">
			<tr >
				<td class="datepicker-table-col">
					<comp:datepicker fmtLabel="dialog.label.iteration.scheduled_start.label" 
						url="${iterationPlanningUrl}" datePickerId="scheduled-start" 
						paramName="scheduledStart" isContextual="true"
						postCallback="refreshIterationInfos"
						initialDate="${iteration.scheduledStartDate.time}"
						editable="${ editable }" >	
					</comp:datepicker>
				</td>
				<td class="datepicker-table-col">
	
					<comp:datepicker-auto
						datePickerId="actual-start"
						url="${iterationPlanningUrl}"
						fmtLabel="dialog.label.iteration.actual_start.label"
						paramName="actualStart"
						autosetParamName="setActualStartAuto"
						isAuto="${iteration.actualStartAuto}"
						postCallback="refreshIterationInfos"
						initialDate="${iteration.actualStartDate.time}"
						isContextual="true"
						editable="${ editable }" >
					</comp:datepicker-auto>
				</td>
			</tr>
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker fmtLabel="dialog.label.iteration.scheduled_end.label" 
						url="${iterationPlanningUrl}" datePickerId="scheduled-end" 
						paramName="scheduledEnd" isContextual="true"
						postCallback="refreshIterationInfos"
						initialDate="${iteration.scheduledEndDate.time}"
						editable="${ editable }" >	
					</comp:datepicker>				
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto
						datePickerId="actual-end"
						url="${iterationPlanningUrl}"
						fmtLabel="dialog.label.iteration.actual_end.label"
						paramName="actualEnd"
						autosetParamName="setActualEndAuto"
						isAuto="${iteration.actualEndAuto}"
						postCallback="refreshIterationInfos"
						initialDate="${iteration.actualEndDate.time}"
						isContextual="true"
						editable="${ editable }">
					</comp:datepicker-auto>
				</td>
			</tr>
		</table>
		

	
	</div>
</jsp:attribute>
</comp:toggle-panel>


<%-- ------------------ test plan ------------------------------ --%>


<comp:toggle-panel titleKey="campaign.test-plan.panel.title" open="true" isContextual="true">
	<jsp:attribute name="panelButtons">
		<c:if test="${ editable }">
			<f:message var="associateLabel" key="campaign.test-plan.manage.button.label"/>
			<f:message var="removeLabel" key="campaign.test-plan.remove.button.label"/>
			<f:message var="assignLabel" key="campaign.test-plan.assign.button.label"/>
			<input id="test-case-button" type="button" value="${associateLabel}" class="button"/>
			<input id="remove-test-case-button" type="button" value="${removeLabel}" class="button"/>
			<input id="assign-test-case-button" type="button" value="${assignLabel}" class="button"/>
		</c:if>
	</jsp:attribute>
	
	<jsp:attribute name="body">
		<%--
		---- 
			requires <jq:execution-status-factory/>
		----  
		--%>
	
		<aggr:decorate-iteration-test-cases-table tableModelUrl="${iterationTestPlanUrl}" testPlanDetailsBaseUrl="${testCaseDetailsBaseUrl}" 
			testPlansUrl="${testCasesUrl}" batchRemoveButtonId="remove-test-case-button" 
			updateTestPlanUrl="${updateTestCaseUrl}" assignableUsersUrl="${assignableUsersUrl}"
			nonBelongingTestPlansUrl="${nonBelongingTestCasesUrl}" testPlanExecutionsUrl="${testCaseExecutionsUrl}" editable="${ editable }" testCaseMultipleRemovalPopupId="delete-multiple-test-plan-dialog" 
			testCaseSingleRemovalPopupId="delete-single-test-plan-dialog" />
		<aggr:iteration-test-cases-table/>
	</jsp:attribute>
</comp:toggle-panel>

<%--------------------------- Deletion confirmation pup for Test plan section ------------------------------------%>

<pop:popup id="delete-multiple-test-plan-dialog" openedBy="remove-test-case-button" titleKey="dialog.remove-testcase-associations.title">
	<jsp:attribute name="buttons">
		<f:message var="label" key="attachment.button.delete.label" />
				'${ label }' : function(){
						$("#delete-multiple-test-plan-dialog").data("answer","yes");
						$("#delete-multiple-test-plan-dialog").dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="body">
		<f:message key="dialog.remove-testcase-associations.message" />
	</jsp:attribute>
</pop:popup>

<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
<pop:popup id="delete-single-test-plan-dialog" openedBy="test-plans-table .delete-test-plan-button" titleKey="dialog.remove-testcase-association.title">
	<jsp:attribute name="buttons">
		<f:message var="label" key="attachment.button.delete.label" />
				'${ label }' : function(){
						$("#delete-single-test-plan-dialog").data("answer","yes");
						$("#delete-single-test-plan-dialog").dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="body">
		<f:message key="dialog.remove-testcase-association.message" />
	</jsp:attribute>
</pop:popup>

<%--------------------------- /Deletion confirmation pup for Test plan section ------------------------------------%>



<%------------------------------ Attachments bloc ---------------------------------------------%> 
<comp:attachment-bloc entity="${iteration}" workspaceName="campaign" editable="${ editable }" />


 
<%-- ---------------------deletion popup------------------------------ --%>
<c:if test="${ editable }">

	<comp:delete-contextual-node-dialog simulationUrl="${simulateDeletionUrl}" confirmationUrl="${confirmDeletionUrl}" 
	itemId="${iteration.id}" successCallback="deleteIterationSuccess" openedBy="delete-iteration-button" titleKey="dialog.delete-iteration.title"/>

</c:if>

<%--------------------------- Assign User popup -------------------------------------%>



<comp:popup id="batch-assign-test-case" titleKey="dialog.assign-test-case.title" 	
	isContextual="true" openedBy="assign-test-case-button" closeOnSuccess="false">
	
		<jsp:attribute name="buttons">
		
			<f:message var="label" key="campaign.test-plan.assign.button.label" />
			'${ label }': function() {
				var url = "${assignTestCasesUrl}";
				var table = $( '#test-plans-table' ).dataTable();
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
			<f:message var="emptyMessage" key="dialog.assign-user.selection.empty.label" />
			<f:message var="confirmMessage" key="dialog.assign-test-case.confirm.label" />
			<script type="text/javascript">
				$("#batch-assign-test-case").bind( "dialogopen", function(event, ui){
					var table = $( '#test-plans-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);

					if (ids.length > 0) {
						var comboBox = $.get("${batchAssignableUsersUrl}", false, function(){
							$("#comboBox-div").html("${confirmMessage}");
							$("#comboBox-div").append(comboBox.responseText);
							$("#comboBox-div").show();
						});
					}
					else {
						alert("${emptyMessage}");
						$(this).dialog('close');
					}
					
				});
			</script>
			<div id="comboBox-div">
			</div>
		</jsp:body>
</comp:popup>

</div>

<comp:decorate-buttons />

<script type="text/javascript">
	$(function(){

		$('#test-case-button').click(function(){
			document.location.href="${testPlanManagerUrl}" ;	
		});		
		
	});
</script>



