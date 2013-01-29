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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;

public interface CustomFieldValueDao {

	
	/**
	 * 'nuff said.
	 * @param newValue
	 */
	void persist(CustomFieldValue newValue);
	

	
	/**
	 * 
	 * 'nuff said.
	 * @param value
	 */
	void delete(CustomFieldValue value);
	
	
	/**
	 * Delete all the CustomFieldValue, given their ids.
	 * 
	 * @param ids
	 */
	void deleteAll(@QueryParam("ids") List<Long> ids);
	
	
	
	/**
	 * Delete all the CustomFieldValue related to a {@link CustomFieldBinding}, given its id.
	 * 
	 * @param bindingId
	 */
	void deleteAllForBinding(@QueryParam("bindingId") Long bindingId);
	
	
	/**
	 * Delete all the custom field values related to a BoundEntity, identified by its id and BindableEntity
	 * 
	 * @param entityId
	 * @param entity
	 */
	void deleteAllForEntity(@QueryParam("entityId") Long entityId, @QueryParam("entityType") BindableEntity entity);

	
	/**
	 * Delete all the custom field values related to a bunch of bound entities
	 * 
	 * @param entityTpe
	 * @param entityIds
	 */
	void deleteAllForEntities(@QueryParam("entityType") BindableEntity entityType, @QueryParam("entityIds") List<Long> entityIds);
	

	
	/**
	 * 'nuff said. 
	 * @param id
	 * @return
	 */
	CustomFieldValue findById(Long id);
	
	
	
	/**
	 * returns the list of {@link CustomFieldValue} for the given entity, sorted according to the 
	 * order specified in their respective {@link CustomFieldBinding}.
	 * 
	 * @param entityId
	 * @param entityType
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValues(long entityId, BindableEntity entityType);

	
	/**
	 * Same as above, list version.
	 * 
	 * @param entityIds
	 * @param entityType
	 * @return
	 */
	List<CustomFieldValue> batchedFindAllCustomValues(@QueryParam("entityIds") List<Long> entityIds, @QueryParam("entityType") BindableEntity entityType);
	
	
	/**
	 * returns all the {@link CustomFieldValue} related to a given {@link CustomFieldBinding}, sorted according to
	 * their custom field binding order.
	 * 
	 * @param customFieldBindingId
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValuesOfBinding(long customFieldBindingId);

	
	/**
	 * returns all the CustomFieldValue related to a list of CustomFieldBinding, the resulting elements will be
	 * returned in unspecified order
	 * @param customFieldBindingIds
	 * @return
	 */
	List<CustomFieldValue> findAllCustomValuesOfBindings(@QueryParam("bindingIds") List<Long>customFieldBindingIds);	
	

	/**
	 * Will return instances of {@link CustomFieldValuesPair}, that will pair two {@link CustomFieldValue} that represents the same
	 * CustomFieldBinding. Those two CustomFieldValue belongs to two BoundEntity as specified by the parameters. 
	 * One of them is considered as the original and the other one is the copy.
	 * 
	 * @param entity
	 * @param origEntityId
	 * @param copyEntityId
	 * @return
	 */
	List<CustomFieldValuesPair> findPairedCustomFieldValues(@QueryParam("entityType") BindableEntity entity, 
								@QueryParam("origEntityId") Long origEntityId, @QueryParam("copyEntityId") Long copyEntityId);
	
	
	
	public static final class CustomFieldValuesPair{
		
		private CustomFieldValue original;
		private CustomFieldValue recipient;
		
		public CustomFieldValuesPair(){
			super();
		}
		
		public CustomFieldValuesPair(CustomFieldValue original,
				CustomFieldValue recipient) {
			super();
			this.original = original;
			this.recipient = recipient;
		}
		
		
		public void setOriginal(CustomFieldValue original) {
			this.original = original;
		}

		public void setRecipient(CustomFieldValue recipient) {
			this.recipient = recipient;
		}

		
		public CustomFieldValue getOriginal() {
			return original;
		}

		public CustomFieldValue getRecipient() {
			return recipient;
		}

		/**
		 * copies the value of the original CustomFieldValue into the value of the recipient CustomFieldValue
		 */
		public void copyContent(){
			recipient.setValue(original.getValue());
		}
	}
	
}
