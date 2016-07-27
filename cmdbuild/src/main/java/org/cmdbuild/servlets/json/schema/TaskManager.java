package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.EXECUTABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.TASK_ASYNCHRONOUS_EVENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TASK_CONNECTOR;
import static org.cmdbuild.servlets.json.CommunicationConstants.TASK_GENERIC;
import static org.cmdbuild.servlets.json.CommunicationConstants.TASK_READ_EMAIL;
import static org.cmdbuild.servlets.json.CommunicationConstants.TASK_START_WORKFLOW;
import static org.cmdbuild.servlets.json.CommunicationConstants.TASK_SYNCHRONOUS_EVENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.TYPE;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.TaskVisitor;
import org.cmdbuild.logic.taskmanager.task.connector.ConnectorTask;
import org.cmdbuild.logic.taskmanager.task.email.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.task.event.asynchronous.AsynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.event.synchronous.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.task.generic.GenericTask;
import org.cmdbuild.logic.taskmanager.task.process.StartWorkflowTask;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class TaskManager extends JSONBaseWithSpringContext {

	private static enum TaskType {

		ASYNCHRONOUS_EVENT(TASK_ASYNCHRONOUS_EVENT), //
		CONNECTOR(TASK_CONNECTOR), //
		GENERIC(TASK_GENERIC), //
		READ_EMAIL(TASK_READ_EMAIL), //
		START_WORKFLOW(TASK_START_WORKFLOW), //
		SYNCHRONOUS_EVENT(TASK_SYNCHRONOUS_EVENT), //
		;

		private final String forJson;

		private TaskType(final String forJson) {
			this.forJson = forJson;
		}

		public String forJson() {
			return forJson;
		}

	}

	private static class TaskTypeResolver implements TaskVisitor {

		public static TaskTypeResolver of(final Task task) {
			return new TaskTypeResolver(task);
		}

		private final Task task;
		private TaskType type;

		private TaskTypeResolver(final Task task) {
			this.task = task;
		}

		public TaskType find() {
			task.accept(this);
			Validate.notNull(type, "type not found");
			return type;
		}

		@Override
		public void visit(final AsynchronousEventTask task) {
			type = TaskType.ASYNCHRONOUS_EVENT;
		}

		@Override
		public void visit(final ConnectorTask task) {
			type = TaskType.CONNECTOR;
		}

		@Override
		public void visit(final GenericTask task) {
			type = TaskType.GENERIC;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			type = TaskType.READ_EMAIL;
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			type = TaskType.START_WORKFLOW;
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			type = TaskType.SYNCHRONOUS_EVENT;
		}

	}

	private static class JsonTask {

		private final Task delegate;

		public JsonTask(final Task delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(TYPE)
		public String getType() {
			return TaskTypeResolver.of(delegate).find().forJson();
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

		@JsonProperty(EXECUTABLE)
		public boolean executable() {
			return delegate.isExecutable();
		}

	}

	public static class JsonElements<T> {

		public static <T> JsonElements<T> of(final Iterable<? extends T> elements) {
			return new JsonElements<T>(elements);
		}

		private final List<? extends T> elements;

		private JsonElements(final Iterable<? extends T> elements) {
			this.elements = Lists.newArrayList(elements);
		}

		@JsonProperty(ELEMENTS)
		public List<? extends T> getElements() {
			return elements;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	public static final Function<Task, JsonTask> TASK_TO_JSON_TASK = new Function<Task, JsonTask>() {

		@Override
		public JsonTask apply(final Task input) {
			return new JsonTask(input);
		}

	};

	@Admin
	@JSONExported
	public JsonResponse readAll() {
		final Iterable<Task> tasks = taskManagerLogic().read();
		return success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@Admin
	@JSONExported
	public JsonResponse start( //
			@Parameter(value = ID) final Long id //
	) {
		taskManagerLogic().activate(id);
		return success();
	}

	@Admin
	@JSONExported
	public JsonResponse stop( //
			@Parameter(value = ID) final Long id //
	) {
		taskManagerLogic().deactivate(id);
		return success();
	}

	@Admin
	@JSONExported
	public JsonResponse execute( //
			@Parameter(value = ID) final Long id //
	) {
		taskManagerLogic().execute(id);
		return success();
	}

}
