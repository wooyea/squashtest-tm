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
package org.squashtest.csp.tm.web.internal.interceptor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenedEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenedEntity.class);
	
	private Map<String, Integer> viewers;
	
	public OpenedEntity(){
		viewers = new HashMap<String, Integer>();
	}
	
	public boolean addViewer(String viewerLogin){
		boolean otherViewers = false;		
		//try to find viewer in list
		Integer numberOfViews = viewers.get(viewerLogin);
		//if already here increment number of his view for this entity
		if(numberOfViews != null){
			numberOfViews ++;
		}
		else{//else create input for this user
			viewers.put(viewerLogin, new Integer(1));
		}
		//if list of users is higher than 1 return true
		if(viewers.size()>1){
			otherViewers = true;
		}
		LOGGER.debug("Other Viewers = "+otherViewers);
		return otherViewers;
		
	}
	public void removeViewer(String viewerLogin){
		
	}
}
