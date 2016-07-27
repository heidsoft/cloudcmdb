package org.cmdbuild.services;

import static java.lang.String.format;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.cmdbuild.common.java.sql.ForwardingDataSource;
import org.cmdbuild.config.DatabaseConfiguration;

public class DefaultDataSourceFactory implements DataSourceFactory {

	private static final String DATASOURCE_NAME = "jdbc/cmdbuild";
	private static final Class<?> DRIVER_CLASS = org.postgresql.Driver.class;

	private static class DefaultDataSource extends ForwardingDataSource {

		private final DatabaseConfiguration configuration;
		private final BasicDataSource dataSource;
		private final Boolean configured = new Boolean(false);

		public DefaultDataSource(final DatabaseConfiguration configuration, final BasicDataSource dataSource) {
			this.configuration = configuration;
			this.dataSource = dataSource;
		}

		@Override
		protected DataSource delegate() {
			return dataSource;
		}

		private DataSource configureDatasource() {
			if (!configuration.isConfigured()) {
				throw new IllegalStateException("database connection not configured");
			}
			dataSource.setDriverClassName(DRIVER_CLASS.getCanonicalName());
			dataSource.setUrl(configuration.getDatabaseUrl());
			dataSource.setUsername(configuration.getDatabaseUser());
			dataSource.setPassword(configuration.getDatabasePassword());
			return dataSource;
		}

		@Override
		public Connection getConnection() throws SQLException {
			if (!configured.booleanValue()) {
				synchronized (configured) {
					if (!configured.booleanValue()) {
						configureDatasource();
					}
				}
			}
			return super.getConnection();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T unwrap(final Class<T> iface) throws SQLException {
			Validate.notNull(iface, "Interface argument must not be null");
			if (!DataSource.class.equals(iface)) {
				final String message = format("data source of type '%s' can only be unwrapped as '%s', not as '%s'", //
						getClass().getName(), //
						DataSource.class.getName(), //
						iface.getName());
				throw new SQLException(message);
			}
			return (T) this;
		}

		@Override
		public boolean isWrapperFor(final Class<?> iface) throws SQLException {
			return DataSource.class.equals(iface);
		}

	}

	private final DatabaseConfiguration configuration;

	public DefaultDataSourceFactory(final DatabaseConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public DataSource create() {
		BasicDataSource dataSource;
		try {
			final InitialContext ictx = new InitialContext();
			final Context ctx = (Context) ictx.lookup("java:/comp/env");
			dataSource = (BasicDataSource) ctx.lookup(DATASOURCE_NAME);
		} catch (final NamingException e) {
			dataSource = new BasicDataSource();
		}
		return new DefaultDataSource(configuration, dataSource);
	}

}
