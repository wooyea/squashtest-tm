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
package org.squashtest.tm.service.annotation;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Julien Thebault
 * @since 1.13
 */
public abstract class IdsCoercerAdapter implements IdsCoercer {
	
	@Override
	public Collection<? extends Serializable> coerce(Object object) {
		Collection<? extends Serializable> resolvedIds = getCoercer().coerce(object);
		return getExtender().doCoerce(resolvedIds);
	}

	public abstract IdsCoercerExtender getExtender();
	
	public abstract IdsCoercer getCoercer();
}
