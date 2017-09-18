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
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode;
import org.squashtest.tm.web.internal.model.jstree.JsTreeNode.State;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

@Component
@Scope("prototype")
public class IterationNodeBuilder extends GenericJsTreeNodeBuilder<Iteration, IterationNodeBuilder> {
	protected InternationalizationHelper internationalizationHelper;

	@Inject
	public IterationNodeBuilder(PermissionEvaluationService permissionEvaluationService, InternationalizationHelper internationalizationHelper) {
		super(permissionEvaluationService);
		this.internationalizationHelper = internationalizationHelper;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doBuild(org.squashtest.tm.web.internal.model.jstree.JsTreeNode,
	 * org.squashtest.tm.domain.Identified)
	 */
	@Override
	protected JsTreeNode doBuild(JsTreeNode node, Iteration model) {

		node.addAttr("name", model.getName());
		node.addAttr("reference", model.getReference());
		node.setTitle(model.getFullName());

		node.addAttr("rel", "iteration");
		node.addAttr("executionStatus", model.getExecutionStatus().toString());
		node.addAttr("resId", String.valueOf(model.getId()));
		node.addAttr("resType", "iterations");
		node.setState(model.hasTestSuites() ? State.closed : State.leaf);
		node.addAttr("iterationIndex", Integer.toString(index + 1));

		node.addAttr("id", model.getClass().getSimpleName() + '-' + model.getId());

		//build tooltip
		String status = model.getExecutionStatus().getI18nKey();
		Locale locale = LocaleContextHolder.getLocale();
		String localizedStatus = internationalizationHelper.internationalize(status, locale);
		String[] args = {localizedStatus};
		String tooltip = internationalizationHelper.getMessage("label.tree.campaign.tooltip", args, status, locale);
		String description = "";
		if (!model.getPlannedTestCase().isEmpty() && model.getPlannedTestCase().get(0).getDescription()!=null) {
			description =  model.getPlannedTestCase().get(0).getDescription();
			if (description.length() > 30) {
				description = description.substring(0, 30);
			}
		}
		node.addAttr("title", tooltip + "\n" + HTMLCleanupUtils.htmlToText(description));

		//milestone attributes
		node.addAttr("milestones", model.getMilestones().size());
		node.addAttr("milestone-creatable-deletable", model.doMilestonesAllowCreation().toString());
		node.addAttr("milestone-editable", model.doMilestonesAllowEdition().toString());
		return node;
	}


	/**
	 * @see org.squashtest.tm.web.internal.model.builder.GenericJsTreeNodeBuilder#doAddChildren(org.squashtest.tm.web.internal.model.jstree.JsTreeNode, java.lang.Object)
	 */
	@Override
	protected void doAddChildren(JsTreeNode node, Iteration model) {
		if (model.hasContent()) {

			TestSuiteNodeBuilder childrenBuilder = new TestSuiteNodeBuilder(permissionEvaluationService, internationalizationHelper);

			List<JsTreeNode> children = new JsTreeNodeListBuilder<>(childrenBuilder)
				.expand(getExpansionCandidates())
				.setModel(model.getOrderedContent())
				.build();

			node.setChildren(children);

			// because of the milestoneFilter it may happen that the children collection ends up empty.
			// in that case we must set the state of the node accordingly
			State state = children.isEmpty() ? State.leaf : State.open;
			node.setState(state);
		}

	}

}
