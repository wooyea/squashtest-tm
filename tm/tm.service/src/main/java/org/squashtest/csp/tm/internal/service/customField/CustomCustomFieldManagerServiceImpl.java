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
package org.squashtest.csp.tm.internal.service.customField;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.customfield.CustomField;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.CustomFieldDao;
import org.squashtest.csp.tm.service.customfield.CustomCustomFieldManagerService;
/**
 * 
 * @author mpagnon
 *
 */
@Service("CustomCustomFieldManagerService")
public class CustomCustomFieldManagerServiceImpl implements CustomCustomFieldManagerService{
	
	@Inject
	private CustomFieldDao customFieldDao;
	
	@Override
	public List<CustomField> findAllOrderedByName() {
		return customFieldDao.finAllOrderedByName();
	}

	@Override
	public FilteredCollectionHolder<List<CustomField>> findSortedCustomFields(CollectionSorting filter) {
		List<CustomField> customFields = customFieldDao.findSortedCustomFields(filter);
		long count = customFieldDao.countCustomFields();
		return new FilteredCollectionHolder<List<CustomField>>(count, customFields);
	}

}
