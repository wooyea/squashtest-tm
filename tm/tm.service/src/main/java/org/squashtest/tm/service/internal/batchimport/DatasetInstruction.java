/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.service.internal.batchimport;


import javax.validation.constraints.NotNull;

public class DatasetInstruction extends Instruction<DatasetTarget> {

	private final DatasetValue datasetValue;

	public DatasetInstruction(@NotNull DatasetTarget target, @NotNull DatasetValue datasetValue) {
		super(target);
		this.datasetValue = datasetValue;
	}


	/**
	 * @return the datasetParamValue
	 */
	public DatasetValue getDatasetValue() {
		return datasetValue;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeUpdate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeUpdate(Facility facility) {
		ParameterTarget parameterTarget = new ParameterTarget();
		parameterTarget.setPath(datasetValue.getParameterOwnerPath());
		parameterTarget.setName(datasetValue.getParameterName());

		return facility.failsafeUpdateParameterValue(getTarget(), parameterTarget, datasetValue.getValue());
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeDelete(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeDelete(Facility facility) {
		return facility.deleteDataset(getTarget());
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.Instruction#executeCreate(org.squashtest.tm.service.internal.batchimport.Facility)
	 */
	@Override
	protected LogTrain executeCreate(Facility facility) {
		return executeUpdate(facility);
	}


}
