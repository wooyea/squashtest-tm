/*
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
define([ "jquery", "backbone", "handlebars", "squash.translator", "app/ws/squashtm.notification", "underscore", 
		"./SearchDateWidget", "./SearchRangeWidget", 
		"./SearchExistsWidget","./SearchMultiAutocompleteWidget", "./SearchMultiSelectWidget", "./SearchCheckboxWidget", "./SearchComboMultiselectWidget", "./SearchRadioWidget", 
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, translator, notification, _) {
	
	/**
	 * handlebars helper. substitutes {{selected}} with selected="selected" when this.selected === true
	 */ 
	Handlebars.registerHelper("selected", function() {
		if (this.selected === true) {
			return 'selected="selected"';
		} 
		return "";
	});
	
	function fieldValue(fieldType, value) {
		if (!value) {
			var text = $(this.element.children()[0]).val();
			var id = $(this.element).attr("id");
			return {
				"type" : fieldType,
				"value" : text,
				"ignoreBridge" : this.options.ignoreBridge
			};
		} else {
			$(this.element.children()[0]).val(value.value);
		}
	}
	
	// text area widget
	var searchTextAreaWidget = $.widget("search.searchTextAreaWidget", {
		options : {
			ignoreBridge : false
		},

		_create : function() {
			this._super();
		},

		fieldvalue : function(value) {
			return fieldValue.call(this, "TEXT", value);
		}
	});

	// text field widget
	var searchTextFieldWidget = $.widget("search.searchTextFieldWidget", {
		options : {
			ignoreBridge : false
		},

		_create : function() {
		},

		fieldvalue : function(value) {
			return fieldValue.call(this,"SINGLE", value);
		}
	});

	

	var TestCaseSearchInputPanel = Backbone.View.extend({

		el : "#advanced-search-input-panel",

		initialize : function() {
			this.model = {fields : []};
			// init templates cache
			this.templates = {};
			this.getInputInterfaceModel();
			
			// templates are no longer needed
			this.templates = {};
		},

		events : {
			"click #advanced-search-button" : "showResults"
		},

		getInputInterfaceModel : function() {
			var self = this;
			
			// compiles the panel template
			var source = self.$("#toggle-panel-template").html();
			
			if (!source) { // could this really happen without being a bug ?
				return;
			}
			
			var template = Handlebars.compile(source);
			
			// parses the search model if any
			var marshalledSearchModel = self.$("#searchModel").text();
			var searchModel = {};
			
			if(marshalledSearchModel){
				searchModel = JSON.parse(marshalledSearchModel).fields;
			}
			
			var searchDomain = self.$("#searchDomain").text();
			
			var formBuilder = function(formModel) {
				$.each(formModel.panels || {}, function(index, panel) {
					var context = {"toggle-panel-id": panel.id+"-panel-id", "toggle-panel-table-id": panel.id+"-panel-table-id"};
					var tableid = panel.id+"-panel-table-id";
					var html = template(context);
					self.$("#advanced-search-input-form-panel-"+panel.location).append(html);
					self.$("#advanced-search-input-form-panel-"+panel.location).addClass(searchDomain);
					
					for (var i = 0, field; i < panel.fields.length; i++){
						field = panel.fields[i];
						var inputType = field.inputType.toLowerCase();
						switch(inputType)
						{
							case "textfield" : 
								self.makeTextField(tableid, field.id, field.title, searchModel[field.id], field.ignoreBridge);
								break;
							case "textarea":
								self.makeTextArea(tableid, field.id, field.title, searchModel[field.id]);
								break;
							case "multiselect" : 
								self.makeMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "multiautocomplete":
								self.makeMultiAutocomplete(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "combomultiselect":
								self.makeComboMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "range" :
								self.makeRangeField(tableid, field.id, field.title, searchModel[field.id]);
								break;
							case "exists" :
								self.makeExistsField(tableid, field.id, field.title, field.possibleValues,searchModel[field.id]);
								break;
							case "date":
								self.makeDateField(tableid, field.id, field.title, searchModel[field.id]);
								break;
							case "checkbox":
								self.makeCheckboxField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case  "radiobutton":
								self.makeRadioField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id], field.ignoreBridge);
								break;
						}
						
					}
					self.makeTogglePanel(panel.id+"-panel-id",panel.title,panel.open,panel.cssClasses);
				});
			};
			
			this._processModel(formBuilder);
			
		},
		
		_processModel : function(formBuilder) {
			if (!!squashtm.app.searchFormModel) {
				formBuilder(squashtm.app.searchFormModel);
				
			} else { // TODO legacy, remove that in Squash 1.9
				$.ajax({
					url : squashtm.app.contextRoot + "/advanced-search/input?"+this.$("#searchDomain").text(),
					data : "nodata",
					dataType : "json"
				}).success(function() {
					formBuilder(squashtm.app.searchFormModel);
				});
			}
		},
		
		/**
		 * returns the html of a compiled template
		 * @param selector jq selector to find the template
		 * @param context the params given to the template 
		 * @returns
		 */
		_compileTemplate : function(selector, context) {
				var template = this.templates[selector];
				
				if (!template) {
					var source = this.$(selector).html();
					template = Handlebars.compile(source);
					this.templates[selector] = template;
				}
				
				return template(context);
		},

		_appendFieldDom : function(tableId, fieldId, fieldHtml) {
			this.$("#"+tableId).append(fieldHtml);
			var escapedId = fieldId.replace(/\./g, "\\.");
			return this.$("#" + escapedId);
		}, 
		
		makeRadioField : function(tableId, fieldId, fieldTitle, options, enteredValue, ignoreBridge) {
			var context = {"text-radio-id": fieldId, "text-radio-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#radio-button-template", context));
			
			$fieldDom.searchRadioWidget({"ignoreBridge" : ignoreBridge});
			$fieldDom.searchRadioWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchRadioWidget("fieldvalue", enteredValue);
				
		},
		
		makeRangeField : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {"text-range-id": fieldId, "text-range-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#range-template", context));

			$fieldDom.searchRangeWidget();
			$fieldDom.searchRangeWidget("fieldvalue", enteredValue);
			
		},
		
		makeExistsField : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"text-exists-id": fieldId, "text-exists-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#exists-template", context));
			$fieldDom.searchExistsWidget();
			$fieldDom.searchExistsWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchExistsWidget("fieldvalue", enteredValue);
		},
			
		makeDateField : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {"text-date-id": fieldId, "text-date-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#date-template", context));
			$fieldDom.searchDateWidget();
			$fieldDom.searchDateWidget("createDom", "F"+fieldId);
			$fieldDom.searchDateWidget("fieldvalue", enteredValue);
		},
			
		makeCheckboxField : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			// FIXME I cannot find the matching template ?!
			var context = {"text-checkbox-id": fieldId, "text-checkbox-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#checkbox-template", context));
			$fieldDom.searchCheckboxWidget();
			$fieldDom.searchCheckboxWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchCheckboxWidget("fieldvalue", enteredValue);
			
		},
			
		makeTextField : function(tableId, fieldId, fieldTitle, enteredValue, ignoreBridge) {
			var context = {
				"text-field-id": fieldId, 
				"text-field-title": fieldTitle, 
				fieldValue : !!enteredValue ? enteredValue.value : ""
			};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#textfield-template", context));
			$fieldDom.searchTextFieldWidget({"ignoreBridge" : ignoreBridge});
		},
		
		makeTextArea : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {
				"text-area-id": fieldId, 
				"text-area-title": fieldTitle, 
				fieldValue : !!enteredValue ? enteredValue.value : ""
			};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#textarea-template", context));
			$fieldDom.searchTextAreaWidget();
		},
		
		makeMultiselect : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			// adds a "selected" property to options
			enteredValue = enteredValue || {};
			// no enteredValue.values means 'select everything'
			_.each(options, function(option) {
				option.selected = (!enteredValue.values) || _.contains(enteredValue.values, option.code);
			});
			var context = {"multiselect-id": fieldId, "multiselect-title": fieldTitle, options: options};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#multiselect-template", context));
			$fieldDom.searchMultiSelectWidget();
		},
		makeMultiAutocomplete : function(tableId, fieldId, fieldTitle, options, enteredValue) {
//			// adds a "selected" property to options
//			enteredValue = enteredValue || {};
//			// no enteredValue.values means 'select everything'
//			_.each(options, function(option) {
//				option.selected = (!enteredValue.values) || _.contains(enteredValue.values, option.code);
//			});
//			var context = {"multiselect-id": fieldId, "multiselect-title": fieldTitle, options: options};
//			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#multiselect-template", context));
//			$fieldDom.searchMultiSelectWidget();			
			var context = {"multiautocomplete-id": fieldId, "multiautocomplete-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#multiautocomplete-template", context));
			$fieldDom.searchMultiAutocompleteWidget({fieldId : fieldId, options : options});
			$fieldDom.searchMultiAutocompleteWidget("fieldvalue", enteredValue);
			
		},
			
		makeComboMultiselect : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"combomultiselect-id": fieldId, "combomultiselect-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#combomultiselect-template", context));
			$fieldDom.searchComboMultiSelectWidget();
			$fieldDom.searchComboMultiSelectWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchComboMultiSelectWidget("fieldvalue", enteredValue);
		},
		
		extractSearchModel : function(){
			var fields = self.$("div.search-input");
			
			var jsonVariable = {};

			for (var i = 0, $field; i < fields.length; i++) {
				$field = $(fields[i]);
				var type = $($field.children()[0]).attr("data-widgetname");
				var key = $field.attr("id");
				var escapedKey = key.replace(/\./g, "\\.");
				var field = $("#"+escapedKey).data("search"+type+"Widget");
				if(field && !!field.fieldvalue()){
					var value = field.fieldvalue();
					if( value ) {
						jsonVariable[key] = value;
					}
				}
			}
			this.model = {fields : jsonVariable};
		},
		
		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
			temp.acceptCharset="UTF-8";

			for ( var x in PARAMS) {
				var opt = document.createElement("textarea");
				opt.name = x;
				opt.value = PARAMS[x];
				temp.appendChild(opt);
			}

			document.body.appendChild(temp);
			temp.submit();
			return temp;
		},
		
		showResults : function() {
			
			this.extractSearchModel();
			
			if (this.emptyCriteria()){
				var message = translator.get('search.validate.empty.label');
				notification.showInfo(message);
				return;
			}
			
			var searchModel = JSON.stringify(this.model);
			var queryString = "searchModel=" + encodeURIComponent(searchModel);

			if(!!$("#associationType").length){				
				var associateResultWithType = $("#associationType").text();
				queryString += "&associateResultWithType=" + encodeURIComponent(associateResultWithType);

				var id = $("#associationId").text();
				queryString += "&id=" + encodeURIComponent(id);
				
			}
				
			document.location.href = squashtm.app.contextRoot + "advanced-search/results?"+$("#searchDomain").text() + "&" + queryString;
		},

		makeTogglePanel : function(id, key, open, css) {
			var title = key;
			
			var infoSettings = {
				initiallyOpen : open,
				title : title, 
				cssClasses : ""
			};
			$panel = this.$("#"+id);
			$panel.togglePanel(infoSettings);
			$("a", $panel.parent()).removeClass("tg-link").addClass(css.toString());
		},
		
		emptyCriteria : function(){
			var hasCriteria = false;
			$.each(this.model.fields, function(namename,field){ 
				// we must distinguish singlevalued and multivalued fields
				// singlevalued fields define a property 'value', while multivalued fields define a property 'values'.
				// a singlevalued field is empty if the property 'value' is empty, 
				// a multivalued field is empty if the property 'values' is null.
				// 
				if ( field.value !== undefined && field.value !== "" ){
					hasCriteria = true;
				} else if (field.values !== undefined && field.values !== null){
					hasCriteria = true;
				}
			});
			return !hasCriteria;
		}

	});
	return TestCaseSearchInputPanel;
});