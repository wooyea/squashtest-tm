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
package org.squashtest.tm.service.optimized;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.customfield.MultiSelectField;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.dto.*;
import org.squashtest.tm.security.UserContextHolder;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.squashtest.tm.service.optimized.SqlCustomFieldModelFactory.*;
import static org.squashtest.tm.service.optimized.SqlRequest.FIND_CUF_BY_IDS;

@Service
@Transactional(readOnly = true)
public class OptimizedServiceImpl implements OptimizedService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OptimizedServiceImpl.class);

	private JdbcTemplate jdbcTemplate;

	@Inject
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Long> findReadableProjectIds() {
		//ACL class of projects is 1
		//ACL ids of profiles that can read are well... everybody ^^
		String username = UserContextHolder.getUsername();
		Long userId = findUserId(username);
		boolean isAdmin = userIsAdmin(userId);

		if (isAdmin) {
			return jdbcTemplate.queryForList("select PROJECT_ID from PROJECT where PROJECT_TYPE = 'P';", Long.class);
		} else {
			//find all the team of the user and it's party id it self.
			List<Long> partyIds = jdbcTemplate.queryForList("select TEAM_ID from CORE_TEAM_MEMBER where USER_ID = ?;", Long.class, userId);
			//add the user party id itself;
			partyIds.add(userId);
			return jdbcTemplate.queryForList(SqlRequest.FIND_READABLE_PROJECT_IDS, Long.class, StringUtils.join(partyIds, ","));
		}

	}

	@Override
	public List<JsonProject> findJsonProjects(List<Long> projectIds) throws SQLException {
		// First we want to cache infolist to avoid hundreds of not useful requests for the same infolist across all projects...
		// Typically, the whole infolist repository is not that big that it could lead to a problem.
		// Even with enthusiast user we should not have more than a dozen of infolist, witch is light compared to 3 * number of project requests
		// or a big union on 3 joins on project and infolist
		final Map<Long, JsonInfoList> infoListMap = getInfolistCache();

		//1 get the projects, hydrated with infolist from the map
		final Map<Long, JsonProject> projects = getJsonProjects(projectIds, infoListMap);

		//2 get the cuf bindings and put them inside projects
		//2.1 prepare the cuf model map
		List<Long> cufIds = getCufIds(projectIds);

		//2.2 Put all cuf model (with all the ugly stuff that create so much n+1 request with hibernate, namely options) in a map
		final Map<Long, CustomFieldModel<?>> cufModelMap = getCufModelMap(cufIds);

		//2.3 get cuf bindings for each project and use cuf model map to populate the bindings.
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("projectIds", projectIds);

		NamedParameterJdbcTemplate template =
			new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());

		template.query(SqlRequest.FIND_CUF_BINDINGS_BY_PROJECT_IDS, parameters, new ResultSetExtractor<Map<Long,JsonProject>>() {

			@Override
			public Map<Long, JsonProject> extractData(ResultSet rs) throws SQLException, DataAccessException {
				while(rs.next()){
					CustomFieldBindingModel customFieldBindingModel = new CustomFieldBindingModel();
					Long projectId = rs.getLong("BOUND_PROJECT_ID");

					customFieldBindingModel.setId(rs.getLong("CFB_ID"));
					customFieldBindingModel.setProjectId(projectId);

					BindableEntityModel bindableEntityModel = new BindableEntityModel();
					bindableEntityModel.setEnumName(rs.getString("BOUND_ENTITY"));

					customFieldBindingModel.setBoundEntity(bindableEntityModel);
					customFieldBindingModel.setCustomField(cufModelMap.get(rs.getLong("CF_ID")));

					String renderingLocation = rs.getString("RENDERING_LOCATION");
					if(StringUtils.isNotBlank(renderingLocation)){
						RenderingLocationModel[] renderingLocations = new RenderingLocationModel[1];
						RenderingLocationModel renderingLocationModel = new RenderingLocationModel();
						renderingLocationModel.setEnumName(renderingLocation);
						renderingLocations[0] = renderingLocationModel;
						customFieldBindingModel.setRenderingLocations(renderingLocations);
					}

					projects.get(projectId).addCustomFieldModel(customFieldBindingModel);
				}

				//nothing to return as we just hydrate the projects with their cuf binding models
				return null;
			}
		});

		return new ArrayList<>(projects.values());
	}

	private Map<Long, CustomFieldModel<?>> getCufModelMap(List<Long> cufIds) throws SQLException {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("cufIds", cufIds);

		NamedParameterJdbcTemplate template =
			new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());

		return template.query(FIND_CUF_BY_IDS, parameters, new ResultSetExtractor<Map<Long, CustomFieldModel<?>>>() {

			@Override
			public Map<Long, CustomFieldModel<?>> extractData(ResultSet rs) throws SQLException, DataAccessException {
				Map<Long, CustomFieldModel<?>> cufModelMap1 = new HashMap<>();

				//remember, here we have tuple with cuf-cufOption join, so take care of multiple line for the same cuf...
				while (rs.next()) {
					Long cufId = rs.getLong("CF_ID");
					//if map contains key, we must check options and skip only if option is absent or already exist in the set
					if (cufModelMap1.containsKey(cufId)) {
						String inputType = rs.getString("INPUT_TYPE");
						//option ? else we pass
						switch (EnumUtils.getEnum(InputType.class, inputType)) {
							case DROPDOWN_LIST:
								handleDropdownListOption(rs, cufModelMap1, cufId);
								break;
							case TAG:
								handleTagOption(rs, cufModelMap1, cufId);
								break;
							default:
								//do nothing it's not an option, so the model is already complete and in the map
						}
					} else {
						//new cuf in map we must do the model for the cuf and eventually, the joined cuf option
						CustomFieldModel<?> customFieldModel;
						String inputType = rs.getString("INPUT_TYPE");
						switch (EnumUtils.getEnum(InputType.class, inputType)) {
							case DROPDOWN_LIST:
								customFieldModel = createSingleSelectCustomField(rs);
								break;
							case TAG:
								customFieldModel = createMultiSelectCustomField(rs);
								break;
							case DATE_PICKER:
							case NUMERIC:
							default:
								//create a standard custom field
								customFieldModel = createCustomField(rs);
						}
						cufModelMap1.put(customFieldModel.getId(), customFieldModel);
					}
				}

				return cufModelMap1;
			}

			private void handleTagOption(ResultSet rs, Map<Long, CustomFieldModel<?>> cufModelMap1, Long cufId) throws SQLException {
				CustomFieldOptionModel optionModel = getCustomFieldOptionModel(rs);
				MultiSelectFieldModel customFieldModel = (MultiSelectFieldModel) cufModelMap1.get(cufId);
				if (!customFieldModel.getOptions().contains(optionModel)) {
					customFieldModel.addOption(optionModel);
				}
			}

			private void handleDropdownListOption(ResultSet rs, Map<Long, CustomFieldModel<?>> cufModelMap1, Long cufId) throws SQLException {
				CustomFieldOptionModel optionModel = getCustomFieldOptionModel(rs);
				SingleSelectFieldModel customFieldModel = (SingleSelectFieldModel) cufModelMap1.get(cufId);
				if (!customFieldModel.getOptions().contains(optionModel)) {
					customFieldModel.addOption(optionModel);
				}
			}

		});
	}


	private List<Long> getCufIds(List<Long> projectIds) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("projectIds", projectIds);

		NamedParameterJdbcTemplate template =
			new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());

		return template.queryForList(SqlRequest.FIND_CUF_IDS_BY_PROJECT_IDS, parameters, Long.class);
	}

	private Map<Long, JsonProject> getJsonProjects(List<Long> projectIds, final Map<Long, JsonInfoList> infoListMap) throws SQLException {

		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("projectIds", projectIds);

		NamedParameterJdbcTemplate template =
			new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());

		return template.query(SqlRequest.FIND_PROJECT_BY_IDS, parameters, new ResultSetExtractor<Map<Long, JsonProject>>() {
			@Override
			public Map<Long, JsonProject> extractData(ResultSet rs) throws SQLException, DataAccessException {
				Map<Long, JsonProject> jsonProjects = new HashMap<>();
				while (rs.next()) {
					JsonProject jsonProject = new JsonProject();
					long projectId = rs.getLong("PROJECT_ID");
					jsonProject.setId(projectId);
					jsonProject.setName(rs.getString("NAME"));
					jsonProject.setRequirementCategories(infoListMap.get(rs.getLong("REQ_CATEGORIES_LIST")));
					jsonProject.setTestCaseNatures(infoListMap.get(rs.getLong("TC_NATURES_LIST")));
					jsonProject.setTestCaseTypes(infoListMap.get(rs.getLong("TC_TYPES_LIST")));
					jsonProjects.put(projectId, jsonProject);
				}
				return jsonProjects;
			}
		});
	}

	private Map<Long, JsonInfoList> getInfolistCache() throws SQLException {
		return jdbcTemplate.query(SqlRequest.FIND_ALL_INFOLIST_AND_ITEMS, new ResultSetExtractor<Map<Long, JsonInfoList>>() {
			@Override
			public Map<Long, JsonInfoList> extractData(ResultSet rs) throws SQLException, DataAccessException {
				Map<Long, JsonInfoList> infolistMap = new HashMap<>();
				while (rs.next()) {
					Long infolistId = rs.getLong("INFO_LIST_ID");
					if (infolistMap.containsKey(infolistId)) {
						//the infolist is already in the map, we only need to add the item
						JsonInfoList infoList = infolistMap.get(infolistId);
						//get the item
						JsonInfoListItem item = getJsonInfoListItem(rs);
						infoList.addItem(item);

					} else {
						JsonInfoList infoList = new JsonInfoList();

						infoList.setId(infolistId);
						infoList.setCode(rs.getString("CODE"));
						infoList.setLabel(rs.getString("LABEL"));

						//now the first item
						JsonInfoListItem item = getJsonInfoListItem(rs);

						infoList.addItem(item);
						infolistMap.put(infolistId, infoList);
					}

				}
				return infolistMap;
			}
		});
	}

	private JsonInfoListItem getJsonInfoListItem(ResultSet rs) throws SQLException {
		JsonInfoListItem item = new JsonInfoListItem();
		item.setId(rs.getLong("ITEM_ID"));
		item.setCode(rs.getString("ITEM_CODE"));
		item.setLabel(rs.getString("ITEM_LABEL"));

		String itemType = rs.getString("ITEM_TYPE");
		if (itemType.equals("SYS")) {
			item.setSystem(true);
		}

		item.setDefault(rs.getBoolean("IS_DEFAULT"));
		item.setIconName(rs.getString("ICON_NAME"));
		return item;
	}

	private boolean userIsAdmin(Long userId) {
		//checking if user is admin, in that case he can read everything
		Integer count = jdbcTemplate.queryForObject(SqlRequest.USER_IS_ADMIN_COUNT, Integer.class, userId);
		return count > 0;
	}

	private Long findUserId(String username) {
		return jdbcTemplate.queryForObject("select PARTY_ID from CORE_USER where LOGIN = ?;", Long.class, username);
	}

	private CustomFieldModel<?> createCustomField(ResultSet rs) throws SQLException {
		CustomFieldModel<String> model = new SingleValuedCustomFieldModel();
		initModel(rs, model);
		model.setDefaultValue(rs.getString("DEFAULT_VALUE"));
		return model;

	}

	private CustomFieldModel<?> createSingleSelectCustomField(ResultSet rs) throws SQLException {
		SingleSelectFieldModel model = new SingleSelectFieldModel();
		initModel(rs, model);
		model.setDefaultValue(rs.getString("DEFAULT_VALUE"));
		CustomFieldOptionModel customFieldOptionModel = getCustomFieldOptionModel(rs);
		model.addOption(customFieldOptionModel);
		return model;
	}

	private CustomFieldModel<?> createMultiSelectCustomField(ResultSet rs) throws SQLException {
		MultiSelectFieldModel model = new MultiSelectFieldModel();
		initModel(rs, model);
		String defaultValue = rs.getString("DEFAULT_VALUE");
		if (StringUtils.isNotBlank(defaultValue)) {
			for (String value : defaultValue.split(MultiSelectField.SEPARATOR_EXPR)) {
				model.addDefaultValue(value);
			}
		}
		CustomFieldOptionModel customFieldOptionModel = getCustomFieldOptionModel(rs);
		model.addOption(customFieldOptionModel);
		return model;
	}

	private void initModel(ResultSet rs, CustomFieldModel model) throws SQLException {
		model.setId(rs.getLong("CF_ID"));
		model.setCode(rs.getString("CODE"));
		model.setLabel(rs.getString("LABEL"));
		model.setName(rs.getString("NAME"));
		model.setOptional(rs.getBoolean("OPTIONAL"));

	}

	private CustomFieldOptionModel getCustomFieldOptionModel(ResultSet rs) throws SQLException {
		String optionLabel = rs.getString("OPTION_LABEL");
		String optionCode = rs.getString("OPTION_CODE");
		return new CustomFieldOptionModel(optionLabel, optionCode);
	}
}
