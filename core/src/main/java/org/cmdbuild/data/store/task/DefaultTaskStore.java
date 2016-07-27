package org.cmdbuild.data.store.task;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static org.cmdbuild.data.store.task.TaskParameterGroupable.groupedBy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps.EntryTransformer;

/**
 * This {@link Store} handles the saving process of {@link Task} elements.
 *
 * @since 2.2
 */
public class DefaultTaskStore implements TaskStore {

	private static final Marker MARKER = MarkerFactory.getMarker(TaskStore.class.getName());

	private static final Function<TaskParameter, String> TASK_PARAMETER_TO_KEY = new Function<TaskParameter, String>() {

		@Override
		public String apply(final TaskParameter input) {
			return input.getKey();
		}

	};

	private static final Function<TaskParameter, String> TASK_PARAMETER_TO_VALUE = new Function<TaskParameter, String>() {

		@Override
		public String apply(final TaskParameter input) {
			return input.getValue();
		}

	};

	private static final Function<TaskParameter, String> BY_NAME = TASK_PARAMETER_TO_KEY;

	private static interface Action<T> {

		T execute();

	}

	private static abstract class AbstractAction<T> implements Action<T> {

		protected final Store<TaskDefinition> definitionsStore;
		protected final Store<TaskParameter> parametersStore;
		protected final Store<TaskRuntime> runtimeStore;

		protected AbstractAction(final Store<TaskDefinition> definitionsStore,
				final Store<TaskParameter> parametersStore, final Store<TaskRuntime> runtimeStore) {
			this.definitionsStore = definitionsStore;
			this.parametersStore = parametersStore;
			this.runtimeStore = runtimeStore;
		}

		protected TaskDefinition definitionOf(final Task task) {
			return new TaskVisitor() {

				private TaskDefinition.Builder<? extends TaskDefinition> builder;

				public TaskDefinition.Builder<? extends TaskDefinition> builder() {
					task.accept(this);
					Validate.notNull(builder, "cannot create builder");
					return builder;
				}

				@Override
				public void visit(final AsynchronousEventTask task) {
					builder = AsynchronousEventTaskDefinition.newInstance();
				}

				@Override
				public void visit(final ConnectorTask task) {
					builder = ConnectorTaskDefinition.newInstance();
				}

				@Override
				public void visit(final GenericTask task) {
					builder = GenericTaskDefinition.newInstance();
				}

				@Override
				public void visit(final ReadEmailTask task) {
					builder = ReadEmailTaskDefinition.newInstance();
				}

				@Override
				public void visit(final StartWorkflowTask task) {
					builder = StartWorkflowTaskDefinition.newInstance();
				}

				@Override
				public void visit(final SynchronousEventTask task) {
					builder = SynchronousEventTaskDefinition.newInstance();
				}

			}.builder() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunning(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.build();
		}

		protected Task merge(final TaskDefinition definition, final Iterable<TaskParameter> parameters,
				final TaskRuntime runtime) {

			return new TaskDefinitionVisitor() {

				private Task.Builder<? extends Task> builder;

				public Task.Builder<? extends Task> builder() {
					definition.accept(this);
					Validate.notNull(builder, "cannot create builder");
					return builder;
				}

				@Override
				public void visit(final AsynchronousEventTaskDefinition taskDefinition) {
					builder = AsynchronousEventTask.newInstance();
				}

				@Override
				public void visit(final ConnectorTaskDefinition taskDefinition) {
					builder = ConnectorTask.newInstance();
				}

				@Override
				public void visit(final GenericTaskDefinition taskDefinition) {
					builder = GenericTask.newInstance();
				}

				@Override
				public void visit(final ReadEmailTaskDefinition taskDefinition) {
					builder = ReadEmailTask.newInstance();
				}

				@Override
				public void visit(final StartWorkflowTaskDefinition taskDefinition) {
					builder = StartWorkflowTask.newInstance();
				}

				@Override
				public void visit(final SynchronousEventTaskDefinition taskDefinition) {
					builder = SynchronousEventTask.newInstance();
				}

			}.builder() //
					.withId(definition.getId()) //
					.withDescription(definition.getDescription()) //
					.withRunningStatus(definition.isRunning()) //
					.withCronExpression(definition.getCronExpression()) //
					.withLastExecution(runtime.getLastExecution()) //
					.withParameters(transformValues( //
							uniqueIndex(parameters, TASK_PARAMETER_TO_KEY), //
							TASK_PARAMETER_TO_VALUE)) //
					.build();
		}

		protected EntryTransformer<String, String, TaskParameter> toTaskParameterMapOf(
				final TaskDefinition definition) {
			return new EntryTransformer<String, String, TaskParameter>() {

				@Override
				public TaskParameter transformEntry(final String key, final String value) {
					return TaskParameter.newInstance().withOwner(definition.getId()) //
							.withKey(key) //
							.withValue(value) //
							.build();
				}

			};
		}

	}

