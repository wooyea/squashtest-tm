/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.domain.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.ParameterizedBridge;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

public class CUFBridge extends SessionFieldBridge implements ParameterizedBridge {

	private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private String type = "";
	
	private List<CustomFieldValue> findCufValuesForType(Session session, Object value, String type){
		
		BindableEntity entityType = null; 
		Long id = null;
		
		if("testcase".equals(type)){
			TestCase testcase = (TestCase) value;
			id = testcase.getId();
			entityType = BindableEntity.TEST_CASE;
			
		} else if ("requirement".equals(type)){
			RequirementVersion requirement = (RequirementVersion) value;
			id = requirement.getId();
			entityType = BindableEntity.REQUIREMENT_VERSION;
		}
				
		return (List<CustomFieldValue>) session
				.createCriteria(CustomFieldValue.class) 
				.add(Restrictions.eq("boundEntityId", id))
				.add(Restrictions.eq("boundEntityType", entityType)).list();
	}

	@Override
	public void setParameterValues(Map<String, String> parameters) {
		if(parameters.containsKey("type")){
			this.type = (String) parameters.get("type");
		}
	}

	@Override
	protected void writeFieldToDocument(String name, Session session, Object value, Document document, LuceneOptions luceneOptions) {
		
		List<CustomFieldValue> cufValues = findCufValuesForType(session, value, type);

		for (CustomFieldValue cufValue : cufValues) {
			
			InputType inputType = cufValue.getBinding().getCustomField().getInputType();
			
			if(org.squashtest.tm.domain.customfield.InputType.DATE_PICKER.equals(inputType)){
				String code = cufValue.getBinding().getCustomField().getCode();
				Date inputDate = null;
				try {
					inputDate = inputFormat.parse(cufValue.getValue());
					Field field = new Field(code, dateFormat.format(inputDate), luceneOptions.getStore(),
						    luceneOptions.getIndex(), luceneOptions.getTermVector() );
						    field.setBoost( luceneOptions.getBoost());
						    document.add(field);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if(org.squashtest.tm.domain.customfield.InputType.DROPDOWN_LIST.equals(inputType)) {

				String code = cufValue.getBinding().getCustomField().getCode();
				String val = cufValue.getValue();
				if("".equals(val)){
					val = "$NO_VALUE";
				}
				
				Field field = new Field(code, val,
						luceneOptions.getStore(), luceneOptions.getIndex(),
						luceneOptions.getTermVector());
				field.setBoost(luceneOptions.getBoost());
				document.add(field);		
				
			} else {

				String code = cufValue.getBinding().getCustomField().getCode();
				Field field = new Field(code, cufValue.getValue(),
						luceneOptions.getStore(), luceneOptions.getIndex(),
						luceneOptions.getTermVector());
				field.setBoost(luceneOptions.getBoost());
				document.add(field);				
			} 
		}
	}

}
