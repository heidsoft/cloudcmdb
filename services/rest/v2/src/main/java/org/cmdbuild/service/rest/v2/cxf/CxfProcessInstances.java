package org.cmdbuild.service.rest.v2.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.dao.entrytype.Functions.attributeName;
import static org.cmdbuild.dao.entrytype.Predicates.attributeTypeInstanceOf;
import static org.cmdbuild.model.widget.Widget.SUBMISSION_PARAM;
import static org.cmdbuild.service.rest.v2.constants.Serialization.UNDERSCORED_STATUS;
import static org.cmdbuild.service.rest.v2.cxf.util.Json.safeJsonArray;
import static org.cmdbuild.service.rest.v2.cxf.util.Json.safeJsonObject;
import static org.cmdbuild.service.rest.v2.model.Models.newMetadata;
import static org.cmdbuild.service.rest.v2.model.Models.newReference;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.v2.model.Models.newResponseSingle;
import static org.cmdbuild.workflow.ProcessAttributes.FlowStatus;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.FilterElementGetters;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.v2.ProcessInstances;
import org.cmdbuild.service.rest.v2.cxf.serialization.DefaultConverter;
import org.cmdbuild.service.rest.v2.cxf.serialization.ToProcessInstance;
import org.cmdbuild.service.rest.v2.model.DetailResponseMetadata.Reference;
import org.cmdbuild.service.rest.v2.model.ProcessInstance;
import org.cmdbuild.service.rest.v2.model.ProcessInstanceAdvanceable;
import org.cmdbuild.service.rest.v2.model.ResponseMultiple;
import org.cmdbuild.service.rest.v2.model.ResponseSingle;
import org.cmdbuild.service.rest.v2.model.Widget;
import org.cmdbuild.workflow.LookupHelper;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstanceWithPosition;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Maps.EntryTransformer;

public class CxfProcessInstances implements ProcessInstances {

	private static final Iterable<Long> NO_INSTANCE_IDS = emptyList();
	private static final Map<Long, Long> NO_POSITIONS = emptyMap();

	private static final Function<Widget, String> WIDGET_ID = new Function<Widget, String>() {

		@Override
		public String apply(final Widget input) {
			return input.getId();
		}

	};

	private static final Function<Widget, Object> WIDGET_OUTPUT = new Function<Widget, Object>() {

		@Override
		public Object apply(final Widget input) {
			final Map<String, Object> map = newHashMap();
			map.put(SUBMISSION_PARAM, input.getOutput());
			return map;
		}

	};

	private final ErrorHandler errorHandler;
	private final WorkflowLogic workflowLogic;
	private final LookupHelper lookupHelper;

	public CxfProcessInstances(final ErrorHandler errorHandler, final WorkflowLogic workflowLogic,
			final LookupHelper lookupHelper) {
		this.errorHandler = errorHandler;
		this.workflowLogic = workflowLogic;
		this.lookupHelper = lookupHelper;
	}

	@Override
	public ResponseSingle<Long> create(final String processId, final ProcessInstanceAdvanceable processInstance) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		try {
			final UserProcessInstance instance = workflowLogic.startProcess( //
					processId, //
					adaptInputValues(found, processInstance), //
					adaptWidgets(processInstance.getWidgets()), //
					processInstance.isAdvance());
			return newResponseSingle(Long.class) //
					.withElement(instance.getId()) //
					.build();
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
		return null;
	}

	@Override
	public ResponseSingle<ProcessInstance> read(final String processId, final Long instanceId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.limit(1) //
				.offset(0) //
				.filter(filterFor(instanceId)) //
				.build();
		final PagedElements<UserProcessInstance> elements = workflowLogic.query(found.getName(), queryOptions);
		if (elements.totalSize() == 0) {
			errorHandler.processInstanceNotFound(instanceId);
		}
		final UserProcessInstance element = from(elements).first().get();
		final Map<Long, Reference> references = newHashMap();
		for (final CMAttribute _element : from(element.getType().getAllAttributes()) //
				.filter(attributeTypeInstanceOf(ReferenceAttributeType.class))) {
			final Object value = element.get(_element.getName());
			if (value instanceof IdAndDescription) {
				final IdAndDescription _value = IdAndDescription.class.cast(value);
				if (_value.getId() != null) {
					references.put(_value.getId(), newReference() //
							.withDescription(_value.getDescription()) //
							.build());
				}
			}
		}
		final Function<UserProcessInstance, ProcessInstance> toProcessInstance = ToProcessInstance.newInstance() //
				.withType(found) //
				.withLookupHelper(lookupHelper) //
				.build();
		return newResponseSingle(ProcessInstance.class) //
				.withElement(toProcessInstance.apply(element)) //
				.withMetadata(newMetadata() //
						.withReferences(references) //
						.build()) //
				.build();
	}

	private JSONObject filterFor(final Long id) {
		try {
			final JSONObject emptyFilter = new JSONObject();
			return new JsonFilterHelper(emptyFilter) //
					.merge(FilterElementGetters.id(id));
		} catch (final JSONException e) {
			errorHandler.propagate(e);
			return new JSONObject();
		}
	}

