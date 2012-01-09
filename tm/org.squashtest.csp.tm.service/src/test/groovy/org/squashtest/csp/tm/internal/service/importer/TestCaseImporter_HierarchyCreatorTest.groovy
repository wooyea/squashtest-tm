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
package org.squashtest.csp.tm.internal.service.importer


import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseFolder;
import org.squashtest.csp.tm.internal.utils.archive.Entry;
import org.squashtest.csp.tm.internal.utils.archive.impl.ZipReader.ZipReaderEntry;

import spock.lang.Specification;


class TestCaseImporter_HierarchyCreatorTest extends Specification {

	def "should find an existing folder"(){
		
		given :
			def importer = new TestCaseImporter.HierarchyCreator();
			
		and :
			def folder = Mock(TestCaseFolder)
			importer.pathMap.put("/toto/folder", folder);
			
		and :
			def entry = Mock(Entry)
			entry.getName() >> "/toto/folder"
					
		when :
			def result = importer.findOrCreateFolder(entry)
		
		
		then :
			result == folder
		
	}
	
	def "should recursively create missing parent folders"(){
		
		given :
			def importer = new TestCaseImporter.HierarchyCreator();
			
			
		and :
			def entry = new ZipReaderEntry(null, "/melvin/van/peebles", true);
		
		when :
			importer.findOrCreateFolder(entry)
			
		then :
			def peebles = importer.pathMap.getMappedElement("/melvin/van/peebles")
			peebles instanceof TestCaseFolder
			peebles.getName() == "peebles"
			
			def van = importer.pathMap.getMappedElement("/melvin/van")
			van instanceof TestCaseFolder
			van.getName() == "van"
			van.getContent() == [peebles] as Set
			
			def melvin = importer.pathMap.getMappedElement("/melvin")
			melvin instanceof TestCaseFolder
			melvin.getName() == "melvin"
			melvin.getContent() == [van] as Set
			
			def root = importer.pathMap.getMappedElement("/")
			root.getContent() == [melvin] as Set
		
	}
	
	def "should create a test case"(){
		
		given :		
			def importer = new TestCaseImporter.HierarchyCreator();
			def parser = Mock(ExcelTestCaseParser)
			importer.setParser(parser)
			
		and :
			def mTc = new TestCase()
			parser.parseFile(_,_) >> mTc
		
		
		and :
			def entry = new ZipReaderEntry(Mock(ZipInputStream), "/melvin/van/peebles", false);
			
		and : 
			def parent = new TestCaseFolder()
			importer.pathMap.put("/melvin/van", parent)
			
		and : 
			
			
		
		when :
			importer.createTestCase(entry)
		
		then :
			def tc = importer.pathMap.getMappedElement("/melvin/van/peebles")
			tc instanceof TestCase
			tc.getName() == "peebles"
			
			parent.getContent() == [tc] as Set
	}
		
}
