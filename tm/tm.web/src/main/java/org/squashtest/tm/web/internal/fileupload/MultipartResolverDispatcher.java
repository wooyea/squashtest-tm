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
package org.squashtest.tm.web.internal.fileupload;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


/**
 * <p>Will redirect a request to a specific MultipartResolver, with specific settings, with respect to the matched URL. This chain is
 * completely dumb and will pick the first match it finds, or the default if none was found.</p>
 * 
 * @author bsiri
 *
 */
public class MultipartResolverDispatcher extends CommonsMultipartResolver {
	
	private CommonsMultipartResolver defaultResolver;
	
	private Map<String, CommonsMultipartResolver> resolverMap;
	
	public void setResolverMap(Map<String, CommonsMultipartResolver> chain){
		this.resolverMap = chain;
	}
	
	public void setDefaultResolver(CommonsMultipartResolver defaultResolver){
		this.defaultResolver = defaultResolver;
	}
	
	@Override
	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		
		String target = request.getRequestURI();
		
		for (String matcher : resolverMap.keySet()){
			if (target.matches(matcher)){
				return resolverMap.get(matcher).resolveMultipart(request);
			}
		}
		
		//else
		return defaultResolver.resolveMultipart(request);
	}
	
	
}
