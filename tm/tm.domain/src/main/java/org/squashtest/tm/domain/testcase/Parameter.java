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
package org.squashtest.tm.domain.testcase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.squashtest.tm.domain.Identified;

@Entity
public class Parameter implements Identified{
	
	@Id
	@GeneratedValue
	@Column(name = "PARAM_ID")
	private Long id;
	
	@NotBlank
	@Size(min = 0, max = 255)
	private String name;
	
	@Lob
	private String description="";
	
	@NotNull
	@ManyToOne
	@JoinColumn(name = "TEST_CASE_ID", referencedColumnName = "TCLN_ID")
	private TestCase testCase;
	
	public Parameter(){
		super();
	}
	
	public Parameter(String name, TestCase testCase) {
		super();
		this.name = name;
		this.testCase = testCase;
		this.testCase.addParameter(this);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(@NotNull String description) {
		this.description = description;
	}
	public TestCase getTestCase() {
		return testCase;
	}
	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}
	public Long getId() {
		return id;
	}
	
	
	
}
