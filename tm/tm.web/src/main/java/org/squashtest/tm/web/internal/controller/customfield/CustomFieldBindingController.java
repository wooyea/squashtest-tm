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
package org.squashtest.tm.web.internal.controller.customfield;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.osgi.extensions.annotation.ServiceReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldBindingModel;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldJsonConverter;
import org.squashtest.tm.web.internal.model.customfield.CustomFieldModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTablePaging;


@Controller
@RequestMapping("/custom-fields-binding")
public class CustomFieldBindingController {

	private CustomFieldBindingModificationService service;

	
	@Inject
	private CustomFieldJsonConverter converter;
	
	
	@ServiceReference
	public void setCustomFieldBindingModificationService(CustomFieldBindingModificationService service){
		this.service=service;
	}


	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId", "!bindableEntity"}, headers="Accept=application/json")
	@ResponseBody
	public List<CustomFieldBindingModel> findAllCustomFieldsForProject(@RequestParam("projectId") Long projectId){
		
		List<CustomFieldBinding> bindings = service.findCustomFieldsForGenericProject(projectId);
		
		return bindingToJson(bindings);
		
	}
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId", "bindableEntity", "!sEcho"}, headers="Accept=application/json")
	@ResponseBody
	public List<CustomFieldBindingModel> findAllCustomFieldsForProject(@RequestParam("projectId") Long projectId, @RequestParam("bindableEntity") BindableEntity bindableEntity){
		
		List<CustomFieldBinding> bindings = service.findCustomFieldsForProjectAndEntity(projectId, bindableEntity);
		
		return bindingToJson(bindings);
		
	}
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId", "bindableEntity", "!sEcho", "optional=false"}, headers="Accept=text/html")
	public String findRequiredAtCreationTime(@RequestParam("projectId") Long projectId, @RequestParam("bindableEntity") BindableEntity bindableEntity, Model model){
		
		List<CustomFieldBinding> bindings = service.findCustomFieldsForProjectAndEntity(projectId, bindableEntity);
		model.addAttribute("bindings", bindings);
		
		return "treepopups/create-node-custom-fields-editor.html";
		
	}
	
	@RequestMapping(method= RequestMethod.GET, params = {"projectId", "bindableEntity", RequestParams.S_ECHO_PARAM})
	@ResponseBody
	public DataTableModel findAllCustomFieldsTableForProject
			(@RequestParam("projectId") Long projectId, 
			 @RequestParam("bindableEntity") BindableEntity bindableEntity, 
			 DataTableDrawParameters params,
			 Locale locale){
		
		
		DataTablePaging paging = new DataTablePaging(params);
		
		PagedCollectionHolder<List<CustomFieldBinding>> bindings = service.findCustomFieldsForProjectAndEntity(projectId, bindableEntity, paging);

		CUFBindingDataTableModelHelper helper = new CUFBindingDataTableModelHelper(converter);
		return helper.buildDataModel(bindings, params.getsEcho());
		
	}
	
	@RequestMapping(value="/{bindingIds}", method=RequestMethod.DELETE)
	@ResponseBody
	public void unbindCustomField(@PathVariable("bindingIds") List<Long> bindingIds){
		service.removeCustomFieldBindings(bindingIds);
	}

	@RequestMapping(value="/available", method = RequestMethod.GET, params = {"projectId", "bindableEntity"}, headers="Accept=application/json")
	@ResponseBody
	public List<CustomFieldModel> findAllAvailableCustomFieldsForProjectAndEntity
													(@RequestParam("projectId") Long projectId, 
													@RequestParam("bindableEntity") BindableEntity bindableEntity ){
		
		List<CustomField> fields = service.findAvailableCustomFields(projectId, bindableEntity);
		return fieldToJson(fields);	
		
	}
	
	@RequestMapping(value="/{bindingIds}/position", method=RequestMethod.POST, params={"newPosition"})
	@ResponseBody
	public void reorderBindings(@PathVariable("bindingIds") List<Long> bindingIds, @RequestParam("newPosition") int newIndex ){
		service.moveCustomFieldbindings(bindingIds, newIndex);
	}
	
	
	@RequestMapping(value="/new-batch", method = RequestMethod.POST)
	@ResponseBody
	public void createNewBinding(@RequestBody CustomFieldBindingModel[] bindingModels){
		// TODO not atomic, push down a level
		for (CustomFieldBindingModel model : bindingModels){
			
			CustomFieldBinding newBinding = new CustomFieldBinding();
			long projectId = model.getProjectId();
			long fieldId = model.getCustomField().getId();
			BindableEntity entity = model.getBoundEntity().toDomain();
			
			service.addNewCustomFieldBinding(projectId, entity, fieldId, newBinding);
			
		}
	}
	
	 @RequestMapping(value="/{bindingId}/renderingLocations/{location}", method=RequestMethod.PUT)
	 @ResponseBody
	 public void addRenderingLocation(@PathVariable Long bindingId, @PathVariable RenderingLocation location){
		 service.addRenderingLocation(bindingId, location);
	 }
	 
	 @RequestMapping(value="/{bindingId}/renderingLocations/{location}", method=RequestMethod.DELETE)
	 @ResponseBody
	 public void removeRenderingLocation(@PathVariable Long bindingId, @PathVariable RenderingLocation location){
		 service.removeRenderingLocation(bindingId, location);
	 }
	 
	  
	
	
	// ********************** private stuffs *********************
	
	private List<CustomFieldBindingModel> bindingToJson(List<CustomFieldBinding> bindings){
		List<CustomFieldBindingModel> result = new LinkedList<CustomFieldBindingModel>();
		
		for (CustomFieldBinding binding : bindings){
			CustomFieldBindingModel model = converter.toJson(binding);
			result.add(model);
		}
		
		return result;
	}
	

	private List<CustomFieldModel> fieldToJson(List<CustomField> fields){
		List<CustomFieldModel> result = new LinkedList<CustomFieldModel>();
		
		for (CustomField field : fields){
			CustomFieldModel model = converter.toJson(field);
			result.add(model);
		}
		
		return result;		
	}
	
	// ************************* inner classes ****************************
	
	
	
	private static class CUFBindingDataTableModelHelper extends DataTableModelHelper<CustomFieldBinding> {
		private CustomFieldJsonConverter converter;

		private CUFBindingDataTableModelHelper(CustomFieldJsonConverter converter) {
			this.converter=converter;
		}
		
		@Override
		public Object buildItemData(CustomFieldBinding item) {
			return converter.toJson(item);
		}
	}

	
}
