package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.data.store.task.DefaultTaskStore;
import org.cmdbuild.data.store.task.TaskDefinition;
import org.cmdbuild.data.store.task.TaskDefinitionConverter;
import org.cmdbuild.data.store.task.TaskParameter;
import org.cmdbuild.data.store.task.TaskParameterConverter;
import org.cmdbuild.data.store.task.TaskRuntime;
import org.cmdbuild.data.store.task.TaskRuntimeConverter;
import org.cmdbuild.data.store.task.TaskStore;
import org.cmdbuild.logic.email.DefaultEmailTemplateSenderFactory;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.logic.taskmanager.TransactionalTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.event.DefaultLogicAndObserverConverter;
import org.cmdbuild.logic.taskmanager.event.DefaultObserverFactory;
import org.cmdbuild.logic.taskmanager.event.DefaultSynchronousEventFacade;
import org.cmdbuild.logic.taskmanager.event.LogicAndObserverConverter;
import org.cmdbuild.logic.taskmanager.event.ObserverFactory;
import org.cmdbuild.logic.taskmanager.event.SynchronousEventFacade;
import org.cmdbuild.logic.taskmanager.scheduler.DefaultLogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.scheduler.DefaultSchedulerFacade;
import org.cmdbuild.logic.taskmanager.scheduler.LogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.scheduler.SchedulerFacade;
import org.cmdbuild.logic.taskmanager.store.DefaultLogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTaskJobFactory;
import org.cmdbuild.logic.taskmanager.task.connector.DefaultAttributeValueAdapter;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTaskJobFactory;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTaskJobFactory;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTaskJobFactory;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTaskJobFactory;
import org.cmdbuild.services.event.DefaultObserverCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Supplier;

@Configuration
public class TaskManager {

	@Autowired
	private Api api;

	@Autowired
	private Data data;

	@Autowired
	private Dms dms;

	@Autowired
	private Email email;

	@Autowired
	private Other other;

	@Autowired
	private Report report;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private Template template;

	@Autowired
	private User user;

	@Autowired
	private UserStore userStore;

	@Autowired
	private Workflow workflow;

	@Bean
	public TaskManagerLogic taskManagerLogic() {
		return new TransactionalTaskManagerLogic(new DefaultTaskManagerLogic( //
				defaultLogicAndStoreConverter(), //
				taskStore(), //
				defaultSchedulerTaskFacade(), //
				defaultSynchronousEventFacade(), //
				email.emailLogic() //
		));
	}

	@Bean
	protected SchedulerFacade defaultSchedulerTaskFacade() {
		return new DefaultSchedulerFacade(scheduler.defaultSchedulerService(), defaultLogicAndSchedulerConverter());
	}

	@Bean
	public DefaultObserverCollector defaultObserverCollector() {
		return new DefaultObserverCollector();
	}

	@Bean
	protected DefaultLogicAndStoreConverter defaultLogicAndStoreConverter() {
		return new DefaultLogicAndStoreConverter();
	}

	@Bean
	protected TaskStore taskStore() {
		return new DefaultTaskStore(taskDefinitionStore(), taskParameterStore(), taskRuntimeStore());
	}

	@Bean
	protected Store<TaskDefinition> taskDefinitionStore() {
		return DataViewStore.<TaskDefinition> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(taskDefinitionConverter()) //
				.build();
	}

	@Bean
	protected StorableConverter<TaskDefinition> taskDefinitionConverter() {
		return new TaskDefinitionConverter();
	}

	@Bean
	protected Store<TaskParameter> taskParameterStore() {
		return DataViewStore.<TaskParameter> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(taskParameterConverter()) //
				.build();
	}

	@Bean
	protected StorableConverter<TaskParameter> taskParameterConverter() {
		return new TaskParameterConverter();
	}

	@Bean
	protected Store<TaskRuntime> taskRuntimeStore() {
		return DataViewStore.<TaskRuntime> newInstance() //
				.withDataView(data.systemDataView()) //
				.withStorableConverter(taskRuntimeConverter()) //
				.build();
	}

