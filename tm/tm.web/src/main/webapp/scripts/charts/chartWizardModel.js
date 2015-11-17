/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
define([ "jquery", "backbone", "underscore"], function($, Backbone, _) {
	"use strict";

return Backbone.Model.extend({
		
	
		toJson : function() {
			
			return JSON.stringify ({
			name : this.get("name"),
			type : this.get("type"),
			query : {
				axis: this.get("axis"),
				measures : this.get("measures"),					
				filters : _.map(this.get("filters"), function(filter) {var newFilter= _.clone(filter); newFilter.values = _.flatten(filter.values); return newFilter;})
			},
			scope : _.map(this.get("scope"), function(val) {var newVal = _.clone(val); newVal.type = val.type.replace("LIBRARIE", "LIBRARY"); return newVal;})
			});
			
		}
	
	
		});


});