/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.hibernate.mapping

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;

import spock.lang.Specification;

/**
 * Superclass for hibernate mapping integration tests.
 */
@ContextConfiguration(["classpath:repository/dependencies-scan-context.xml", "classpath:no-validation-config-context.xml", "classpath*:META-INF/**/datasource-context.xml", "classpath*:META-INF/**/repository-context.xml"])
@TransactionConfiguration(transactionManager = "squashtest.tm.hibernate.TransactionManager")
abstract class HibernateMappingSpecification extends Specification {
	@Inject SessionFactory sessionFactory;
	/**
	 * Runs action closure in a new transaction created from a new session.
	 * @param action 
	 * @return propagates closure result.
	 */
	def final doInTransaction(def action) {
		Session s = sessionFactory.openSession()
		Transaction tx = s.beginTransaction()

		try {
			def res =action(s)

			s.flush()
			tx.commit()
			return res
		} finally {
			s?.close()
		}
	}

	def final Session getCurrentSession() {
		sessionFactory.currentSession
	}
	/**
	 * Persists a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	def final persistFixture(Object... fixtures) {
		fixtures.each {  fixture ->
			doInTransaction { it.persist fixture }
		}
	}
	/**
	 * Deletes a fixture in a separate session / transaction
	 * @param fixture
	 * @return
	 */
	def final deleteFixture(Object... fixtures) {
		fixtures.each {  fixture ->
			doInTransaction {  it.delete fixture  }
		}
	}
}
