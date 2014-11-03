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
package org.squashtest.tm.web.internal.controller.execution;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.MultiSelectFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedMultiSelectField;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

class ExecutionStepDataTableModelHelper extends DataTableModelBuilder<ExecutionStep> { // NOSONAR this cannot be made final because there are subclasses of it already


	private static final int DEFAULT_MAP_CAPACITY = 16;

	Locale locale;
	InternationalizationHelper messageSource;
	boolean isAutomated = false;

	private int nbCufsPerEntity;
	private int nbDenoPerEntity;

	private Map<Long, Map<String, CustomFieldValueTableModel>> customFieldValuesById;
	private Map<Long, Map<String, CustomFieldValueTableModel>> denormalizedFieldValuesById;



	ExecutionStepDataTableModelHelper(Locale locale, InternationalizationHelper messageSource, boolean isAutomated) {
		this.locale = locale;
		this.messageSource = messageSource;
		this.isAutomated = isAutomated;
	}

	@Override
	public Map<Object, Object> buildItemData(ExecutionStep item) {

		Map<Object, Object> res = new HashMap<Object, Object>(13 + nbCufsPerEntity + nbDenoPerEntity);

		res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, item.getExecutionStepOrder() + 1);
		res.put("action", item.getAction());
		res.put("expected", item.getExpectedResult());
		res.put("last-exec-on", formatDate(item.getLastExecutedOn(), locale));
		res.put("last-exec-by", item.getLastExecutedBy());
		res.put("comment", item.getComment());
		res.put("bug-list", ExecutionModificationController.createBugList(item));
		res.put("bug-button", "");
		res.put(DataTableModelConstants.DEFAULT_NB_ATTACH_KEY, item.getAttachmentList().size());
		res.put(DataTableModelConstants.DEFAULT_ATTACH_LIST_ID_KEY, item.getAttachmentList().getId());
		res.put("run-step-button", "");

		// XXX : why this ?
		String status = (isAutomated) ? "--" : ExecutionModificationController.localizedStatus(item.getExecutionStatus(), locale, messageSource);
		res.put("status", status);

		appendCustomFields(res);
		appendDenoFields(res);

		return res;
	}


	// *********************** handling of custom fields *****************************

	public void usingCustomFields(Collection<CustomFieldValue> cufValues) {
		usingCustomFields(cufValues, DEFAULT_MAP_CAPACITY);
	}

	public void usingCustomFields(Collection<CustomFieldValue> cufValues, int nbFieldsPerEntity) {
		customFieldValuesById = new HashMap<Long, Map<String, CustomFieldValueTableModel>>();
		nbCufsPerEntity = nbFieldsPerEntity;

		for (CustomFieldValue value : cufValues) {
			Long entityId = value.getBoundEntityId();
			Map<String, CustomFieldValueTableModel> values = customFieldValuesById.get(entityId);

			if (values == null) {
				values = new HashMap<String, CustomFieldValueTableModel>(nbCufsPerEntity);
				customFieldValuesById.put(entityId, values);
			}

			values.put(value.getCustomField().getCode(), new CustomFieldValueTableModel(value));

		}
	}


	private void appendCustomFields(Map<Object, Object> item) {
		Map<String, CustomFieldValueTableModel> cufValues = getCustomFieldsFor((Long) item.get(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY));
		item.put("customFields", cufValues);

	}

	private Map<String, CustomFieldValueTableModel> getCustomFieldsFor(Long id) {
		if (customFieldValuesById == null) {
			return new HashMap<String, CustomFieldValueTableModel>();
		}

		Map<String, CustomFieldValueTableModel> values = customFieldValuesById.get(id);

		if (values == null) {
			values = new HashMap<String, CustomFieldValueTableModel>();
		}
		return values;

	}


	// *********************** handling of denormalized fields *****************************

	public void usingDenoFields(Collection<DenormalizedFieldValue> cufValues) {
		usingDenormalizedFields(cufValues, DEFAULT_MAP_CAPACITY);
	}

	public void usingDenormalizedFields(Collection<DenormalizedFieldValue> dfvValues, int nbFieldsPerEntity) {

		denormalizedFieldValuesById = new HashMap<Long, Map<String, CustomFieldValueTableModel>>();
		nbDenoPerEntity = nbFieldsPerEntity;

		for (DenormalizedFieldValue value : dfvValues) {
			Long entityId = value.getDenormalizedFieldHolderId();
			Map<String, CustomFieldValueTableModel> values = denormalizedFieldValuesById.get(entityId);

			if (values == null) {
				values = new HashMap<String, CustomFieldValueTableModel>(nbDenoPerEntity);
				denormalizedFieldValuesById.put(entityId, values);
			}

			values.put(value.getCode(), new CustomFieldValueTableModel(value));

		}
	}


	private void appendDenoFields(Map<Object, Object> item) {
		Map<String, CustomFieldValueTableModel> cufValues = getDenoFieldsFor((Long) item.get(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY));
		item.put("denormalizedFields", cufValues);

	}

	private Map<String, CustomFieldValueTableModel> getDenoFieldsFor(Long id) {
		if (denormalizedFieldValuesById == null) {
			return new HashMap<String, CustomFieldValueTableModel>();
		}

		Map<String, CustomFieldValueTableModel> values = denormalizedFieldValuesById.get(id);

		if (values == null) {
			values = new HashMap<String, CustomFieldValueTableModel>();
		}
		return values;

	}

	// ************************ inner class ***********************************************************

	protected static class CustomFieldValueTableModel {
		private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionStepDataTableModelHelper.CustomFieldValueTableModel.class);

		private String value;
		private List<String> values;
		private Long id;

		@SuppressWarnings("unused")
		public Object getValue() {
			return (value != null) ? value : values;
		}

		@SuppressWarnings("unused")
		public Long getId() {
			return id;
		}

		@SuppressWarnings("unused")
		public CustomFieldValueTableModel() {
			super();
		}

		@SuppressWarnings("unused")
		public Date getValueAsDate() {
			try {
				return DateUtils.parseIso8601Date(value);
			} catch (ParseException e) {
				LOGGER.debug("Unable to parse date {} of custom field #{}", value, id);
			}

			return null;
		}

		private CustomFieldValueTableModel(CustomFieldValue value) {
			this.id = value.getId();

			if (MultiSelectFieldValue.class.isAssignableFrom(value.getClass())) {
				this.values = ((MultiSelectFieldValue)value).getValues();
			}
			else{
				this.value = value.getValue();
			}
		}

		private CustomFieldValueTableModel(DenormalizedFieldValue value) {
			this.id = value.getId();
			if (DenormalizedMultiSelectField.class.isAssignableFrom(value.getClass())) {
				this.values = ((DenormalizedMultiSelectField)value).getValues();
			}
			else{
				this.value = value.getValue();
			}
		}
	}

	private String formatDate(Date date, Locale locale) {
		return messageSource.localizeDate(date, locale);
	}

}