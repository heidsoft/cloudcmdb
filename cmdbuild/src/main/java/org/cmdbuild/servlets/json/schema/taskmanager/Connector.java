package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.java.sql.DataSourceTypes.mysql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.oracle;
import static org.cmdbuild.common.java.sql.DataSourceTypes.postgresql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.sqlserver;
import static org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.NULL_SOURCE_CONFIGURATION;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTE_MAPPING;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_ATTRIBUTE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_MAPPING;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CREATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_CONFIGURATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_ADDRESS;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_PORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_USERNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_INSTANCE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_TYPE_SQL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DELETE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.EXECUTABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.IS_KEY;
import static org.cmdbuild.servlets.json.CommunicationConstants.MYSQL_LABEL;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_TEMPLATE_ERROR;
import static org.cmdbuild.servlets.json.CommunicationConstants.ORACLE_LABEL;
import static org.cmdbuild.servlets.json.CommunicationConstants.POSTGRESQL_LABEL;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_ATTRIBUTE;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.SQLSERVER_LABEL;
import static org.cmdbuild.servlets.json.CommunicationConstants.UPDATE;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceType;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.AttributeMapping;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.ClassMapping;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SourceConfiguration;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SourceConfigurationVisitor;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.CommunicationConstants;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.json.util.JsonImmutableEntry;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

public class Connector extends JSONBaseWithSpringContext {

	private static abstract class JsonSource {
	}

	private static class JsonSqlSource extends JsonSource {

		private final SqlSourceConfiguration delegate;

		public JsonSqlSource(final SqlSourceConfiguration delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(DATA_SOURCE_DB_TYPE)
		public String getType() {
			return JsonSqlSourceHandler.of(delegate.getType()).client;
		}

		@JsonProperty(DATA_SOURCE_DB_ADDRESS)
		public String getHost() {
			return delegate.getHost();
		}

		@JsonProperty(DATA_SOURCE_DB_PORT)
		public int getPort() {
			return delegate.getPort();
		}

		@JsonProperty(DATA_SOURCE_DB_NAME)
		public String getDatabase() {
			return delegate.getDatabase();
		}

		@JsonProperty(DATA_SOURCE_DB_USERNAME)
		public String getUsername() {
			return delegate.getUsername();
		}

		@JsonProperty(DATA_SOURCE_DB_PASSWORD)
		public String getPassword() {
			return delegate.getPassword();
		}

		@JsonProperty(DATA_SOURCE_DB_FILTER)
		public String getFilter() {
			return delegate.getFilter();
		}

	}

	private static enum JsonSqlSourceHandler {

		MYSQL(CommunicationConstants.MYSQL, mysql(), MYSQL_LABEL), //
		ORACLE(CommunicationConstants.ORACLE, oracle(), ORACLE_LABEL), //
		POSTGRES(CommunicationConstants.POSTGRESQL, postgresql(), POSTGRESQL_LABEL), //
		SQLSERVER(CommunicationConstants.SQLSERVER, sqlserver(), SQLSERVER_LABEL), //
		UNKNOWN(null, null, null);
		;

		public static JsonSqlSourceHandler of(final String client) {
			for (final JsonSqlSourceHandler value : values()) {
				if (ObjectUtils.equals(value.client, client)) {
					return value;
				}
			}
			return UNKNOWN;
		}

		public static JsonSqlSourceHandler of(final DataSourceType type) {
			for (final JsonSqlSourceHandler value : values()) {
				if (ObjectUtils.equals(value.server, type)) {
					return value;
				}
			}
			return UNKNOWN;
		}

		public final String client;
		public final DataSourceType server;
		public final String label;

		private JsonSqlSourceHandler(final String client, final DataSourceType server, final String label) {
			this.client = client;
			this.server = server;
			this.label = label;
		}

	}

	private static class JsonClassMapping {

		private String sourceType;
		private String targetType;
		private boolean create;
		private boolean update;
		private boolean delete;

		@JsonProperty(SOURCE_NAME)
		public String getSourceType() {
			return sourceType;
		}

		public void setSourceType(final String sourceType) {
			this.sourceType = sourceType;
		}

		@JsonProperty(CLASS_NAME)
		public String getTargetType() {
			return targetType;
		}

		public void setTargetType(final String targetType) {
			this.targetType = targetType;
		}

		@JsonProperty(CREATE)
		public boolean isCreate() {
			return create;
		}

		public void setCreate(final boolean create) {
			this.create = create;
		}

		@JsonProperty(UPDATE)
		public boolean isUpdate() {
			return update;
		}

		public void setUpdate(final boolean update) {
			this.update = update;
		}

		@JsonProperty(DELETE)
		public boolean isDelete() {
			return delete;
		}

