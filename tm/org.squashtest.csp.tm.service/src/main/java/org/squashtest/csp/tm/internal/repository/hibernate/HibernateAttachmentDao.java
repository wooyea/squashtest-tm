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
package org.squashtest.csp.tm.internal.repository.hibernate;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.squashtest.csp.tm.domain.attachment.Attachment;
import org.squashtest.csp.tm.domain.attachment.AttachmentList;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.internal.repository.AttachmentDao;

@Repository
public class HibernateAttachmentDao extends HibernateEntityDao<Attachment>
 implements AttachmentDao {

	@Override
	public Attachment findAttachmentWithContent(Long attachmentId) {
		Attachment attachment = findById(attachmentId);
		Hibernate.initialize(attachment.getContent());
		return attachment;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Attachment> findAllAttachments(Long attachmentListId) {
		List<Map<String,?>> rawResult= currentSession().createCriteria(AttachmentList.class)
										.add(Restrictions.eq("id", attachmentListId))
										.createAlias("attachments", "attach")
										.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
										.list();


		Set<Attachment> attachs = new HashSet<Attachment>();
		ListIterator<Map<String,?>> iter = rawResult.listIterator();
		while ( iter.hasNext() ) {
		    Map<String,?> map = iter.next();
		    attachs.add((Attachment) map.get("attach"));
		}


		return attachs;


	}

	private long getContentId(final Long attachmentId){
		return (Long) executeEntityNamedQuery("attachment.findContentId", new AttachIdQueyParameterCallback(attachmentId));
	}
	
	private class AttachIdQueyParameterCallback implements SetQueryParametersCallback {
		private Long attachmentId;
		private AttachIdQueyParameterCallback(Long attachmentId){
			this.attachmentId = attachmentId;
		}
		@Override
		public void setQueryParameters(Query query) {
			query.setLong("attachId", attachmentId);
		}
	}

	@Override
	public void removeAttachment(Long attachmentId) {

		Attachment attachment = findById(attachmentId);

		Long contentId=getContentId(attachmentId);

		//remove the attachment first because of referential integrity constraints
		remove(attachment);

		//now force the session to commit changes because the damn entity is still 'alive' in the session cache or
		//whatsoever
		currentSession().flush();
		currentSession().evict(attachment);

		Query q = currentSession().getNamedQuery("attachment.removeContent");
		q.setLong("contentId", contentId);
		q.executeUpdate();


	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Attachment> findAllAttachmentsFiltered(
			Long attachmentListId,
			CollectionSorting filter) {


		Session session = currentSession();

		String sortedAttribute = filter.getSortedAttribute();
		String order = filter.getSortingOrder();

		Criteria crit = session.createCriteria(AttachmentList.class,"AttachmentList")
		.add(Restrictions.eq("id",attachmentListId))
		.createAlias("attachments", "Attachment")
		.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		/* add ordering */
		if (sortedAttribute!=null){
			if (order.equals("asc")){
				crit.addOrder(Order.asc(sortedAttribute));
			}
			else{
				crit.addOrder(Order.desc(sortedAttribute));
			}
		}


		/* result range */
		crit.setFirstResult(filter.getFirstItemIndex());
		crit.setMaxResults(filter.getPageSize());


		List<Map<String,?>> rawResult = crit.list();

		List<Attachment> atts = new LinkedList<Attachment>();
		ListIterator<Map<String,?>> iter = rawResult.listIterator();
		while ( iter.hasNext() ) {
		    Map<String,?> map = iter.next();
		    atts.add((Attachment) map.get("Attachment"));
		}


		return atts;
	}


}
