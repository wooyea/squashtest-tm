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

package org.squashtest.tm.web.internal.controller.users;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.TeamFinderService;
import org.squashtest.tm.service.user.TeamModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.administration.UserModel;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter;
import org.squashtest.tm.web.internal.model.datatable.DataTableMapperPagingAndSortingAdapter.SortedAttributeSource;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelHelper;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;
/**
 * @author mpagnon
 * 
 */
@Controller
@RequestMapping("/administration/teams")
public class TeamController extends PartyControllerSupport {
	@Inject
	private TeamModificationService service;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private TeamFinderService teamFinderService;

	private static final String TEAM_ID_URL = "/{teamId}";

	private DatatableMapper<String> teamsMapper = new NameBasedMapper(9)
			.mapAttribute(Team.class, "name", String.class, "name")
			.mapAttribute(Team.class, "description", String.class, "description")
			.mapAttribute(Team.class, "members.size", Long.class, "nb-associated-users")
			.mapAttribute(Team.class, "audit.createdOn", Date.class, "created-on")
			.mapAttribute(Team.class, "audit.createdBy", String.class, "created-by")
			.mapAttribute(Team.class, "audit.lastModifiedOn", Date.class, "last-mod-on")
			.mapAttribute(Team.class, "audit.lastModifiedBy", String.class, "last-mod-by");

	private DatatableMapper<String> membersMapper = new NameBasedMapper(1).mapAttribute(User.class, "firstName",
			String.class, "user-name");

	private DatatableMapper<String> permissionMapper = new NameBasedMapper(2).mapAttribute(ProjectPermission.class,
			"project.name", String.class, "project-name").mapAttribute(ProjectPermission.class,
			"permissionGroup.qualifiedName", String.class, "permission-name");

	private static final Logger LOGGER = LoggerFactory.getLogger(TeamController.class);

	/**
	 * Creates a new Team 
	 * 
	 * @param team : the given {@link Team} filled with a name and a description
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public void createNew(@Valid @ModelAttribute("add-team") Team team) {
		LOGGER.info(ToStringBuilder.reflectionToString(team));
		service.persist(team);
	}

	/**
	 * Return the DataTableModel to display the table of all teams.
	 * 
	 * @param params
	 *            the {@link DataTableDrawParameters} for the teams table
	 * @return the {@link DataTableModel} with organized {@link Team} infos.
	 */
	@RequestMapping( method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTableModel(final DataTableDrawParameters params, final Locale locale) {
		
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, teamsMapper);
		Filtering filtering = new  DataTableFiltering(params);

		PagedCollectionHolder<List<Team>> holder = service.findAllFiltered(paging, filtering);

		return new TeamsDataTableModelHelper(locale, messageSource).buildDataModel(holder, params.getsEcho());
	}
	
