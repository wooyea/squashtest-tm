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
package org.squashtest.tm.service.denormalizedfield;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;

/**
 * 
 * @author mpagnon
 *
 */
@Transactional(readOnly=true)
public interface DenormalizedFieldValueFinder {

	/**
	 * Will return all {@link DenormalizedFieldValue} attached to the given {@link DenormalizedFieldHolder} ordered by dfv.position asc.
	 * 
	 * @param denormalizedFieldHolder
	 * @return a list of {@link DenormalizedFieldValue} ordered by position asc.
	 */
	List<DenormalizedFieldValue> findAllForEntity(DenormalizedFieldHolder denormalizedFieldHolder);
	
	/**
	 * Will return all {@link DenormalizedFieldValue} attached to the given {@link DenormalizedFieldHolder} and having the given {@link RenderingLocation}, ordered by dfv.position asc.
	 * 
	 * @param denormalizedFieldHolder
	 * @param renderingLocation
	 * @return
	 */
	List<DenormalizedFieldValue> findAllForEntityAndRenderingLocation(DenormalizedFieldHolder denormalizedFieldHolder, RenderingLocation renderingLocation);
	
	
	/**
	 * Will return all the {@link DenormalizedFieldValue} for all the supplied {@link DenormalizedFieldHolder}. If the collection of 
	 * locations is null, then the result won't be filtered by location. If it is non null, then only the denormalized field values 
	 * displayed in at least one of those locations wil be returned.
	 * 
	 * @param entities
	 * @param location
	 * @return
	 */
	List<DenormalizedFieldValue> findAllForEntities(Collection<DenormalizedFieldHolder> entities, Collection<RenderingLocation> nullOrlocations);
	
}
