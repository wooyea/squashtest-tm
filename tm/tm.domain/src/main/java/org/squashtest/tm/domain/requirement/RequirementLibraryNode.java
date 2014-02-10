/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.domain.requirement;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.squashtest.tm.domain.attachment.AttachmentList;
import org.squashtest.tm.domain.audit.Auditable;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.resource.Resource;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Auditable
@Indexed
public abstract class RequirementLibraryNode<RESOURCE extends Resource> implements LibraryNode {
	@Id
	@GeneratedValue
	@Column(name = "RLN_ID")
	@Field(analyze=Analyze.NO, store=Store.YES)
	@DocumentId
	private Long id;

	@ManyToOne
	@JoinColumn(name = "PROJECT_ID")
	@IndexedEmbedded
	private Project project;

	@Override
	public Project getProject() {
		return project;
	}

	/**
	 * Notifies this object it is now a resource of the given project.
	 * 
	 * @param project
	 */
	@Override
	public void notifyAssociatedWithProject(Project project) {
		this.project = project;

	}

	public RequirementLibraryNode() {
		super();
	}

	public RequirementLibraryNode(String name, String description) {
		setName(name);
		setDescription(description);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	@AclConstrainedObject
	public Library<?> getLibrary() {
		return getProject().getRequirementLibrary();
	}

	@Override
	public AttachmentList getAttachmentList() {
		return getResource().getAttachmentList();
	}

	/**
	 * Implementors should ask the visitor to visit this object.
	 * 
	 * @param visitor
	 */
	public abstract void accept(RequirementLibraryNodeVisitor visitor);

	public abstract RESOURCE getResource();
}
