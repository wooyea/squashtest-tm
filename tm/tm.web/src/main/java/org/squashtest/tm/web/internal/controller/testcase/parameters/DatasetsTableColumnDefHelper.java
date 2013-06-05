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
package org.squashtest.tm.web.internal.controller.testcase.parameters;

import java.util.ArrayList;
import java.util.List;

import org.squashtest.tm.web.internal.controller.generic.DataTableColumnDefHelper;
import org.squashtest.tm.web.internal.controller.widget.AoColumnDef;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;

public final class DatasetsTableColumnDefHelper extends DataTableColumnDefHelper {
	private static final List<AoColumnDef> baseColumns = new ArrayList<AoColumnDef>(4);
	static {
		String smallWidth = "2em";
		// columns.add(new AoColumnDef(bVisible, bSortable, sClass, sWidth, mDataProp))
		baseColumns.add(new AoColumnDef(false, false, "", null, DataTableModelHelper.DEFAULT_ENTITY_ID_KEY));// 0
		baseColumns.add(new AoColumnDef(true, false, "select-handle centered", smallWidth,
				DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY));// 1
		baseColumns.add(new AoColumnDef(true, false, "dataset-name", null, DataTableModelHelper.NAME_KEY));// 2
		baseColumns.add(new AoColumnDef(true, false, "delete-button", smallWidth,
				DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY));// 3
	}
	private List<AoColumnDef> columns = new ArrayList<AoColumnDef>();

	public DatasetsTableColumnDefHelper() {
		columns.addAll(baseColumns);
	}

	public List<AoColumnDef> getAoColumnDefs(List<Long> parameterIds, boolean editable) {
		columns.get(columns.size() - 1).setbVisible(editable);
		if (!parameterIds.isEmpty()) {
			List<AoColumnDef> parameterColumns = new ArrayList<AoColumnDef>(parameterIds.size());
			for (Long parameterId : parameterIds) {
				AoColumnDef aoColumn = new AoColumnDef(true, false, "parameter", null, "parameter-" + parameterId);
				parameterColumns.add(aoColumn);
			}
			columns.addAll(3, parameterColumns);
		}
		addATargets(columns);
		return columns;
	}

}