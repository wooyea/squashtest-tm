/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
/**
 * This file is part of the Squash TM management services for SaaS / Squash On Demand (saas.management.fragment) project.
 * Copyright (C) 2015 - 2016 Henix, henix.fr - All Rights Reserved
 * <p>
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * (C)Henix. Tous droits réservés.
 * <p>
 * Avertissement : ce programme est protégé par la loi relative au droit d'auteur et par les conventions internationales. Toute reproduction ou distribution partielle ou totale du logiciel, par quelque moyen que ce soit, est strictement interdite.
 */
package org.squashtest.tm.domain;

/**
 * Defines global size constraints
 * @author Gregory Fouquet
 * @since 1.14.0  07/07/16
 */
public final  class Sizes {
	private Sizes() {
		super();
	}

	public static final int NAME_MAX = 255;

	public static final int LABEL_MAX = 255;

	public static final int STATUS_MAX = 50;
}
