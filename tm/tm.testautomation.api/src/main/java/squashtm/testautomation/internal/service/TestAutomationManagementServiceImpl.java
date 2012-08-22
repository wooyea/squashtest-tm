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
package squashtm.testautomation.internal.service;

import java.net.URL;
import java.util.Collection;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import squashtm.testautomation.domain.TestAutomationProject;
import squashtm.testautomation.domain.TestAutomationServer;
import squashtm.testautomation.repository.TestAutomationProjectDao;
import squashtm.testautomation.repository.TestAutomationServerDao;
import squashtm.testautomation.spi.TestAutomationConnector;

@Transactional
public class TestAutomationManagementServiceImpl implements  InsecureTestAutomationManagementService{

	@Inject
	private TestAutomationServerDao serverDao;
	
	@Inject
	private TestAutomationProjectDao projectDao;
	
	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;

	
	@Inject
	private TestAutomationServer defaultServer;
	
	
	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) {
		
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());
		
		connector.checkCredentials(server);	
		
		return connector.listProjectsOnServer(server);
	}
	
	
	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(URL serverURL, String login, String password) {
			
		TestAutomationServer server = new TestAutomationServer(serverURL, login, password);
		
		return listProjectsOnServer(server);
		
	}

	
	//from the insecure interface
	@Override
	public TestAutomationProject persistOrAttach(TestAutomationProject newProject) {
		
		TestAutomationServer inBaseServer = serverDao.uniquePersist(newProject.getServer());
				
		return projectDao.uniquePersist(newProject.newWithServer(inBaseServer));
		
	}


	//from the insecure interface
	@Override
	public TestAutomationServer getDefaultServer() {
		return defaultServer;
	}

}
