/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.web.internal.controller.customreport;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.web.internal.argumentresolver.MilestoneConfigResolver.CurrentMilestone;
import org.squashtest.tm.web.internal.controller.testcase.TestCaseLibraryNavigationController;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;

/**
 * This controller is bloated because the tree client side is made for {@link LibraryNode}.
 * The tree send several distinct requests for the different type of node. This way had sense with the initial tree model,
 * but isn't optimized for the new tree model.
 * As we haven't the time to redefine the client tree, this controller just follow the client jstree requests... 
 * Also, no milestones for v1, but we require active milestone as it probably will be in v2 ans the tree give it anyway
 * @author jthebault
 *
 */
@Controller
@RequestMapping("/custom-report-browser")
public class CustomReportNavigationController {
	
	@Inject
	CustomReportWorkspaceService workspaceService;
	
	@Inject
	CustomReportLibraryNodeService customReportLibraryNodeService;
	
	public static final Logger LOGGER = LoggerFactory.getLogger(TestCaseLibraryNavigationController.class);

	//----- CREATE NODE METHODS -----

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value="/drives/{libraryId}/content/new-folder", method=RequestMethod.POST)
	public @ResponseBody JsTreeNode createNewFolderInLibrary(@PathVariable Long libraryId, @RequestBody CustomReportFolder customReportFolder){
		LOGGER.debug("JTH custom-report-browser/drives/{libraryId}/content/new-folder " + libraryId);
		return createNewCustomReportLibraryNode(libraryId, customReportFolder);
	}
	
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value="/folders/{folderId}/content/new-folder", method=RequestMethod.POST)
	public @ResponseBody JsTreeNode createNewFolderInFolder(@PathVariable Long folderId, @RequestBody CustomReportFolder customReportFolder){
		LOGGER.debug("JTH custom-report-browser/folders/{libraryId}/content/new-folder " + folderId);
		return createNewCustomReportLibraryNode(folderId, customReportFolder);
	}
	
	@RequestMapping(value = "/drives/{libraryId}/content", method = RequestMethod.GET)
	public @ResponseBody List<JsTreeNode> getRootContentTreeModel(@PathVariable long libraryId,
			@CurrentMilestone Milestone activeMilestone) {
		return getNodeContent(libraryId, activeMilestone);
	}
	
	@RequestMapping(value = "/folders/{libraryId}/content", method = RequestMethod.GET)
	public @ResponseBody List<JsTreeNode> getFolderContentTreeModel(@PathVariable long folderId,
			@CurrentMilestone Milestone activeMilestone) {
		return getNodeContent(folderId, activeMilestone);
	}
	
	private JsTreeNode createNewCustomReportLibraryNode(Long libraryId, TreeEntity entity){
		CustomReportLibraryNode newNode = customReportLibraryNodeService.createNewCustomReportLibraryNode(libraryId, entity);
		return new CustomReportTreeNodeBuilder().build(newNode);
	}
	
	private List<JsTreeNode> getNodeContent( long folderId,
			@CurrentMilestone Milestone activeMilestone) {
		List<TreeLibraryNode> children = workspaceService.findContent(folderId);
		return new CustomReportListTreeNodeBuilder(children).build();
	}
	
}
