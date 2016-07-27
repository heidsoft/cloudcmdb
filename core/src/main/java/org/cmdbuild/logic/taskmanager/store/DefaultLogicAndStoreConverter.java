package org.cmdbuild.logic.taskmanager.store;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.math.NumberUtils.createInteger;
import static org.cmdbuild.common.java.sql.DataSourceTypes.mysql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.oracle;
import static org.cmdbuild.common.java.sql.DataSourceTypes.postgresql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.sqlserver;
import static org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.NULL_SOURCE_CONFIGURATION;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceType;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.logic.taskmanager.TaskVisitor;
import org.cmdbuild.logic.taskmanager.store.ParameterNames.AsynchronousEvent;
import org.cmdbuild.logic.taskmanager.store.ParameterNames.Connector;
import org.cmdbuild.logic.taskmanager.store.ParameterNames.Generic;
import org.cmdbuild.logic.taskmanager.store.ParameterNames.ReadEmail;
import org.cmdbuild.logic.taskmanager.store.ParameterNames.StartWorkflow;
import org.cmdbuild.logic.taskmanager.store.ParameterNames.SynchronousEvent;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.AttributeMapping;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.ClassMapping;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SourceConfiguration;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SourceConfigurationVisitor;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.email.mapper.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.task.email.mapper.MapperEngine;
import org.cmdbuild.logic.taskmanager.task.email.mapper.MapperEngineVisitor;
import org.cmdbuild.logic.taskmanager.task.email.mapper.NullMapperEngine;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask.Phase;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

public class DefaultLogicAndStoreConverter implements LogicAndStoreConverter {

	private static enum SqlSourceHandler {

		MYSQL("mysql", mysql()), //
		ORACLE("oracle", oracle()), //
		POSTGRES("postgresql", postgresql()), //
		SQLSERVER("sqlserver", sqlserver()), //
		UNKNOWN(null, null);
		;

		public static SqlSourceHandler of(final String store) {
			for (final SqlSourceHandler value : values()) {
				if (ObjectUtils.equals(value.store, store)) {
					return value;
				}
			}
			return UNKNOWN;
		}

		public static SqlSourceHandler of(final DataSourceType type) {
			for (final SqlSourceHandler value : values()) {
				if (ObjectUtils.equals(value.type, type)) {
					return value;
				}
			}
			return UNKNOWN;
		}

		public final String store;
		public final DataSourceType type;

		private SqlSourceHandler(final String client, final DataSourceType server) {
			this.store = client;
			this.type = server;
		}

	}

	private static final Function<ClassMapping, String> CLASS_MAPPING_TO_STRING = new Function<ClassMapping, String>() {

		@Override
		public String apply(final ClassMapping input) {
			return Joiner.on(Connector.MAPPING_SEPARATOR) //
					.join(asList( //
							input.getSourceType(), //
							input.getTargetType(), //
							Boolean.toString(input.isCreate()), //
							Boolean.toString(input.isUpdate()), //
							Boolean.toString(input.isDelete()) //
			));
		}

	};

	private static final Function<AttributeMapping, String> ATTRIBUTE_MAPPING_TO_STRING = new Function<AttributeMapping, String>() {

		@Override
		public String apply(final AttributeMapping input) {
			return Joiner.on(Connector.MAPPING_SEPARATOR) //
					.join(asList( //
							input.getSourceType(), //
							input.getSourceAttribute(), //
							input.getTargetType(), //
							input.getTargetAttribute(), //
							Boolean.toString(input.isKey()) //
			));
		}

	};

	private static final Function<String, ClassMapping> STRING_TO_CLASS_MAPPING = new Function<String, ClassMapping>() {

		@Override
		public ClassMapping apply(final String input) {
			final List<String> elements = Splitter.on(Connector.MAPPING_SEPARATOR).splitToList(input);
			return ClassMapping.newInstance() //
					.withSourceType(elements.get(0)) //
					.withTargetType(elements.get(1)) //
					.withCreateStatus(Boolean.parseBoolean(elements.get(2))) //
					.withUpdateStatus(Boolean.parseBoolean(elements.get(3))) //
					.withDeleteStatus(Boolean.parseBoolean(elements.get(4))) //
					.build();
		}

	};

