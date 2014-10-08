<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components"%>


<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>
<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>
<%@ attribute name="testCaseImportanceLabel"  required="true" type="java.lang.String"  description="a label related to test case importance, not sure to remember what." %>


<c:url var="testCaseUrl" 					value="/test-cases/${testCase.id}"/>


<comp:toggle-panel id="test-case-attribut-panel"
				titleKey="label.Attributes"
				   open="true">
				   
	<jsp:attribute name="body">
	<div id="test-case-attribut-table"  class="display-table">
<div class="display-table-row">
			<label for="test-case-importance" class="display-table-cell"><f:message key="test-case.importance.combo.label" /></label>
			<div class="display-table-cell">
			<span id="test-case-importance-icon" class="test-case-importance-${testCase.importance}">&nbsp </span>	<span id="test-case-importance">${testCaseImportanceLabel}</span>
				<c:if test="${ writable }">
					<comp:select-jeditable-auto associatedSelectJeditableId="test-case-importance" />
				</c:if>
			</div>
		</div>
		
		<div class="display-table-row">
			<label for="test-case-nature" class="display-table-cell"><f:message key="test-case.nature.combo.label" /></label>
			<div class="display-table-cell">
				<span id="test-case-nature">${ testCaseNatureLabel }</span>
			</div>
		</div>
		
		<div class="display-table-row">
			<label for="test-case-type" class="display-table-cell">
				<f:message key="test-case.type.combo.label" />
			</label>
			<div class="display-table-cell">
				<span id="test-case-type">${ testCaseTypeLabel }</span>
			</div>
		</div>
		
		</div>
	</jsp:attribute>
</comp:toggle-panel>