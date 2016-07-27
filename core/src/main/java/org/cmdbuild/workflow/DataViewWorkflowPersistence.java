package org.cmdbuild.workflow;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.Constants.BASE_PROCESS_CLASS_NAME;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.workflow.ProcessAttributes.FlowStatus;
import static org.cmdbuild.workflow.ProcessAttributes.ProcessInstanceId;
import static org.cmdbuild.workflow.service.WSProcessInstanceState.OPEN;
import static org.cmdbuild.workflow.service.WSProcessInstanceState.SUSPENDED;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataViewCardFetcher;
import org.cmdbuild.workflow.WorkflowUpdateHelper.WorkflowUpdateHelperBuilder;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.user.ForwardingUserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstanceWithPosition;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class DataViewWorkflowPersistence implements WorkflowPersistence {

	private static final Marker marker = MarkerFactory.getMarker(DataViewWorkflowPersistence.class.getName());
	private static final Logger logger = Log.PERSISTENCE;

	public static class DataViewWorkflowPersistenceBuilder implements Builder<DataViewWorkflowPersistence> {

		private PrivilegeContext privilegeContext;
		private OperationUser operationUser;
		private CMDataView dataView;
		private ProcessDefinitionManager processDefinitionManager;
		public LookupHelper lookupHelper;
		private CMWorkflowService workflowService;
		private ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory;

		private DataViewWorkflowPersistenceBuilder() {
			// use factory method
		}

		@Override
		public DataViewWorkflowPersistence build() {
			validate();
			return new DataViewWorkflowPersistence(this);
		}

		private void validate() {
			Validate.notNull(privilegeContext, "invalid privilege context");
			Validate.notNull(operationUser, "invalid operation user");
			Validate.notNull(dataView, "invalid data view");
			Validate.notNull(processDefinitionManager, "invalid process definition manager");
			Validate.notNull(lookupHelper, "invalid lookup helper");
			Validate.notNull(workflowService, "invalid workflow service");
		}

		public DataViewWorkflowPersistenceBuilder withPrivilegeContext(final PrivilegeContext privilegeContext) {
			setPrivilegeContext(privilegeContext);
			return this;
		}

		public void setPrivilegeContext(final PrivilegeContext privilegeContext) {
			this.privilegeContext = privilegeContext;
		}

		public DataViewWorkflowPersistenceBuilder withOperationUser(final OperationUser operationUser) {
			setOperationUser(operationUser);
			return this;
		}

		public void setOperationUser(final OperationUser operationUser) {
			this.operationUser = operationUser;
		}

		public DataViewWorkflowPersistenceBuilder withDataView(final CMDataView dataView) {
			setDataView(dataView);
			return this;
		}

		public void setDataView(final CMDataView dataView) {
			this.dataView = dataView;
		}

		public DataViewWorkflowPersistenceBuilder withProcessDefinitionManager(
				final ProcessDefinitionManager processDefinitionManager) {
			setProcessDefinitionManager(processDefinitionManager);
			return this;
		}

		public void setProcessDefinitionManager(final ProcessDefinitionManager processDefinitionManager) {
			this.processDefinitionManager = processDefinitionManager;
		}

		public DataViewWorkflowPersistenceBuilder withLookupHelper(final LookupHelper lookupHelper) {
			setLookupHelper(lookupHelper);
			return this;
		}

		public void setLookupHelper(final LookupHelper lookupHelper) {
			this.lookupHelper = lookupHelper;
		}

		public DataViewWorkflowPersistenceBuilder withWorkflowService(final CMWorkflowService workflowService) {
			setWorkflowService(workflowService);
			return this;
		}

		public void setWorkflowService(final CMWorkflowService workflowService) {
			this.workflowService = workflowService;
		}

		public DataViewWorkflowPersistenceBuilder withActivityPerformerTemplateResolverFactory(
				final ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory) {
			setActivityPerformerTemplateResolverFactory(activityPerformerTemplateResolverFactory);
			return this;
		}

		public void setActivityPerformerTemplateResolverFactory(
				final ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory) {
			this.activityPerformerTemplateResolverFactory = activityPerformerTemplateResolverFactory;
		}

	}

	public static DataViewWorkflowPersistenceBuilder newInstance() {
		return new DataViewWorkflowPersistenceBuilder();
	}

	private static class UserProcessInstanceWithPositionImpl extends ForwardingUserProcessInstance implements
			UserProcessInstanceWithPosition {

		private final UserProcessInstance delegate;
		private final Long position;

		public UserProcessInstanceWithPositionImpl(final UserProcessInstance delegate, final Long position) {
			this.delegate = delegate;
			this.position = position;
		}

		@Override
		protected UserProcessInstance delegate() {
			return delegate;
		}

		@Override
		public Long getPosition() {
			return position;
		}

	}

	private static final Iterable<Long> NO_VALUE = emptyList();

	private final PrivilegeContext privilegeContext;
	private final OperationUser operationUser;
	private final CMDataView dataView;
	private final ProcessDefinitionManager processDefinitionManager;
	private final LookupHelper lookupHelper;
	private final CMWorkflowService workflowService;
	private final ActivityPerformerTemplateResolverFactory activityPerformerTemplateResolverFactory;

	private DataViewWorkflowPersistence(final DataViewWorkflowPersistenceBuilder builder) {
		this.privilegeContext = builder.privilegeContext;
		this.operationUser = builder.operationUser;
		this.dataView = builder.dataView;
		this.processDefinitionManager = builder.processDefinitionManager;
		this.lookupHelper = builder.lookupHelper;
		this.workflowService = builder.workflowService;
		this.activityPerformerTemplateResolverFactory = builder.activityPerformerTemplateResolverFactory;
	}

	@Override
	public Iterable<UserProcessClass> getAllProcessClasses() {
		logger.info(marker, "getting all process classes");
		return from(dataView.findClasses()) //
				.filter(activityClasses()) //
				.filter(grantedClasses()) //
				.transform(toUserProcessClass());
	}

	private Predicate<CMClass> activityClasses() {
		logger.debug(marker, "filtering activity classes");
		return new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return dataView.getActivityClass().isAncestorOf(input);
			}
		};
	}

	private Predicate<CMClass> grantedClasses() {
		logger.debug(marker, "filtering activity granted classes");
		return new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return input.getName().equals(BASE_PROCESS_CLASS_NAME) || //
						privilegeContext.hasAdministratorPrivileges() || //
						privilegeContext.hasReadAccess(input);
			}
		};
	}

	private Function<CMClass, UserProcessClass> toUserProcessClass() {
		logger.debug(marker, "transforming from '{}' to '{}'", CMClass.class, UserProcessClass.class);
		return new Function<CMClass, UserProcessClass>() {
			@Override
			public UserProcessClass apply(final CMClass input) {
				return wrap(input);
			}
		};
	}

	@Override
	public UserProcessClass findProcessClass(final Long id) {
		logger.info(marker, "getting process class with id '{}'", id);
		return from(getAllProcessClasses()) //
				.filter(processClassWithId(id)) //
				.get(0);
	}

	private Predicate<UserProcessClass> processClassWithId(final Long id) {
		logger.debug(marker, "filtering process classes with id '{}'", id);
		final Predicate<UserProcessClass> predicate = new Predicate<UserProcessClass>() {
			@Override
			public boolean apply(final UserProcessClass input) {
				return input.getId().equals(id);
			}
		};
		return predicate;
	}

	@Override
	public UserProcessClass findProcessClass(final String name) {
		logger.info(marker, "getting process class with name '{}'", name);
		return from(getAllProcessClasses()) //
				.filter(processClassWithName(name)) //
				.get(0);
	}

	private Predicate<UserProcessClass> processClassWithName(final String name) {
		logger.debug(marker, "filtering process classes with name '{}'", name);
		return new Predicate<UserProcessClass>() {
			@Override
			public boolean apply(final UserProcessClass input) {
				return input.getName().equals(name);
			}
		};
	}

	@Override
	public UserProcessInstance createProcessInstance(final WSProcessInstInfo processInstInfo,
			final ProcessData processData) throws CMWorkflowException {
		logger.info(marker, "creating process instance of '{}' '{}'", //
				processInstInfo.getPackageId(), //
				processInstInfo.getProcessDefinitionId());
		final String processClassName = processDefinitionManager.getProcessClassName(processInstInfo
				.getProcessDefinitionId());
		final CMProcessClass processClass = wrap(dataView.findClass(processClassName));
		return createProcessInstance(processClass, processInstInfo, processData);
	}

	@Override
	public UserProcessInstance createProcessInstance(final CMProcessClass processClass,
			final WSProcessInstInfo processInstInfo, final ProcessData processData) throws CMWorkflowException {
		logger.info(marker, "creating process instance for class '{}'", processClass);
		final CMCardDefinition cardDefinition = dataView.createCardFor(processClass);
		final CMCard createdCard = newWorkflowUpdateHelper(processClass, cardDefinition) //
				.withProcessInstInfo(processInstInfo) //
				.build() //
				.set(processData) //
				.save();
		return wrap(createdCard);
	}

	@Override
	public UserProcessInstance updateProcessInstance(final CMProcessInstance processInstance,
			final ProcessData processData) throws CMWorkflowException {
		logger.info(marker, "updating process instance for class '{}' and id '{}'", //
				processInstance.getType().getName(), processInstance.getCardId());
		final CMCard card = findProcessCard(processInstance);
		final CMCardDefinition cardDefinition = dataView.update(card);
		final CMCard updatedCard = newWorkflowUpdateHelper(card.getType(), cardDefinition) //
				.withProcessDefinitionManager(processDefinitionManager) //
				.withCard(card) //
				.withProcessInstance(processInstance) //
				.build() //
				.set(processData) //
				.save();
		return wrap(updatedCard);
	}

	private WorkflowUpdateHelperBuilder newWorkflowUpdateHelper(final CMClass processClass,
			final CMCardDefinition cardDefinition) {
		return WorkflowUpdateHelper.newInstance(operationUser, processClass, cardDefinition) //
				.withWorkflowService(workflowService) //
				.withLookupHelper(lookupHelper) //
				.withActivityPerformerTemplateResolverFactory(activityPerformerTemplateResolverFactory);
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessInstance processInstance) {
		logger.info(marker, "getting process instance for class '{}' and card id '{}'", //
				processInstance.getType(), processInstance.getCardId());
		return wrap(findProcessCard(processInstance));
	}

	@Override
	public UserProcessInstance findProcessInstance(final WSProcessInstInfo processInstInfo) throws CMWorkflowException {
		final String processClassName = processDefinitionManager.getProcessClassName(processInstInfo
				.getProcessDefinitionId());
		final CMProcessClass processClass = wrap(dataView.findClass(processClassName));
		return wrap(findProcessCard(processClass, processInstInfo.getProcessInstanceId()));
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessClass processClass, final Long cardId) {
		logger.info(marker, "getting process instance for class '{}' and card id '{}'", processClass, cardId);
		return wrap(findProcessCard(processClass, cardId));
	}

	@Override
	public Iterable<? extends UserProcessInstance> queryOpenAndSuspended(final UserProcessClass processClass) {
		logger.info(marker, "getting all opened and suspended process instances for class '{}'", processClass);
		final Optional<Lookup> open = lookupHelper.lookupForState(OPEN);
		final Optional<Lookup> suspended = lookupHelper.lookupForState(SUSPENDED);
		final Object[] ids = new Long[] { open.isPresent() ? open.get().getId() : null,
				suspended.isPresent() ? suspended.get().getId() : null };
		logger.debug(marker, "lookup ids are '{}'", ids);
		return from(dataView.select(anyAttribute(processClass)) //
				.from(processClass) //
				.where(condition(attribute(processClass, FlowStatus.dbColumnName()), in(ids))) //
				.run() //
		).transform(toProcessInstanceOf(processClass));
	}

	private Function<CMQueryRow, UserProcessInstance> toProcessInstanceOf(final CMProcessClass processClass) {
		logger.debug(marker, "transforming from '{}' to '{}'", CMQueryRow.class, UserProcessInstance.class);
		return new Function<CMQueryRow, UserProcessInstance>() {
			@Override
			public UserProcessInstance apply(final CMQueryRow input) {
				final CMCard card = input.getCard(processClass);
				return wrap(card);
			}
		};
	}

	@Override
	public PagedElements<UserProcessInstance> query(final String className, final QueryOptions queryOptions) {
		final PagedElements<CMCard> cards = DataViewCardFetcher.newInstance() //
				.withDataView(dataView) //
				.withClassName(className) //
				.withQueryOptions(queryOptions) //
				.build() //
				.fetch();
		return new PagedElements<UserProcessInstance>(from(cards) //
				.transform(toUserProcessInstance()), cards.totalSize());
	}

	@Override
	public PagedElements<UserProcessInstanceWithPosition> queryWithPosition(final String className,
			final QueryOptions queryOptions, final Iterable<Long> cardId) {
		final UserProcessClass target = findProcessClass(className);
		final Iterable<Long> _cardId = defaultIfNull(cardId, NO_VALUE);
		final Long id = isEmpty(_cardId) ? Long.valueOf(0L) : get(_cardId, 0);
		final PagedElements<CMQueryRow> rows = DataViewCardFetcher.newInstance() //
				.withDataView(dataView) //
				.withClassName(className) //
				.withQueryOptions(queryOptions) //
				.build() //
				.fetchNumbered(condition(attribute(target, ID_ATTRIBUTE), eq(id)));
		return new PagedElements<UserProcessInstanceWithPosition>( //
				from(rows) //
						.transform(new Function<CMQueryRow, UserProcessInstanceWithPosition>() {

							@Override
							public UserProcessInstanceWithPosition apply(final CMQueryRow input) {
								final CMCard card = input.getCard(target);
								final UserProcessInstance userProcessInstance = toUserProcessInstance().apply(card);
								return new UserProcessInstanceWithPositionImpl(userProcessInstance,
										input.getNumber() - 1);
							}

						}), //
				rows.totalSize());
	}

	private Function<CMCard, UserProcessInstance> toUserProcessInstance() {
		return new Function<CMCard, UserProcessInstance>() {
			@Override
			public UserProcessInstance apply(final CMCard input) {
				return wrap(input);
			}
		};
	}

	private UserProcessClass wrap(final CMClass clazz) {
		logger.debug(marker, "wrapping '{}' into '{}'", CMClass.class, UserProcessClass.class);
		return new ProcessClassImpl(operationUser, privilegeContext, clazz, processDefinitionManager);
	}

	private UserProcessInstance wrap(final CMCard card) {
		logger.debug(marker, "wrapping '{}' into '{}'", CMCard.class, UserProcessInstance.class);
		return ProcessInstanceImpl.newInstance() //
				.withOperationUser(operationUser) //
				.withPrivilegeContext(privilegeContext) //
				.withProcessDefinitionManager(processDefinitionManager) //
				.withLookupHelper(lookupHelper) //
				.withCard(card) //
				.build();
	}

	private CMCard findProcessCard(final CMProcessInstance processInstance) {
		return findProcessCard(processInstance.getType(), processInstance.getCardId());
	}

	private CMCard findProcessCard(final CMProcessClass processClass, final Long cardId) {
		logger.debug(marker, "getting process card for class '{}' and card id '{}'", processClass, cardId);
		return dataView.select(anyAttribute(processClass)) //
				.from(processClass) //
				.where(condition(attribute(processClass, ID_ATTRIBUTE), eq(cardId))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow() //
				.getCard(processClass);
	}

	private CMCard findProcessCard(final CMProcessClass processClass, final String processInstanceId) {
		logger.debug(marker, "getting process card for class '{}' and process instance id '{}'", processClass,
				processInstanceId);
		return dataView.select(anyAttribute(processClass)) //
				.from(processClass) //
				.where(condition(attribute(processClass, ProcessInstanceId.dbColumnName()), eq(processInstanceId))) //
				.limit(1) //
				.skipDefaultOrdering() //
				.run() //
				.getOnlyRow() //
				.getCard(processClass);
	}

}
