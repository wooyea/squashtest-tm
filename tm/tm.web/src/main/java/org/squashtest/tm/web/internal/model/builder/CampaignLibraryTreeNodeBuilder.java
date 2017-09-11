/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
package org.squashtest.tm.web.internal.model.builder;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.CampaignFolder;
import org.squashtest.tm.domain.campaign.CampaignLibraryNode;
import org.squashtest.tm.domain.campaign.CampaignLibraryNodeVisitor;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

@Component
@Scope("prototype")
public class CampaignLibraryTreeNodeBuilder extends LibraryTreeNodeBuilder<CampaignLibraryNode> {
	protected InternationalizationHelper internationalizationHelper;

	/**
	 * This visitor is used to populate custom attributes of the {@link JsTreeNode} currently built
	 */
	private class CustomAttributesPopulator implements CampaignLibraryNodeVisitor {
		private final JsTreeNode builtNode;


		public CustomAttributesPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 * @see org.squashtest.tm.domain.campaign.CampaignLibraryNodeVisitor#visit(org.squashtest.tm.domain.campaign.CampaignFolder)
		 */
		@Override
		public void visit(CampaignFolder folder) {
			addFolderAttributes("campaign-folders");
			State state = folder.hasContent() ? State.closed : State.leaf;
			builtNode.setState(state);
		}

		/**
		 * @see org.squashtest.tm.domain.campaign.CampaignLibraryNodeVisitor#visit(org.squashtest.tm.domain.campaign.Campaign)
		 */
		@Override
		public void visit(Campaign campaign) {

			// name and title
			builtNode.addAttr("name", campaign.getName());
			builtNode.addAttr("reference", campaign.getReference());
			builtNode.setTitle(campaign.getFullName());

			// other attributes
			builtNode.addAttr("rel", "campaign");
			builtNode.addAttr("executionStatus", campaign.getExecutionStatus().toString());
			builtNode.addAttr("resType", "campaigns");
			builtNode.addAttr("reference", campaign.getReference());
			State state = campaign.hasIterations() ? State.closed : State.leaf;
			builtNode.setState(state);

			//build tooltip
			String status = campaign.getExecutionStatus().getI18nKey();
			Locale locale = LocaleContextHolder.getLocale();
			String localizedStatus = internationalizationHelper.internationalize(status, locale);
			String[] args = {localizedStatus};
			String tooltip = internationalizationHelper.getMessage("label.tree.campaign.tooltip", args, status, locale);
			String description = "";
			if (!campaign.getTestPlan().isEmpty()) {
				description = campaign.getTestPlan().get(0).getReferencedTestCase().getDescription();
				if (description.length() > 30) {
					description = description.substring(0, 30);
				}
			}
			builtNode.addAttr("title", tooltip + "\n" + HTMLCleanupUtils.htmlToText(description));

			// milestones
			builtNode.addAttr("milestones", campaign.getMilestones().size());
			builtNode.addAttr("milestone-creatable-deletable", campaign.doMilestonesAllowCreation().toString());
			builtNode.addAttr("milestone-editable", campaign.doMilestonesAllowEdition().toString());
		}
	}

	/**
	 * This visitor is used to populate the children of the currently built {@link JsTreeNode}
	 *
	 * @author Gregory Fouquet
	 */
	private class ChildrenPopulator implements CampaignLibraryNodeVisitor {
		private final JsTreeNode builtNode;

		public ChildrenPopulator(JsTreeNode builtNode) {
			super();
			this.builtNode = builtNode;
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCase)
		 */
		@Override
		public void visit(Campaign visited) {
			if (visited.hasContent()) {
				builtNode.setState(State.open);

				IterationNodeBuilder childrenBuilder = new IterationNodeBuilder(permissionEvaluationService, internationalizationHelper);

				List<JsTreeNode> children = new JsTreeNodeListBuilder<>(childrenBuilder)
					.expand(getExpansionCandidates())
					.setModel(visited.getOrderedContent())
					.build();

				builtNode.setChildren(children);
			}
		}

		/**
		 * @see org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor#visit(org.squashtest.tm.domain.testcase.TestCaseFolder)
		 */
		@Override
		public void visit(CampaignFolder visited) {
			if (visited.hasContent()) {

				CampaignLibraryTreeNodeBuilder childrenBuilder = new CampaignLibraryTreeNodeBuilder(permissionEvaluationService, internationalizationHelper);
				childrenBuilder.filterByMilestone(milestoneFilter);


				List<JsTreeNode> children = new JsTreeNodeListBuilder<>(childrenBuilder)
					.expand(getExpansionCandidates())
					.setModel(visited.getOrderedContent())
					.build();

				builtNode.setChildren(children);


				// because of the milestoneFilter it may happen that the children collection ends up empty.
				// in that case we must set the state of the node accordingly
				State state = children.isEmpty() ? State.leaf : State.open;
				builtNode.setState(state);
			}
		}

	}

	@Inject
	public CampaignLibraryTreeNodeBuilder(PermissionEvaluationService permissionEvaluationService, InternationalizationHelper internationalizationHelper) {
		super(permissionEvaluationService);
		this.internationalizationHelper = internationalizationHelper;
	}

	@Override
	protected void addCustomAttributes(CampaignLibraryNode libraryNode, JsTreeNode treeNode) {
		libraryNode.accept(new CustomAttributesPopulator(treeNode));

	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 * org.squashtest.tm.domain.Identified)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, CampaignLibraryNode model) {
		model.accept(new ChildrenPopulator(node));

	}


	@Override
	protected boolean passesMilestoneFilter() {
		if (milestoneFilter != null) {
			return new MilestoneFilter(milestoneFilter).isValid(node);
		} else {
			return true;
		}
	}


	private static final class MilestoneFilter implements CampaignLibraryNodeVisitor {

		private Milestone milestone;
		private boolean isValid;


		private MilestoneFilter(Milestone milestone) {
			this.milestone = milestone;
		}

		public boolean isValid(CampaignLibraryNode node) {
			isValid = false;
			node.accept(this);
			return isValid;
		}

		@Override
		public void visit(CampaignFolder folder) {
			isValid = true;
		}

		@Override
		public void visit(Campaign campaign) {
			isValid = campaign.isMemberOf(milestone);
		}

	}


}
