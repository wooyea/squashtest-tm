/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
package org.squashtest.csp.tm.internal.service.importer;

import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNodeVisitor;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReader;
import org.squashtest.csp.tm.internal.utils.archive.ArchiveReaderFactory;
import org.squashtest.csp.tm.service.TestCaseLibraryNavigationService;
import org.squashtest.csp.tm.service.importer.ImportSummary;


@Component
public class TestCaseImporter {

	@Inject
	private TestCaseLibraryNavigationService service;
	
	@Inject
	private ArchiveReaderFactory factory;
	
	//@Inject
	private ExcelTestCaseParser parser;
	
	
	public ImportSummary importExcelTestCases(InputStream archiveStream, Long libraryId){

		
		ArchiveReader reader = factory.createReader(archiveStream);
		ImportSummaryImpl summary = new ImportSummaryImpl();
		
		/* phase 1 : convert the content of the archive into Squash entities */
		
		HierarchyCreator creator = new HierarchyCreator();
		creator.setArchiveReader(reader);
		creator.setParser(parser);
		
		creator.create();
		
		TestCaseFolder root = creator.getNodes();
		summary.add(creator.getSummary());
		
		
		/* phase 2 : merge with the actual database content */
		
		TestCaseLibrary library = service.findLibrary(libraryId);		

		return null;
	}


	
	
}
