/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.domain.requirement

import static org.squashtest.csp.tm.domain.requirement.RequirementStatus.*
import spock.lang.Specification
import spock.lang.Unroll

class RequirementStatusTest extends Specification {


	@Unroll("next statuses of #status should be #expectedSet")
	def "correctedness of next available status"(){
		expect :
			expectedSet == status.getAvailableNextStatus()

		where :
			status				|	expectedSet
			WORK_IN_PROGRESS  	|	[ OBSOLETE, WORK_IN_PROGRESS, UNDER_REVIEW ] as Set
			UNDER_REVIEW		|	[ OBSOLETE, UNDER_REVIEW, APPROVED, WORK_IN_PROGRESS ] as Set
			APPROVED			|	[ OBSOLETE, APPROVED ] as Set
			OBSOLETE			|	[ OBSOLETE ] as Set

	}
	@Unroll("next disabled statuses of #status should be #expectedSet")
	def "correctedness of next disabled status"(){
		expect :
			expectedSet == status.getDisabledStatus()

		where :
			status				|	expectedSet
			WORK_IN_PROGRESS  	|	[ APPROVED ] as Set
			UNDER_REVIEW		|	[ ] as Set
			APPROVED			|	[ UNDER_REVIEW, WORK_IN_PROGRESS ] as Set
			OBSOLETE			|	[ APPROVED, UNDER_REVIEW, WORK_IN_PROGRESS ] as Set

	}

	@Unroll("transitions from #status to #allowedTransitions should be legal")
	def "transitions from #status to #allowedTransitions should be legal"(){
		expect :
		assertLegalTransitions(status, allowedTransitions)

		where :
		status				|	allowedTransitions
		WORK_IN_PROGRESS  	|	[ OBSOLETE, WORK_IN_PROGRESS, UNDER_REVIEW ] as Set
		UNDER_REVIEW		|	[ OBSOLETE, UNDER_REVIEW, APPROVED, WORK_IN_PROGRESS ] as Set
		APPROVED			|	[ OBSOLETE, APPROVED ] as Set
		OBSOLETE			|	[ OBSOLETE ] as Set
	}

	def assertLegalTransitions(def from, def tos) {
		tos.each {
			assert from.isTransitionLegal(it)
		}
		true
	}

	@Unroll("transitions from #status to something other than #allowedTransitions should not be legal")
	def "transitions from #status to something other than #allowedTransitions should not be legal"(){
		expect :
		assertIllegalTransitions(status, unallowedTransitions)

		where :
		status				|	unallowedTransitions
		WORK_IN_PROGRESS  	|	[ APPROVED ]
		UNDER_REVIEW		|	[ ]
		APPROVED			|	[ WORK_IN_PROGRESS, UNDER_REVIEW ]
		OBSOLETE			|	[ UNDER_REVIEW, APPROVED, WORK_IN_PROGRESS ]
	}

	def assertIllegalTransitions(def from, def tos) {
		tos.each {
			assert !from.isTransitionLegal(it)
		}
		true
	}

	@Unroll("specifications for the i18n key for #status")
	def "correctedness of the i18n"(){
		expect :
			expectedKey == status.getI18nKey()

		where :
			status 				|	expectedKey
			WORK_IN_PROGRESS  	| 	"requirement.status.WORK_IN_PROGRESS"
			UNDER_REVIEW		|	"requirement.status.UNDER_REVIEW"
			APPROVED			|	"requirement.status.APPROVED"
			OBSOLETE			|	"requirement.status.OBSOLETE"
	}

	@Unroll("requirement modifiable state should be #expected for status #status")
	def "correctedness of update allowance"(){

		expect :
			expected == status.requirementModifiable

		where :
			status				|	expected
			WORK_IN_PROGRESS	|	true
			UNDER_REVIEW		|	true
			APPROVED			|	false
			OBSOLETE			|	false
	}


	@Unroll("specifications for status update allowance for #status")
	def "correctedness for status update allowance"(){

		expect :
			expected == status.getAllowsStatusUpdate()

		where :
			status				|	expected
			WORK_IN_PROGRESS	|	true
			UNDER_REVIEW		|	true
			APPROVED			|	true
			OBSOLETE			|	false
	}

	@Unroll("requirement linkable state should be #linkable for status #status")
	def "requirement linkable state should be #linkable for status #status"(){

		expect :
			status.requirementLinkable == linkable

		where :
			status				|	linkable
			WORK_IN_PROGRESS	|	true
			UNDER_REVIEW		|	true
			APPROVED			|	true
			OBSOLETE			|	false
	}

}