	@Override
	public ResponseMultiple<ProcessInstance> read(final String processId, final String filter, final String sort,
			final Integer limit, final Integer offset, final Set<Long> instanceIds) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		// TODO do it better
		// <<<<<
		final String regex = "\"attribute\"[\\s]*:[\\s]*\"" + UNDERSCORED_STATUS + "\"";
		final String replacement = "\"attribute\":\"" + FlowStatus.dbColumnName() + "\"";
		final String _filter = defaultString(filter).replaceAll(regex, replacement);
		// <<<<<
		final Iterable<String> attributes = activeAttributes(found);
		final Iterable<String> _attributes = concat(attributes, asList(FlowStatus.dbColumnName()));
		final QueryOptions queryOptions = QueryOptions.newQueryOption() //
				.onlyAttributes(_attributes) //
				.filter(safeJsonObject(_filter)) //
				.orderBy(safeJsonArray(sort)) //
				.limit(limit) //
				.offset(offset) //
				.build();
		final PagedElements<? extends UserProcessInstance> elements;
		final Map<Long, Long> positions;
		if (isEmpty(defaultIfNull(instanceIds, NO_INSTANCE_IDS))) {
			elements = workflowLogic.query(found.getName(), queryOptions);
			positions = NO_POSITIONS;
		} else {
			final PagedElements<UserProcessInstanceWithPosition> response0 = workflowLogic.queryWithPosition(
					found.getName(), queryOptions, instanceIds);
			elements = response0;
			positions = transformValues( //
					uniqueIndex(response0, new Function<UserProcessInstanceWithPosition, Long>() {

						@Override
						public Long apply(final UserProcessInstanceWithPosition input) {
							return input.getId();
						}

					}), //
					new Function<UserProcessInstanceWithPosition, Long>() {

						@Override
						public Long apply(final UserProcessInstanceWithPosition input) {
							return input.getPosition();
						};

					});
		}
		final Map<Long, Reference> references = newHashMap();
		for (final UserProcessInstance element : elements) {
			for (final CMAttribute _element : from(element.getType().getAllAttributes()) //
					.filter(attributeTypeInstanceOf(ReferenceAttributeType.class))) {
				final Object value = element.get(_element.getName());
				if (value instanceof IdAndDescription) {
					final IdAndDescription _value = IdAndDescription.class.cast(value);
					if (_value.getId() != null) {
						references.put(_value.getId(), newReference() //
								.withDescription(_value.getDescription()) //
								.build());
					}
				}
			}
		}
		final Function<UserProcessInstance, ProcessInstance> toProcessInstance = ToProcessInstance.newInstance() //
				.withType(found) //
				.withLookupHelper(lookupHelper) //
				.withAttributes(attributes) //
				.build();
		return newResponseMultiple(ProcessInstance.class) //
				.withElements(from(elements) //
						.transform(toProcessInstance)) //
				.withMetadata(newMetadata() //
						.withTotal(Long.valueOf(elements.totalSize())) //
						.withPositions(positions) //
						.withReferences(references) //
						.build()) //
				.build();
	}

	private Iterable<String> activeAttributes(final UserProcessClass target) {
		return from(target.getActiveAttributes()) //
				.transform(attributeName());
	}

	@Override
	public void update(final String processId, final Long instanceId, final ProcessInstanceAdvanceable processInstance) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		try {
			final UserProcessInstance instance = workflowLogic.getProcessInstance(processId, instanceId);
			final UserActivityInstance activity = instance.getActivityInstance(processInstance.getActivity());
			if (activity == null) {
				errorHandler.processActivityNotFound(processInstance.getActivity());
			}
			workflowLogic.updateProcess( //
					processId, //
					instanceId, //
					activity.getId(), //
					adaptInputValues(found, processInstance), //
					adaptWidgets(processInstance.getWidgets()), //
					processInstance.isAdvance());
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
	}

	@Override
	public void delete(final String processId, final Long instanceId) {
		final UserProcessClass found = workflowLogic.findProcessClass(processId);
		if (found == null) {
			errorHandler.processNotFound(processId);
		}
		try {
			workflowLogic.abortProcess(processId, instanceId);
		} catch (final Throwable e) {
			errorHandler.propagate(e);
		}
	}

	private Map<String, Object> adaptInputValues(final UserProcessClass userProcessClass,
			final ProcessInstanceAdvanceable processInstanceAdvanceable) {
		return transformEntries(processInstanceAdvanceable.getValues(), new EntryTransformer<String, Object, Object>() {

			@Override
			public Object transformEntry(final String key, final Object value) {
				final CMAttribute attribute = userProcessClass.getAttribute(key);
				final Object _value;
				if (attribute == null) {
					_value = value;
				} else {
					final CMAttributeType<?> attributeType = attribute.getType();
					_value = DefaultConverter.newInstance() //
							.build() //
							.fromClient() //
							.convert(attributeType, value);
				}
				return _value;
			}

		});
	}

	private Map<String, Object> adaptWidgets(final Collection<Widget> elements) {
		return transformValues(uniqueIndex(elements, WIDGET_ID), WIDGET_OUTPUT);
	}

}
