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
package org.squashtest.tm.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;

/**
 * Bean that stores the number of some entities in the database. <br>
 * Contains the following properties :
 * <ul>
 * <li>projectsNumber</li>
 * <li>usersNumber</li>
 * <li>requirementsNumber</li>
 * <li>testCasesNumber</li>
 * <li>campaignsNumber</li>
 * <li>iterationsNumber</li>
 * <li>executionsNumber</li>
 * </ul>
 *
 * @author mpagnon
 *
 * **/
@Entity
@Table(name = "ADMINISTRATION_STATISTICS")
public class AdministrationStatistics {
	@Id
	@Column(name = "STATISTICS_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "administration_statistics_statistics_id_seq")
	@SequenceGenerator(name = "administration_statistics_statistics_id_seq", sequenceName = "administration_statistics_statistics_id_seq", allocationSize = 1)
	private Long id;

	private Long projectsNumber;

	private Long usersNumber;

	private Long requirementsNumber;

	private Long testCasesNumber;

	private Long campaignsNumber;

	private Long iterationsNumber;

	private Long executionsNumber;

	@Temporal(TemporalType.TIMESTAMP)
	private Date requirementIndexingDate;

	@Column(name = "TEST_CASE_INDEXING_DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date testcaseIndexingDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date campaignIndexingDate;

	private BigInteger databaseSize;

	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	private Date savedDate;

	/**
	 * Will read the <code>Object[]</code> param and fill it's properties with the following "index/property" mapping :
	 * <ul>
	 * <li>0/projectsNumber</li>
	 * <li>1/usersNumber</li>
	 * <li>2/requirementsNumber</li>
	 * <li>3/testCasesNumber</li>
	 * <li>4/campaignsNumber</li>
	 * <li>5/iterationsNumber</li>
	 * <li>6/executionsNumber</li>
	 * </ul>
	 *
	 * @param resultParam
	 */
	public AdministrationStatistics(Object[] resultParam, BigInteger databaseSize) {
		Object[] result = resultParam.clone();
		this.projectsNumber = (Long) result[0];
		this.usersNumber = (Long) result[1];
		this.requirementsNumber = (Long) result[2];
		this.testCasesNumber = (Long) result[3];
		this.campaignsNumber = (Long) result[4];
		this.iterationsNumber = (Long) result[5];
		this.executionsNumber = (Long) result[6];
		this.databaseSize = databaseSize;
	}

	public AdministrationStatistics() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getProjectsNumber() {
		return projectsNumber;
	}

	public void setProjectsNumber(long projectsNumber) {
		this.projectsNumber = projectsNumber;
	}

	public long getUsersNumber() {
		return usersNumber;
	}

	public void setUsersNumber(long usersNumber) {
		this.usersNumber = usersNumber;
	}

	public long getRequirementsNumber() {
		return requirementsNumber;
	}

	public void setRequirementsNumber(long requirementsNumber) {
		this.requirementsNumber = requirementsNumber;
	}

	public long getTestCasesNumber() {
		return testCasesNumber;
	}

	public void setTestCasesNumber(long testCasesNumber) {
		this.testCasesNumber = testCasesNumber;
	}

	public long getCampaignsNumber() {
		return campaignsNumber;
	}

	public void setCampaignsNumber(long campaignsNumber) {
		this.campaignsNumber = campaignsNumber;
	}

	public long getIterationsNumber() {
		return iterationsNumber;
	}

	public void setIterationsNumber(long iterationsNumber) {
		this.iterationsNumber = iterationsNumber;
	}

	public long getExecutionsNumber() {
		return executionsNumber;
	}

	public void setExecutionsNumber(long executionsNumber) {
		this.executionsNumber = executionsNumber;
	}

	public Date getRequirementIndexingDate() {
		return requirementIndexingDate;
	}

	public void setRequirementIndexingDate(Date requirementIndexingDate) {
		this.requirementIndexingDate = requirementIndexingDate;
	}

	public Date getTestcaseIndexingDate() {
		return testcaseIndexingDate;
	}

	public void setTestcaseIndexingDate(Date testcaseIndexingDate) {
		this.testcaseIndexingDate = testcaseIndexingDate;
	}

	public Date getCampaignIndexingDate() {
		return campaignIndexingDate;
	}

	public void setCampaignIndexingDate(Date campaignIndexingDate) {
		this.campaignIndexingDate = campaignIndexingDate;
	}

	public BigInteger getDatabaseSize() {
		return databaseSize;
	}

	public void setDatabaseSize(BigInteger databaseSize) {
		this.databaseSize = databaseSize;
	}

	public Date getSavedDate() {
		return savedDate;
	}

	public void setSavedDate(Date savedDate) {
		this.savedDate = savedDate;
	}

	public void setProjectsNumber(Long projectsNumber) {
		this.projectsNumber = projectsNumber;
	}

	public void setUsersNumber(Long usersNumber) {
		this.usersNumber = usersNumber;
	}

	public void setRequirementsNumber(Long requirementsNumber) {
		this.requirementsNumber = requirementsNumber;
	}

	public void setTestCasesNumber(Long testCasesNumber) {
		this.testCasesNumber = testCasesNumber;
	}

	public void setCampaignsNumber(Long campaignsNumber) {
		this.campaignsNumber = campaignsNumber;
	}

	public void setIterationsNumber(Long iterationsNumber) {
		this.iterationsNumber = iterationsNumber;
	}

	public void setExecutionsNumber(Long executionsNumber) {
		this.executionsNumber = executionsNumber;
	}
}