		public void setDelete(final boolean delete) {
			this.delete = delete;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof JsonClassMapping)) {
				return false;
			}
			final JsonClassMapping other = JsonClassMapping.class.cast(obj);
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

	}

	private static class JsonAttributeMapping {

		private String sourceType;
		private String sourceAttribute;
		private String targetType;
		private String targetAttribute;
		private boolean key;

		@JsonProperty(SOURCE_NAME)
		public String getSourceType() {
			return sourceType;
		}

		public void setSourceType(final String sourceType) {
			this.sourceType = sourceType;
		}

		@JsonProperty(SOURCE_ATTRIBUTE)
		public String getSourceAttribute() {
			return sourceAttribute;
		}

		public void setSourceAttribute(final String sourceAttribute) {
			this.sourceAttribute = sourceAttribute;
		}

		@JsonProperty(CLASS_NAME)
		public String getTargetType() {
			return targetType;
		}

		public void setTargetType(final String targetType) {
			this.targetType = targetType;
		}

		@JsonProperty(CLASS_ATTRIBUTE)
		public String getTargetAttribute() {
			return targetAttribute;
		}

		public void setTargetAttribute(final String targetAttribute) {
			this.targetAttribute = targetAttribute;
		}

		@JsonProperty(IS_KEY)
		public boolean isKey() {
			return defaultIfNull(key, false);
		}

