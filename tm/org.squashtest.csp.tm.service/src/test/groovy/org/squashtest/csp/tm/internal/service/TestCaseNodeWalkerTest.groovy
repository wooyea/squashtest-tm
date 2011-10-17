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
package org.squashtest.csp.tm.internal.service


import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder 
import spock.lang.Specification;

class TestCaseNodeWalkerTest extends Specification {
	
   /*
	* test :
	*
	* - all the test cases above are included (1)
	* - none of the test case folders are part of the result (2)
	* - tc1 is in first position, tc3 in fourth position, tc5 in last position (3)
	* - the order of other test cases inbetween is undefined (4)
	*/
	
	def "should walk reccursively in a test case hierarchy and return the test cases"(){
		
		given : "declare the entities"
			def tc1 = new TestCase(name:"tc1")
			def tc3 = new TestCase(name:"tc3")
			def tc5 = new TestCase(name:"tc5")
			def tc21 = new TestCase(name:"tc21")
			def tc22 = new TestCase(name:"tc22")
			def tc41 = new TestCase(name:"tc41")
			def tc43 = new TestCase(name:"tc42")
			def tc421 = new TestCase(name:"tc411")
		
			def fold2 = new TestCaseFolder(name:"folder2")
			def fold4 = new TestCaseFolder(name:"folder4")
			def fold42 = new TestCaseFolder(name:"folder42")
			
		and : "bind them together"
			fold2.addContent(tc21)
			fold2.addContent(tc22)
			
			fold4.addContent(tc41)
			fold4.addContent(fold42)
			fold4.addContent(tc43)
			
			fold42.addContent(tc421)
			
		and : "expected results"
			def resultList = [tc1, tc21, tc22, tc3, tc41, tc421, tc43, tc5]
			def noFolder = [fold2, fold4, fold42]
			def fixedTcPos = [0 : tc1, 3 : tc3, 7 : tc5]
			def floatingTcPos = [ 
					[ index : [1, 2], 
					  tcs : [tc21, tc22] ],
					[ index : [4, 5, 6],
					  tcs : [tc41, tc421, tc43] ]
				]
								
			
		when : "now walk"
			def result = new TestCaseNodeWalker().walk([tc1, fold2, tc3, fold4, tc5]) 
			
		then : "test"
			//test (1)
			result.size() == resultList.size()
			result.containsAll(resultList)
			
			//test (2)
			noFolder.collect{result.contains it} == [false, false, false]
			
			
			//test (3)
			fixedTcPos.collect { result[it.key] == it.value } == [true, true, true]
	
		
			//test (4)
			floatingTcPos.collect { result[it.index].containsAll(it.tcs)  } == [true, true] 
		
	}

}
