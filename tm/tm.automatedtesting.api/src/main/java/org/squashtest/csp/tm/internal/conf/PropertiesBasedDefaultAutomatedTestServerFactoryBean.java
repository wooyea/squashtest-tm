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
package org.squashtest.csp.tm.internal.conf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.squashtest.csp.tm.domain.automatest.AutomatedTestServer;

/*
 * That class exists because the good old PropertyPlaceholderConfigurer won't work. There is another one 
 * configured in the context of tm.service (the one configuring the bugtracker) that will fail 
 * when the property is not set.
 * 
 */
public class PropertiesBasedDefaultAutomatedTestServerFactoryBean implements FactoryBean<AutomatedTestServer>{

	private static final Logger logger = LoggerFactory.getLogger(PropertiesBasedDefaultAutomatedTestServerFactoryBean.class);
	
	private static final String DEFAULT_URL_KEY = "tm.test.automation.server.defaulturl";
	private static final String DEFAULT_LOGIN_KEY = "tm.test.automation.server.defaultlogin";
	private static final String DEFAULT_PASSWORD_KEY = "tm.test.automation.server.defaultpassword";
	
	
	
	@Inject
	@Qualifier("squashtest.tm.ta.defaults")
	private Properties defaultsProperties;
	
	
	
	public void setDefaultsProperties(Properties defaultsProperties) {
		this.defaultsProperties = defaultsProperties;
	}

	@Override
	public AutomatedTestServer getObject() throws Exception {
		//there's no point in going through all the singleton synchronizing plumbing
		//here because there will be only one call to that method.
		
		AutomatedTestServer defaultServer = new AutomatedTestServer();
		
		String baseStrUrl = defaultsProperties.getProperty(DEFAULT_URL_KEY, "");
		/*
		try{
			URL baseURL = new URL(baseStrUrl);
		}catch(MalformedURLException ex){
			if (LOGGER.)
		}
		
		
		defaultServer.setBaseURL(baseStrUrl)/*/
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return AutomatedTestServer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	
	
}
