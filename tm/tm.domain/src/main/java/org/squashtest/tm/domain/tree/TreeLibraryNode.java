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
package org.squashtest.tm.domain.tree;

import java.util.List;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.exception.NameAlreadyInUseException;

/**
 * Interface for a tree node without data. The main goal of this API is to separate concern between
 * a tree node and the entity referenced by this tree node.
 * By design, a tree node and an entity have a 1:1 relationship.
 * @author jthebault
 *
 */
public interface TreeLibraryNode extends TreeVisitable, Identified {
	final int MAX_NAME_SIZE = 255;
	
	String getName ();
	
	void setName(String name);
	
	long getEntityId();
	
	TreeEntity getEntity();
	
	void setEntity(TreeEntity treeEntity);
	
	TreeEntityDefinition getEntityType();
	
	TreeLibraryNode getParent();
	
	void setParent(TreeLibraryNode parent);
	
	List<TreeLibraryNode> getChildren();
	
	TreeLibrary getLibrary();
	
	void setLibrary(TreeLibrary treeLibrary);
	
	void addChild(TreeLibraryNode treeLibraryNode) throws UnsupportedOperationException,IllegalArgumentException,NameAlreadyInUseException;

	void removeChild(TreeLibraryNode treeLibraryNode);
	/**
	 * Check if a {@link CustomReportLibraryNode} is consistent with it's linked {@link TreeEntity}. 
	 * Throws {@link IllegalArgumentException} if not, as user action haven't any way to create this kind of inconsistency.
	 * @param treeLibraryNode
	 * @return
	 */
	void isCoherentWithEntity();
	
	boolean hasContent();
	
	void renameNode(String newName);

}