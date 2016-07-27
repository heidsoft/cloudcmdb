package org.cmdbuild.workflow;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.apache.commons.lang3.ArrayUtils.INDEX_NOT_FOUND;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.ArrayUtils.indexOf;
import static org.apache.commons.lang3.ArrayUtils.remove;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.workflow.ProcessAttributes.ActivityDefinitionId;
import static org.cmdbuild.workflow.ProcessAttributes.ActivityInstanceId;
import static org.cmdbuild.workflow.ProcessAttributes.AllActivityPerformers;
import static org.cmdbuild.workflow.ProcessAttributes.CurrentActivityPerformers;
import static org.cmdbuild.workflow.ProcessAttributes.FlowStatus;
import static org.cmdbuild.workflow.ProcessAttributes.ProcessInstanceId;
import static org.cmdbuild.workflow.ProcessAttributes.UniqueProcessDefinition;
import static org.cmdbuild.workflow.WorkflowPersistence.ProcessData.NO_ACTIVITIES;
import static org.cmdbuild.workflow.WorkflowPersistence.ProcessData.NO_PROCESS_INSTANCE_INFO;
import static org.cmdbuild.workflow.WorkflowPersistence.ProcessData.NO_STATE;
import static org.cmdbuild.workflow.WorkflowPersistence.ProcessData.NO_VALUES;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.auth.context.SystemPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessData;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Optional;

class WorkflowUpdateHelper {

	private static final Marker marker = MarkerFactory.getMarker(WorkflowUpdateHelper.class.getName());
	private static final Logger logger = Log.PERSISTENCE;

	public static class WorkflowUpdateHelperBuilder implements Builder<WorkflowUpdateHelper> {

		private OperationUser operationUser;
		private CMClass processClass;
		private CMCardDefinition cardDefinition;
		private WSProcessInstInfo processInstInfo;
		private CMCard card;
		private CMProcessInstance processInstance;
		private ProcessDefinitionManager processDefinitionManager;
		private LookupHelper lookupHelper;
		private CMWorkflowService workflowService;
		private ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory;

		@Override
		public WorkflowUpdateHelper build() {
			validate();
			return new WorkflowUpdateHelper(this);
		}

		private void validate() {
			Validate.notNull(operationUser, "invalid %s", OperationUser.class);
			Validate.notNull(processClass, "invalid %s", CMProcessClass.class);
			Validate.notNull(cardDefinition, "invalid %s", CMCardDefinition.class);
			Validate.notNull(lookupHelper, "invalid %s", LookupHelper.class);
		}

		private WorkflowUpdateHelperBuilder withOperationUser(final OperationUser value) {
			operationUser = value;
			return this;
		}

		private WorkflowUpdateHelperBuilder withProcessClass(final CMClass value) {
			processClass = value;
			return this;
		}

