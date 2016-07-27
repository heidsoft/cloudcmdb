package org.cmdbuild.data.store.task;

import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.dao.BaseStorableConverter;
import org.cmdbuild.data.store.task.TaskDefinition.Builder;

import com.google.common.collect.Maps;

public class TaskDefinitionConverter extends BaseStorableConverter<TaskDefinition> {

	private static enum Factory {

		ASYNCHRONOUS_EVENT(TYPE_ASYNCHRONOUS_EVENT) {

			@Override
			protected Builder<? extends TaskDefinition> create(final CMCard card) {
				return AsynchronousEventTaskDefinition.newInstance();
			}

		}, //
		CONNECTOR(TYPE_CONNECTOR) {

			@Override
			protected Builder<? extends TaskDefinition> create(final CMCard card) {
				return ConnectorTaskDefinition.newInstance();
			}

		}, //
		GENERIC(TYPE_GENERIC) {

			@Override
			protected Builder<? extends TaskDefinition> create(final CMCard card) {
				return GenericTaskDefinition.newInstance();
			}

		}, //
		READ_EMAIL(TYPE_EMAIL) {

			@Override
			protected Builder<? extends TaskDefinition> create(final CMCard card) {
				return ReadEmailTaskDefinition.newInstance();
			}

		}, //
		START_WORKFLOW(TYPE_WORKFLOW) {

			@Override
			protected Builder<? extends TaskDefinition> create(final CMCard card) {
				return StartWorkflowTaskDefinition.newInstance();
			}

		}, //
		SYNCHRONOUS_EVENT(TYPE_SYNCHRONOUS_EVENT) {

			@Override
			protected Builder<? extends TaskDefinition> create(final CMCard card) {
				return SynchronousEventTaskDefinition.newInstance();
			}

		}, //
		;

		private final String attributeValue;

		private Factory(final String attributeValue) {
			this.attributeValue = attributeValue;
		}

		protected abstract Builder<? extends TaskDefinition> create(CMCard card);

		public static Builder<? extends TaskDefinition> from(final CMCard card) {
			final String type = card.get(TYPE, String.class);
			for (final Factory element : values()) {
				if (element.attributeValue.equals(type)) {
					return element.create(card);
				}
			}
			throw new IllegalArgumentException("unrecognized type");
		}

		public static Factory from(final TaskDefinition storable) {
			return new TaskDefinitionVisitor() {

				private Factory element;

				public Factory type() {
					storable.accept(this);
					Validate.notNull(element, "unrecognized type");
					return element;
				}

				@Override
				public void visit(final AsynchronousEventTaskDefinition taskDefinition) {
					element = ASYNCHRONOUS_EVENT;
				}

				@Override
				public void visit(final ConnectorTaskDefinition taskDefinition) {
					element = CONNECTOR;
				}

				@Override
				public void visit(final GenericTaskDefinition taskDefinition) {
					element = GENERIC;
				}

				@Override
				public void visit(final ReadEmailTaskDefinition taskDefinition) {
					element = READ_EMAIL;
				}

				@Override
				public void visit(final StartWorkflowTaskDefinition taskDefinition) {
					element = START_WORKFLOW;
				}

				@Override
				public void visit(final SynchronousEventTaskDefinition taskDefinition) {
					element = SYNCHRONOUS_EVENT;
				}

			}.type();
		}

	}

	private static final String CLASSNAME = "_Task";

	private static final String CRON_EXPRESSION = "CronExpression";
	private static final String TYPE = "Type";
	private static final String RUNNING = "Running";

	private static final String TYPE_ASYNCHRONOUS_EVENT = "asynchronous_event";
	private static final String TYPE_CONNECTOR = "connector";
	private static final String TYPE_EMAIL = "emailService";
	private static final String TYPE_GENERIC = "generic";
	private static final String TYPE_SYNCHRONOUS_EVENT = "synchronous_event";
	private static final String TYPE_WORKFLOW = "workflow";

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public TaskDefinition convert(final CMCard card) {
		return Factory.from(card) //
				.withId(card.getId()) //
				.withDescription(card.get(DESCRIPTION_ATTRIBUTE, String.class)) //
				.withCronExpression(card.get(CRON_EXPRESSION, String.class)) //
				.withRunning(card.get(RUNNING, Boolean.class)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final TaskDefinition storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(DESCRIPTION_ATTRIBUTE, storable.getDescription());
		values.put(CRON_EXPRESSION, storable.getCronExpression());
		values.put(TYPE, Factory.from(storable).attributeValue);
		values.put(RUNNING, storable.isRunning());
		return values;
	}

}