	private static class Create extends AbstractAction<Storable> {

		private static final Iterable<TaskParameter> NO_PARAMETERS = Collections.emptyList();

		private final Task storable;

		public Create(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Store<TaskRuntime> runtimeStore, final Task storable) {
			super(definitions, parametersStore, runtimeStore);
			this.storable = storable;
		}

		@Override
		public Storable execute() {
			final TaskDefinition definition = definitionOf(storable);
			final Storable createdDefinition = definitionsStore.create(definition);
			final TaskDefinition readedDefinition = definitionsStore.read(createdDefinition);
			for (final TaskParameter element : transformEntries(storable.getParameters(),
					toTaskParameterMapOf(readedDefinition)).values()) {
				parametersStore.create(element);
			}
			final Storable createdRuntime = runtimeStore.create(TaskRuntime.newInstance() //
					.withOwner(readedDefinition.getId()) //
					.withLastExecution(storable.getLastExecution()) //
					.build());
			final TaskRuntime readedRuntime = runtimeStore.read(createdRuntime);
			return merge(readedDefinition, NO_PARAMETERS, readedRuntime);
		}

	}

	private static class Read extends AbstractAction<Task> {

		private final Storable storable;

		public Read(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Store<TaskRuntime> runtimeStore, final Storable storable) {
			super(definitions, parametersStore, runtimeStore);
			this.storable = storable;
		}

		@Override
		public Task execute() {
			final Task task = Task.class.cast(storable);
			final TaskDefinition definition = definitionsStore.read(definitionOf(task));
			final Iterable<TaskParameter> parameters = parametersStore.readAll(groupedBy(definition));
			final Optional<TaskRuntime> _runtime = from(runtimeStore.readAll(groupedBy(definition))).first();
			final TaskRuntime runtime = _runtime.isPresent() ? _runtime.get()
					: TaskRuntime.newInstance() //
							.withOwner(definition.getId()) //
							.build();
			return merge(definition, parameters, runtime);
		}

	}

	private static class ReadAll extends AbstractAction<List<Task>> {

		private final Groupable groupable;

		public ReadAll(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Store<TaskRuntime> runtimeStore) {
			this(definitions, parametersStore, runtimeStore, null);
		}

		public ReadAll(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Store<TaskRuntime> runtimeStore, final Groupable groupable) {
			super(definitions, parametersStore, runtimeStore);
			this.groupable = groupable;
		}

		@Override
		public List<Task> execute() {
			final Iterable<TaskDefinition> list = (groupable == null) ? definitionsStore.readAll()
					: definitionsStore.readAll(groupable);
			return from(list) //
					.transform(new Function<TaskDefinition, Task>() {

						@Override
						public Task apply(final TaskDefinition input) {
							final Iterable<TaskParameter> parameters = parametersStore.readAll(groupedBy(input));
							final Optional<TaskRuntime> _runtime = from(runtimeStore.readAll(groupedBy(input))).first();
							final TaskRuntime runtime = _runtime.isPresent() ? _runtime.get()
									: TaskRuntime.newInstance() //
											.withOwner(input.getId()) //
											.build();
							return merge(input, parameters, runtime);
						}

					}) //
					.toList();
		}

	}

	private static class Update extends AbstractAction<Void> {

		private final Task storable;

		public Update(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Store<TaskRuntime> runtimeStore, final Task storable) {
			super(definitions, parametersStore, runtimeStore);
			this.storable = storable;
		}

		@Override
		public Void execute() {
			final TaskDefinition definition = definitionOf(storable);
			definitionsStore.update(definition);
			final Map<String, TaskParameter> left = transformEntries(storable.getParameters(),
					toTaskParameterMapOf(definition));
			final Map<String, TaskParameter> right = uniqueIndex(parametersStore.readAll(groupedBy(definition)),
					BY_NAME);
			final MapDifference<String, TaskParameter> difference = difference(left, right);
			for (final TaskParameter element : difference.entriesOnlyOnLeft().values()) {
				parametersStore.create(element);
			}
			for (final ValueDifference<TaskParameter> valueDifference : difference.entriesDiffering().values()) {
				final TaskParameter element = valueDifference.leftValue();
				parametersStore.update(TaskParameter.newInstance() //
						.withId(valueDifference.rightValue().getId()) //
						.withOwner(element.getOwner()) //
						.withKey(element.getKey()) //
						.withValue(element.getValue()) //
						.build());
			}
			for (final TaskParameter element : difference.entriesOnlyOnRight().values()) {
				parametersStore.delete(element);
			}
			/*
			 * should be one-only, but who knows...
			 */
			boolean done = false;
			for (final TaskRuntime element : runtimeStore.readAll(groupedBy(definition))) {
				runtimeStore.update(TaskRuntime.newInstance() //
						.withId(element.getId()) //
						.withOwner(element.getOwner()) //
						.withLastExecution(storable.getLastExecution()) //
						.build());
				done = true;
			}
			if (!done) {
				runtimeStore.create(TaskRuntime.newInstance() //
						.withOwner(storable.getId()) //
						.withLastExecution(storable.getLastExecution()) //
						.build());
			}
			return null;
		}

	}