		private WorkflowUpdateHelperBuilder withCardDefinition(final CMCardDefinition value) {
			cardDefinition = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withLookupHelper(final LookupHelper value) {
			lookupHelper = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withProcessInstInfo(final WSProcessInstInfo value) {
			processInstInfo = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withCard(final CMCard value) {
			card = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withProcessInstance(final CMProcessInstance value) {
			processInstance = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withProcessDefinitionManager(final ProcessDefinitionManager value) {
			processDefinitionManager = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withWorkflowService(final CMWorkflowService value) {
			workflowService = value;
			return this;
		}

		public WorkflowUpdateHelperBuilder withActivityPerformerTemplateResolverFactory(
				final ActivityPerformerTemplateResolverFactory value) {
			activityPerformerTemplateResolverFactory = value;
			return this;
		}

	}

	public static WorkflowUpdateHelperBuilder newInstance(final OperationUser operationUser,
			final CMClass processClass, final CMCardDefinition cardDefinition) {
		return new WorkflowUpdateHelperBuilder() //
				.withOperationUser(operationUser) //
				.withProcessClass(processClass) //
				.withCardDefinition(cardDefinition);
	}

	private static final String UNRESOLVABLE_PARTICIPANT_GROUP = EMPTY;

	private final OperationUser operationUser;
	private final CMClass processClass;
	private final CMCardDefinition cardDefinition;
	private final WSProcessInstInfo processInstInfo;
	private final CMCard card;
	private final CMProcessInstance processInstance;
	private final ProcessDefinitionManager processDefinitionManager;
	private final LookupHelper lookupHelper;
	private final CMWorkflowService workflowService;
	private final ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory;

	private String code;
	private String uniqueProcessDefinition;
	private String processInstanceId;
	private String[] activityInstanceIds;
	private String[] activityDefinitionIds;
	private String[] currentActivityPerformers;
	private String[] allActivityPerformers;

	private WorkflowUpdateHelper(final WorkflowUpdateHelperBuilder builder) {
		this.operationUser = builder.operationUser;
		this.processClass = builder.processClass;
		this.cardDefinition = builder.cardDefinition;
		this.processInstInfo = builder.processInstInfo;
		this.card = builder.card;
		this.processInstance = builder.processInstance;
		this.processDefinitionManager = builder.processDefinitionManager;
		this.lookupHelper = builder.lookupHelper;
		this.workflowService = builder.workflowService;
		this.activityPerformerTemplateResolverFactory = builder.activityPerformerTemplateResolverFactory;

		logger.debug(marker, "setting internal values");
		if (card != null) {
			this.code = String.class.cast(card.getCode());
			this.uniqueProcessDefinition = card.get(UniqueProcessDefinition.dbColumnName(), String.class);
			this.processInstanceId = card.get(ProcessInstanceId.dbColumnName(), String.class);
			this.activityInstanceIds = card.get(ActivityInstanceId.dbColumnName(), String[].class);
			this.activityDefinitionIds = card.get(ActivityDefinitionId.dbColumnName(), String[].class);
			this.currentActivityPerformers = card.get(CurrentActivityPerformers.dbColumnName(), String[].class);
			this.allActivityPerformers = card.get(AllActivityPerformers.dbColumnName(), String[].class);
		} else {
			logger.debug(marker, "card not found, setting default values");
			this.processInstanceId = processInstInfo.getProcessInstanceId();
			this.activityInstanceIds = EMPTY_STRING_ARRAY;
			this.activityDefinitionIds = EMPTY_STRING_ARRAY;
			this.currentActivityPerformers = EMPTY_STRING_ARRAY;
			this.allActivityPerformers = EMPTY_STRING_ARRAY;
		}
		logger.debug(marker, "getting stored activity instance ids", Object.class.cast(activityInstanceIds));
		logger.debug(marker, "getting stored activity definition ids", Object.class.cast(activityDefinitionIds));
		logger.debug(marker, "getting stored current activity performers", Object.class.cast(currentActivityPerformers));
		logger.debug(marker, "getting stored all activity performers", Object.class.cast(allActivityPerformers));
	}

	public CMCard save() {
		// FIXME operation user must be always valid
		if (operationUser.isValid()) {
			cardDefinition.setUser(operationUser.getAuthenticatedUser().getUsername());
		} else if (operationUser.getPrivilegeContext() instanceof SystemPrivilegeContext) {
			cardDefinition.setUser("system");
		}
		cardDefinition.setCode(code);
		cardDefinition.set(UniqueProcessDefinition.dbColumnName(), uniqueProcessDefinition);
		cardDefinition.set(ProcessInstanceId.dbColumnName(), processInstanceId);
		cardDefinition.set(ActivityInstanceId.dbColumnName(), activityInstanceIds);
		cardDefinition.set(ActivityDefinitionId.dbColumnName(), activityDefinitionIds);
		cardDefinition.set(CurrentActivityPerformers.dbColumnName(), currentActivityPerformers);
		cardDefinition.set(AllActivityPerformers.dbColumnName(), allActivityPerformers);
		return cardDefinition.save();
	}

	public WorkflowUpdateHelper set(final ProcessData processData) throws CMWorkflowException {
		logger.info(marker, "filling process card");
		if (processData.state() != NO_STATE) {
			logger.debug(marker, "updating state");
			final Optional<Lookup> lookup = lookupHelper.lookupForState(processData.state());
			final Object id = lookup.isPresent() ? lookup.get().getId() : null;
			cardDefinition.set(FlowStatus.dbColumnName(), id);
		}
		if (processData.processInstanceInfo() != NO_PROCESS_INSTANCE_INFO) {
			logger.debug(marker, "updating process instance info");
			final WSProcessInstInfo info = processData.processInstanceInfo();
			final String value = format("%s#%s#%s", info.getPackageId(), info.getPackageVersion(),
					info.getProcessDefinitionId());
			uniqueProcessDefinition = value;
		}
		if (processData.values() != NO_VALUES) {
			logger.debug(marker, "updating values");
			for (final Entry<String, ?> entry : processData.values().entrySet()) {
				final String name = entry.getKey();
				final CMAttribute attribute = processClass.getAttribute(name);
				if (attribute == null) {
					logger.debug(marker, "skipping non-existent attribute '{}'", name);
					continue;
				}
				if (attribute.isSystem()) {
					logger.debug(marker, "skipping system attribute '{}'", name);
					continue;
				}
				logger.debug(marker, "updating process attribute '{}' with value '{}'", entry.getKey(),
						entry.getValue());
				cardDefinition.set(name, entry.getValue());
			}
		}
		if (processData.addActivities() != NO_ACTIVITIES) {
			logger.debug(marker, "adding activities");
			for (final WSActivityInstInfo activityInstanceInfo : processData.addActivities()) {
				logger.debug(marker, "adding activity '{}' '{}'", //
						activityInstanceInfo.getActivityDefinitionId(), //
						activityInstanceInfo.getActivityInstanceId());
				addActivity(activityInstanceInfo);
			}
		}
		if (processData.activities() != NO_ACTIVITIES) {
			logger.debug(marker, "setting activities");
			final WSActivityInstInfo[] activityInfos = processData.activities();
			removeClosedActivities(activityInfos);
			addNewActivities(activityInfos);
			updateCodeWithFirstActivityInfo();
		}
		return this;
	}

	private void addActivity(final WSActivityInstInfo activityInfo) throws CMWorkflowException {
		Validate.notNull(activityInfo);
		Validate.notNull(activityInfo.getActivityInstanceId());
		final String participantGroup = extractActivityParticipantGroup(activityInfo);
		if (participantGroup != UNRESOLVABLE_PARTICIPANT_GROUP) {
			activityInstanceIds = add(activityInstanceIds, activityInfo.getActivityInstanceId());
			activityDefinitionIds = add(activityDefinitionIds, activityInfo.getActivityDefinitionId());

			currentActivityPerformers = add(currentActivityPerformers, participantGroup);
			allActivityPerformers = addIfMissing(allActivityPerformers, participantGroup);
			updateCodeWithFirstActivityInfo();
		}
	}

	private String extractActivityParticipantGroup(final WSActivityInstInfo activityInfo) throws CMWorkflowException {
		final CMActivity activity = processDefinitionManager.getActivity(processInstance,
				activityInfo.getActivityDefinitionId());
		final ActivityPerformer performer = activity.getFirstNonAdminPerformer();
		final String group;
		switch (performer.getType()) {
		case ROLE:
			group = performer.getValue();
			break;
		case EXPRESSION:
			final String expression = performer.getValue();
			final Set<String> names = evaluatorFor(expression).getNames();
			if (activityInfo.getParticipants().length == 0) {
				/*
				 * an arbitrary expression in a non-starting activity, so should
				 * be a single name
				 */
				final Iterator<String> namesItr = names.iterator();
				group = namesItr.hasNext() ? namesItr.next() : UNRESOLVABLE_PARTICIPANT_GROUP;
			} else {
				final String maybeParticipantGroup = activityInfo.getParticipants()[0];
				group = names.contains(maybeParticipantGroup) ? maybeParticipantGroup : UNRESOLVABLE_PARTICIPANT_GROUP;
			}
			break;
		default:
			group = UNRESOLVABLE_PARTICIPANT_GROUP;
		}
		return group;
	}

	private ActivityPerformerExpressionEvaluator evaluatorFor(final String expression) throws CMWorkflowException {
		final TemplateResolver templateResolver = activityPerformerTemplateResolverFactory.create();
		final String resolvedExpression = templateResolver.resolve(expression);

		final ActivityPerformerExpressionEvaluator evaluator = new BshActivityPerformerExpressionEvaluator(
				resolvedExpression);
		final Map<String, Object> rawWorkflowVars = workflowService //
				.getProcessInstanceVariables(processInstance.getProcessInstanceId());
		evaluator.setVariables(rawWorkflowVars);
		return evaluator;
	}

	private static <T> T[] addIfMissing(final T[] original, final T element) {
		final T[] output;
		if (element == null) {
			output = original;
		} else if (contains(original, element)) {
			output = original;
		} else {
			output = add(original, element);
		}
		return output;
	}

	private void removeClosedActivities(final WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		logger.debug(marker, "removing closed activivities");

		logger.debug(marker, "building actual activities list");
		final Set<String> newActivityInstInfoIds = new HashSet<String>(activityInfos.length);
		for (final WSActivityInstInfo ai : activityInfos) {
			logger.debug(marker, "adding activity '{}' to actual list", ai.getActivityInstanceId());
			newActivityInstInfoIds.add(ai.getActivityInstanceId());
		}

		logger.debug(marker, "removing persisted activities not contained in actual activities list");
		for (final String oldActInstId : activityInstanceIds) {
			final boolean contained = newActivityInstInfoIds.contains(oldActInstId);
			logger.debug(marker, "persisted activity '{}' is contained in actual list? {}", oldActInstId, contained);
			if (!contained) {
				removeActivity(oldActInstId);
			}
		}
	}

	public void removeActivity(final String activityInstanceId) throws CMWorkflowException {
		logger.info(marker, "removing persisted activity '{}'", activityInstanceId);
		final int index = indexOf(activityInstanceIds, activityInstanceId);
		logger.debug(marker, "index of '{}' is '{}'", activityInstanceId, index);
		if (index != INDEX_NOT_FOUND) {
			activityInstanceIds = String[].class.cast(remove(activityInstanceIds, index));
			activityDefinitionIds = String[].class.cast(remove(activityDefinitionIds, index));
			currentActivityPerformers = String[].class.cast(remove(currentActivityPerformers, index));

			logger.debug(marker, "new activity instance ids: '{}'", Object.class.cast(activityInstanceIds));
			logger.debug(marker, "new activity definition ids: '{}'", Object.class.cast(activityDefinitionIds));
			logger.debug(marker, "new activity instance performers: '{}'", Object.class.cast(currentActivityPerformers));

			updateCodeWithFirstActivityInfo();
		}
	}

	private void addNewActivities(final WSActivityInstInfo[] activityInfos) throws CMWorkflowException {
		final Set<String> oldActivityInstanceIds = new HashSet<String>();
		for (final String aiid : activityInstanceIds) {
			oldActivityInstanceIds.add(aiid);
		}
		for (final WSActivityInstInfo ai : activityInfos) {
			if (oldActivityInstanceIds.contains(ai.getActivityInstanceId())) {
				continue;
			}
			addActivity(ai);
		}
	}

	private void updateCodeWithFirstActivityInfo() throws CMWorkflowException {
		final Iterable<String> activities = Arrays.asList(activityDefinitionIds);
		if (isEmpty(activities)) {
			code = null;
		} else {
			final String activityDefinitionId = get(activities, 0);
			final CMActivity activity = processDefinitionManager.getActivity(processInstance, activityDefinitionId);
			final String label = defaultString(activity.getDescription());
			if (size(activities) > 1) {
				code = format("%s, ...", label);
			} else {
				code = label;
			}
		}
	}

}
