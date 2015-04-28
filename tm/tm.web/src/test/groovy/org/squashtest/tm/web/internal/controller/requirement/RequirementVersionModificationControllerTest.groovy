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
package org.squashtest.tm.web.internal.controller.requirement



import javax.inject.Provider

import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.service.audit.RequirementAuditTrailService
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.requirement.RequirementVersionResolverService
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper
import org.squashtest.tm.web.internal.controller.milestone.MilestoneUIConfigurationService;
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter
import org.squashtest.tm.web.internal.helper.LabelFormatter
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper
import org.squashtest.tm.web.internal.model.builder.JsonInfoListBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel
import org.squashtest.tm.web.testutils.MockFactory;

import spock.lang.Specification


class RequirementVersionModificationControllerTest extends Specification {
	RequirementVersionModificationController controller = new RequirementVersionModificationController()
	RequirementVersionManagerService requirementVersionModificationService= Mock()
	InternationalizationHelper i18nHelper = Mock()
	LabelFormatter formatter = new LevelLabelFormatter(i18nHelper)

	Provider criticalityBuilderProvider = criticalityBuilderProvider()

	JsonInfoListBuilder infoListBuilder = mockJsonInfoListBuilder();
	Provider statusBuilderProvider = statusBuilderProvider()
	Provider levelFormatterProvider = levelFormatterProvider()
	VerifyingTestCaseManagerService verifTCService = Mock()
	ServiceAwareAttachmentTableModelHelper attachmentsHelper = Mock()
	RequirementAuditTrailService auditTrailService = Mock()
	MilestoneUIConfigurationService milestoneConfigurer = Mock()

	MockFactory mockFactory = new MockFactory()

	def setup() {
		controller.requirementVersionManager = requirementVersionModificationService
		controller.criticalityComboBuilderProvider = criticalityBuilderProvider
		controller.statusComboDataBuilderProvider = statusBuilderProvider
		controller.levelFormatterProvider = levelFormatterProvider
		controller.cufValueService = Mock(CustomFieldValueFinderService)
		controller.verifyingTestCaseManager = verifTCService
		controller.attachmentsHelper = attachmentsHelper
		controller.auditTrailService = auditTrailService;
		controller.infoListBuilder = infoListBuilder
		controller.milestoneConfService = milestoneConfigurer

		mockAuditTrailService()
	}

	def mockAuditTrailService(){
		PagedCollectionHolder holder = Mock()
		holder.getFirstItemIndex() >> 0
		holder.getPagedItems() >> []
		holder.getTotalNumberOfItems() >> 0
		auditTrailService.findAllByRequirementVersionIdOrderedByDate(_,_)>> holder
	}

	def criticalityBuilderProvider() {
		RequirementCriticalityComboDataBuilder builder = new RequirementCriticalityComboDataBuilder()
		builder.labelFormatter = formatter

		Provider provider = Mock()
		provider.get() >> builder

		return provider
	}

	def mockJsonInfoListBuilder(){
		def builder = Mock(JsonInfoListBuilder)
		builder.toJson(_) >> [:]
		return builder
	}

	def statusBuilderProvider() {
		RequirementStatusComboDataBuilder builder = new RequirementStatusComboDataBuilder()
		builder.labelFormatter = formatter

		Provider provider = Mock()
		provider.get() >> builder

		return provider
	}

	def levelFormatterProvider() {
		Provider provider = Mock()
		provider.get() >> formatter

		return provider
	}

	def "should return requirement page fragment"() {
		given:
		RequirementVersion req = mockRequirementAmongOtherThings()
		req.getCriticality() >> RequirementCriticality.UNDEFINED
		req.getStatus() >> RequirementStatus.WORK_IN_PROGRESS
		req.getCategory() >> new ListItemReference("CAT_UNDEFINED")
		long reqId=15
		requirementVersionModificationService.findById(15) >> req
		Model model = Mock()
		attachmentsHelper.findPagedAttachments(_) >> Mock(DataTableModel)

		when:
		String res = controller.showRequirementVersion(reqId, model, null, null)

		then:
		res == "fragment/requirements/requirement-version"
		1 * model.addAttribute('requirementVersion', req)
	}



	def mockRequirementAmongOtherThings(){

		Requirement r = Mock()
		RequirementVersion v = Mock()
		r.getCurrentVersion() >> v
		v.getId() >> 0
		r.getUnmodifiableVersions() >> [v]
		v.getRequirement() >> r

		PagedCollectionHolder<?> ch = Mock()
		ch.getFirstItemIndex() >> 0
		ch.getPagedItems() >> []

		Project p = mockFactory.mockProject()
		r.getProject() >> p
		v.getProject() >> p

		verifTCService.findAllByRequirementVersion(_,_)>> ch

		return v
	}
}
