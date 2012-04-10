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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<s:url var="newExecutionUrl"
	value="/iterations/{iterId}/test-plan/{tpId}/new-execution">
	<s:param name="iterId" value="${iterationId}" />
	<s:param name="tpId" value="${testPlanId}" />
</s:url>


<s:url var="showExecutionUrl" value="/executions" />

<c:set var="textcolor" value="#555555" />


<td colspan="8"><table>
		<!-- -----------------------------------------------ROW OF EXECUTION -->
		<c:forEach items="${ executions }" var="execution" varStatus="status">
			<tr>
				<td></td>
				<td></td>
				<td
					style="margin-left: 10px; color: ${textcolor}; font-style:italic; text-decoration: underline"><a
					href="${showExecutionUrl}/${execution.id}"><b>Exec.
							${status.index + 1} :</b> ${ execution.name }</a>
				</td>
				<td style="width: 7.5em;color: ${textcolor}; font-style:italic;"><f:message
						key="${ execution.executionMode.i18nKey }" />
				</td>
				<td style="width: 12em; color: ${textcolor} font-style:italic;"><f:message
						key="execution.execution-status.${execution.executionStatus}" />

				</td>
				<td style="width: 12em; color: ${textcolor}"><c:choose>
						<c:when test="${ execution.lastExecutedBy != null }">
							<i>${ execution.lastExecutedBy }</i>
						</c:when>
						<c:otherwise>
							<i><f:message key="squashtm.nodata" /> </i>
						</c:otherwise>
					</c:choose>
				</td>
				<td style="width: 12em; color: ${textcolor}"><c:choose>
						<c:when test="${ execution.lastExecutedOn != null }">
							<f:message var="dateFormat" key="squashtm.dateformat" />
							<i><f:formatDate value="${ execution.lastExecutedOn }"
									pattern="${dateFormat}" /> </i>
						</c:when>
						<c:otherwise>
							<i><f:message key="squashtm.nodata" /> </i>
						</c:otherwise>
					</c:choose>
				</td>
				<td style="width: 2em;"><c:if test="${ editableIteration }">
						<button id="delete-execution-table-button-${execution.id}"
							class="delete-execution-table-button"></button>
					</c:if>
				</td>
			</tr>
		</c:forEach>
		<!-- ------------------------------------------END ROW OF EXECUTION -->


		<!-- ---------------------------------------------ROW NEW EXECUTION -->

		<c:if test="${ editableIteration }">
			<tr>
				<td colspan="8" style="text-align: left;"> <a id="new-exec-${testPlanId}" style="font-size:0.8em;"
						class="button" href="javascript:void(0)" data-new-exec="${newExecutionUrl}"><f:message
								key="execution.iteration-test-plan-row.new" /> </a> </td>
			</tr>
		</c:if>
		<!-- ---------------------------------------------END ROW NEW EXECUTION -->
	</table>
</td>

<c:if test="${ editableIteration }">
	<script>
		$(function() {
			bindDeleteButtonsToFunctions();
			decorateDeleteButtons($(".delete-execution-table-button"));
			$('a.button').button();
		});

		function bindDeleteButtonsToFunctions() {
			var execOffset = "delete-execution-table-button-";
			$(".delete-execution-table-button").click(
					function() {
						//console.log("delete execution #"+idExec);
						var execId = $(this).attr("id");
						var idExec = execId.substring(execOffset.length);
						var execRow = $(this).closest("tr");
						var testPlanHyperlink = $(this).closest("tr").closest(
								"tr").prev().find("a.test-case-name-hlink");

						confirmeDeleteExecution(idExec, testPlanHyperlink,
								execRow);
					});

		}
		function confirmeDeleteExecution(idExec, testPlanHyperlink, execRow) {
			oneShotConfirm("<f:message key='dialog.delete-execution.title'/>",
					"<f:message key='dialog.delete-execution.message'/>",
					"<f:message key='dialog.button.confirm.label'/>",
					"<f:message key='dialog.button.cancel.label'/>").done(
					function() {
						doDeleteExecution(idExec, testPlanHyperlink, execRow);
					});
		}
		function doDeleteExecution(idExec, testPlanHyperlink, execRow) {
			deleteExecutionOfSpecifiedId(idExec).done(function(data) {
				refreshTable(testPlanHyperlink, execRow, data)
			});
		}
		function deleteExecutionOfSpecifiedId(idExec) {
			return $.ajax({
				'url' : "${showExecutionUrl}/" + idExec,
				type : 'DELETE',
				data : [],
				dataType : "json"
			});
		}
		function refreshTable(testPlanHyperlink, execRow, data) {
			// 1/ refresh execution table
			//
			// 		$(testPlanHyperlink).click();
			// 		$(testPlanHyperlink).click();
			//
			//OR  
			//
			// 2 / just remove the execution row 
			// I choose this solution because it is easier to delete severas executions in a row without waiting for the execution table to reload.
			// the drawback is that the number of the executions is not updated. 
			//console.log("execRow = " + execRow);
			//$(execRow).detach();
			//or 
			//3/ Because can't refresh only the status & last executed by yet
			refreshTestPlans();

			refreshTestSuiteInfos();
			refreshStats();
			refreshExecButtons();
		}
	</script>
</c:if>