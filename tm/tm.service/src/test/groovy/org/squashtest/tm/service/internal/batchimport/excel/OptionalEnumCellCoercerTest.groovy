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
package org.squashtest.tm.service.internal.batchimport.excel;

import static org.junit.Assert.*;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;
import org.squashtest.tm.domain.testcase.TestCaseImportance;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class OptionalEnumCellCoercerTest extends Specification {
	@Unroll
	def"should coerce #cellValue into #enumValue"() {
		given: 
		Cell cell = Mock()
		cell.getCellType() >> cellType
		cell.getStringCellValue() >> cellValue
		
		expect:
		OptionalEnumCellCoercer.forEnum(TestCaseImportance).coerce(cell) == enumValue
		
		where:
		cellValue 	| cellType 				| enumValue
		"VERY_HIGH"	| Cell.CELL_TYPE_STRING	| TestCaseImportance.VERY_HIGH
		""			| Cell.CELL_TYPE_BLANK	| null
	}
}