	private static class Delete extends AbstractAction<Void> {

		private final Storable storable;

		public Delete(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Store<TaskRuntime> runtimeStore, final Storable storable) {
			super(definitions, parametersStore, runtimeStore);
			this.storable = storable;
		}

		@Override
		public Void execute() {
			Validate.isInstanceOf(Task.class, storable);
			final Task task = Task.class.cast(storable);
			final TaskDefinition definition = definitionsStore.read(definitionOf(task));
			/*
			 * should be one-only, but who knows...
			 */
			for (final Storable element : runtimeStore.readAll(groupedBy(definition))) {
				runtimeStore.delete(element);
			}
			for (final Storable element : parametersStore.readAll(groupedBy(definition))) {
				parametersStore.delete(element);
			}
			definitionsStore.delete(definition);
			return null;
		}

	}

	private static class UpdateLastExecution extends AbstractAction<Void> {

		private final Task storable;

		public UpdateLastExecution(final Store<TaskDefinition> definitions, final Store<TaskParameter> parametersStore,
				final Store<TaskRuntime> runtimeStore, final Task storable) {
			super(definitions, parametersStore, runtimeStore);
			this.storable = storable;
		}

		@Override
		public Void execute() {
			final TaskDefinition definition = definitionOf(storable);
			/*
			 * should be one-only, but who knows...
			 */
			boolean done = false;
			for (final TaskRuntime element : runtimeStore.readAll(groupedBy(definition))) {
				runtimeStore.update(TaskRuntime.newInstance() //
						.withId(element.getId()) //
						.withOwner(element.getOwner()) //
						.withLastExecution(storable.getLastExecution()) //
						.build());
				done = true;
			}
			if (!done) {
				runtimeStore.create(TaskRuntime.newInstance() //
						.withOwner(storable.getId()) //
						.withLastExecution(storable.getLastExecution()) //
						.build());
			}
			return null;
		}

	}

	private final Store<TaskDefinition> definitionsStore;
	private final Store<TaskParameter> parametersStore;
	private final Store<TaskRuntime> runtimeStore;

	public DefaultTaskStore(final Store<TaskDefinition> definitionsStore, final Store<TaskParameter> parametersStore,
			final Store<TaskRuntime> runtimeStore) {
		this.definitionsStore = definitionsStore;
		this.parametersStore = parametersStore;
		this.runtimeStore = runtimeStore;
	}

	@Override
	public Storable create(final Task storable) {
		logger.info(MARKER, "creating new element '{}'", storable);
		return execute(doCreate(storable));
	}

	@Override
	public Task read(final Storable storable) {
		logger.info(MARKER, "reading existing element '{}'", storable);
		return execute(doRead(storable));
	}

	@Override
	public Collection<Task> readAll() {
		logger.info(MARKER, "getting all existing elements");
		return execute(doReadAll());
	}

	@Override
	public Collection<Task> readAll(final Groupable groupable) {
		logger.info(MARKER, "getting all existing elements for group '{}'", groupable);
		return execute(doReadAll(groupable));
	}

	@Override
	public void update(final Task storable) {
		logger.info(MARKER, "updating existing element '{}'", storable);
		execute(doUpdate(storable));
	}

	@Override
	public void delete(final Storable storable) {
		logger.info(MARKER, "deleting existing element '{}'", storable);
		execute(doDelete(storable));
	}

	@Override
	public Task read(final Long id) {
		logger.info(MARKER, "reading existing element with id '{}'", id);
		for (final Task element : readAll()) {
			if (element.getId().equals(id)) {
				return element;
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public void updateLastExecution(final Task storable) {
		logger.info(MARKER, "updating only last execution for existing element '{}'", storable);
		execute(doUpdateLastExecution(storable));
	}

	private Create doCreate(final Task storable) {
		return new Create(definitionsStore, parametersStore, runtimeStore, storable);
	}

	private Read doRead(final Storable storable) {
		return new Read(definitionsStore, parametersStore, runtimeStore, storable);
	}

	private ReadAll doReadAll() {
		return new ReadAll(definitionsStore, parametersStore, runtimeStore);
	}

	private ReadAll doReadAll(final Groupable groupable) {
		return new ReadAll(definitionsStore, parametersStore, runtimeStore, groupable);
	}

	private Update doUpdate(final Task storable) {
		return new Update(definitionsStore, parametersStore, runtimeStore, storable);
	}

	private Delete doDelete(final Storable storable) {
		return new Delete(definitionsStore, parametersStore, runtimeStore, storable);
	}

	private UpdateLastExecution doUpdateLastExecution(final Task storable) {
		return new UpdateLastExecution(definitionsStore, parametersStore, runtimeStore, storable);
	}

	private <T> T execute(final Action<T> action) {
		return action.execute();
	}

}