	/**
	 * Will delete the given team along with it's permissions.
	 * will not delete it's associated users
	 * 
	 * @param teamId
	 */
	@RequestMapping(value = TEAM_ID_URL, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTeam(@PathVariable long teamId) {
		service.deleteTeam(teamId);
	}
	
	/**
	 * Will return a view for the team of the given id
	 * 
	 * @param teamId
	 */
	@RequestMapping(value = TEAM_ID_URL, method = RequestMethod.GET)
	public String showTeamModificationPage(@PathVariable Long teamId, Model model) {
		if(!permissionEvaluationService.hasRole("ROLE_ADMIN")){
			throw new AccessDeniedException("Access is denied");
		}
		Team team = service.findById(teamId);
		model.addAttribute("team", team);

		List<?> permissionModel = createPermissionTableModel(teamId, new DefaultPagingAndSorting(),
				DefaultFiltering.NO_FILTERING, "").getAaData();
		model.addAttribute("permissions", permissionModel);

		List<?> userModel = createMembersTableModel(teamId, new DefaultPagingAndSorting(), DefaultFiltering.NO_FILTERING,
				"").getAaData();
		model.addAttribute("users", userModel);

		Map<String, Object> permissionPopupModel = getPermissionPopup(teamId);
		model.addAttribute("permissionList", permissionPopupModel.get("permissionList"));
		model.addAttribute("myprojectList", permissionPopupModel.get("myprojectList"));

		return "team-modification.html";
	}
	
	@RequestMapping(value = TEAM_ID_URL , method = RequestMethod.POST, params = "id=team-description")
	@ResponseBody 
	public String changeDescription(@PathVariable Long teamId , @RequestParam String value ){
		service.changeDescription(teamId, value);
		return value;
	}
	
	@RequestMapping(value = TEAM_ID_URL+"/name" , method = RequestMethod.POST)
	@ResponseBody 
	public RenameModel changeName(@PathVariable Long teamId , @RequestParam String value ){
		service.changeName(teamId, value);
		return new RenameModel(value);
	}
	
	@RequestMapping(value = TEAM_ID_URL+"/general")
	public String refreshGeneralInfos(@PathVariable("teamId") long teamId, Model model){
		Team team = service.findById(teamId);
		model.addAttribute("auditableEntity", team);
		return "fragments-utils/general-information-panel.html";
	}

	
	// ************************************ team members section ************************ 
	
	
	
	@RequestMapping(value=TEAM_ID_URL+"/members", method = RequestMethod.GET, params=RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getMembersTableModel(DataTableDrawParameters params, @PathVariable("teamId") long teamId){
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, membersMapper, SortedAttributeSource.SINGLE_ENTITY);
		Filtering filtering = new DataTableFiltering(params);
		return createMembersTableModel(teamId, paging, filtering, params.getsEcho());
	}
	
	
	@RequestMapping(value=TEAM_ID_URL+"/members/{memberIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void removeMember(@PathVariable("teamId") long teamId, @PathVariable("memberIds") List<Long> memberIds){
		service.removeMembers(teamId, memberIds);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value=TEAM_ID_URL+"/non-members", headers="Accept=application/json")
	@ResponseBody
	public Collection<UserModel> getNonMembers(@PathVariable("teamId") long teamId){
		List<User> nonMembers = service.findAllNonMemberUsers(teamId);
		return CollectionUtils.collect(nonMembers, new UserModelCreator());
	}
	
	
	@RequestMapping(value=TEAM_ID_URL+"/members/{logins}", method = RequestMethod.PUT)
	@ResponseBody
	public void addMembers(@PathVariable("teamId") long teamId, @PathVariable("logins") List<String> userlogins){
		service.addMembers(teamId, userlogins);
	}
	
	// **************************** team permission section ************************
	
	@RequestMapping(value = TEAM_ID_URL+"/add-permission", method = RequestMethod.POST)
	@ResponseBody
	public void addNewPermission(@RequestParam("project") long projectId, @PathVariable long teamId,
			@RequestParam String permission) {
		permissionService.addNewPermissionToProject(teamId, projectId, permission);
	}

	@RequestMapping(value = TEAM_ID_URL+"/remove-permission", method = RequestMethod.POST)
	@ResponseBody
	public void removePermission(@RequestParam("project") long projectId, @PathVariable long teamId) {
		permissionService.removeProjectPermission(teamId, projectId);
	}

	@RequestMapping(value = TEAM_ID_URL+"/permissions", method = RequestMethod.GET, params=RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getPermissionTableModel(DataTableDrawParameters params, @PathVariable("teamId") long teamId) {
		PagingAndSorting paging = new DataTableMapperPagingAndSortingAdapter(params, permissionMapper);
		Filtering filtering = new DataTableFiltering(params);
		return createPermissionTableModel(teamId, paging, filtering, params.getsEcho());
	}

	
	// ******************************* private *************************************

	private DataTableModel createMembersTableModel(long teamId, PagingAndSorting paging, Filtering filtering, String secho) {
		PagedCollectionHolder<List<User>> holder = service.findAllTeamMembers(teamId, paging, filtering);
		return new MembersTableModelHelper().buildDataModel(holder, secho);
	}

	@RequestMapping(value = TEAM_ID_URL + "/permission-popup", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getPermissionPopup(@PathVariable long teamId) {
		return createPermissionPopupModel(teamId);
	}
	
	// ************************* private classes ***********************
	
	
	private static final class UserModelCreator implements Transformer{
		@Override
		public Object transform(Object user) {
			return new UserModel((User) user);
		}
	}
	private static final class TeamsDataTableModelHelper extends DataTableModelHelper<Team> {
		private InternationalizationHelper messageSource;
		private Locale locale;
		private TeamsDataTableModelHelper(Locale locale, InternationalizationHelper messageSource){
			this.locale = locale;
			this.messageSource = messageSource;
		}
		@Override
		public Map<String, Object> buildItemData(Team item) {
			final AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<String, Object>();
			res.put(DataTableModelHelper.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelHelper.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put("name", item.getName());
			res.put("description", item.getDescription());
			res.put("nb-associated-users", item.getMembers().size());
			res.put("created-on", messageSource.localizeDate(auditable.getCreatedOn(), locale));
			res.put("created-by", auditable.getCreatedBy());
			res.put("last-mod-on", messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
			res.put("last-mod-by", auditable.getLastModifiedBy());			
			res.put(DataTableModelHelper.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return res;
		}
	}
	
	private static final class MembersTableModelHelper extends DataTableModelHelper<User>{
		private MembersTableModelHelper() {
			super();
		}
		@Override
		protected Map<?,?> buildItemData(User item) {
			Map<String,Object> res = new HashMap<String, Object>();
			res.put("user-id", item.getId());
			res.put("user-index", getCurrentIndex());
			res.put("user-name", item.getFirstName()+" "+item.getLastName()+" ("+item.getLogin()+")");
			res.put("empty-delete-holder", null);
			return res;
		}
	}
}
