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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.springframework.data.repository.query.Param;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionLinkDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jlor on 19/05/2017.
 */
public class RequirementVersionLinkDaoImpl extends HibernateEntityDao<RequirementVersionLink> implements CustomRequirementVersionLinkDao {

	@PersistenceContext
	EntityManager entityManager;

	@Override
	public List<RequirementVersionLink> findAllByReqVersionId(long requirementVersionId, PagingAndSorting pagingAndSorting) {

		Query query = entityManager.createNamedQuery("RequirementVersionLink.findAllByReqVersionId");
		query.setParameter("requirementVersionId", requirementVersionId);
		return query.getResultList();
	}

	@Override
	public boolean linkAlreadyExists(Long reqVersionId1, Long reqVersionId2) {
		Query existQuery = entityManager.createNamedQuery("RequirementVersionLink.linkAlreadyExists");
		existQuery.setParameter("reqVersionId1", reqVersionId1);
		existQuery.setParameter("reqVersionId2", reqVersionId2);
		return (Long)existQuery.getSingleResult() > 0;
	}
}
