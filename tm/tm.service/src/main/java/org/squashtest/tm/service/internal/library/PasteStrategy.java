/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.tm.service.internal.library;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean;
import org.squashtest.tm.domain.library.NodeContainer;
import org.squashtest.tm.domain.library.TreeNode;
import org.squashtest.tm.service.internal.repository.EntityDao;
import org.squashtest.tm.service.internal.repository.GenericDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;

public class PasteStrategy<CONTAINER extends NodeContainer<COPIED>, COPIED extends TreeNode> {

	private static final String CREATE = "CREATE";
	private static final String READ = "READ";

	private ObjectFactory<TreeNodeCopier> copier;
	private GenericDao<Object> genericDao;
	private EntityDao<CONTAINER> containerDao;
	private EntityDao<COPIED> copiedDao;
	private PermissionEvaluationService permissionService;

	public void setGenericDao(GenericDao<Object> genericDao) {
		this.genericDao = genericDao;
	}

	public void setContainerDao(EntityDao<CONTAINER> containerDao) {
		this.containerDao = containerDao;
	}

	public void setCopiedDao(EntityDao<COPIED> copiedDao) {
		this.copiedDao = copiedDao;
	}

	@SuppressWarnings("unchecked")
	public List<COPIED> pasteNodes(long containerId, List<Long> list) {

		// fetch
		CONTAINER container = containerDao.findById(containerId);

		// check. Note : we wont recursively check for the whole hierarchy as it's supposed to have the same
		// identity holder
		for (Long id : list) {
			COPIED node = copiedDao.findById(id);

			PermissionsUtils.checkPermission(permissionService, new SecurityCheckableObject(container, CREATE),
					new SecurityCheckableObject(node, READ));
		}

		// proceed : will copy and persist each node of copied trees generation by generation.
		List<COPIED> nodeList = new ArrayList<COPIED>(list.size());

		// initialize generation memorizers = list of destination/sources couples
		Map<NodeContainer<TreeNode>, Collection<TreeNode>> nextGeneration = new HashMap<NodeContainer<TreeNode>, Collection<TreeNode>>();
		Map<NodeContainer<TreeNode>, Collection<TreeNode>> sourceGeneration = null;
		Map<NodeContainer<TreeNode>, Collection<TreeNode>> parents = null;

		// copy first generation and memorize copied entities
		for (Long id : list) {
			COPIED node = copiedDao.findById(id);

			COPIED copy = (COPIED) createCopier().copy(node, (NodeContainer<TreeNode>) container, nextGeneration);
			nodeList.add(copy);
		}

		// loop on all following generations
		while (!nextGeneration.isEmpty()) {
			removeCopiedNodesFromNextGeneration(nodeList, nextGeneration);

			if (!nextGeneration.isEmpty()) {
				if (parents != null) {
					// if we cont flush and then evict, some entities might not be persisted
					genericDao.flush();
					// when moving to a next row, evict the parents that just became grandparents.
					// note: will note evict the nodes to return because they never been in the "sourceRow" map.
					for (Entry<NodeContainer<TreeNode>, Collection<TreeNode>> grandParentGenerationEntry : parents
							.entrySet()) {
						NodeContainer<TreeNode> grandPa = grandParentGenerationEntry.getKey();
						Collection<TreeNode> grandPas = grandParentGenerationEntry.getValue();
						genericDao.clearFromCache(grandPa);
						genericDao.clearFromCache(grandPas);
					}
				}
				// move to next generation
				parents = sourceGeneration;
				sourceGeneration = nextGeneration;
				nextGeneration = new HashMap<NodeContainer<TreeNode>, Collection<TreeNode>>();

				// loop in all node of source generation and copy them
				for (Entry<NodeContainer<TreeNode>, Collection<TreeNode>> sourceEntry : sourceGeneration.entrySet()) {
					Collection<TreeNode> sources = sourceEntry.getValue();
					NodeContainer<TreeNode> destination = sourceEntry.getKey();
					for (TreeNode source : sources) {
						createCopier().copy(source, destination, nextGeneration);
					}
				}
			}

		}
		// after copying last row, evict source generation and their parents. They will never be grandparents.
		return nodeList;
	}

	private TreeNodeCopier createCopier() {
		return copier.getObject();
	}

	// this is to avoid infinite loop in case someone copy a folder and paste it into itself.
	// XXX maybe we can avoid this with a finer control of what's put in nextGeneration
	private void removeCopiedNodesFromNextGeneration(List<COPIED> nodeList,
			Map<NodeContainer<TreeNode>, Collection<TreeNode>> nextGeneration) {
		for (Entry<NodeContainer<TreeNode>, Collection<TreeNode>> nextGenerationEntry : nextGeneration.entrySet()) {
			nextGenerationEntry.getValue().removeAll(nodeList);
		}

	}

	/**
	 * @param copier
	 *            the copier to set
	 */
	public void setCopier(ObjectFactory<TreeNodeCopier> copier) {
		this.copier = copier;
	}

	/**
	 * @param permissionService
	 *            the permissionService to set
	 */
	public void setPermissionService(PermissionEvaluationService permissionService) {
		this.permissionService = permissionService;
	}

}
