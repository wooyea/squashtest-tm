/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.service.internal.feature;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.milestone.MilestoneManagerService;

/**
 * @author Gregory Fouquet
 *
 */
@Service("featureManager")
@Transactional
public class FeatureManagerImpl implements FeatureManager {
	@Inject
	private ConfigurationService configuration;

	@Inject
	private MilestoneManagerService milestoneManager;

	/**
	 * This feature is polled once per thread (because il could be polled many
	 * times). If more features require caching, consider something more
	 * scalable than adding a field.
	 */
	private final ThreadLocal<Boolean> caseInsensitiveLogin = new ThreadLocal<>();

	/**
	 * @see org.squashtest.tm.service.feature.FeatureManager#isEnabled(org.squashtest.tm.service.feature.FeatureManager.Feature)
	 */
	@Override
	public boolean isEnabled(Feature feature) {
		boolean enabled;

		switch (feature) {
		case MILESTONE:
			enabled = configuration.getBoolean(ConfigurationService.MILESTONE_FEATURE_ENABLED);
			break;

		case CASE_INSENSITIVE_LOGIN:
			if (caseInsensitiveLogin.get() == null) {
				caseInsensitiveLogin.set(configuration
						.getBoolean(ConfigurationService.CASE_INSENSITIVE_LOGIN_FEATURE_ENABLED));
			}
			enabled = caseInsensitiveLogin.get();
			break;
		default:
			throw new IllegalArgumentException("I don't know feature '" + feature
					+ "'. I am unable to tell if it's enabled or not");
		}

		return enabled;
	}

	/**
	 *
	 * @see org.squashtest.tm.service.feature.FeatureManager#setEnabled(org.squashtest.tm.service.feature.FeatureManager.Feature,
	 *      boolean)
	 */
	@Override
	public void setEnabled(Feature feature, boolean enabled) {
		switch (feature) {
		case MILESTONE:
			setMilestoneFeatureEnabled(enabled);
			break;

		case CASE_INSENSITIVE_LOGIN:
			setCaseInsensitiveLoginFeatureEnabled(enabled);
			break;

		default:
			throw new IllegalArgumentException("I don't know feature '" + feature
					+ "'. I am unable to switch its enabled status to " + enabled);
		}
	}

	/**
	 * @param enabled
	 */
	private void setCaseInsensitiveLoginFeatureEnabled(boolean enabled) {
		// TODO check if possible
		configuration.set(ConfigurationService.CASE_INSENSITIVE_LOGIN_FEATURE_ENABLED, enabled);
		caseInsensitiveLogin.set(enabled);
	}

	private void setMilestoneFeatureEnabled(boolean enabled) {
		configuration.set(ConfigurationService.MILESTONE_FEATURE_ENABLED, enabled);
		if (enabled) {
			milestoneManager.enableFeature();
		} else {
			milestoneManager.disableFeature();
		}

	}
}
