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
package org.squashtest.tm.service.internal.batchimport

import javax.inject.Inject
import javax.inject.Provider

import org.hibernate.Hibernate
import org.hibernate.SessionFactory
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.testcase.ActionTestStep
import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode
import org.squashtest.tm.domain.testcase.TestCaseNature
import org.squashtest.tm.domain.testcase.TestCaseStatus
import org.squashtest.tm.domain.testcase.TestCaseType
import org.squashtest.tm.service.DbunitServiceSpecification
import org.squashtest.tm.service.customfield.CustomFieldValueFinderService
import org.squashtest.tm.service.internal.batchimport.Model.Existence
import org.squashtest.tm.service.testcase.TestCaseLibraryFinderService
import org.unitils.dbunit.annotation.DataSet
import static org.squashtest.tm.service.internal.batchimport.Messages.*
import static org.squashtest.tm.service.importer.ImportStatus.*

import spock.lang.Unroll
import spock.unitils.UnitilsSupport

/**
 <b> To walk you through that dataset :</b><br/><br/>
 1/ test cases and step types :<br/>
 ------------------------------<br/>
 <table>
 <tr><td>/Test Project-1/test 3</td><td>[Action Call Action]</td></tr>
 <tr><td>/Test Project-1/dossier 1/test case 1</td><td>[Action Action Action]</td></tr>
 <tr><td>/Test Project-1/dossier 1/test case 2</td><td>[Action Action]</td></tr>
 <tr><td>/Test Project-1/dossier 2/0 test case \/ with slash</td><td>[Action Action Call]</td></tr>
 <tr><td>/autre project/TEST A</td><td>[Call Action Call]</td></tr>
 <tr><td>/autre project/folder/TEST B</td><td>[Action Action]</td></tr>
 </table>
 <br/><br/>
 2/ which test case calls which one <br/>
 ------------------------------------<br/>
 <table>
 <tr><td>/autre project/TEST A</td><td>/Test Project-1/dossier 1/test case 2</td></tr>
 <tr><td>/autre project/TEST A</td><td>/Test Project-1/dossier 2/0 test case \/ with slash</td></tr>
 <tr><td>/Test Project-1/dossier 2/0 test case \/ with slash</td><td>/Test Project-1/test 3</td></tr>
 <tr><td>/Test Project-1/test 3</td><td>/Test Project-1/dossier 1/test case 1</td></tr>
 </table>
 <br/><br/>
 3/ test cases and parameters :<br/>
 ------------------------------<br/>
 <table>
 <tr><td>/Test Project-1/test 3</td><td>param_test_3, reparam_test_3</td></tr>
 <tr><td>/Test Project-1/dossier 1/test case 1</td><td></td></tr>
 <tr><td>/Test Project-1/dossier 1/test case 2 </td><td></td></tr>
 <tr><td>/Test Project-1/dossier 2/0 test case \/ with slash</td><td>param_test_0</td></tr>
 <tr><td>/autre project/TEST A</td><td>param_A</td></tr>
 <tr><td>/autre project/folder/TEST B</td><td></td></tr></table><br/><br/>
 4/ test cases and datasets :<br/>
 -----------------------------<br/>
 <table>
 <tr><td>/Test Project-1/dossier 2/0 test case \/ with slash</td><td>dataset_with_slash</td></tr>
 <tr><td>/autre project/TEST A</td><td>ultimate ds</td></tr></table><br/><br/>
 5/ custom fields :<br/>
 ----------------------------<br/>
 <table>
 <tr><td>text test case : text, TXT_TC, mandatory</td><td>(proj1, test case)</td></tr>
 <tr><td>check test case : checkbox, CK_TC, optional</td><td>(proj1, test case), (proj2, test case)</td></tr>
 <tr><td>DATE : date_picker, DATE, optional</td><td>(proj2, test case), (proj2, test step)</td></tr>
 <tr><td>list step : dropdown list, LST_ST, optional</td><td>(proj1, test step), (proj2, test step)</td></tr>
 */
@UnitilsSupport
@Transactional
@RunWith(Sputnik)
public class FacilityImplIT extends DbunitServiceSpecification {



	@Inject
	private TestCaseLibraryFinderService finder

	@Inject
	private CustomFieldValueFinderService cufFinder

	@Inject
	private SessionFactory sessionFactory

	@Inject
	Provider<FacilityImpl> implProvider


	SimulationFacility simulator

