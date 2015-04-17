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
/*
 * This is a simple web storage wrapper that handles object values, if we need something more
 * powerful we could consider something different like store.js.
 *
 */

/*
 * Expiration.
 *
 * The method .set() accepts an expiration date as an optional argument. Valid expiration dates are the following :
 * - ATOM date (string),
 * - number of hours starting from now (int)
 *
 */

define([ "underscore", "squash.dateutils" ], function(_, dateutils) {

	// TODO : Bad copy is bad. Need to refactor this with workspace.storage.js 
	
	var storage = sessionStorage || {
		setItem : function() {
		},
		getItem : function() {
		},
		removeItem : function() {
		}
	};

	function asExpiration(exp) {
		if (_.isString(exp)) {
			return exp;
		} else if (_.isNumber(exp)) {
			var now = new Date();
			var hours = now.getHours() + exp;
			now.setHours(hours);
			return dateutils.format(now);
		} else {
			return null;
		}
	}

	function hasExpired(stored) {
		if (!!stored._time) {
			var now = new Date(), expiration = dateutils.parse(stored._time);
			return (now.getTime() > expiration.getTime());
		} else {
			return false;
		}
	}

	return {

		// value can be string, array or object. Third argument, if set, is the Expiration (see top level doc), if unset
		// the
		// data is permanent.
		set : function(key, value, expiration) {

			var stored = {
				_time : asExpiration(expiration),
				data : value
			};

			var stringified = JSON.stringify(stored);

			storage.setItem(key, stringified);
		},

		get : function(key) {
			var stored = storage.getItem(key);

			if (stored === null || stored === undefined) {
				return undefined;
			}

			var obj = JSON.parse(stored);

			if (hasExpired(obj)) {
				this.remove(key);
				return undefined;
			} else {

				return obj.data;
			}

		},

		remove : function(key) {
			storage.removeItem(key);
		}

	};

});