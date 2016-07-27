package org.cmdbuild.logic.taskmanager.task.connector;

import static com.google.common.collect.Iterables.addAll;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceType;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.TaskVisitor;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;

public class ConnectorTask implements ScheduledTask {

	public static interface SourceConfiguration {

		void accept(SourceConfigurationVisitor visitor);

	}

	public static interface SourceConfigurationVisitor {

		void visit(SqlSourceConfiguration sourceConfiguration);

	}

	public static final SourceConfiguration NULL_SOURCE_CONFIGURATION = new SourceConfiguration() {

		@Override
		public void accept(final SourceConfigurationVisitor visitor) {
			// nothing to do
		}

	};

	private static abstract class AbstractSourceConfiguration implements SourceConfiguration {

		@Override
		public final boolean equals(final Object obj) {
			return doEquals(obj);
		}

		protected abstract boolean doEquals(Object obj);

		@Override
		public final int hashCode() {
			return doHashCode();
		}

		protected abstract int doHashCode();

		@Override
		public final String toString() {
			return doToString();
		}

		protected abstract String doToString();

	}

	public static final class SqlSourceConfiguration extends AbstractSourceConfiguration
			implements DataSourceHelper.Configuration {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<SqlSourceConfiguration> {

			private DataSourceType type;
			private String host;
			private Integer port;
			private String database;
			private String instance;
			private String username;
			private String password;
			private String filter;

			private Builder() {
				// user factory method
			}

			@Override
			public SqlSourceConfiguration build() {
				validate();
				return new SqlSourceConfiguration(this);
			}

			private void validate() {
				port = defaultIfNull(port, 0);
			}

			public Builder withType(final DataSourceType type) {
				this.type = type;
				return this;
			}

			public Builder withHost(final String host) {
				this.host = host;
				return this;
			}

			public Builder withPort(final Integer port) {
				this.port = port;
				return this;
			}

			public Builder withDatabase(final String database) {
				this.database = database;
				return this;
			}

			public Builder withInstance(final String instance) {
				this.instance = instance;
				return this;
			}

			public Builder withUsername(final String username) {
				this.username = username;
				return this;
			}

			public Builder withPassword(final String password) {
				this.password = password;
				return this;
			}

			public Builder withFilter(final String filter) {
				this.filter = filter;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final DataSourceType type;
		private final String host;
		private final int port;
		private final String database;
		private final String instance;
		private final String username;
		private final String password;
		private final String filter;

		private SqlSourceConfiguration(final Builder builder) {
			this.type = builder.type;
			this.host = builder.host;
			this.port = builder.port;
			this.database = builder.database;
			this.instance = builder.instance;
			this.username = builder.username;
			this.password = builder.password;
			this.filter = builder.filter;
		}

		@Override
		public void accept(final SourceConfigurationVisitor visitor) {
			visitor.visit(this);
		}

		@Override
		public DataSourceType getType() {
			return type;
		}

		@Override
		public String getHost() {
			return host;
		}

		@Override
		public int getPort() {
			return port;
		}

		@Override
		public String getDatabase() {
			return database;
		}

		@Override
		public String getInstance() {
			return instance;
		}

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public String getPassword() {
			return password;
		}

		public String getFilter() {
			return filter;
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof SqlSourceConfiguration)) {
				return false;
			}
			final SqlSourceConfiguration other = SqlSourceConfiguration.class.cast(obj);
			return new EqualsBuilder() //
					.append(host, other.host) //
					.append(port, other.port) //
					.append(database, other.database) //
					.append(username, other.username) //
					.append(password, other.password) //
					.append(filter, other.filter) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(host) //
					.append(port) //
					.append(database) //
					.append(username) //
					.append(password) //
					.append(filter) //
					.toHashCode();
		}

		@Override
		protected String doToString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	public static class ClassMapping {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassMapping> {

			private String sourceType;
			private String targetType;
			private Boolean create;
			private Boolean update;
			private Boolean delete;

			private Builder() {
				// user factory method
			}

			@Override
			public ClassMapping build() {
				validate();
				return new ClassMapping(this);
			}

			private void validate() {
				create = defaultIfNull(create, false);
				update = defaultIfNull(update, false);
				delete = defaultIfNull(delete, false);
			}

			public Builder withSourceType(final String sourceType) {
				this.sourceType = sourceType;
				return this;
			}

			public Builder withTargetType(final String targetType) {
				this.targetType = targetType;
				return this;
			}

			public Builder withCreateStatus(final boolean create) {
				this.create = create;
				return this;
			}

			public Builder withUpdateStatus(final boolean update) {
				this.update = update;
				return this;
			}

			public Builder withDeleteStatus(final boolean delete) {
				this.delete = delete;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final String sourceType;
		private final String targetType;
		private final boolean create;
		private final boolean update;
		private final boolean delete;

		private ClassMapping(final Builder builder) {
			this.sourceType = builder.sourceType;
			this.targetType = builder.targetType;
			this.create = builder.create;
			this.update = builder.update;
			this.delete = builder.delete;
		}

		public String getSourceType() {
			return sourceType;
		}

		public String getTargetType() {
			return targetType;
		}

		public boolean isCreate() {
			return create;
		}

		public boolean isUpdate() {
			return update;
		}

		public boolean isDelete() {
			return delete;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AttributeMapping)) {
				return false;
			}
			final AttributeMapping other = AttributeMapping.class.cast(obj);
			return new EqualsBuilder() //
					.append(sourceType, other.sourceType) //
					.append(targetType, other.targetType) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(sourceType) //
					.append(targetType) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	public static class AttributeMapping {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeMapping> {

			private String sourceType;
			private String sourceAttribute;
			private String targetType;
			private String targetAttribute;
			private Boolean isKey;

			private Builder() {
				// user factory method
			}

			@Override
			public AttributeMapping build() {
				validate();
				return new AttributeMapping(this);
			}

			private void validate() {
				isKey = defaultIfNull(isKey, false);
			}

			public Builder withSourceType(final String sourceType) {
				this.sourceType = sourceType;
				return this;
			}

			public Builder withSourceAttribute(final String sourceAttribute) {
				this.sourceAttribute = sourceAttribute;
				return this;
			}

			public Builder withTargetType(final String targetType) {
				this.targetType = targetType;
				return this;
			}

			public Builder withTargetAttribute(final String targetAttribute) {
				this.targetAttribute = targetAttribute;
				return this;
			}

			public Builder withKeyStatus(final boolean isKey) {
				this.isKey = isKey;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final String sourceType;
		private final String sourceAttribute;
		private final String targetType;
		private final String targetAttribute;
		private final boolean isKey;

		private AttributeMapping(final Builder builder) {
			this.sourceType = builder.sourceType;
			this.sourceAttribute = builder.sourceAttribute;
			this.targetType = builder.targetType;
			this.targetAttribute = builder.targetAttribute;
			this.isKey = builder.isKey;
		}

		public String getSourceType() {
			return sourceType;
		}

		public String getSourceAttribute() {
			return sourceAttribute;
		}

		public String getTargetType() {
			return targetType;
		}

		public String getTargetAttribute() {
			return targetAttribute;
		}

		public boolean isKey() {
			return isKey;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AttributeMapping)) {
				return false;
			}
			final AttributeMapping other = AttributeMapping.class.cast(obj);
			return new EqualsBuilder() //
					.append(sourceType, other.sourceType) //
					.append(sourceAttribute, other.sourceAttribute) //
					.append(targetType, other.targetType) //
					.append(targetAttribute, other.targetAttribute) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(sourceType) //
					.append(sourceAttribute) //
					.append(targetType) //
					.append(targetAttribute) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ConnectorTask> {

		private static final Iterable<? extends ClassMapping> NO_CLASS_MAPPINGS = Collections.emptyList();
		private static final Iterable<AttributeMapping> NO_ATTRIBUTE_MAPPINGS = Collections.emptyList();

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private DateTime lastExecution;
		private Boolean notificationActive;
		private String notificationAcccount;
		private String notificationErrorTemplate;
		private SourceConfiguration sourceConfiguration;
		private final Collection<ClassMapping> classMappings = Sets.newHashSet();
		private final Collection<AttributeMapping> attributeMappings = Sets.newHashSet();

		private Builder() {
			// use factory method
		}

		@Override
		public ConnectorTask build() {
			validate();
			return new ConnectorTask(this);
		}

		private void validate() {
			active = defaultIfNull(active, Boolean.FALSE);
			notificationActive = defaultIfNull(notificationActive, false);
			sourceConfiguration = defaultIfNull(sourceConfiguration, NULL_SOURCE_CONFIGURATION);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder withLastExecution(final DateTime lastExecution) {
			this.lastExecution = lastExecution;
			return this;
		}

		public Builder withNotificationStatus(final Boolean notificationActive) {
			this.notificationActive = notificationActive;
			return this;
		}

		public Builder withNotificationAccount(final String notificationAcccount) {
			this.notificationAcccount = notificationAcccount;
			return this;
		}

		public Builder withNotificationErrorTemplate(final String notificationErrorTemplate) {
			this.notificationErrorTemplate = notificationErrorTemplate;
			return this;
		}

		public Builder withSourceConfiguration(final SourceConfiguration sourceConfiguration) {
			this.sourceConfiguration = sourceConfiguration;
			return this;
		}

		public Builder withClassMapping(final ClassMapping classMappings) {
			this.classMappings.add(classMappings);
			return this;
		}

		public Builder withClassMappings(final Iterable<? extends ClassMapping> classMappings) {
			addAll(this.classMappings, defaultIfNull(classMappings, NO_CLASS_MAPPINGS));
			return this;
		}

		public Builder withAttributeMapping(final AttributeMapping attributeMapping) {
			this.attributeMappings.add(attributeMapping);
			return this;
		}

		public Builder withAttributeMappings(final Iterable<? extends AttributeMapping> attributeMappings) {
			addAll(this.attributeMappings, defaultIfNull(attributeMappings, NO_ATTRIBUTE_MAPPINGS));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final String cronExpression;
	private final DateTime lastExecution;
	private final boolean notificationActive;
	private final String notificationAcccount;
	private final String notificationErrorTemplate;
	private final SourceConfiguration sourceConfiguration;
	private final Collection<ClassMapping> classMappings;
	private final Iterable<AttributeMapping> attributeMappings;

	private ConnectorTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.lastExecution = builder.lastExecution;
		this.notificationActive = builder.notificationActive;
		this.notificationAcccount = builder.notificationAcccount;
		this.notificationErrorTemplate = builder.notificationErrorTemplate;
		this.sourceConfiguration = builder.sourceConfiguration;
		this.classMappings = builder.classMappings;
		this.attributeMappings = builder.attributeMappings;
	}

	@Override
	public void accept(final TaskVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isExecutable() {
		return true;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public DateTime getLastExecution() {
		return lastExecution;
	}

	public boolean isNotificationActive() {
		return notificationActive;
	}

	public String getNotificationAccount() {
		return notificationAcccount;
	}

	public String getNotificationErrorTemplate() {
		return notificationErrorTemplate;
	}

	public SourceConfiguration getSourceConfiguration() {
		return sourceConfiguration;
	}

	public Collection<ClassMapping> getClassMappings() {
		return classMappings;
	}

	public Iterable<AttributeMapping> getAttributeMappings() {
		return attributeMappings;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