	@Bean
	protected StorableConverter<TaskRuntime> taskRuntimeConverter() {
		return new TaskRuntimeConverter();
	}

	@Bean
	protected LogicAndSchedulerConverter defaultLogicAndSchedulerConverter() {
		final DefaultLogicAndSchedulerConverter converter = new DefaultLogicAndSchedulerConverter();
		converter.register(AsynchronousEventTask.class, asynchronousEventTaskJobFactory());
		converter.register(ConnectorTask.class, connectorTaskJobFactory());
		converter.register(GenericTask.class, genericTaskJobFactory());
		converter.register(ReadEmailTask.class, readEmailTaskJobFactory());
		converter.register(StartWorkflowTask.class, startWorkflowTaskJobFactory());
		return converter;
	}

	@Bean
	protected AsynchronousEventTaskJobFactory asynchronousEventTaskJobFactory() {
		return new AsynchronousEventTaskJobFactory( //
				data.systemDataView(), //
				email.emailAccountFacade(), //
				email.emailTemplateLogic(), //
				taskStore(), //
				defaultLogicAndStoreConverter(), //
				emailTemplateSenderFactory() //
		);
	}

	@Bean
	protected ConnectorTaskJobFactory connectorTaskJobFactory() {
		return new ConnectorTaskJobFactory( //
				data.systemDataView(), //
				other.dataSourceHelper(), //
				defaultAttributeValueAdapter(), //
				email.emailAccountFacade(), //
				email.emailTemplateLogic(), //
				emailTemplateSenderFactory() //
		);
	}

	@Bean
	protected DefaultAttributeValueAdapter defaultAttributeValueAdapter() {
		return new DefaultAttributeValueAdapter(data.systemDataView(), data.lookupStore());
	}

	private GenericTaskJobFactory genericTaskJobFactory() {
		return new GenericTaskJobFactory( //
				email.emailAccountFacade(), //
				email.emailTemplateLogic(), //
				report.reportLogic(), //
				data.systemDataView(), //
				template.databaseTemplateEngine(), //
				emailTemplateSenderFactory() //
		);
	}

	@Bean
	protected ReadEmailTaskJobFactory readEmailTaskJobFactory() {
		return new ReadEmailTaskJobFactory( //
				email.emailAccountFacade(), //
				email.emailServiceFactory(), //
				email.subjectHandler(), //
				email.emailStore(), //
				workflow.systemWorkflowLogicBuilder() //
						.build(), //
				dms.defaultDmsLogic(), //
				data.systemDataView(), //
				email.emailTemplateLogic(), //
				template.databaseTemplateEngine(), //
				emailTemplateSenderFactory() //
		);
	}

	@Bean
	protected StartWorkflowTaskJobFactory startWorkflowTaskJobFactory() {
		return new StartWorkflowTaskJobFactory(workflow.systemWorkflowLogicBuilder().build());
	}

	@Bean
	protected SynchronousEventFacade defaultSynchronousEventFacade() {
		return new DefaultSynchronousEventFacade(defaultObserverCollector(), logicAndObserverConverter());
	}

	@Bean
	protected LogicAndObserverConverter logicAndObserverConverter() {
		return new DefaultLogicAndObserverConverter(observerFactory());
	}

	@Bean
	protected ObserverFactory observerFactory() {
		return new DefaultObserverFactory( //
				userStore, //
				api.systemFluentApi(), //
				workflow.systemWorkflowLogicBuilder().build(), //
				email.emailAccountFacade(), //
				email.emailTemplateLogic(), //
				data.systemDataView(), //
				new Supplier<CMDataView>() {

					@Override
					public CMDataView get() {
						return user.userDataView();
					}

				}, //
				emailTemplateSenderFactory() //
		);
	}

	@Bean
	protected EmailTemplateSenderFactory emailTemplateSenderFactory() {
		return new DefaultEmailTemplateSenderFactory(email.emailServiceFactory(), email.emailLogic(),
				email.emailAttachmentsLogic());
	}

}
