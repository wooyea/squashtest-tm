<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2016 Henix, henix.fr

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
<%@ tag body-content="empty" description="jqueryfies a linked reqs table" %>

<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of linked requirements" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="requirementVersion" type="java.lang.Object" required="true" description="The RequirementVersion instance for which we render the linked requirements" %>
<%@ attribute name="model" type="java.lang.Object" required="true" description="the initial rows of the table"%>
<%@ attribute name="milestoneConf" type="java.lang.Object" required="true" description="an instance of MilestoneFeatureConfiguration" %>


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%-- ======================== VARIABLES & URLS ============================ --%>


<s:url var="tableModelUrl" value="/requirement-versions/${requirementVersion.id}/linked-requirement-versions/table" />
<c:url var="requirementVersionUrl" value="/requirement-versions"/>

<f:message var="labelConfirm" key="label.Confirm"/>
<f:message var="labelCancel"  key="label.Cancel"/>
<f:message var="labelClose"  key="label.Close"/>

<c:set var="tblRemoveBtnClause" value=""/>
<c:if test="${editable}" >
	<c:set var="tblRemoveBtnClause" value=", unbind-button" />
</c:if>
<c:set var="tblEditTypeBtnClause" value=""/>
<c:if test="${editable}" >
	<c:set var="tblEditTypeBtnClause" value="edit-link-type-button" />
</c:if>

<%-- ======================== /VARIABLES & URLS ============================ --%>

<c:set var="milestoneVisibility" value="${(milestoneConf.milestoneDatesColumnVisible) ? '' : ', invisible'}"/>

<table id="linked-requirement-versions-table" class="unstyled-table" data-def="ajaxsource=${tableModelUrl}, deferloading=${model.iTotalRecords},
  datakeys-id=rv-id, pre-sort=2-asc, pagesize=50 ">
  <thead>
    <tr>
      <th data-def="map=rv-index, select">#</th>
      <th data-def="map=project-name, sortable"><f:message key="label.project" /></th>
      <th data-def="sortable, map=milestone-dates, tooltip-target=milestone ${milestoneVisibility}"><f:message key="label.Milestones"/></th>
      <th data-def="map=rv-reference, sortable"><f:message key="label.reference" /></th>
      <th data-def="map=rv-name, sortable, link=${requirementVersionUrl}/{rv-id}/info"><f:message key="requirement.name.label" /></th>
      <th data-def="map=rv-version, sortable"><f:message key="requirement-version.version-number.label" /></th>
      <th data-def="map=rv-role, sortable"><f:message key="requirement-version.linked-requirement-version.table.col-header.role"/></th>
      <th data-def="map=empty-edit-holder, narrow, sClass=${tblEditTypeBtnClause}">&nbsp;</th>
      <th data-def="map=empty-delete-holder${tblRemoveBtnClause}">&nbsp;</th>
      <th data-def="map=milestone, invisible"></th>
    </tr>
  </thead>
  <tbody>
  </tbody>
</table>

<script type="text/x-handlebars-template" id="unbind-linked-reqs-dialog-tpl">
  <div id="{{dialogId}}" class="not-displayed popup-dialog" title="<f:message key='dialog.remove-requirement-version-links.title'/>">
    <div data-def="state=confirm-deletion">
      <span><f:message key="dialog.remove-requirement-version-link.message"/></span>
    </div>
    <div data-def="state=multiple-tp">
      <span><f:message key="dialog.remove-requirement-version-links.message"/></span>
    </div>
    <div class="popup-dialog-buttonpane">
      <input type="button" class="button" value="${labelConfirm}" data-def="evt=confirm, mainbtn"/>
      <input type="button" class="button" value="${labelCancel}" data-def="evt=cancel"/>
    </div>
  </div>
</script>

<script type="text/x-handlebars-template" id="choose-link-type-dialog-tpl">
  <div id="{{dialogId}}" class="not-displayed popup-dialog" title="<f:message key='requirement-version.link.type.dialog.title'/>">

    <div data-def="state=confirm">
      <table id="involved-req-versions">
      	<tr>
      		<td>
				<span>${requirementVersion.name}</span>
      		</td>
      		<td>
      			<span id="relatedRequirementName">${{relatedRequirement.name}}</span>
      		</td>
      	</tr>
      	<tr>
      		<td>
      			<span>${requirementVersion.description}</span>
      		</td>
      		<td>
      			<span id="relatedRequirementDescription">${{relatedRequirement.description}}</span>
      		</td>
      	</tr>
      </table>
      <label>Link types: </label>
      <select id="link-types-options">
      	<option value="1_0">Related - Related</option>
      	<option value="2_0">Parent - Child</option>
      	<option value="2_1">Child - Parent</option>
      </select>
    </div>
	<div data-def="state=wait">
	</div>
    <div class="popup-dialog-buttonpane">
      <input type="button" class="button" value="${labelConfirm}" data-def="evt=confirm, mainbtn"/>
      <input type="button" class="button" value="${labelClose}" data-def="evt=cancel"/>
    </div>
  </div>
</script>

<script type="text/x-handlebars-template" id="add-summary-dialog-tpl">
	<div id="add-summary-dialog" class="not-displayed" title="<f:message key='requirement-version.linked-requirement-versions.add-summary-dialog.title' />">
		<ul><li>summary message here</li></ul>
	</div>
</script>
