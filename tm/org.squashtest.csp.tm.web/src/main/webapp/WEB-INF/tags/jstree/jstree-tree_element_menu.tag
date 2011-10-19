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
<%@ tag description="Holds the html and javascript code necessary to display the tree element toolbar and bind it to events" %>
<%@ attribute name="newLeafButtonMessage" required="true" %>
<%@ attribute name="newResourceButtonMessage" required="false" %>
<%@ attribute name="workspace" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div id="tree_element_menu" class="tree-top-toolbar">
	<a id="tree-create-button" href="#tree-create-menu" class="fg-button fg-button-icon-right ui-widget ui-state-default ui-corner-all" >
	<span class="ui-icon ui-icon-triangle-1-s"></span>todo : regionalize
	</a>
	<div class="not-displayed">
	<div id="tree-create-menu">
	<ul>
		<li><a id="new-folder-tree-button" class="menu-disabled" href="#"><fmt:message key="tree.button.new-folder.label" />...</a></li>
		<li><a id="new-leaf-tree-button"  class="menu-disabled" href="#"><fmt:message key="${newLeafButtonMessage}" />...</a></li>
		<c:if test="${ not empty newResourceButtonMessage }">
		<li><a id="new-resource-tree-button" class="tree-create-menu-disabled"  href="#"><fmt:message key="${newResourceButtonMessage}" />...</a></li>
		</c:if>
	</ul>
	</div>
	</div>
</div>


<script type="text/javascript">
	$(function(){
		squashtm.treemenu = {};
		
		var createOption = {
			"folderButton" : "#new-folder-tree-button",
			"fileButton" : "#new-leaf-tree-button"
			<c:if test="${ not empty newResourceButtonMessage }">
			,"resourceButton" : "#new-resource-tree-button"
			</c:if>
		};
		
		squashtm.treemenu.create = $('#tree-create-button').treeMenu("#tree-create-menu", createOption);
	});

</script>


<%--
	<button id="new-folder-tree-button"><fmt:message key="tree.button.new-folder.label" />...</button>
	<button id="new-leaf-tree-button"><fmt:message key="${newLeafButtonMessage}" />...</button>
	<c:if test="${ not empty newResourceButtonMessage }">
	<button id="new-resource-tree-button"><fmt:message key="${newResourceButtonMessage}" />...</button>
	</c:if>
	<button id="copy-node-tree-button"><fmt:message key="tree.button.copy-node.label" />...</button>
	<button id="paste-node-tree-button"><fmt:message key="tree.button.paste-node.label" />...</button>
	<button id="rename-node-tree-button"><fmt:message key="tree.button.rename-node.label" />...</button>
	<button id="delete-node-tree-button"><fmt:message key="tree.button.delete.label" />...</button>
	
	
	
	$(function() {
var initButton = function(bSelector, cssIcon){
$(bSelector).button({
disabled : true,
text : false,
icons : {
primary : cssIcon
}
});
};
initButton("#new-folder-tree-button", "ui-icon-folder-collapsed");
initButton("#new-leaf-tree-button", "ui-icon-document");
initButton("#copy-node-tree-button", "ui-icon-clipboard");
initButton("#paste-node-tree-button", "ui-icon-copy");
initButton("#rename-node-tree-button", "ui-icon-pencil");
initButton("#delete-node-tree-button", "ui-icon-trash" );
}); 
	
	
 --%>
