package utils;

import static com.google.common.reflect.Reflection.newProxy;
import static java.lang.String.format;
import static org.cmdbuild.common.utils.Reflection.defaultValues;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang3.SystemUtils;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.dao.driver.AbstractDBDriver.DefaultTypeObjectCache;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.services.ForwardingPatchManager;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.database.DatabaseConfigurator;

public class DBInitializer implements LoggingSupport {

	private static class FakePatchManager extends ForwardingPatchManager {

		private static final PatchManager DELEGATE = newProxy(PatchManager.class, defaultValues());

		@Override
		protected PatchManager delegate() {
			return DELEGATE;
		}

		@Override
		public Iterable<Patch> getAvaiblePatch() {
			return Collections.emptyList();
		}

		@Override
		public boolean isUpdated() {
			return true;
		}

	}

	private static final PatchManager FAKE_PATCH_MANAGER = new FakePatchManager();

	private static final String DATABASE_PROPERTIES = "database.properties";
	// TODO it's a little ugly, at the moment is ok
	private static final String SQL_PATH = "/../../cmdbuild/src/main/webapp/WEB-INF/sql/";

	private final DatabaseConfigurator.Configuration dbConfiguration;
	private final DatabaseConfigurator dbConfigurator;
	private final PostgresDriver pgDriver;
	private final PatchManager patchManager;

	public DBInitializer() {
		final Properties properties = readDatabaseProperties();
		final String webRoot = SystemUtils.USER_DIR.concat(SQL_PATH);
		// FIXME needed for PatchManager... no comment
		Settings.getInstance().setRootPath(SystemUtils.USER_DIR.concat("/../../cmdbuild/src/main/webapp/"));
		dbConfiguration = new DatabaseConfigurator.Configuration() {

			@Override
			public String getHost() {
				return properties.getProperty("host");
			}

			@Override
			public int getPort() {
				return Integer.parseInt(properties.getProperty("port"));
			}

			@Override
			public String getUser() {
				return properties.getProperty("super.user");
			}

			@Override
			public String getPassword() {
				return properties.getProperty("super.password");
			}

			@Override
			public String getDatabaseName() {
				return properties.getProperty("db.name");
			}

			@Override
			public String getDatabaseType() {
				return DatabaseConfigurator.EMPTY_DBTYPE;
			}

			@Override
			public boolean useLimitedUser() {
				return false;
			}

			@Override
			public String getLimitedUser() {
				return properties.getProperty("user");
			}

			@Override
			public String getLimitedUserPassword() {
				return properties.getProperty("password");
			}

			@Override
			public boolean useSharkSchema() {
				return false;
			}

			@Override
			public String getSqlPath() {
				return webRoot;
			}

		};
		patchManager = FAKE_PATCH_MANAGER;
		final DatabaseConfiguration databaseConfiguration = new DatabaseProperties();
		dbConfigurator = new DatabaseConfigurator(dbConfiguration, databaseConfiguration, patchManager);
		pgDriver = new PostgresDriver(dbConfigurator.systemDataSource(), new DefaultTypeObjectCache());
	}

	private Properties readDatabaseProperties() {
		InputStream inputStream = null;
		try {
			final ClassLoader classLoader = DBInitializer.class.getClassLoader();
			inputStream = classLoader.getResourceAsStream(DATABASE_PROPERTIES);
			final Properties properties = new Properties();
			properties.load(inputStream);
			return properties;
		} catch (final Exception e) {
			logger.error("cannot read database properties", e);
			throw new Error(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (final IOException e) {
					logger.warn("canno close input stream");
				}
			}
		}
	}

	public void initialize() {
		logger.info("initializing database (if needed)");
		setupDatabaseProperties();
		if (!databaseExists()) {
			logger.info("database not found");
			createDatabase();
		}
		updateWithPatches();
	}

	public void drop() {
		logger.info("dropping database");
		if (databaseExists()) {
			dbConfigurator.drop();
			logger.info("database dropped");
		}
	}

	private void setupDatabaseProperties() {
		final org.cmdbuild.config.DatabaseConfiguration dp = DatabaseProperties.getInstance();
		dp.setDatabaseUrl(format("jdbc:postgresql://%1$s:%2$s/%3$s", //
				dbConfiguration.getHost(), //
				dbConfiguration.getPort(), //
				dbConfiguration.getDatabaseName()));
		dp.setDatabaseUser(dbConfiguration.getUser());
		dp.setDatabasePassword(dbConfiguration.getPassword());
	}

	private void updateWithPatches() {
		if (!patchManager.isUpdated()) {
			patchManager.applyPatchList();
		}
	}

	private void createDatabase() {
		logger.info("creating database");
		dbConfigurator.configureAndDoNotSaveSettings();
	}

	private boolean databaseExists() {
		logger.info("checking database");
		final DataSource dataSource = dbConfigurator.systemDataSource();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			logger.info("database found");
			return true;
		} catch (final SQLException ex) {
			logger.info("database not found");
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (final SQLException e) {
					logger.warn("error closing connection", e);
				}
			}
		}
	}

	public DataSource dataSource() {
		return dbConfigurator.systemDataSource();
	}

	public DBDriver getDriver() {
		return pgDriver;
	}

}