	private static final Function<String, AttributeMapping> STRING_TO_ATTRIBUTE_MAPPING = new Function<String, AttributeMapping>() {

		@Override
		public AttributeMapping apply(final String input) {
			final List<String> elements = Splitter.on(Connector.MAPPING_SEPARATOR).splitToList(input);
			return AttributeMapping.newInstance() //
					.withSourceType(elements.get(0)) //
					.withSourceAttribute(elements.get(1)) //
					.withTargetType(elements.get(2)) //
					.withTargetAttribute(elements.get(3)) //
					.withKeyStatus(Boolean.parseBoolean(elements.get(4))) //
					.build();
		}

	};

	public static final String KEY_VALUE_SEPARATOR = "=";
	public static final String GROUPS_SEPARATOR = ",";

	/**
	 * Used for separate those elements that should be separated by a line-feed
	 * but that cannot be used because:<br>
	 * 1) it could be used inside values<br>
	 * 2) someone could edit database manually from a Windows host<br>
	 * It's the HTML entity for the '|' character.
	 */
	public static final String SPECIAL_SEPARATOR = "&#124;";

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultLogicAndStoreConverter.class.getName());

	private static class MapperToParametersConverter implements MapperEngineVisitor {

		public static MapperToParametersConverter of(final MapperEngine mapper) {
			return new MapperToParametersConverter(mapper);
		}

		private final MapperEngine mapper;

		private MapperToParametersConverter(final MapperEngine mapper) {
			this.mapper = mapper;
		}

		private Map<String, String> parameters;

		public Map<String, String> convert() {
			parameters = Maps.newLinkedHashMap();
			mapper.accept(this);
			return parameters;
		}

		@Override
		public void visit(final KeyValueMapperEngine mapper) {
			parameters.put(ReadEmail.MapperEngine.TYPE, ReadEmail.KeyValueMapperEngine.TYPE_VALUE);
			parameters.put(ReadEmail.KeyValueMapperEngine.KEY_INIT, mapper.getKeyInit());
			parameters.put(ReadEmail.KeyValueMapperEngine.KEY_END, mapper.getKeyEnd());
			parameters.put(ReadEmail.KeyValueMapperEngine.VALUE_INIT, mapper.getValueInit());
			parameters.put(ReadEmail.KeyValueMapperEngine.VALUE_END, mapper.getValueEnd());
		}

		@Override
		public void visit(final NullMapperEngine mapper) {
			// nothing to do
		}

	}

	// TODO do in some way with visitor
	private static enum ParametersToMapperConverter {

		KEY_VALUE(ReadEmail.KeyValueMapperEngine.TYPE_VALUE) {

			@Override
			public MapperEngine convert(final Map<String, String> parameters) {
				return KeyValueMapperEngine.newInstance() //
						.withKey( //
								parameters.get(ReadEmail.KeyValueMapperEngine.KEY_INIT), //
								parameters.get(ReadEmail.KeyValueMapperEngine.KEY_END) //
				) //
						.withValue( //
								parameters.get(ReadEmail.KeyValueMapperEngine.VALUE_INIT), //
								parameters.get(ReadEmail.KeyValueMapperEngine.VALUE_END) //
				) //
						.build();
			}

		}, //
		UNDEFINED(EMPTY) {

			@Override
			public MapperEngine convert(final Map<String, String> parameters) {
				return NullMapperEngine.getInstance();
			}

		}, //
		;

		public static ParametersToMapperConverter of(final String type) {
			for (final ParametersToMapperConverter element : values()) {
				if (element.type.equals(type)) {
					return element;
				}
			}
			return UNDEFINED;
		}

		private final String type;

		private ParametersToMapperConverter(final String type) {
			this.type = type;
		}

		public abstract MapperEngine convert(Map<String, String> parameters);

	}

	private static class PhaseToStoreConverter implements SynchronousEventTask.PhaseIdentifier {

		private final SynchronousEventTask task;
		private String converted;

		public PhaseToStoreConverter(final SynchronousEventTask task) {
			this.task = task;
		}

		public String toStore() {
			if (task.getPhase() != null) {
				task.getPhase().identify(this);
				Validate.notNull(converted, "conversion error");
			} else {
				converted = null;
			}
			return converted;
		}

		@Override
		public void afterCreate() {
			converted = SynchronousEvent.PHASE_AFTER_CREATE;
		}

		@Override
		public void beforeUpdate() {
			converted = SynchronousEvent.PHASE_BEFORE_UPDATE;
		}

		@Override
		public void afterUpdate() {
			converted = SynchronousEvent.PHASE_AFTER_UPDATE;
		}

		@Override
		public void beforeDelete() {
			converted = SynchronousEvent.PHASE_BEFORE_DELETE;
		}

	}

	private static class PhaseToLogicConverter {

		private final String stored;

		public PhaseToLogicConverter(final String stored) {
			this.stored = stored;
		}

		public Phase toLogic() {
			final Phase converted;
			if (SynchronousEvent.PHASE_AFTER_CREATE.equals(stored)) {
				converted = Phase.AFTER_CREATE;
			} else if (SynchronousEvent.PHASE_BEFORE_UPDATE.equals(stored)) {
				converted = Phase.BEFORE_UPDATE;
			} else if (SynchronousEvent.PHASE_AFTER_UPDATE.equals(stored)) {
				converted = Phase.AFTER_UPDATE;
			} else if (SynchronousEvent.PHASE_BEFORE_DELETE.equals(stored)) {
				converted = Phase.BEFORE_DELETE;
			} else {
				converted = null;
			}
			return converted;
		}

	}

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter, TaskVisitor {

		private final Task source;

		private org.cmdbuild.data.store.task.Task target;

		public DefaultLogicAsSourceConverter(final Task source) {
			this.source = source;
		}

		@Override
		public org.cmdbuild.data.store.task.Task toStore() {
			logger.info(marker, "converting logic task '{}' to store task", source);
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final AsynchronousEventTask task) {
			this.target = org.cmdbuild.data.store.task.AsynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withParameter(AsynchronousEvent.FILTER_CLASSNAME, task.getTargetClassname()) //
					.withParameter(AsynchronousEvent.FILTER_CARDS, task.getFilter()) //
					.withParameter(AsynchronousEvent.EMAIL_ACTIVE, //
							Boolean.toString(task.isNotificationActive())) //
					.withParameter(AsynchronousEvent.EMAIL_ACCOUNT, task.getNotificationAccount()) //
					.withParameter(AsynchronousEvent.EMAIL_TEMPLATE, task.getNotificationTemplate()) //
					.build();
		}

		@Override
		public void visit(final ConnectorTask task) {
			final SourceConfiguration sourceConfiguration = task.getSourceConfiguration();
			this.target = org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withParameter(Connector.NOTIFICATION_ACTIVE, //
							Boolean.toString(task.isNotificationActive())) //
					.withParameter(Connector.NOTIFICATION_ACCOUNT, task.getNotificationAccount()) //
					.withParameter(Connector.NOTIFICATION_ERROR_TEMPLATE, task.getNotificationErrorTemplate()) //
					.withParameters(parametersOf(sourceConfiguration)) //
					.withParameter(Connector.MAPPING_TYPES,
							Joiner.on(SPECIAL_SEPARATOR) //
									.join( //
											FluentIterable.from(task.getClassMappings()) //
													.transform(CLASS_MAPPING_TO_STRING)) //
			) //
					.withParameter(Connector.MAPPING_ATTRIBUTES,
							Joiner.on(SPECIAL_SEPARATOR) //
									.join( //
											FluentIterable.from(task.getAttributeMappings()) //
													.transform(ATTRIBUTE_MAPPING_TO_STRING)) //
			) //
					.build();
		}

		private Map<String, String> parametersOf(final SourceConfiguration sourceConfiguration) {
			final Map<String, String> parameters = Maps.newHashMap();
			sourceConfiguration.accept(new SourceConfigurationVisitor() {

				@Override
				public void visit(final SqlSourceConfiguration sourceConfiguration) {
					final Map<String, String> map = Maps.newHashMap();
					map.put(Connector.SQL_TYPE, SqlSourceHandler.of(sourceConfiguration.getType()).store);
					map.put(Connector.SQL_HOSTNAME, sourceConfiguration.getHost());
					map.put(Connector.SQL_PORT, Integer.toString(sourceConfiguration.getPort()));
					map.put(Connector.SQL_DATABASE, sourceConfiguration.getDatabase());
					map.put(Connector.SQL_INSTANCE, sourceConfiguration.getInstance());
					map.put(Connector.SQL_USERNAME, sourceConfiguration.getUsername());
					map.put(Connector.SQL_PASSWORD, sourceConfiguration.getPassword());
					map.put(Connector.SQL_FILTER, sourceConfiguration.getFilter());
					parameters.put(Connector.DATA_SOURCE_TYPE, "sql");
					parameters.put(Connector.DATA_SOURCE_CONFIGURATION,
							Joiner.on(SPECIAL_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.useForNull(EMPTY) //
									.join(map));
				}
			});
			return parameters;
		}

		@Override
		public void visit(final GenericTask task) {
			this.target = org.cmdbuild.data.store.task.GenericTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withParameters(context(task)) //
					.withParameter(Generic.EMAIL_ACTIVE, Boolean.toString(task.isEmailActive())) //
					.withParameter(Generic.EMAIL_TEMPLATE, task.getEmailTemplate()) //
					.withParameter(Generic.EMAIL_ACCOUNT, task.getEmailAccount()) //
					.withParameter(Generic.REPORT_ACTIVE, Boolean.toString(task.isReportActive())) //
					.withParameter(Generic.REPORT_NAME, task.getReportName()) //
					.withParameter(Generic.REPORT_EXTENSION, task.getReportExtension()) //
					.withParameters(reportParameters(task)) //
					.build();
		}

		private Map<String, ? extends String> context(final GenericTask task) {
			final Map<String, String> output = newHashMap();
			task.getContext().entrySet().stream() //
					.forEach(input -> input.getValue().entrySet().stream() //
							.forEach(_input -> output.put(Generic.context(input.getKey(), _input.getKey()),
									_input.getValue()))

			);
			return output;
		}

		private Map<String, ? extends String> reportParameters(final GenericTask task) {
			final Map<String, String> output = newHashMap();
			task.getReportParameters().entrySet().stream() //
					.forEach(input -> output.put(Generic.REPORT_PARAMETERS_PREFIX + input.getKey(), input.getValue()));
			return output;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			this.target = org.cmdbuild.data.store.task.ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withParameter(ReadEmail.ACCOUNT_NAME, task.getEmailAccount()) //
					.withParameter(ReadEmail.INCOMING_FOLDER, task.getIncomingFolder()) //
					.withParameter(ReadEmail.PROCESSED_FOLDER, task.getProcessedFolder()) //
					.withParameter(ReadEmail.REJECTED_FOLDER, task.getRejectedFolder()) //
					.withParameter(ReadEmail.FILTER_REJECT, Boolean.toString(task.isRejectNotMatching())) //
					.withParameter(ReadEmail.FILTER_TYPE, task.getFilterType()) //
					.withParameter(ReadEmail.FILTER_FROM_REGEX,
							Joiner.on(SPECIAL_SEPARATOR) //
									.join(task.getRegexFromFilter())) //
					.withParameter(ReadEmail.FILTER_SUBJECT_REGEX,
							Joiner.on(SPECIAL_SEPARATOR) //
									.join(task.getRegexSubjectFilter())) //
					.withParameter(ReadEmail.FILTER_FUNCTION_NAME, task.getFilterFunction()) //
					.withParameter(ReadEmail.NOTIFICATION_ACTIVE, //
							Boolean.toString(task.isNotificationActive())) //
					.withParameter(ReadEmail.NOTIFICATION_TEMPLATE, //
							defaultString(task.getNotificationTemplate())) //
					.withParameter(ReadEmail.ATTACHMENTS_ACTIVE, //
							Boolean.toString(task.isAttachmentsActive())) //
					.withParameter(ReadEmail.ATTACHMENTS_CATEGORY, //
							task.getAttachmentsCategory()) //
					.withParameter(ReadEmail.WORKFLOW_ACTIVE, //
							Boolean.toString(task.isWorkflowActive())) //
					.withParameter(ReadEmail.WORKFLOW_CLASS_NAME, task.getWorkflowClassName()) //
					.withParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING,
							Joiner.on(SPECIAL_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.join(task.getWorkflowAttributes())) //
					.withParameter(ReadEmail.WORKFLOW_ADVANCE, //
							Boolean.toString(task.isWorkflowAdvanceable())) //
					.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, //
							Boolean.toString(task.isWorkflowAttachments())) //
					.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, //
							task.getWorkflowAttachmentsCategory()) //
					.withParameters(MapperToParametersConverter.of(task.getMapperEngine()).convert()) //
					.build();
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			this.target = org.cmdbuild.data.store.task.StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withParameter(StartWorkflow.CLASSNAME, task.getProcessClass()) //
					.withParameter(StartWorkflow.ATTRIBUTES,
							Joiner.on(SPECIAL_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.join(task.getAttributes())) //
					.build();
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			this.target = org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withParameter(SynchronousEvent.PHASE, new PhaseToStoreConverter(task).toStore()) //
					.withParameter(SynchronousEvent.FILTER_GROUPS,
							Joiner.on(GROUPS_SEPARATOR) //
									.join(task.getGroups())) //
					.withParameter(SynchronousEvent.FILTER_CLASSNAME, task.getTargetClassname()) //
					.withParameter(SynchronousEvent.FILTER_CARDS, task.getFilter()) //
					.withParameter(SynchronousEvent.EMAIL_ACTIVE, //
							Boolean.toString(task.isEmailEnabled())) //
					.withParameter(SynchronousEvent.EMAIL_ACCOUNT, task.getEmailAccount()) //
					.withParameter(SynchronousEvent.EMAIL_TEMPLATE, task.getEmailTemplate()) //
					.withParameter(SynchronousEvent.WORKFLOW_ACTIVE, //
							Boolean.toString(task.isWorkflowEnabled())) //
					.withParameter(SynchronousEvent.WORKFLOW_CLASS_NAME, task.getWorkflowClassName()) //
					.withParameter(SynchronousEvent.WORKFLOW_ATTRIBUTES,
							Joiner.on(SPECIAL_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.join(task.getWorkflowAttributes())) //
					.withParameter(SynchronousEvent.WORKFLOW_ADVANCE, //
							Boolean.toString(task.isWorkflowAdvanceable())) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, Boolean.toString(task.isScriptingEnabled())) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, task.getScriptingEngine()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, task.getScriptingScript()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_SAFE, Boolean.toString(task.isScriptingSafe())) //
					.build();
		}
	}

	private static class DefaultStoreAsSourceConverter
			implements StoreAsSourceConverter, org.cmdbuild.data.store.task.TaskVisitor {

		private static final Iterable<ClassMapping> NO_CLASS_MAPPINGS = Collections.emptyList();
		private static final Iterable<AttributeMapping> NO_ATTRIBUTE_MAPPINGS = Collections.emptyList();

		private static final Iterable<String> EMPTY_GROUPS = Collections.emptyList();
		private static final Iterable<String> EMPTY_FILTERS = Collections.emptyList();
		private static final Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

		private final org.cmdbuild.data.store.task.Task source;

		private Task target;

		public DefaultStoreAsSourceConverter(final org.cmdbuild.data.store.task.Task source) {
			this.source = source;
		}

		@Override
		public Task toLogic() {
			logger.info(marker, "converting store task '{}' to logic task", source);
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.AsynchronousEventTask task) {
			target = AsynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withTargetClass(task.getParameter(AsynchronousEvent.FILTER_CLASSNAME)) //
					.withFilter(task.getParameter(AsynchronousEvent.FILTER_CARDS)) //
					.withNotificationStatus( //
							Boolean.valueOf(task.getParameter(AsynchronousEvent.EMAIL_ACTIVE))) //
					.withNotificationAccount(task.getParameter(AsynchronousEvent.EMAIL_ACCOUNT)) //
					.withNotificationErrorTemplate(task.getParameter(AsynchronousEvent.EMAIL_TEMPLATE)) //
					.build();
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.ConnectorTask task) {
			final String dataSourceConfiguration = task.getParameter(Connector.DATA_SOURCE_CONFIGURATION);
			final String typeMapping = task.getParameter(Connector.MAPPING_TYPES);
			final String attributeMapping = task.getParameter(Connector.MAPPING_ATTRIBUTES);
			target = ConnectorTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withNotificationStatus( //
							Boolean.valueOf(task.getParameter(Connector.NOTIFICATION_ACTIVE))) //
					.withNotificationAccount(task.getParameter(Connector.NOTIFICATION_ACCOUNT)) //
					.withNotificationErrorTemplate(task.getParameter(Connector.NOTIFICATION_ERROR_TEMPLATE)) //
					.withSourceConfiguration(sourceConfigurationOf(dataSourceConfiguration)) //
					.withClassMappings( //
							isEmpty(typeMapping) ? NO_CLASS_MAPPINGS
									: FluentIterable
											.from( //
													Splitter.on(SPECIAL_SEPARATOR) //
															.split(typeMapping)) //
											.transform(STRING_TO_CLASS_MAPPING)) //
					.withAttributeMappings( //
							isEmpty(attributeMapping) ? NO_ATTRIBUTE_MAPPINGS
									: FluentIterable
											.from( //
													Splitter.on(SPECIAL_SEPARATOR) //
															.split(attributeMapping)) //
											.transform(STRING_TO_ATTRIBUTE_MAPPING)) //
					.build();
		}

		private SourceConfiguration sourceConfigurationOf(final String configuration) {
			final SourceConfiguration sourceConfiguration;
			if (isBlank(configuration)) {
				sourceConfiguration = NULL_SOURCE_CONFIGURATION;
			} else {
				final Map<String, String> map = Splitter.on(SPECIAL_SEPARATOR) //
						.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
						.split(defaultString(configuration));
				sourceConfiguration = SqlSourceConfiguration.newInstance() //
						.withType(SqlSourceHandler.of(map.get(Connector.SQL_TYPE)).type) //
						.withHost(map.get(Connector.SQL_HOSTNAME)) //
						.withPort(createInteger(defaultString(map.get(Connector.SQL_PORT), null))) //
						.withDatabase(map.get(Connector.SQL_DATABASE)) //
						.withInstance(map.get(Connector.SQL_INSTANCE)) //
						.withUsername(map.get(Connector.SQL_USERNAME)) //
						.withPassword(map.get(Connector.SQL_PASSWORD)) //
						.withFilter(map.get(Connector.SQL_FILTER)) //
						.build();
			}
			return sourceConfiguration;
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.GenericTask task) {
			target = GenericTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withContext(context(task)) //
					.withEmailActive(Boolean.valueOf(task.getParameter(Generic.EMAIL_ACTIVE))) //
					.withEmailTemplate(task.getParameter(Generic.EMAIL_TEMPLATE)) //
					.withEmailAccount(task.getParameter(Generic.EMAIL_ACCOUNT)) //
					.withReportActive(Boolean.valueOf(task.getParameter(Generic.REPORT_ACTIVE))) //
					.withReportName(task.getParameter(Generic.REPORT_NAME)) //
					.withReportExtension(task.getParameter(Generic.REPORT_EXTENSION)) //
					.withReportParameters(reportParameters(task)) //
					.build();
		}

		private Map<String, Map<String, String>> context(final org.cmdbuild.data.store.task.GenericTask task) {
			final Map<String, Map<String, String>> output = newHashMap();
			task.getParameters().entrySet().stream() //
					.filter(input -> input.getKey().startsWith(Generic.CONTEXT_PREFIX)) //
					.forEach(input -> {
						final String contextAndKey = input.getKey().substring(Generic.CONTEXT_PREFIX.length());
						final String[] elements = contextAndKey.split("\\.");
						final String context = elements[0];
						final String key = elements[1];
						final Map<String, String> sub;
						if (output.containsKey(context)) {
							sub = output.get(context);
						} else {
							sub = newHashMap();
							output.put(context, sub);
						}
						sub.put(key, input.getValue());
					});
			return output;
		}

		private Map<String, String> reportParameters(final org.cmdbuild.data.store.task.GenericTask task) {
			final Map<String, String> output = newHashMap();
			task.getParameters().entrySet().stream() //
					.filter(input -> input.getKey().startsWith(Generic.REPORT_PARAMETERS_PREFIX)) //
					.forEach(input -> output.put(input.getKey().substring(Generic.REPORT_PARAMETERS_PREFIX.length()),
							input.getValue()));
			return output;
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.ReadEmailTask task) {
			final String fromRegexFilters = task.getParameter(ReadEmail.FILTER_FROM_REGEX);
			final String subjectRegexFilters = task.getParameter(ReadEmail.FILTER_SUBJECT_REGEX);
			final String attributesAsString = defaultString(task.getParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING));
			target = ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withEmailAccount(task.getParameter(ReadEmail.ACCOUNT_NAME)) //
					.withIncomingFolder(task.getParameter(ReadEmail.INCOMING_FOLDER)) //
					.withProcessedFolder(task.getParameter(ReadEmail.PROCESSED_FOLDER)) //
					.withRejectedFolder(task.getParameter(ReadEmail.REJECTED_FOLDER)) //
					.withRejectNotMatching( //
							Boolean.valueOf(task.getParameter(ReadEmail.FILTER_REJECT))) //
					.withFilterType(task.getParameter(ReadEmail.FILTER_TYPE)) //
					.withRegexFromFilter( //
							isEmpty(fromRegexFilters) ? EMPTY_FILTERS
									: Splitter.on(SPECIAL_SEPARATOR) //
											.split(fromRegexFilters)) //
					.withRegexSubjectFilter( //
							isEmpty(subjectRegexFilters) ? EMPTY_FILTERS
									: Splitter.on(SPECIAL_SEPARATOR) //
											.split(subjectRegexFilters)) //
					.withFilterFunction(task.getParameter(ReadEmail.FILTER_FUNCTION_NAME)) //
					.withNotificationStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.NOTIFICATION_ACTIVE))) //
					.withNotificationTemplate( //
							task.getParameter(ReadEmail.NOTIFICATION_TEMPLATE)) //
					.withAttachmentsActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.ATTACHMENTS_ACTIVE))) //
					.withAttachmentsCategory(task.getParameter(ReadEmail.ATTACHMENTS_CATEGORY)) //
					.withWorkflowActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ACTIVE))) //
					.withWorkflowClassName(task.getParameter(ReadEmail.WORKFLOW_CLASS_NAME)) //
					.withWorkflowAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : splitProperties(attributesAsString)) //
					.withWorkflowAdvanceableStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ADVANCE))) //
					.withWorkflowAttachmentsStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE))) //
					.withWorkflowAttachmentsCategory(task.getParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY)) //
					.withMapperEngine(mapperOf(task.getParameters())) //
					.build();
		}

		private MapperEngine mapperOf(final Map<String, String> parameters) {
			final String type = parameters.get(ReadEmail.MapperEngine.TYPE);
			return ParametersToMapperConverter.of(type).convert(parameters);
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.StartWorkflowTask task) {
			final String attributesAsString = defaultString(task.getParameter(StartWorkflow.ATTRIBUTES));
			target = StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withLastExecution(task.getLastExecution()) //
					.withProcessClass(task.getParameter(StartWorkflow.CLASSNAME)) //
					.withAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : splitProperties(attributesAsString)) //
					.build();
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.SynchronousEventTask task) {
			final String groupsAsString = defaultString(task.getParameter(SynchronousEvent.FILTER_GROUPS));
			final String attributesAsString = defaultString(task.getParameter(SynchronousEvent.WORKFLOW_ATTRIBUTES));
			target = SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withPhase( //
							new PhaseToLogicConverter(task.getParameter(SynchronousEvent.PHASE)) //
									.toLogic()) //
					.withGroups(isEmpty(groupsAsString) ? EMPTY_GROUPS
							: Splitter.on(GROUPS_SEPARATOR) //
									.split(groupsAsString)) //
					.withTargetClass(task.getParameter(SynchronousEvent.FILTER_CLASSNAME)) //
					.withFilter(task.getParameter(SynchronousEvent.FILTER_CARDS)) //
					.withEmailEnabled( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.EMAIL_ACTIVE))) //
					.withEmailAccount(task.getParameter(SynchronousEvent.EMAIL_ACCOUNT)) //
					.withEmailTemplate(task.getParameter(SynchronousEvent.EMAIL_TEMPLATE)) //
					.withWorkflowEnabled( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.WORKFLOW_ACTIVE))) //
					.withWorkflowClassName(task.getParameter(SynchronousEvent.WORKFLOW_CLASS_NAME)) //
					.withWorkflowAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : splitProperties(attributesAsString)) //
					.withWorkflowAdvanceable( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.WORKFLOW_ADVANCE))) //
					.withScriptingEnableStatus( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE))) //
					.withScriptingEngine(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE)) //
					.withScript(task.getParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT)) //
					.withScriptingSafeStatus( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.ACTION_SCRIPT_SAFE))) //
					.build();
		}

		private Map<String, String> splitProperties(final String value) {
			final Iterable<String> lines = Splitter.on(SPECIAL_SEPARATOR) //
					.omitEmptyStrings() //
					.split(value);
			final Map<String, String> properties = newHashMap();
			for (final String line : lines) {
				final List<String> elements = Splitter.on(KEY_VALUE_SEPARATOR) //
						.limit(2) //
						.splitToList(line);
				properties.put(elements.get(0), elements.get(1));
			}
			return properties;
		}

	}

	@Override
	public LogicAsSourceConverter from(final Task source) {
		return new DefaultLogicAsSourceConverter(source);
	}

	@Override
	public StoreAsSourceConverter from(final org.cmdbuild.data.store.task.Task source) {
		return new DefaultStoreAsSourceConverter(source);
	}

}
