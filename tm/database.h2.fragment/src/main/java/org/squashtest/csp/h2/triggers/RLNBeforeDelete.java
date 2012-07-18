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