	FacilityImpl impl

	Model model

	def setup(){

		impl = implProvider.get()

		addMixins()
	}


	def commit(){
		getSession().getTransaction().commit()
	}

	def flush(){
		getSession().flush()
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should create a new test case, some attributes are specified and some are left to default"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/Test Project-1/dossier 2/mytestcase")

		TestCase tc = emptyTC()
		stuffWith(tc, [name:"mytestcase", description :"<p>ouaaahpaaa</p>", nature: TestCaseNature.SECURITY_TESTING])

		def cufs = [
			"TXT_TC" : "shazam",
			"CK_TC" : "false",
			"inexistant" : "azeaer"
		]


		when :
		LogTrain logtrain = impl.createTestCase(target, tc, cufs)

		flush()

		then :

		logtrain.hasCriticalErrors() == false

		TestCase t = (TestCase)finder.findNodeByPath(target.path)

		def storedcufs = cufFinder.findAllCustomFieldValues t


		t.id != null
		t.name == "mytestcase"
		t.description == "<p>ouaaahpaaa</p>"
		t.nature == TestCaseNature.SECURITY_TESTING
		t.status == TestCaseStatus.WORK_IN_PROGRESS
		t.importanceAuto == Boolean.FALSE
		t.importance == TestCaseImportance.LOW

