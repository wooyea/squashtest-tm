/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "./abstractStepView","../custom-report-workspace/utils","./customFieldPopup"],
	function($, backbone, _, Handlebars, AbstractStepView,chartUtils,CustomFieldPopup) {
	"use strict";

	var attributesStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#attributes-step-tpl";
			this.model = data;
			data.name = "attributes";
			this.model.set("computedColumnsPrototypes",this.computeColumnsPrototypes());
			this.model.set ("cufMapByEntity",chartUtils.extractCufsMapFromWorkspace());
			this._initialize(data, wizrouter);
			//listen to changes in cuf selected attributes
			
			this.listenTo(this.model, 'change:selectedCufAttributes', this.updateSelectedAttributesWithCuf);
		},
		
		events : {
			"click .wizard-cuf-btn" : "openCufPopup"
		},
		
		

		
		updateModel : function() {

			var self = this;
			var ids = _.pluck($('[id^="attributes-selection-"]').filter(":checked"), "name");
			var selectedAttributes = [];
			if (ids && ids.length > 0) {
				selectedAttributes.push(ids);
			}
			selectedAttributes.push(this.model.get);
			this.model.set({selectedAttributes : ids});

			//now retrieve the selected entities type to updated filter and operation view
			var selectedEntities = _.chain(ids)
				.map(function(id){
					var allProtos = _.chain(self.model.get("computedColumnsPrototypes")).values().flatten().value();
					return _.find(allProtos,function(proto){
						return proto.id === id || proto.id.toString() === id;
					});
				})
				.map(function(proto){
					return proto.specializedType.entityType;
				})
				.uniq()
				.value();

			this.model.set({"selectedEntity" : selectedEntities});
			
		},
		
		filterWithValidIds : function (col) {		
			var self = this;
			return _.chain(col)
			.filter(function(val){return _.contains(self.model.get("selectedAttributes"), val.column.id.toString());})
			.value();
			
		},
		
		computeColumnsPrototypes : function () {
			var initialColumnsPrototypes = this.model.get('columnPrototypes');

			//1 creating synthetics prototypes and merging with natural
			var mergedProto = this.mergeProtoypes(initialColumnsPrototypes);
			
			//2 reorder to follow the squashtm workspace order
			var orderedProtos = _.pick(mergedProto,["REQUIREMENT","REQUIREMENT_VERSION","TEST_CASE","CAMPAIGN","ITERATION","ITEM_TEST_PLAN","EXECUTION"]);

			return orderedProtos;
		},
		
		getSelectedProject : function () {
			var projectsScope = this.model.get('projectsScope');
			return _.filter(squashtm.workspace.projects,function (project) {
				return _.contains(projectsScope,project.id);
			});
		},

		//This function will return a map with synthetic column proto for cuf merged into the original map of prototypes
		mergeProtoypes : function (selectedEntitiesColumnsPrototypes) {
			var cufPrototypes = [];
			var mapOfNaturalPrototypes = {};

			//first we separate all generic cuf column prototypes from initial list of column prototype from the other one (attributes and calculated)
			_.each(selectedEntitiesColumnsPrototypes, function (prototypes, key) {
				//grouping by column type
				var groupedcolumnsPrototype = _.groupBy(prototypes,function(prototype){
					return prototype.columnType;
				});
				//extracting cuf prototype for this entity type and put in array of all cuf column proto
				var cufPrototypesForOneEntityType = groupedcolumnsPrototype["CUF"];
				if(cufPrototypesForOneEntityType){
					cufPrototypes = cufPrototypes.concat(cufPrototypesForOneEntityType);
				}
				//now inject into computedColumnsPrototypes all the natural column prototypes
				var naturalPrototypes = groupedcolumnsPrototype["ATTRIBUTE"];
				naturalPrototypes = naturalPrototypes.concat(groupedcolumnsPrototype["CALCULATED"]);
				mapOfNaturalPrototypes[key] = naturalPrototypes;
			});

			//now we create the map of synthetic column proto
			//first we create a map of all cuf binding for projects in perimeter
			var cufBindingMap = this.getCufProjectMap();
			//now we generate the synthetics columns prototypes
			var syntheticColumnPrototypes = this.getCufProtoForBindings(cufBindingMap,cufPrototypes);

			//finally we merge the the two maps and return the result
			var mergedPrototypes = chartUtils.getEmptyCufMap();
			_.each(mapOfNaturalPrototypes,function (values,key) {
				var syntheticColumnPrototypesForEntity = syntheticColumnPrototypes[key];
				if(syntheticColumnPrototypesForEntity && syntheticColumnPrototypesForEntity.length > 0){
					var allProto = values.concat(syntheticColumnPrototypesForEntity);
					mergedPrototypes[key] = allProto;
				}
				else{
					mergedPrototypes[key] = values;
				}
			});

			return mergedPrototypes;
		},

		//return a map with cuf bindings by entitity type : {"CAMPAIGN":[{cufBinding1},{cufBinding2}],"ITERATION":[{cufBinding1},{cufBinding2}]...}
		getCufProjectMap : function () {
			var selectedProjects = this.getSelectedProject();
			var selectedEntities = this.model.get('selectedEntity');
			var self = this;
			var cufMap = _.reduce(selectedProjects,function (memo, project) {
				_.each(project.customFieldBindings,function (values,key) {
					if(values.length > 0 && memo.hasOwnProperty(key)){
						memo[key] = memo[key].concat(values);
					}
				});
				return memo;
			},chartUtils.getEmptyCufMap());

			//TO DO FILTER OUT DUPLICATE CUF FOR THE PERIMETER EVOLUTION IF NEEDED
			return cufMap;
		},

		getEmptyCufMap : function () {
			return {
				"REQUIREMENT_VERSION":[],
				"TEST_CASE":[],
				"CAMPAIGN":[],
				"ITERATION":[],
				"ITEM_TEST_PLAN":[],
				"EXECUTION":[]
			};
		},

		getCufProtoForBindings : function (bindingMap,cufPrototypes) {
			var protoForCufBinding = chartUtils.getEmptyCufMap();
			var self = this;
			_.each(bindingMap,function (values,key) {
				var generatedPrototypes = _.map(values,function (cufBinding) {
					//1 find the proto name
					var protoLabel = key + "_" + self.getProtoSuffix(cufBinding);
					//2 find the prototype and upgrade it with cufCode and label
					var cufPrototype = _.find(cufPrototypes,function (proto) {
						return proto.label === protoLabel;
					});
					if (cufPrototype) {
						cufPrototype = _.clone(cufPrototype);
						cufPrototype.code = cufBinding.customField.code;
						cufPrototype.cufLabel = cufBinding.customField.label;
						cufPrototype.cufName = cufBinding.customField.name;
						cufPrototype.cufId = cufBinding.customField.id;
						cufPrototype.isCuf = true;
						cufPrototype.originalPrototypeId = cufPrototype.id;
						cufPrototype.id = cufPrototype.id + "-" + cufBinding.customField.id;
						cufPrototype.cufType = cufBinding.customField.inputType.enumName;
						if (cufPrototype.cufType === "DROPDOWN_LIST" || cufPrototype.cufType === "TAG") {
							cufPrototype.cufListOptions = cufBinding.customField.options;
						}
						return cufPrototype;
					}
				});
				if(generatedPrototypes){
					protoForCufBinding[key] = generatedPrototypes;
				}
			});
			return protoForCufBinding;
		},

		getProtoSuffix : function (value) {
			var suffix;
			switch (value.customField.inputType.enumName) {
				case "PLAIN_TEXT":
					suffix = "CUF_TEXT";
					break;
				case "CHECKBOX":
					suffix = "CUF_CHECKBOX";
					break;				
				case "DROPDOWN_LIST":
					suffix = "CUF_LIST";
					break;				
				case "DATE_PICKER":
					suffix = "CUF_DATE";
					break;				
				case "TAG":
					suffix = "CUF_TAG";
					break;
				case "NUMERIC":
					suffix = "CUF_NUMERIC";
					break;
			}
			return suffix;
		},

		openCufPopup : function(event) {
			var self = this;
			var entityType = event.target.getAttribute("data-entity");
			var cufMapByEntity = this.model.get("cufMapByEntity");
			var cufToDisplay = cufMapByEntity[entityType];
			this.model.set("cufToDisplay",cufToDisplay);
			this.model.set("selectedCufEntity",entityType);
			var cufPopup = new CustomFieldPopup(this.model);
			
		},
		//callback executed when selected cuf changes
		updateSelectedAttributesWithCuf:function(model, newSelectedIds, options) {
			console.log("CAHNGED !!!");
			var self = this;
			var previousSelectedIds = model.previous("selectedCufAttributes");
			var idsToHide = _.difference(previousSelectedIds, newSelectedIds);

			_.each(idsToHide, function(id) {
				var checkBoxSelector = '[id="attributes-selection-'+ id + '"]';
				var checkBox = self.$el.find(checkBoxSelector);
				checkBox.prop("checked",false);
				var checkBoxWrapperSelector = '[id="wrapper-attributes-selection-'+ id + '"]';
				var wrapper = self.$el.find(checkBoxWrapperSelector);
				wrapper.removeClass("chart-wizard-visible");
				wrapper.addClass("chart-wizard-hidden");
			});

			var idsToShow = _.difference(newSelectedIds, previousSelectedIds);
			_.each(idsToShow, function(id) {
				var checkBoxSelector = '[id="attributes-selection-'+ id + '"]';
				var checkBox = self.$el.find(checkBoxSelector);
				checkBox.prop("checked",true);
				var checkBoxWrapperSelector = '[id="wrapper-attributes-selection-'+ id + '"]';
				var wrapper = self.$el.find(checkBoxWrapperSelector);
				wrapper.addClass("chart-wizard-visible");
				wrapper.removeClass("chart-wizard-hidden");
			});
		}
	});
	return attributesStepView;
});