package unit.logic.taskmanager.store;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Ordering.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.java.sql.DataSourceTypes.mysql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.oracle;
import static org.cmdbuild.common.java.sql.DataSourceTypes.postgresql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.sqlserver;
import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.logic.taskmanager.store.DefaultLogicAndStoreConverter.KEY_VALUE_SEPARATOR;
import static org.cmdbuild.logic.taskmanager.store.DefaultLogicAndStoreConverter.SPECIAL_SEPARATOR;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.store.DefaultLogicAndStoreConverter;
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
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.email.mapper.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.task.email.mapper.MapperEngine;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask.Phase;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class DefaultLogicAndStoreConverterTest {

	private static Comparator<ClassMapping> CLASS_MAPPING_COMPARATOR = new Comparator<ClassMapping>() {

		@Override
		public int compare(final ClassMapping o1, final ClassMapping o2) {
			return o1.getSourceType().compareTo(o2.getSourceType());
		}

	};

	private static Comparator<AttributeMapping> ATTRIBUTE_MAPPING_COMPARATOR = new Comparator<AttributeMapping>() {

		@Override
		public int compare(final AttributeMapping o1, final AttributeMapping o2) {
			return o1.getSourceType().compareTo(o2.getSourceType());
		}

	};

	private static final DateTime NOW = DateTime.now();

	private DefaultLogicAndStoreConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new DefaultLogicAndStoreConverter();
	}

	@Test
	public void asynchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final AsynchronousEventTask source = a(AsynchronousEventTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withLastExecution(NOW) //
				.withTargetClass("classname") //
				.withFilter("filter") //
				.withNotificationStatus(true) //
				.withNotificationAccount("account") //
				.withNotificationErrorTemplate("error template") //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.AsynchronousEventTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.isRunning(), equalTo(true));
		assertThat(converted.getLastExecution(), equalTo(NOW));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(AsynchronousEvent.FILTER_CLASSNAME, "classname"));
		assertThat(parameters, hasEntry(AsynchronousEvent.FILTER_CARDS, "filter"));
		assertThat(parameters, hasEntry(AsynchronousEvent.EMAIL_ACTIVE, "true"));
		assertThat(parameters, hasEntry(AsynchronousEvent.EMAIL_ACCOUNT, "account"));
		assertThat(parameters, hasEntry(AsynchronousEvent.EMAIL_TEMPLATE, "error template"));
	}

	@Test
	public void asynchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.AsynchronousEventTask source = a(
				org.cmdbuild.data.store.task.AsynchronousEventTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withLastExecution(NOW) //
						.withParameter(AsynchronousEvent.FILTER_CLASSNAME, "classname") //
						.withParameter(AsynchronousEvent.FILTER_CARDS, "filter") //
						.withParameter(AsynchronousEvent.EMAIL_ACTIVE, "true") //
						.withParameter(AsynchronousEvent.EMAIL_ACCOUNT, "account") //
						.withParameter(AsynchronousEvent.EMAIL_TEMPLATE, "error template") //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(AsynchronousEventTask.class));
		final AsynchronousEventTask converted = AsynchronousEventTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getLastExecution(), equalTo(NOW));
		assertThat(converted.getTargetClassname(), equalTo("classname"));
		assertThat(converted.getFilter(), equalTo("filter"));
		assertThat(converted.isNotificationActive(), equalTo(true));
		assertThat(converted.getNotificationAccount(), equalTo("account"));
		assertThat(converted.getNotificationTemplate(), equalTo("error template"));
	}

	@Test
	public void connectorTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask source = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withLastExecution(NOW) //
				.withNotificationStatus(true) //
				.withNotificationAccount("account") //
				.withNotificationErrorTemplate("error template") //
				.withClassMapping(a(ClassMapping.newInstance() //
						.withSourceType("sourceTypeA") //
						.withTargetType("targetTypeA") //
						.withCreateStatus(true) //
						.withUpdateStatus(true) //
						.withDeleteStatus(false) //
		)) //
				.withClassMapping(a(ClassMapping.newInstance() //
						.withSourceType("sourceTypeB") //
						.withTargetType("targetTypeB") //
						.withCreateStatus(false) //
						.withUpdateStatus(false) //
						.withDeleteStatus(true) //
		)) //
				.withAttributeMapping(a(AttributeMapping.newInstance() //
						.withSourceType("sourceTypeA") //
						.withSourceAttribute("sourceAttributeA") //
						.withTargetType("targetTypeA") //
						.withTargetAttribute("targetAttributeA") //
						.withKeyStatus(true) //
		)) //
				.withAttributeMapping(a(AttributeMapping.newInstance() //
						.withSourceType("sourceTypeB") //
						.withSourceAttribute("sourceAttributeB") //
						.withTargetType("targetTypeB") //
						.withTargetAttribute("targetAttributeB") //
						.withKeyStatus(false) //
		)) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.isRunning(), equalTo(true));
		assertThat(converted.getLastExecution(), equalTo(NOW));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(Connector.NOTIFICATION_ACTIVE, "true"));
		assertThat(parameters, hasEntry(Connector.NOTIFICATION_ACCOUNT, "account"));
		assertThat(parameters, hasEntry(Connector.NOTIFICATION_ERROR_TEMPLATE, "error template"));
		assertThat(parameters,
				hasEntry(Connector.MAPPING_TYPES,
						"" //
								+ "sourceTypeA,targetTypeA,true,true,false" + SPECIAL_SEPARATOR //
								+ "sourceTypeB,targetTypeB,false,false,true" //
		));
		assertThat(parameters,
				hasEntry(Connector.MAPPING_ATTRIBUTES,
						"" //
								+ "sourceTypeA,sourceAttributeA,targetTypeA,targetAttributeA,true" + SPECIAL_SEPARATOR //
								+ "sourceTypeB,sourceAttributeB,targetTypeB,targetAttributeB,false" //
		));
	}

	@Test
	public void sqlDataSourceSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask source = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(postgresql()) //
						.withHost("example.com") //
						.withPort(12345) //
						.withDatabase("db") //
						.withInstance("instance") //
						.withUsername("user") //
						.withPassword("pwd") //
						.withFilter("filter") //
		)) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(Connector.DATA_SOURCE_TYPE, "sql"));

		final Map<String, String> configuration = Splitter.on(SPECIAL_SEPARATOR) //
				.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
				.split(parameters.get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "postgresql"));
		assertThat(configuration, hasEntry(Connector.SQL_HOSTNAME, "example.com"));
		assertThat(configuration, hasEntry(Connector.SQL_PORT, "12345"));
		assertThat(configuration, hasEntry(Connector.SQL_DATABASE, "db"));
		assertThat(configuration, hasEntry(Connector.SQL_INSTANCE, "instance"));
		assertThat(configuration, hasEntry(Connector.SQL_USERNAME, "user"));
		assertThat(configuration, hasEntry(Connector.SQL_PASSWORD, "pwd"));
		assertThat(configuration, hasEntry(Connector.SQL_FILTER, "filter"));
	}

	@Test
	public void mysqlTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(mysql()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(SPECIAL_SEPARATOR) //
				.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "mysql"));
	}

	@Test
	public void oracleTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(oracle()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(SPECIAL_SEPARATOR) //
				.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "oracle"));
	}

	@Test
	public void postgresqlTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(postgresql()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(SPECIAL_SEPARATOR) //
				.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "postgresql"));
	}

	@Test
	public void sqlserverTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(sqlserver()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(SPECIAL_SEPARATOR) //
				.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "sqlserver"));
	}

	@Test
	public void connectorTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ConnectorTask source = a(
				org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withLastExecution(NOW) //
						.withParameter(Connector.NOTIFICATION_ACTIVE, "true") //
						.withParameter(Connector.NOTIFICATION_ACCOUNT, "account") //
						.withParameter(Connector.NOTIFICATION_ERROR_TEMPLATE, "error template") //
						.withParameter(Connector.MAPPING_TYPES,
								"" //
										+ "sourceTypeA,targetTypeA,true,true,false" + SPECIAL_SEPARATOR //
										+ "sourceTypeB,targetTypeB,false,false,true" //
		) //
						.withParameter(Connector.MAPPING_ATTRIBUTES, "" //
								+ "sourceTypeA,sourceAttributeA,targetTypeA,targetAttributeA,true" + SPECIAL_SEPARATOR //
								+ "sourceTypeB,sourceAttributeB,targetTypeB,targetAttributeB,false" //
		) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getLastExecution(), equalTo(NOW));
		assertThat(converted.isNotificationActive(), equalTo(true));
		assertThat(converted.getNotificationAccount(), equalTo("account"));
		assertThat(converted.getNotificationErrorTemplate(), equalTo("error template"));
		final List<ClassMapping> classMappings = from(CLASS_MAPPING_COMPARATOR) //
				.immutableSortedCopy(converted.getClassMappings());
		final ClassMapping firstClassMapping = classMappings.get(0);
		assertThat(firstClassMapping.getSourceType(), equalTo("sourceTypeA"));
		assertThat(firstClassMapping.getTargetType(), equalTo("targetTypeA"));
		assertThat(firstClassMapping.isCreate(), is(true));
		assertThat(firstClassMapping.isUpdate(), is(true));
		assertThat(firstClassMapping.isDelete(), is(false));
		final ClassMapping secondClassMapping = classMappings.get(1);
		assertThat(secondClassMapping.getSourceType(), equalTo("sourceTypeB"));
		assertThat(secondClassMapping.getTargetType(), equalTo("targetTypeB"));
		assertThat(secondClassMapping.isCreate(), is(false));
		assertThat(secondClassMapping.isUpdate(), is(false));
		assertThat(secondClassMapping.isDelete(), is(true));
		final List<AttributeMapping> attributeMappings = from(ATTRIBUTE_MAPPING_COMPARATOR) //
				.immutableSortedCopy(converted.getAttributeMappings());
		final AttributeMapping firstAttributeMapping = attributeMappings.get(0);
		assertThat(firstAttributeMapping.getSourceType(), equalTo("sourceTypeA"));
		assertThat(firstAttributeMapping.getSourceAttribute(), equalTo("sourceAttributeA"));
		assertThat(firstAttributeMapping.getTargetType(), equalTo("targetTypeA"));
		assertThat(firstAttributeMapping.getTargetAttribute(), equalTo("targetAttributeA"));
		assertThat(firstAttributeMapping.isKey(), is(true));
		final AttributeMapping secondAttributeMapping = attributeMappings.get(1);
		assertThat(secondAttributeMapping.getSourceType(), equalTo("sourceTypeB"));
		assertThat(secondAttributeMapping.getSourceAttribute(), equalTo("sourceAttributeB"));
		assertThat(secondAttributeMapping.getTargetType(), equalTo("targetTypeB"));
		assertThat(secondAttributeMapping.getTargetAttribute(), equalTo("targetAttributeB"));
		assertThat(secondAttributeMapping.isKey(), is(false));
	}

	@Test
	public void sqlDataSourceSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = newHashMap();
		configuration.put(Connector.SQL_TYPE, "postgresql");
		configuration.put(Connector.SQL_HOSTNAME, "example.com");
		configuration.put(Connector.SQL_PORT, "12345");
		configuration.put(Connector.SQL_DATABASE, "db");
		configuration.put(Connector.SQL_INSTANCE, "instance");
		configuration.put(Connector.SQL_USERNAME, "user");
		configuration.put(Connector.SQL_PASSWORD, "pwd");
		configuration.put(Connector.SQL_FILTER, "filter");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(
				org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
						.withParameter(Connector.DATA_SOURCE_CONFIGURATION,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration,
				equalTo(a(SqlSourceConfiguration.newInstance() //
						.withType(postgresql()) //
						.withHost("example.com") //
						.withPort(12345) //
						.withDatabase("db") //
						.withInstance("instance") //
						.withUsername("user") //
						.withPassword("pwd") //
						.withFilter("filter") //
		)) //
		);
	}

	@Test
	public void mysqlTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = newHashMap();
		configuration.put(Connector.SQL_TYPE, "mysql");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(
				org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
						.withParameter(Connector.DATA_SOURCE_CONFIGURATION,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(mysql()))) //
		);
	}

	@Test
	public void oracleTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = newHashMap();
		configuration.put(Connector.SQL_TYPE, "oracle");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(
				org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
						.withParameter(Connector.DATA_SOURCE_CONFIGURATION,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(oracle()))) //
		);
	}

	@Test
	public void postgresqlTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = newHashMap();
		configuration.put(Connector.SQL_TYPE, "postgresql");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(
				org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
						.withParameter(Connector.DATA_SOURCE_CONFIGURATION,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(postgresql()))) //
		);
	}

	@Test
	public void sqlserverTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = newHashMap();
		configuration.put(Connector.SQL_TYPE, "sqlserver");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(
				org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
						.withParameter(Connector.DATA_SOURCE_CONFIGURATION,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(sqlserver()))) //
		);
	}

	@Test
	public void genericTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final GenericTask source = a(GenericTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withContext(ChainablePutMap.of(new HashMap<String, Map<String, String>>()) //
						.chainablePut("foo",
								ChainablePutMap.of(new HashMap<String, String>()) //
										.chainablePut("f", "o") //
										.chainablePut("o", "!")) //
						.chainablePut("bar",
								ChainablePutMap.of(new HashMap<String, String>()) //
										.chainablePut("b", "a") //
										.chainablePut("r", "!"))) //
				.withLastExecution(NOW) //
				.withEmailActive(true) //
				.withEmailTemplate("email template") //
				.withEmailAccount("email account") //
				.withReportActive(true) //
				.withReportName("report name") //
				.withReportExtension("report extension") //
				.withReportParameters(ChainablePutMap.of(new HashMap<String, String>()) //
						.chainablePut("foo", "oof") //
						.chainablePut("bar", "rab") //
						.chainablePut("baz", "zab")) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.GenericTask.class));
		assertThat(converted,
				equalTo(a(org.cmdbuild.data.store.task.GenericTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withLastExecution(NOW) //
						.withParameter(Generic.context("foo", "f"), "o") //
						.withParameter(Generic.context("foo", "o"), "!") //
						.withParameter(Generic.context("bar", "b"), "a") //
						.withParameter(Generic.context("bar", "r"), "!") //
						.withParameter(Generic.EMAIL_ACTIVE, "true") //
						.withParameter(Generic.EMAIL_TEMPLATE, "email template") //
						.withParameter(Generic.EMAIL_ACCOUNT, "email account") //
						.withParameter(Generic.REPORT_ACTIVE, "true") //
						.withParameter(Generic.REPORT_NAME, "report name") //
						.withParameter(Generic.REPORT_EXTENSION, "report extension") //
						.withParameter(Generic.REPORT_PARAMETERS_PREFIX + "foo", "oof") //
						.withParameter(Generic.REPORT_PARAMETERS_PREFIX + "bar", "rab") //
						.withParameter(Generic.REPORT_PARAMETERS_PREFIX + "baz", "zab") //
		)));
	}

	@Test
	public void genericTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.GenericTask source = a(org.cmdbuild.data.store.task.GenericTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withLastExecution(NOW) //
				.withParameter(Generic.context("foo", "f"), "o") //
				.withParameter(Generic.context("foo", "o"), "!") //
				.withParameter(Generic.context("bar", "b"), "a") //
				.withParameter(Generic.context("bar", "r"), "!") //
				.withParameter(Generic.EMAIL_ACTIVE, "true") //
				.withParameter(Generic.EMAIL_TEMPLATE, "email template") //
				.withParameter(Generic.EMAIL_ACCOUNT, "email account") //
				.withParameter(Generic.REPORT_ACTIVE, "true") //
				.withParameter(Generic.REPORT_NAME, "report name") //
				.withParameter(Generic.REPORT_EXTENSION, "report extension") //
				.withParameter(Generic.REPORT_PARAMETERS_PREFIX + "foo", "oof") //
				.withParameter(Generic.REPORT_PARAMETERS_PREFIX + "bar", "rab") //
				.withParameter(Generic.REPORT_PARAMETERS_PREFIX + "baz", "zab") //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(GenericTask.class));
		final GenericTask converted = GenericTask.class.cast(_converted);
		assertThat(converted,
				equalTo(a(GenericTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withActiveStatus(true) //
						.withCronExpression("cron expression") //
						.withLastExecution(NOW) //
						.withContext(ChainablePutMap.of(new HashMap<String, Map<String, String>>()) //
								.chainablePut("foo",
										ChainablePutMap.of(new HashMap<String, String>()) //
												.chainablePut("f", "o") //
												.chainablePut("o", "!")) //
								.chainablePut("bar",
										ChainablePutMap.of(new HashMap<String, String>()) //
												.chainablePut("b", "a") //
												.chainablePut("r", "!"))) //
				.withEmailActive(true) //
				.withEmailTemplate("email template") //
				.withEmailAccount("email account") //
				.withReportActive(true) //
				.withReportName("report name") //
				.withReportExtension("report extension") //
				.withReportParameters(ChainablePutMap.of(new HashMap<String, String>()) //
						.chainablePut("foo", "oof") //
						.chainablePut("bar", "rab") //
						.chainablePut("baz", "zab")) //
		)));
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = newLinkedHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final ReadEmailTask source = a(ReadEmailTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withLastExecution(NOW) //
				.withEmailAccount("email account") //
				.withIncomingFolder("incoming folder") //
				.withProcessedFolder("processed folder") //
				.withRejectedFolder("rejected folder") //
				.withRejectNotMatching(true) //
				.withFilterType("filter type") //
				.withRegexFromFilter(asList("regex", "from", "filter")) //
				.withRegexSubjectFilter(asList("regex", "subject", "filter")) //
				.withFilterFunction("filter function") //
				.withNotificationStatus(true) //
				.withNotificationTemplate("template") //
				.withAttachmentsActive(true) //
				.withAttachmentsCategory("category") //
				.withWorkflowActive(true) //
				.withWorkflowClassName("workflow class name") //
				.withWorkflowAttributes(attributes) //
				.withWorkflowAdvanceableStatus(true) //
				.withWorkflowAttachmentsStatus(true) //
				.withWorkflowAttachmentsCategory("workflow's attachments category") //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ReadEmailTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isRunning(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getLastExecution(), equalTo(NOW));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(ReadEmail.ACCOUNT_NAME, "email account"));
		assertThat(parameters, hasEntry(ReadEmail.INCOMING_FOLDER, "incoming folder"));
		assertThat(parameters, hasEntry(ReadEmail.PROCESSED_FOLDER, "processed folder"));
		assertThat(parameters, hasEntry(ReadEmail.REJECTED_FOLDER, "rejected folder"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_REJECT, "true"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_TYPE, "filter type"));
		assertThat(parameters,
				hasEntry(ReadEmail.FILTER_FROM_REGEX,
						"regex" + SPECIAL_SEPARATOR //
								+ "from" + SPECIAL_SEPARATOR //
								+ "filter"));
		assertThat(parameters,
				hasEntry(ReadEmail.FILTER_SUBJECT_REGEX,
						"regex" + SPECIAL_SEPARATOR //
								+ "subject" + SPECIAL_SEPARATOR //
								+ "filter"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_FUNCTION_NAME, "filter function"));
		assertThat(parameters, hasEntry(ReadEmail.NOTIFICATION_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.NOTIFICATION_TEMPLATE, "template"));
		assertThat(parameters, hasEntry(ReadEmail.ATTACHMENTS_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.ATTACHMENTS_CATEGORY, "category"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_CLASS_NAME, "workflow class name"));
		assertThat(Splitter.on(SPECIAL_SEPARATOR) //
				.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
				.split(parameters.get(ReadEmail.WORKFLOW_FIELDS_MAPPING)), equalTo(attributes));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ADVANCE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, "workflow's attachments category"));
	}

	@Test
	public void keyValueMapperSuccessfullyConvertedToStore() throws Exception {
		// given
		final ReadEmailTask source = a(ReadEmailTask.newInstance() //
				.withMapperEngine(a(KeyValueMapperEngine.newInstance() //
						.withKey("key_init", "key_end") //
						.withValue("value_init", "value_end") //
		) //
		) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.TYPE, "keyvalue"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.KEY_INIT, "key_init"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.KEY_END, "key_end"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.VALUE_INIT, "value_init"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.VALUE_END, "value_end"));
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> fieldsMapping = newHashMap();
		fieldsMapping.put("foo", "bar");
		fieldsMapping.put("bar", "baz");
		fieldsMapping.put("baz", "foo");
		final org.cmdbuild.data.store.task.ReadEmailTask source = a(
				org.cmdbuild.data.store.task.ReadEmailTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withLastExecution(NOW) //
						.withParameter(ReadEmail.ACCOUNT_NAME, "email account") //
						.withParameter(ReadEmail.INCOMING_FOLDER, "incoming folder") //
						.withParameter(ReadEmail.PROCESSED_FOLDER, "processed folder") //
						.withParameter(ReadEmail.REJECTED_FOLDER, "rejected folder") //
						.withParameter(ReadEmail.FILTER_REJECT, "true") //
						.withParameter(ReadEmail.FILTER_TYPE, "filter type") //
						.withParameter(ReadEmail.FILTER_FROM_REGEX,
								"regex" + SPECIAL_SEPARATOR //
										+ "from" + SPECIAL_SEPARATOR //
										+ "filter") //
						.withParameter(ReadEmail.FILTER_SUBJECT_REGEX,
								"regex" + SPECIAL_SEPARATOR //
										+ "subject" + SPECIAL_SEPARATOR //
										+ "filter") //
						.withParameter(ReadEmail.FILTER_FUNCTION_NAME, "filter function") //
						.withParameter(ReadEmail.NOTIFICATION_ACTIVE, "true") //
						.withParameter(ReadEmail.NOTIFICATION_TEMPLATE, "template") //
						.withParameter(ReadEmail.ATTACHMENTS_ACTIVE, "true") //
						.withParameter(ReadEmail.ATTACHMENTS_CATEGORY, "category") //
						.withParameter(ReadEmail.WORKFLOW_ACTIVE, "true") //
						.withParameter(ReadEmail.WORKFLOW_CLASS_NAME, "workflow class name") //
						.withParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(fieldsMapping)) //
						.withParameter(ReadEmail.WORKFLOW_ADVANCE, "true") //
						.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, "true") //
						.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, "workflow's attachments category") //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ReadEmailTask.class));
		final ReadEmailTask converted = ReadEmailTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getLastExecution(), equalTo(NOW));
		assertThat(converted.getEmailAccount(), equalTo("email account"));
		assertThat(converted.getIncomingFolder(), equalTo("incoming folder"));
		assertThat(converted.getProcessedFolder(), equalTo("processed folder"));
		assertThat(converted.getRejectedFolder(), equalTo("rejected folder"));
		assertThat(converted.isRejectNotMatching(), equalTo(true));
		assertThat(converted.isNotificationActive(), equalTo(true));
		assertThat(converted.getNotificationTemplate(), equalTo("template"));
		assertThat(converted.getFilterType(), equalTo("filter type"));
		assertThat(converted.getRegexFromFilter(), containsInAnyOrder("regex", "from", "filter"));
		assertThat(converted.getRegexSubjectFilter(), containsInAnyOrder("regex", "subject", "filter"));
		assertThat(converted.getFilterFunction(), equalTo("filter function"));
		assertThat(converted.isAttachmentsActive(), equalTo(true));
		assertThat(converted.getAttachmentsCategory(), equalTo("category"));
		assertThat(converted.isWorkflowActive(), equalTo(true));
		assertThat(converted.getWorkflowClassName(), equalTo("workflow class name"));
		final Map<String, String> attributes = newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		assertThat(converted.getWorkflowAttributes(), equalTo(attributes));
		assertThat(converted.isWorkflowAdvanceable(), equalTo(true));
		assertThat(converted.isWorkflowAttachments(), equalTo(true));
		assertThat(converted.getWorkflowAttachmentsCategory(), equalTo("workflow's attachments category"));
	}

	@Test
	public void keyValueMapperSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask source = a(
				org.cmdbuild.data.store.task.ReadEmailTask.newInstance() //
						.withParameter(ReadEmail.KeyValueMapperEngine.TYPE, "keyvalue") //
						.withParameter(ReadEmail.KeyValueMapperEngine.KEY_INIT, "key_init") //
						.withParameter(ReadEmail.KeyValueMapperEngine.KEY_END, "key_end") //
						.withParameter(ReadEmail.KeyValueMapperEngine.VALUE_INIT, "value_init") //
						.withParameter(ReadEmail.KeyValueMapperEngine.VALUE_END, "value_end") //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ReadEmailTask.class));
		final ReadEmailTask converted = ReadEmailTask.class.cast(_converted);
		final MapperEngine mapper = converted.getMapperEngine();
		assertThat(mapper, instanceOf(KeyValueMapperEngine.class));
		final KeyValueMapperEngine keyValueMapper = KeyValueMapperEngine.class.cast(mapper);
		assertThat(keyValueMapper.getKeyInit(), equalTo("key_init"));
		assertThat(keyValueMapper.getKeyEnd(), equalTo("key_end"));
		assertThat(keyValueMapper.getValueInit(), equalTo("value_init"));
		assertThat(keyValueMapper.getValueEnd(), equalTo("value_end"));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final StartWorkflowTask source = a(StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withLastExecution(NOW) //
				.withProcessClass("class name") //
				.withAttributes(attributes) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.StartWorkflowTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.isRunning(), equalTo(true));
		assertThat(converted.getLastExecution(), equalTo(NOW));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(StartWorkflow.CLASSNAME, "class name"));
		assertThat(parameters,
				hasEntry(StartWorkflow.ATTRIBUTES,
						Joiner.on(SPECIAL_SEPARATOR) //
								.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
								.join(attributes)));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> storedAttributes = newHashMap();
		storedAttributes.put("foo", "bar");
		storedAttributes.put("bar", "baz");
		storedAttributes.put("baz", "foo");
		final org.cmdbuild.data.store.task.StartWorkflowTask source = a(
				org.cmdbuild.data.store.task.StartWorkflowTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withLastExecution(NOW) //
						.withParameter(StartWorkflow.CLASSNAME, "class name") //
						.withParameter(StartWorkflow.ATTRIBUTES,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(storedAttributes)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(StartWorkflowTask.class));
		final StartWorkflowTask converted = StartWorkflowTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getLastExecution(), equalTo(NOW));
		assertThat(converted.getProcessClass(), equalTo("class name"));
		final Map<String, String> attributes = newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		assertThat(converted.getAttributes(), equalTo(attributes));
	}

	@Test
	public void startWorkflowTaskWithEmptyAttributesSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.StartWorkflowTask source = a(
				org.cmdbuild.data.store.task.StartWorkflowTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withCronExpression("cron expression") //
						.withLastExecution(NOW) //
						.withParameter(StartWorkflow.CLASSNAME, "class name") //
						.withParameter(StartWorkflow.ATTRIBUTES, EMPTY) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(StartWorkflowTask.class));
		final StartWorkflowTask converted = StartWorkflowTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getLastExecution(), equalTo(NOW));
		assertThat(converted.getProcessClass(), equalTo("class name"));
		assertThat(converted.getAttributes().isEmpty(), is(true));
	}

	@Test
	public void synchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final SynchronousEventTask source = a(SynchronousEventTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withPhase(Phase.AFTER_CREATE) //
				.withGroups(asList("foo", "bar", "baz")) //
				.withTargetClass("classname") //
				.withFilter("card's filter") //
				.withEmailEnabled(true) //
				.withEmailAccount("email account") //
				.withEmailTemplate("email template") //
				.withWorkflowEnabled(true) //
				.withWorkflowClassName("workflow class name") //
				.withWorkflowAttributes(attributes) //
				.withScriptingEnableStatus(true) //
				.withScriptingEngine("groovy") //
				.withScript("blah blah blah") //
				.withScriptingSafeStatus(true) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.SynchronousEventTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isRunning(), equalTo(true));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(SynchronousEvent.PHASE, "after_create"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_GROUPS, "foo,bar,baz"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_CLASSNAME, "classname"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_CARDS, "card's filter"));
		assertThat(parameters, hasEntry(SynchronousEvent.EMAIL_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.EMAIL_ACCOUNT, "email account"));
		assertThat(parameters, hasEntry(SynchronousEvent.EMAIL_TEMPLATE, "email template"));
		assertThat(parameters, hasEntry(SynchronousEvent.WORKFLOW_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.WORKFLOW_CLASS_NAME, "workflow class name"));
		assertThat(Splitter.on(SPECIAL_SEPARATOR) //
				.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
				.split(parameters.get(SynchronousEvent.WORKFLOW_ATTRIBUTES)), equalTo(attributes));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_SAFE, "true"));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final SynchronousEventTask missingPhase = a(SynchronousEventTask.newInstance());
		final SynchronousEventTask afterCreate = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_CREATE) //
		);
		final SynchronousEventTask beforeUpdate = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_UPDATE) //
		);
		final SynchronousEventTask afterUpdate = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_UPDATE) //
		);
		final SynchronousEventTask beforeDelete = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_DELETE) //
		);

		// when
		final org.cmdbuild.data.store.task.Task convertedMissingPhase = converter.from(missingPhase).toStore();
		final org.cmdbuild.data.store.task.Task convertedAfterCreate = converter.from(afterCreate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeUpdate = converter.from(beforeUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedAfterUpdate = converter.from(afterUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeDelete = converter.from(beforeDelete).toStore();

		// then
		assertThat(convertedMissingPhase.getParameters(), hasEntry(SynchronousEvent.PHASE, null));
		assertThat(convertedAfterCreate.getParameters(), hasEntry(SynchronousEvent.PHASE, "after_create"));
		assertThat(convertedBeforeUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "before_update"));
		assertThat(convertedAfterUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "after_update"));
		assertThat(convertedBeforeDelete.getParameters(), hasEntry(SynchronousEvent.PHASE, "before_delete"));
	}

	@Test
	public void synchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> workflowAttributes = newHashMap();
		workflowAttributes.put("foo", "bar");
		workflowAttributes.put("bar", "baz");
		workflowAttributes.put("baz", "foo");
		final org.cmdbuild.data.store.task.SynchronousEventTask source = a(
				org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
						.withId(42L) //
						.withDescription("description") //
						.withRunningStatus(true) //
						.withParameter(SynchronousEvent.PHASE, "after_create") //
						.withParameter(SynchronousEvent.FILTER_GROUPS, "foo,bar,baz") //
						.withParameter(SynchronousEvent.FILTER_CLASSNAME, "classname") //
						.withParameter(SynchronousEvent.FILTER_CARDS, "card's filter") //
						.withParameter(SynchronousEvent.EMAIL_ACTIVE, "true") //
						.withParameter(SynchronousEvent.EMAIL_ACCOUNT, "email account") //
						.withParameter(SynchronousEvent.EMAIL_TEMPLATE, "email template") //
						.withParameter(SynchronousEvent.WORKFLOW_ACTIVE, "true") //
						.withParameter(SynchronousEvent.WORKFLOW_CLASS_NAME, "workflow class name") //
						.withParameter(SynchronousEvent.WORKFLOW_ATTRIBUTES,
								Joiner.on(SPECIAL_SEPARATOR) //
										.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
										.join(workflowAttributes)) //
						.withParameter(SynchronousEvent.WORKFLOW_ADVANCE, "true") //
						.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true") //
						.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy") //
						.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah") //
						.withParameter(SynchronousEvent.ACTION_SCRIPT_SAFE, "true") //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(SynchronousEventTask.class));
		final SynchronousEventTask converted = SynchronousEventTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getPhase(), equalTo(Phase.AFTER_CREATE));
		assertThat(converted.getGroups(), containsInAnyOrder("foo", "bar", "baz"));
		assertThat(converted.getTargetClassname(), equalTo("classname"));
		assertThat(converted.getFilter(), equalTo("card's filter"));
		assertThat(converted.isEmailEnabled(), equalTo(true));
		assertThat(converted.getEmailAccount(), equalTo("email account"));
		assertThat(converted.getEmailTemplate(), equalTo("email template"));
		assertThat(converted.isWorkflowEnabled(), equalTo(true));
		assertThat(converted.getWorkflowClassName(), equalTo("workflow class name"));
		assertThat(converted.getWorkflowAttributes(), hasEntry("foo", "bar"));
		assertThat(converted.getWorkflowAttributes(), hasEntry("bar", "baz"));
		assertThat(converted.getWorkflowAttributes(), hasEntry("baz", "foo"));
		assertThat(converted.isWorkflowAdvanceable(), equalTo(true));
		assertThat(converted.isScriptingEnabled(), equalTo(true));
		assertThat(converted.getScriptingEngine(), equalTo("groovy"));
		assertThat(converted.getScriptingScript(), equalTo("blah blah blah"));
		assertThat(converted.isScriptingSafe(), equalTo(true));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask missingPhase = a(
				org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
						.withParameter(SynchronousEvent.PHASE, null) //
		);
		final org.cmdbuild.data.store.task.SynchronousEventTask afterCreate = a(
				org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
						.withParameter(SynchronousEvent.PHASE, "after_create") //
		);
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeUpdate = a(
				org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
						.withParameter(SynchronousEvent.PHASE, "before_update") //
		);
		final org.cmdbuild.data.store.task.SynchronousEventTask afterUpdate = a(
				org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
						.withParameter(SynchronousEvent.PHASE, "after_update") //
		);
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeDelete = a(
				org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
						.withParameter(SynchronousEvent.PHASE, "before_delete") //
		);

		// when
		final SynchronousEventTask convertedMissingPhase = SynchronousEventTask.class
				.cast(converter.from(missingPhase).toLogic());
		final SynchronousEventTask convertedAfterCreate = SynchronousEventTask.class
				.cast(converter.from(afterCreate).toLogic());
		final SynchronousEventTask convertedBeforeUpdate = SynchronousEventTask.class
				.cast(converter.from(beforeUpdate).toLogic());
		final SynchronousEventTask convertedAfterUpdate = SynchronousEventTask.class
				.cast(converter.from(afterUpdate).toLogic());
		final SynchronousEventTask convertedBeforeDelete = SynchronousEventTask.class
				.cast(converter.from(beforeDelete).toLogic());

		// then
		assertThat(convertedMissingPhase.getPhase(), is(nullValue()));
		assertThat(convertedAfterCreate.getPhase(), equalTo(Phase.AFTER_CREATE));
		assertThat(convertedBeforeUpdate.getPhase(), equalTo(Phase.BEFORE_UPDATE));
		assertThat(convertedAfterUpdate.getPhase(), equalTo(Phase.AFTER_UPDATE));
		assertThat(convertedBeforeDelete.getPhase(), equalTo(Phase.BEFORE_DELETE));
	}

}
