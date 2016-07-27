package org.cmdbuild.servlets.json;

import static java.util.stream.Collectors.toList;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.CommunicationConstants.PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.SESSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_NAME;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.Collection;
import java.util.Collections;

import org.cmdbuild.auth.GroupFetcher;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.listeners.CMDBContext;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.privileges.fetchers.factories.PrivilegeFetcherFactory;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Optional;

public class Session extends JSONBaseWithSpringContext {

	public static class Group {

		private String name;
		private String description;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	public static class Response {

		private String sessionId;
		private String userName;
		private String group;
		private Collection<Group> groups;

		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(final String sessionId) {
			this.sessionId = sessionId;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(final String userName) {
			this.userName = userName;
		}

		public String getGroup() {
			return group;
		}

		public void setGroup(final String group) {
			this.group = group;
		}

		public Collection<Group> getGroups() {
			return groups;
		}

		public void setGroups(final Collection<Group> groups) {
			this.groups = groups;
		}

	}

	@JSONExported
	@Unauthorized
	public JsonResponse create( //
			@Parameter(value = USER_NAME) final String loginString, //
			@Parameter(value = PASSWORD) final String password) {
		final String sessionId = authLogic().create(LoginDTO.newInstance() //
				.withLoginString(loginString)//
				.withPassword(password)//
				.build());
		return success(responseOf(sessionId));
	}

	@JSONExported
	@Unauthorized
	public JsonResponse readCurrent() {
		final String sessionId = authLogic().getCurrent();
		return success(responseOf(sessionId));
	}

	@JSONExported
	@Unauthorized
	public JsonResponse update( //
			@Parameter(value = SESSION) final String sessionId, //
			@Parameter(value = GROUP) final String groupName //
	) {
		authLogic().update(sessionId,
				LoginDTO.newInstance() //
						.withGroupName(groupName)//
						.build());
		return success(responseOf(sessionId));
	}

	@JSONExported
	public JsonResponse delete( //
			@Parameter(value = SESSION) final String sessionId //
	) {
		authLogic().delete(sessionId);
		{
			/*
			 * It's needed because the HTTP session is still used for other
			 * stuff (e.g. reports).
			 */
			final Optional<CMDBContext> element = contextStore().get();
			if (element.isPresent()) {
				element.get().getRequest().getSession().invalidate();
			}
		}
		return success();
	}

	private Response responseOf(final String sessionId) {
		return new Response() {
			{
				setSessionId(sessionId);

				if (sessionId != null) {
					final OperationUser user = authLogic().getUser(sessionId);
					setUserName(user.getAuthenticatedUser().getUsername());
					setGroup(user.isValid() ? user.getPreferredGroup().getName() : null);
					setGroups(user.getAuthenticatedUser().getGroupNames().stream() //
							.map(input -> new Group() {
						{
							setName(input);
							setDescription(authLogic().getGroupWithName(input).getDescription());
						}
					}) //
							.collect(toList()));
				}
			}
		};
	}

	/*
	 * DON'T REMOVE, used within index.jsp
	 */
	public static JSONArray serializeGroupForLogin(final Iterable<String> groupNames) throws JSONException {
		final JSONArray jsonGroups = new JSONArray();
		final CMDataView dataView = applicationContext().getBean(DBDataView.class);
		final Iterable<PrivilegeFetcherFactory> factories = Collections.emptyList();
		final GroupFetcher groupFetcher = new DBGroupFetcher(dataView, factories);
		for (final String groupName : groupNames) {
			final CMGroup group = groupFetcher.fetchGroupWithName(groupName);
			final JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("name", groupName);
			jsonGroup.put("description", group.getDescription());
			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}

}
