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

package org.squashtest.tm.service.internal.repository.hibernate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.internal.foundation.collection.JpaPagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomConnectionLogDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * @author aguilhem
 */
public class ConnectionLogDaoImpl implements CustomConnectionLogDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLogDaoImpl.class);

	private static final String HQL_FIND_CONNECTION_LOGS_BASE = "from ConnectionLog ConnectionLog ";

	private static final String HQL_FIND_CONNECTION_LOGS_FILTER_BY_LOGIN = "where ConnectionLog.login like :loginFilter ";
	private static final String HQL_FIND_CONNECTION_LOGS_FILTER_BY_DATE = "ConnectionLog.connectionDate between :startDate and :endDate ";
	private static final String HQL_WHERE = "where ";
	private static final String HQL_AND = "and ";

	private static final String CONNECTION_DATE_DATA = "connection-date";
	private static final String LOGIN_DATA = "login";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String LOGIN = "loginFilter";

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<ConnectionLog> findSortedConnections(PagingAndSorting paging, ColumnFiltering columnFiltering) {

		StringBuilder sQuery = new StringBuilder(HQL_FIND_CONNECTION_LOGS_BASE);
		String login="";
		String dates="";
		String dateFilterRequestBeginningWord;

		if(columnFiltering.isDefined()){
			login = columnFiltering.getFilter(LOGIN_DATA);
			dates = columnFiltering.getFilter(CONNECTION_DATE_DATA);
			dateFilterRequestBeginningWord = HQL_WHERE;
			if(!login.isEmpty()){
				sQuery.append(HQL_FIND_CONNECTION_LOGS_FILTER_BY_LOGIN);
				dateFilterRequestBeginningWord = HQL_AND;
			}
			if(!dates.isEmpty()){
				sQuery.append(dateFilterRequestBeginningWord);
				sQuery.append(HQL_FIND_CONNECTION_LOGS_FILTER_BY_DATE);
			}

		}

		SortingUtils.addOrder(sQuery, paging);

		Query hQuery = entityManager.createQuery(sQuery.toString());

		if(!login.isEmpty()){
			hQuery.setParameter(LOGIN, "%" + login + "%");
		}
		if(!dates.isEmpty()){
			setQueryStartAndEndDateParameters(dates, hQuery);
		}

		JpaPagingUtils.addPaging(hQuery, paging);

		return hQuery.getResultList();

	}

	@Override
	public List<ConnectionLog> findFilteredConnections(ColumnFiltering columnFiltering) {
		DefaultPagingAndSorting sorting = new DefaultPagingAndSorting();
		sorting.setShouldDisplayAll(true);
		return findSortedConnections(sorting, columnFiltering);
	}

	private void setQueryStartAndEndDateParameters(String dates, Query query){
		Date startDate = null;
		Date endDate = null;


		if (dates.contains("-")) {
			String[] dateArray = dates.split("-");
			try {
				startDate = DateUtils.parseDdMmYyyyDate(dateArray[0].trim());
				endDate = DateUtils.parseDdMmYyyyDate(dateArray[1].trim());
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}

		} else {
			try {
				startDate = DateUtils.parseDdMmYyyyDate(dates.trim());
				endDate = DateUtils.nextDay(startDate);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage(), e);
			}

		}
		query.setParameter(START_DATE, startDate);
		query.setParameter(END_DATE, endDate);
	}
}
