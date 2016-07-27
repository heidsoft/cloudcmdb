package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.CommunicationConstants.ADMIN_PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.ADMIN_USER;
import static org.cmdbuild.servlets.json.CommunicationConstants.CATEGORY;
import static org.cmdbuild.servlets.json.CommunicationConstants.DB_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DB_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.HOST;
import static org.cmdbuild.servlets.json.CommunicationConstants.LANGUAGE;
import static org.cmdbuild.servlets.json.CommunicationConstants.LANGUAGE_PROMPT;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIM_PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.LIM_USER;
import static org.cmdbuild.servlets.json.CommunicationConstants.MANAGEMENT_DATABASE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.PATCHES;
import static org.cmdbuild.servlets.json.CommunicationConstants.PORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.SHARK_SCHEMA;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER;
import static org.cmdbuild.servlets.json.CommunicationConstants.USER_TYPE;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.UserDTO;
import org.cmdbuild.services.PatchManager.Patch;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.database.DatabaseConfigurator;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.ds.PGSimpleDataSource;

public class Configure extends JSONBaseWithSpringContext {

	private static final String SUPERUSER_LOWER = "superuser";
	private static final String SUPER_USER = "SuperUser";

	@JSONExported
	@Configuration
	@Unauthorized
	public void testConnection( //
			@Parameter(HOST) final String host, //
			@Parameter(PORT) final int port, //
			@Parameter(USER) final String user, //
			@Parameter(PASSWORD) final String password //
	) {
		testDatabaseConnection(host, port, user, password);
	}

	private void testDatabaseConnection(final String host, final int port, final String username,
			final String plainPassword) {
		try {
			// FIXME hide implementation details somewhere else
			final PGSimpleDataSource dataSource = new PGSimpleDataSource();
			dataSource.setServerName(host);
			dataSource.setPortNumber(port);
			dataSource.setDatabaseName(MANAGEMENT_DATABASE);
			dataSource.setUser(username);
			dataSource.setPassword(plainPassword);
			dataSource.getConnection();
		} catch (final SQLException ex) {
			Log.CMDBUILD.info("Test connection failed: " + ex.getMessage());
			throw ORMExceptionType.ORM_DATABASE_CONNECTION_ERROR.createException();
		}
	}

	@JSONExported
	@Configuration
	@Unauthorized
	public void apply( //
			@Parameter(LANGUAGE) final String language, //
			@Parameter(LANGUAGE_PROMPT) final boolean languagePrompt, //
			@Parameter(DB_TYPE) final String dbType, //
			@Parameter(DB_NAME) final String dbName, //
			@Parameter(HOST) final String host, //
			@Parameter(PORT) final int port, //
			@Parameter(SHARK_SCHEMA) final boolean createSharkSchema, //
			@Parameter(USER) final String user, //
			@Parameter(PASSWORD) final String password, //
			@Parameter(value = USER_TYPE, required = false) final String systemUserType, //
			@Parameter(value = LIM_USER, required = false) final String limitedUser, //
			@Parameter(value = LIM_PASSWORD, required = false) final String limitedPassword, //
			@Parameter(value = ADMIN_USER, required = false) final String adminUser, //
			@Parameter(value = ADMIN_PASSWORD, required = false) final String adminPassword //
	) throws IOException {
		testDatabaseConnection(host, port, user, password);
		final CmdbuildProperties cmdbuildProps = cmdbuildConfiguration();
		cmdbuildProps.setLanguage(language);
		cmdbuildProps.setLanguagePrompt(languagePrompt);
		cmdbuildProps.store();

		final DatabaseConfigurator.Configuration configuration = new DatabaseConfigurator.Configuration() {

			@Override
			public String getHost() {
				return host;
			}

			@Override
			public int getPort() {
				return port;
			}

			@Override
			public String getUser() {
				return user;
			}

			@Override
			public String getPassword() {
				return password;
			}

			@Override
			public String getDatabaseName() {
				return dbName;
			}

			@Override
			public String getDatabaseType() {
				return dbType;
			}

			@Override
			public boolean useLimitedUser() {
				return !(systemUserType == null || SUPERUSER_LOWER.equals(systemUserType));
			}

			@Override
			public String getLimitedUser() {
				return limitedUser;
			}

			@Override
			public String getLimitedUserPassword() {
				return limitedPassword;
			}

			@Override
			public boolean useSharkSchema() {
				return createSharkSchema;
			}

			@Override
			public String getSqlPath() {
				return Settings.getInstance().getRootPath() + "WEB-INF" + File.separator + "sql" + File.separator;
			}

		};
		final DatabaseConfiguration databaseConfiguration = DatabaseProperties.getInstance();
		final DatabaseConfigurator configurator = new DatabaseConfigurator(configuration, databaseConfiguration,
				patchManager());
		configurator.configureAndSaveSettings();

		if (DatabaseConfigurator.EMPTY_DBTYPE.equals(dbType)) {
			final GroupDTO groupDto = GroupDTO.newInstance() //
					.withName(SUPER_USER) //
					.withAdminFlag(true) //
					.withDescription(SUPER_USER) //
					.build();
			final CMGroup superUserGroup = groupsLogic().createGroup(groupDto);
			final UserDTO userDto = UserDTO.newInstance() //
					.withUsername(adminUser) //
					.withDescription(adminUser) //
					.withPassword(adminPassword) //
					.build();
			final CMUser administrator = authLogic().createUser(userDto);
			groupsLogic().addUserToGroup(administrator.getId(), superUserGroup.getId());
		}
		patchManager().reset();
		/*
		 * TODO move elsewhere
		 * 
		 * should be moved in another part, maybe in some filter, but being sure
		 * of keep synchronized some resources
		 */
		if (!startupLogic().migrationRequired()) {
			startupLogic().earlyStart();
		}
	}

	@JSONExported
	@Unauthorized
	public JSONObject getPatches( //
			final JSONObject serializer //
	) throws JSONException {
		final Iterable<Patch> avaiablePatches = patchManager().getAvaiblePatch();
		for (final Patch patch : avaiablePatches) {
			final JSONObject jsonPatch = new JSONObject();
			jsonPatch.put(NAME, patch.getVersion());
			jsonPatch.put(DESCRIPTION, patch.getDescription());
			jsonPatch.put(CATEGORY, patch.getCategory());
			serializer.append(PATCHES, jsonPatch);
		}
		return serializer;
	}

	@JSONExported
	@Unauthorized
	public JSONObject applyPatches( //
			final JSONObject serializer //
	) throws Exception {
		startupLogic().migrate();
		return serializer;
	}

}
