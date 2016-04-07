/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyPreference;
import org.squashtest.tm.service.internal.repository.CustomPartyPreferenceDao;

@Repository("CustomPartyPreferenceDao")
public class HibernateCustomPartyPreferenceDao extends HibernateEntityDao<PartyPreference> implements CustomPartyPreferenceDao {


	@Override
	public Map<String, String> findAllPreferencesForParty(Party party) {
		return findAllPreferencesForParty(party.getId());
	}

	@Override
	public Map<String, String> findAllPreferencesForParty(long partyId) {
		Map<String,String> result = new HashMap<>();
		Query q = currentSession().getNamedQuery("partyPreference.findAllForParty");
		q.setParameter("partyId",partyId);
		List<PartyPreference> prefs = q.list();
		for (PartyPreference pref : prefs) {
			result.put(pref.getPreferenceKey(),pref.getPreferenceValue());
		}
		return result;
	}

	@Override
	public PartyPreference findByPartyAndPreferenceKey(Party party, String preferenceKey) {
		return findByPartyAndPreferenceKey(party.getId(),preferenceKey);
	}

	@Override
	public PartyPreference findByPartyAndPreferenceKey(long partyId, String preferenceKey) {
		Query q = currentSession().getNamedQuery("partyPreference.findByPartyAndKey");
		q.setParameter("partyId",partyId);
		q.setParameter("preferenceKey",preferenceKey);
		return (PartyPreference) q.uniqueResult();
	}
}