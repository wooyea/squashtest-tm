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

package org.squashtest.tm.api.report.jasperreports;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.util.Assert;
import org.springframework.web.servlet.View;
import org.squashtest.tm.api.report.ReportView;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.query.ReportQuery;
import org.squashtest.tm.core.i18n.Labelled;

/**
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
public final class JasperReportsView extends Labelled implements ReportView, InitializingBean, MessageSourceAware {
	private String[] formats;
	private View viewBean;
	private ReportQuery query;
	private MessageSource messageSource;

	/**
	 * @see org.squashtest.tm.api.report.ReportView#getFormats()
	 */
	@Override
	public String[] getFormats() {
		return formats;
	}

	/**
	 * @param formats
	 *            the formats to set
	 */
	public void setFormats(String[] formats) {
		this.formats = formats;
	}

	/**
	 * @see org.squashtest.tm.api.report.ReportView#buildViewModel(java.lang.String, java.util.Map)
	 */
	@Override
	public Map<String, Object> buildViewModel(String format, Map<String, Criteria> criteria) {
		Map<String, Object> res = new HashMap<String, Object>();

		query.executeQuery(criteria, res);

		res.put("format", format);
		res.put(JRParameter.REPORT_RESOURCE_BUNDLE,
				new MessageSourceResourceBundle(messageSource, LocaleContextHolder.getLocale()));

		return res;
	}

	/**
	 * Sets the Spring MVC View bean. It should be a JasperReportMultiFormatView.
	 * 
	 * @param viewBean
	 *            the springView to set
	 */
	public void setViewBean(View viewBean) {
		this.viewBean = viewBean;
	}

	/**
	 * @return the springView
	 */
	@Override
	public View getViewBean() {
		return viewBean;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(ReportQuery query) {
		this.query = query;
	}

	/**
	 * Checks the state of this bean.
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		Assert.notNull(viewBean, "viewBean must not be null");
		Assert.notNull(query, "query must not be null");

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.tm.core.i18n.ContextBasedInternationalized#setMessageSource(org.springframework.context.MessageSource
	 * )
	 */
	@Override
	public void setMessageSource(MessageSource messageSource) {
		super.setMessageSource(messageSource);
		this.messageSource = messageSource;
	}

}