		public void setKey(final boolean key) {
			this.key = key;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof JsonAttributeMapping)) {
				return false;
			}
			final JsonAttributeMapping other = JsonAttributeMapping.class.cast(obj);
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

	}

	private static class JsonConnectorTask {

		private final ConnectorTask delegate;

		public JsonConnectorTask(final ConnectorTask delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(ID)
		public Long getId() {
			return delegate.getId();
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return delegate.getDescription();
		}

		@JsonProperty(ACTIVE)
		public boolean isActive() {
			return delegate.isActive();
		}

		@JsonProperty(CRON_EXPRESSION)
		public String getCronExpression() {
			return delegate.getCronExpression();
		}

		@JsonProperty(EXECUTABLE)
		public boolean executable() {
			return delegate.isExecutable();
		}

		@JsonProperty(NOTIFICATION_ACTIVE)
		public boolean isNotificationActive() {
			return delegate.isNotificationActive();
		}

		@JsonProperty(NOTIFICATION_EMAIL_ACCOUNT)
		public String getNotificationAcccount() {
			return delegate.getNotificationAccount();
		}

		@JsonProperty(NOTIFICATION_EMAIL_TEMPLATE_ERROR)
		public String getNotificationErrorTemplate() {
			return delegate.getNotificationErrorTemplate();
		}

		@JsonProperty(DATA_SOURCE_TYPE)
		public String getSourceType() {
			return new SourceConfigurationVisitor() {

				private String type;

				public String asJsonObject() {
					delegate.getSourceConfiguration().accept(this);
					return type;
				}

				@Override
				public void visit(final SqlSourceConfiguration sourceConfiguration) {
					type = DATA_SOURCE_TYPE_SQL;
				}

			}.asJsonObject();
		}

		@JsonProperty(DATA_SOURCE_CONFIGURATION)
		public JsonSource getSourceConfiguration() {
			return new SourceConfigurationVisitor() {

				private JsonSource jsonDataSource;

				public JsonSource asJsonObject() {
					delegate.getSourceConfiguration().accept(this);
					return jsonDataSource;
				}

				@Override
				public void visit(final SqlSourceConfiguration sourceConfiguration) {
					jsonDataSource = new JsonSqlSource(sourceConfiguration);
				}

			}.asJsonObject();
		}

		@JsonProperty(CLASS_MAPPING)
		public List<JsonClassMapping> getClassMapping() {
			return from(delegate.getClassMappings()) //
					.transform(CLASS_MAPPING_TO_JSON_CLASS_MAPPING) //
					.toList();
		}

		@JsonProperty(ATTRIBUTE_MAPPING)
		public List<JsonAttributeMapping> getAttributeMapping() {
			return from(delegate.getAttributeMappings()) //
					.transform(ATTRIBUTE_MAPPING_TO_JSON_ATTRIBUTE_MAPPING) //
					.toList();
		}

	}

	private static final Function<ClassMapping, JsonClassMapping> CLASS_MAPPING_TO_JSON_CLASS_MAPPING = new Function<ClassMapping, JsonClassMapping>() {

		@Override
		public JsonClassMapping apply(final ClassMapping input) {
			final JsonClassMapping output = new JsonClassMapping();
			output.setSourceType(input.getSourceType());
			output.setTargetType(input.getTargetType());
			output.setCreate(input.isCreate());
			output.setUpdate(input.isUpdate());
			output.setDelete(input.isDelete());
			return output;
		}

	};

	private static final Function<JsonClassMapping, ClassMapping> JSON_CLASS_MAPPING_TO_CLASS_MAPPING = new Function<JsonClassMapping, ClassMapping>() {

		@Override
		public ClassMapping apply(final JsonClassMapping input) {
			return ClassMapping.newInstance() //
					.withSourceType(input.getSourceType()) //
					.withTargetType(input.getTargetType()) //
					.withCreateStatus(input.isCreate()) //
					.withUpdateStatus(input.isUpdate()) //
					.withDeleteStatus(input.isDelete()) //
					.build();
		}

	};

	private static final Function<AttributeMapping, JsonAttributeMapping> ATTRIBUTE_MAPPING_TO_JSON_ATTRIBUTE_MAPPING = new Function<AttributeMapping, JsonAttributeMapping>() {

		@Override
		public JsonAttributeMapping apply(final AttributeMapping input) {
			final JsonAttributeMapping output = new JsonAttributeMapping();
			output.setSourceType(input.getSourceType());
			output.setSourceAttribute(input.getSourceAttribute());
			output.setTargetType(input.getTargetType());
			output.setTargetAttribute(input.getTargetAttribute());
			output.setKey(input.isKey());
			return output;
		}

	};

	private static final Function<JsonAttributeMapping, AttributeMapping> JSON_ATTRIBUTE_MAPPING_TO_ATTRIBUTE_MAPPING = new Function<JsonAttributeMapping, AttributeMapping>() {

		@Override
		public AttributeMapping apply(final JsonAttributeMapping input) {
			return AttributeMapping.newInstance() //
					.withSourceType(input.getSourceType()) //
					.withSourceAttribute(input.getSourceAttribute()) //
					.withTargetType(input.getTargetType()) //
					.withTargetAttribute(input.getTargetAttribute()) //
					.withKeyStatus(input.isKey()) //
					.build();
		}

	};

	private static final TypeReference<Set<? extends JsonClassMapping>> JSON_CLASS_MAPPINGS_TYPE_REFERENCE = new TypeReference<Set<? extends JsonClassMapping>>() {
	};

	private static final TypeReference<Set<? extends JsonAttributeMapping>> JSON_ATTRIBUTE_MAPPINGS_TYPE_REFERENCE = new TypeReference<Set<? extends JsonAttributeMapping>>() {
	};

	private static final Iterable<JsonClassMapping> NO_CLASS_MAPPINGS = Collections.emptyList();

	private static final Iterable<JsonAttributeMapping> NO_ATTRIBUTE_MAPPINGS = Collections.emptyList();

	@Admin
	@JSONExported
	public JsonResponse availableSqlSources() {
		final Collection<JsonImmutableEntry> availableTypes = Sets.newHashSet();
		for (final DataSourceType element : dataSourceHelper().getAvailableTypes()) {
			final JsonSqlSourceHandler handler = JsonSqlSourceHandler.of(element);
			availableTypes.add(JsonImmutableEntry.of(handler.client, handler.label));
		}
		return success(availableTypes);
	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean notificationActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String notificationAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE_ERROR, required = false) final String notificationTemplate, //
			@Parameter(value = DATA_SOURCE_TYPE, required = false) final String dataSourceType, //
			@Parameter(value = DATA_SOURCE_CONFIGURATION, required = false) final String jsonDataSourceConfiguration, //
			@Parameter(value = CLASS_MAPPING, required = false) final String jsonclassMapping, //
			@Parameter(value = ATTRIBUTE_MAPPING, required = false) final String jsonAttributeMapping //
	) throws Exception {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withNotificationStatus(notificationActive) //
				.withNotificationAccount(notificationAccount) //
				.withNotificationErrorTemplate(notificationTemplate) //
				.withSourceConfiguration(sourceConfigurationOf(dataSourceType, jsonDataSourceConfiguration)) //
				.withClassMappings(classMappingOf(jsonclassMapping)) //
				.withAttributeMappings(attributeMappingOf(jsonAttributeMapping)) //
				.build();
		final Long id = taskManagerLogic().create(task);
		return success(id);
	}

	@Admin
	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withId(id) //
				.build();
		final ConnectorTask readed = taskManagerLogic().read(task, ConnectorTask.class);
		return success(new JsonConnectorTask(readed));
	}

	@Admin
	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(ConnectorTask.class);
		return success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@Admin
	@JSONExported
	public JsonResponse update( // //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean notificationActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String notificationAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE_ERROR, required = false) final String notificationTemplate, //
			@Parameter(value = DATA_SOURCE_TYPE, required = false) final String dataSourceType, //
			@Parameter(value = DATA_SOURCE_CONFIGURATION, required = false) final String jsonDataSourceConfiguration, //
			@Parameter(value = CLASS_MAPPING, required = false) final String jsonclassMapping, //
			@Parameter(value = ATTRIBUTE_MAPPING, required = false) final String jsonAttributeMapping //
	) throws Exception {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withNotificationStatus(notificationActive) //
				.withNotificationAccount(notificationAccount) //
				.withNotificationErrorTemplate(notificationTemplate) //
				.withSourceConfiguration(sourceConfigurationOf(dataSourceType, jsonDataSourceConfiguration)) //
				.withClassMappings(classMappingOf(jsonclassMapping)) //
				.withAttributeMappings(attributeMappingOf(jsonAttributeMapping)) //
				.build();
		taskManagerLogic().update(task);
		return success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

	/*
	 * Utilities
	 */

	private static enum JsonSourceConfigurationHandler {

		SQL(DATA_SOURCE_TYPE_SQL) {

			@Override
			public SourceConfiguration convert(final String json) throws Exception {
				final JsonNode jsonNode = objectMapper.readTree(json);
				return SqlSourceConfiguration.newInstance() //
						.withType(JsonSqlSourceHandler.of(jsonNode.get(DATA_SOURCE_DB_TYPE).asText()).server) //
						.withHost(textOf(jsonNode, DATA_SOURCE_DB_ADDRESS)) //
						.withPort(integerOf(jsonNode, DATA_SOURCE_DB_PORT)) //
						.withDatabase(textOf(jsonNode, DATA_SOURCE_DB_NAME)) //
						.withInstance(textOf(jsonNode, DATA_SOURCE_INSTANCE)) //
						.withUsername(textOf(jsonNode, DATA_SOURCE_DB_USERNAME)) //
						.withPassword(textOf(jsonNode, DATA_SOURCE_DB_PASSWORD)) //
						.withFilter(textOf(jsonNode, DATA_SOURCE_DB_FILTER)) //
						.build();
			}

		}, //
		UNDEFINED(null) {

			@Override
			public SourceConfiguration convert(final String json) throws Exception {
				return NULL_SOURCE_CONFIGURATION;
			}

		}, //
		;

		private static String textOf(final JsonNode node, final String name) {
			return node.has(name) ? node.get(name).asText() : null;
		}

		private static Integer integerOf(final JsonNode node, final String name) {
			return node.has(name) ? node.get(name).asInt() : null;
		}

		private static final ObjectMapper objectMapper = new ObjectMapper();

		public static JsonSourceConfigurationHandler of(final String type) {
			JsonSourceConfigurationHandler found = null;
			for (final JsonSourceConfigurationHandler element : values()) {
				if (ObjectUtils.equals(element.clientValue, type)) {
					found = element;
					break;
				}
			}
			Validate.notNull(found, "type '%s' not found", type);
			return found;
		}

		private final String clientValue;

		private JsonSourceConfigurationHandler(final String clientValue) {
			this.clientValue = clientValue;
		}

		public abstract SourceConfiguration convert(String json) throws Exception;

	}

	private SourceConfiguration sourceConfigurationOf(final String type, final String jsonConfiguration)
			throws Exception {
		return JsonSourceConfigurationHandler.of(type).convert(jsonConfiguration);
	}

	private Iterable<ClassMapping> classMappingOf(final String json)
			throws JsonParseException, JsonMappingException, IOException {
		final Iterable<JsonClassMapping> jsonClassMapping;
		if (isBlank(json)) {
			jsonClassMapping = NO_CLASS_MAPPINGS;
		} else {
			jsonClassMapping = new ObjectMapper() //
					.readValue(json, JSON_CLASS_MAPPINGS_TYPE_REFERENCE);
		}
		return from(jsonClassMapping) //
				.transform(JSON_CLASS_MAPPING_TO_CLASS_MAPPING);
	}

	private Iterable<AttributeMapping> attributeMappingOf(final String json)
			throws JsonParseException, JsonMappingException, IOException {
		final Iterable<JsonAttributeMapping> jsonAttributeMappings;
		if (isBlank(json)) {
			jsonAttributeMappings = NO_ATTRIBUTE_MAPPINGS;
		} else {
			jsonAttributeMappings = new ObjectMapper() //
					.readValue(json, JSON_ATTRIBUTE_MAPPINGS_TYPE_REFERENCE);
		}
		return from(jsonAttributeMappings) //
				.transform(JSON_ATTRIBUTE_MAPPING_TO_ATTRIBUTE_MAPPING);
	}

}
