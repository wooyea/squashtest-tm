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
define([ "jquery", "app/pubsub", "app/ws/squashtm.navbar", "project-filter/ProjectFilter",
		"app/ws/squashtm.notification", "squash.session-pinger" ], function($, ps, NavBar, ProjectFilter, WTF, SSP) {

	ps.subscribe("load.navBar", NavBar.init);
	ps.subscribe("load.projectFilter", ProjectFilter.init);
	ps.subscribe("load.notification", function() {
		WTF.init(squashtm.app.notificationConf);
	});

	/* session ping */
	new SSP();

	function init() {
		/* Try to prevent FOUCs */
		$(".unstyled").fadeIn("fast", function() {
			$(this).removeClass("unstyled");
		});
	}

	return {
		init : init
	};
});