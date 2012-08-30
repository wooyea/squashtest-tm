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
<%@ tag body-content="empty" description="inserts the html table of test cases" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<table id="test-suite-test-plans-table"  >
	<thead>
		<tr>
			<th>Test Plan Id</th>
			<th>#</th>
			<th><f:message key="iteration.executions.table.column-header.project.label" /></th>
			<th><f:message key="iteration.executions.table.column-header.test-case.label" /></th>
			<th><f:message key="iteration.executions.table.column-header.importance.label" /></th>
			<th><f:message key="iteration.executions.table.column-header.type.label" /></th>
			<th><f:message key="iteration.executions.table.column-header.status.label" /></th>
			<th><f:message key="iteration.executions.table.column-header.user.label" /></th>
			<th><f:message key="iteration.executions.table.column-header.execution-date.label" /></th>
			<th>is tc deleted (masked)</th>
			<th>&nbsp;</th>				
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>
	<div id="test-case-row-buttons" class="not-displayed">
	<a id="delete-test-suite-test-plan-button" href="javascript:void(0)" class="delete-test-suite-test-plan-button"><f:message key="dialog.remove-testcase-testsuite-association.title2" /></a>
</div> 
