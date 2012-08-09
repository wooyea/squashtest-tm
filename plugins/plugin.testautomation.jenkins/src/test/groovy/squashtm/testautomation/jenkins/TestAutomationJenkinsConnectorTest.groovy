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
package squashtm.testautomation.jenkins

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.squashtest.csp.tm.domain.testautomation.TestAutomationServer;

import spock.lang.Specification

class TestAutomationJenkinsConnectorTest extends Specification {
	
	private TestAutomationJenkinsConnector connector
	
	def setup(){
		connector = new TestAutomationJenkinsConnector()
	}
	
	def "should return a well formatted query"(){
		
		given :
			TestAutomationServer server = new TestAutomationServer(new URL("http://ci.jruby.org"), "", "")
			
		when :
			def method = connector.newGetJobsMethod(server)
			
		then :
			method.path == "http://ci.jruby.org/api/json"
			method.queryString == "tree=jobs%5Bname%5D"
		
		
	}
	
}
