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
package org.squashtest.csp.h2.triggers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.tools.TriggerAdapter;


/**
 * Triggered before a RequirementLibraryNode is deleted. It will remove the self-reference of that 
 * node in RLN_RELATIONSHIP_CLOSURE that was previously inserted by {@link RLNAfterInsert}. 
 * 
 * @author bsiri
 *
 */
public class RLNBeforeDelete extends TriggerAdapter {
	
	private static final String SQL = "delete from RLN_RELATIONSHIP_CLOSURE where ancestor_id = ? and descendant_id = ?;";
	
	@Override
	public void fire(Connection conn, ResultSet oldRow, ResultSet newRow)
			throws SQLException {
		
		PreparedStatement stmt = conn.prepareStatement(SQL);
		
		Long id = oldRow.getLong(1);
		stmt.setLong(1, id);
		stmt.setLong(2,id);
		
		stmt.execute();
		
	}
	
}
