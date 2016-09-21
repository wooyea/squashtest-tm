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
package org.squashtest.tm.service.internal.chart;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.chart.ChartInstance;
import org.squashtest.tm.domain.chart.ChartSeries;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnType;
import org.squashtest.tm.domain.chart.QColumnPrototype;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.chart.engine.ChartDataFinder;
import org.squashtest.tm.service.internal.repository.CustomChartDefinitionDao;

import com.querydsl.jpa.hibernate.HibernateQueryFactory;

@Service("squashtest.tm.service.ChartModificationService")
public class ChartModificationServiceImpl implements ChartModificationService {

	@PersistenceContext
	private EntityManager em;

	@Inject
	private ChartDataFinder dataFinder;

	@Inject
	private CustomChartDefinitionDao chartDefinitionDao;

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;

	@Override
	public void persist(ChartDefinition newChartDefinition) {
		session().persist(newChartDefinition);
	}

	@Override
	public ChartDefinition findById(long id) {
		return (ChartDefinition) session().get(ChartDefinition.class, id);
	}


	@Override
	public Map<EntityType, Set<ColumnPrototype>> getColumnPrototypes() {

		HibernateQueryFactory factory = new HibernateQueryFactory(session());
		QColumnPrototype prototype = QColumnPrototype.columnPrototype;

		Map<EntityType, Set<ColumnPrototype>> prototypes;

		prototypes = factory.from(prototype).where(prototype.business.eq(true)).orderBy(prototype.id.asc())
				.transform(groupBy(prototype.specializedType.entityType).as(set(prototype)));

		return prototypes;
	}

	@Override
	public void update(ChartDefinition chartDef) {
		session().saveOrUpdate(chartDef);
	}


	@Override
	public ChartInstance generateChart(long chartDefId, List<EntityReference> dynamicScope, Long dashboardId){
		ChartDefinition def = findById(chartDefId);
		return generateChart(def,dynamicScope,dashboardId);

	}

	@Override
	public ChartInstance generateChart(ChartDefinition chartDef, Long projectId){
		if(chartDef.getId() != null){
			throw new IllegalArgumentException("This implementation of generate chart should be only used with transient Chart Definition (ie for previews only)");
		}
		Project project = em.find(Project.class,projectId);
		chartDef.setProject(project);
		return generateChart(chartDef,null,null);
	}


	private Session session(){
		return em.unwrap(Session.class);
	}

	@Override
	public ChartInstance generateChart(ChartDefinition definition, List<EntityReference> dynamicScope, Long dashboardId) {
		ChartSeries series = dataFinder.findData(definition, dynamicScope, dashboardId);
		return new ChartInstance(definition, series);
	}

	@Override
	@PreAuthorize("hasPermission(#definition.id, 'org.squashtest.tm.domain.chart.ChartDefinition' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void updateDefinition(ChartDefinition definition, ChartDefinition oldDef) {
		definition.setProject(oldDef.getProject());
		((AuditableMixin) definition).setCreatedBy(((AuditableMixin) oldDef).getCreatedBy());
		((AuditableMixin) definition).setCreatedOn(((AuditableMixin) oldDef).getCreatedOn());
		//rename if needed without forgot to rename the node.
		if (!definition.getName().equals(oldDef.getName())) {
			CustomReportLibraryNode node = customReportLibraryNodeService.findNodeFromEntity(oldDef);
			node.renameNode(definition.getName());
		}
		session().flush();
		session().clear();
		update(definition);
	}

	@Override
	public boolean hasChart(List<Long> userIds) {
		return chartDefinitionDao.hasChart(userIds);
	}

}