		storedcufs.size() == 2
		storedcufs.hasCuf "TXT_TC", "shazam"
		storedcufs.hasCuf "CK_TC", "false"
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should not create a test case"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/flawed target/flawed test")
		TestCase tc = new TestCase(name:"")
		def cufs = new HashMap()


		when :
		LogTrain logtrain = impl.createTestCase(target, tc, cufs)
		flush()

		then :
		finder.findNodeByPath(target.path) == null
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should update a test case"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/Test Project-1/test 3")

		TestCase tc = emptyTC()
		stuffWith(tc, [name : "renamed", description : "this description has been modified", importance : TestCaseImportance.HIGH, reference : "modified"])

		def cufs = [ TXT_TC : "changed the cuf value"]


		when :
		LogTrain train = impl.updateTestCase(target, tc, cufs)

		flush()


		then :
		train.hasCriticalErrors() == false

		TestCase t = (TestCase) finder.findNodeByPath("/Test Project-1/renamed")

		def storedcufs = cufFinder.findAllCustomFieldValues t

		// the modified values
		t.name == "renamed"
		t.reference == "modified"
		t.description == "this description has been modified"
		t.importance == TestCaseImportance.HIGH
		storedcufs.hasCuf "TXT_TC" ,"changed the cuf value"

		// the unmodified values
		t.id == 245l
		t.importanceAuto == false
		t.nature == TestCaseNature.BUSINESS_TESTING
		t.status == TestCaseStatus.WORK_IN_PROGRESS
		t.type == TestCaseType.REGRESSION_TESTING
		storedcufs.hasCuf  "CK_TC" , "false"

	}
	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should not create a test case instead of updating it"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/Test Project-1/dossier 2/inexistant")

		TestCase tc = emptyTC()
		stuffWith(tc, [name:"inexistant"])

		def cufs = [:]


		when :
		LogTrain logtrain = impl.updateTestCase(target, tc, cufs)

		flush()

		TestCase found = (TestCase)finder.findNodeByPath(target.path)

		then :

		logtrain.hasCriticalErrors() == true

		found == null
	}
	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should create a test case with a different name because of name clash"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/Test Project-1/dossier 1/test case 1")

		TestCase tc = emptyTC()
		stuffWith(tc, [name:"test case 1", description : "special description"])

		def cufs = [:]


		when :
		LogTrain logtrain = impl.createTestCase(target, tc, cufs)

		flush()

		TestCase found = impl.validator.model.get(target)

		then :

		logtrain.hasCriticalErrors() == false

		logtrain.hasSuchError ERROR_TC_ALREADY_EXISTS , WARNING


		found.id != null
		found.id != 242l
		found.name == "test case 1 (1)"	// means test case 1 with at least one extra character
		found.description == "special description"
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should delete a test case"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/autre project/TEST A")

		when :
		LogTrain train = impl.deleteTestCase(target)

		flush()

		then :
		train.hasCriticalErrors() == false
		allDeleted("TestCase", [246l])
		impl.validator.model.getStatus(target).status == Existence.NOT_EXISTS
	}


	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should not delete a test case because it's being called"(){

		given :
		TestCaseTarget target = new TestCaseTarget("/Test Project-1/dossier 1/test case 2")

		when :
		LogTrain train = impl.deleteTestCase(target)

		then :
		train.hasCriticalErrors() == true
		train.hasSuchError ERROR_REMOVE_CALLED_TC ,  FAILURE
	}



	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@Unroll("should add an action step to a test case at last position because of #humanmsg")
	@DataSet("batchimport.sandbox.xml")
	def "should add an action step to a test case at the end"(){

		given :
		TestStepTarget target = new TestStepTarget(new TestCaseTarget("/Test Project-1/dossier 1/test case 2"),idx)
		ActionTestStep astep = new ActionTestStep(action:"new action", expectedResult : "new expectedResult")
		def cufs = [LST_ST: "b"]

		when :
		LogTrain train = impl.addActionStep(target, astep, cufs)

		flush()

		then :

		train.hasCriticalErrors() == false

		TestCase found = (TestCase)finder.findNodeByPath("/Test Project-1/dossier 1/test case 2")

		found.steps.size() == 3
		found.steps[2].action == "new action"
		found.steps[2].expectedResult == "new expectedResult"

		def storedcufs = cufFinder.findAllCustomFieldValues found.steps[2]

		storedcufs.hasCuf "LST_ST", "b"

		where :
		idx						|	humanmsg
		null					|	"because null index"
		4						|	"because excessive index"
		-1						|	"because negative index"
		2						|	"because that was what we wanted indeed"
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should insert a call step at the correct position"(){

		given :
		TestCaseTarget callertc = new TestCaseTarget("/Test Project-1/dossier 1/test case 2")
		TestCaseTarget calledtc = new TestCaseTarget("/autre project/folder/TEST B")

		TestStepTarget steptarget = new TestStepTarget(callertc, 1)
		CallTestStep callstep = new CallTestStep()
		ActionTestStep actionStepBackup = new ActionTestStep()


		when :
		LogTrain train = impl.addCallStep(steptarget, callstep, calledtc, actionStepBackup)

		flush()

		then :

		train.hasCriticalErrors() == false

		TestCase found = (TestCase)finder.findNodeByPath("/Test Project-1/dossier 1/test case 2")

		found.steps.size() == 3
		found.steps[1].calledTestCase.id == 248l

		impl.validator.model.isCalledBy(calledtc, callertc)
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should reject a call step because of cycle"(){

		given :
		TestCaseTarget callertc = new TestCaseTarget("/Test Project-1/dossier 1/test case 1")
		TestCaseTarget calledtc = new TestCaseTarget("/autre project/TEST A")

		TestStepTarget steptarget = new TestStepTarget(callertc, 1)
		CallTestStep callstep = new CallTestStep()
		ActionTestStep actionStepBackup = new ActionTestStep()

		when :
		LogTrain train = impl.addCallStep(steptarget, callstep, calledtc, actionStepBackup)

		flush()

		then :

		train.hasCriticalErrors() == true

		train.entries

		! impl.validator.model.isCalledBy(calledtc, callertc)
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should update an action step"(){

		given :
		TestCaseTarget tc = new TestCaseTarget("/Test Project-1/dossier 1/test case 1")
		TestStepTarget target = new TestStepTarget(tc, 0)
		ActionTestStep astep = new ActionTestStep(action:"updated action")
		def cufs = [ "LST_ST" : "c" ]

		when :
		LogTrain train = impl.updateActionStep(target, astep, cufs)

		flush()

		def updatedstep = getSession().get(ActionTestStep, 168l)
		def updatedcufs = cufFinder.findAllCustomFieldValues updatedstep

		then :

		train.hasCriticalErrors() == false

		updatedstep.action == "updated action"
		updatedstep.expectedResult == "<p>result 1</p>"	//unmodified

		updatedcufs.hasCuf "LST_ST", "c"

	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	@Unroll("should not update an action step because #humanmsg")
	def "should not update an action step"(){
		given :
		TestStepTarget target = new TestStepTarget(new TestCaseTarget("/Test Project-1/test 3"),idx)
		ActionTestStep astep = new ActionTestStep(action:"new action", expectedResult : "new expectedResult")
		def cufs = [LST_ST: "b"]

		when :
		LogTrain train = impl.updateActionStep(target, astep, cufs)

		flush()

		then :
		train.hasCriticalErrors() == true
		train.hasSuchError msg ,  status

		where :
		idx						|	humanmsg											|	msg							| status
		null					|	"null index"										|	ERROR_STEPINDEX_EMPTY		| FAILURE
		4						|	"excessive index"									|	ERROR_STEPINDEX_OVERFLOW	| FAILURE
		-1						|	"negative index"									|	ERROR_STEPINDEX_NEGATIVE	| FAILURE
		1						|	"trying to update a call step with an action step"	|	ERROR_NOT_AN_ACTIONSTEP		| FAILURE
	}

	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should update a call step (change the called test case)"(){

		given : "instead of '/Test Project-1/dossier 2/0 test case \\/ with slash', will now call '/autre project/folder/TEST B'"

		TestCaseTarget callertc = new TestCaseTarget("/autre project/TEST A")
		TestCaseTarget calledtc = new TestCaseTarget("/autre project/folder/TEST B")

		TestStepTarget steptarget = new TestStepTarget(callertc, 2)
		CallTestStep callstep = new CallTestStep()
		ActionTestStep actionStepBackup = new ActionTestStep()

		when :
		LogTrain train = impl.updateCallStep(steptarget, callstep, calledtc, actionStepBackup)

		flush()

		then :

		train.hasCriticalErrors() == false

		TestCase found = (TestCase)finder.findNodeByPath("/autre project/TEST A")

		found.steps.size() == 3
		found.steps[2].calledTestCase.id == 248l

		impl.validator.model.isCalledBy(calledtc, callertc)
	}
	/**
	 *
	 * See  {@link FacilityImplIT} for dataset description
	 *
	 */
	@DataSet("batchimport.sandbox.xml")
	def "should not update a call step because the target would create a cycle"(){

		given : "instead of '/Test Project-1/dossier 1/test case 2', will now call '/Test Project-1/test 3'"

		TestCaseTarget callertc = new TestCaseTarget("/Test Project-1/test 3")
		TestCaseTarget calledtc = new TestCaseTarget("/autre project/TEST A")

		TestStepTarget steptarget = new TestStepTarget(callertc, 1)
		CallTestStep callstep = new CallTestStep()
		ActionTestStep actionTestStepBackup = new ActionTestStep()

		when :
		LogTrain train = impl.updateCallStep(steptarget, callstep, calledtc, actionTestStepBackup)

		flush()

		then :

		train.hasCriticalErrors() == true

		long id = finder.findNodeIdByPath("/Test Project-1/test 3")
		TestCase found = sessionFactory.currentSession.load(TestCase, id)

		found.steps.size() == 3
		found.steps[1].calledTestCase.id == 242l // and not 246l

		! impl.validator.model.isCalledBy(calledtc, callertc)

	}



	@DataSet("batchimport.sandbox.xml")
	def "should not update an action step because the target would create a cycle"(){

		given : "instead of '/Test Project-1/dossier 1/test case 2', will now call '/Test Project-1/test 3'"

		TestCaseTarget callertc = new TestCaseTarget("/Test Project-1/test 3")
		TestCaseTarget calledtc = new TestCaseTarget("/autre project/TEST A")

		TestStepTarget steptarget = new TestStepTarget(callertc, 1)
		CallTestStep callstep = new CallTestStep()
		ActionTestStep actionTestStepBackup = new ActionTestStep()

		when :
		LogTrain train = impl.updateCallStep(steptarget, callstep, calledtc, actionTestStepBackup)

		flush()

		then :

		train.hasCriticalErrors() == true

		long id = finder.findNodeIdByPath("/Test Project-1/test 3")
		TestCase found = sessionFactory.currentSession.load(TestCase, id)

		found.getSteps().size() == 3
		found.getSteps()[1].calledTestCase.id == 242l // and not 246l

		! impl.validator.model.isCalledBy(calledtc, callertc)

	}

	// ********************* private stuffs **********************


	def addMixins(){
		Collection.metaClass.mixin(CufsPredicates)
		LogTrain.metaClass.mixin(LogsPredicates)
	}

	def emptyTC(){
		return TestCase.createBlankTestCase()
	}

	def stuffWith(tc, attributes){
		attributes.each { k,v -> tc[k] = v }
	}

}

class CufsPredicates{
	static hasCuf(cufs, code, value){
		cufs.find { it.customField.code == code }.value == value
	}

}

class LogsPredicates{

	static hasSuchError(train, error, status){
		train.entries.find { it.i18nError == error}.status == status
	}
}

