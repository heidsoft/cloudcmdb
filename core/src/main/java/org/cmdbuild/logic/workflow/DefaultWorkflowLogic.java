package org.cmdbuild.logic.workflow;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.filter;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.cmdbuild.logic.PrivilegeUtils.assure;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataSource;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.Constants;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.logic.data.LockLogic;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.ProcessEntryFiller;
import org.cmdbuild.logic.data.access.resolver.AbstractSerializer;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.QueryableUserWorkflowEngine;
import org.cmdbuild.workflow.user.ForwardingUserProcessInstance;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstanceWithPosition;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import net.jcip.annotations.NotThreadSafe;

class DefaultWorkflowLogic implements WorkflowLogic {

	private static class UserProcessInstanceWithPositionImpl extends ForwardingUserProcessInstance
			implements UserProcessInstanceWithPosition {

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

	private static final UserActivityInstance NULL_ACTIVITY_INSTANCE = null;

	private static final String BEGIN_DATE_ATTRIBUTE = "beginDate";
	private static final String SKETCH_PATH = "images" + File.separator + "workflow" + File.separator;

	private final PrivilegeContext privilegeContext;
	private final QueryableUserWorkflowEngine workflowEngine;
	private final CMDataView dataView;
	private final WorkflowConfiguration configuration;
	private final FilesStore filesStore;
	private final LockLogic lockLogic;

	public DefaultWorkflowLogic( //
			final PrivilegeContext privilegeContext, //
			final QueryableUserWorkflowEngine workflowEngine, //
			final CMDataView dataView, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore, //
			final LockLogic lockLogic //
	) {
		this.privilegeContext = privilegeContext;
		this.workflowEngine = workflowEngine;
		this.dataView = dataView;
		this.configuration = configuration;
		this.filesStore = filesStore;
		this.lockLogic = lockLogic;
	}

	/*
	 * Ungliness to be used in old code
	 */

	@Override
	public boolean isProcessUsable(final String className) {
		return isWorkflowEnabled() && workflowEngine.findProcessClassByName(className).isUsable();
	}

	@Override
	public boolean isWorkflowEnabled() {
		return configuration.isEnabled();
	}

	@Override
	public PagedElements<UserProcessInstance> query(final String className, final QueryOptions queryOptions) {
		return query(dataView.findClass(className), queryOptions);
	}

	@Override
	public PagedElements<UserProcessInstance> query(final CMClass processClass, final QueryOptions queryOptions) {
		final PagedElements<UserProcessInstance> fetchedProcesses = workflowEngine.query(processClass.getName(),
				queryOptions);
		final Iterable<UserProcessInstance> processes = resolve(fetchedProcesses);
		return new PagedElements<UserProcessInstance>(processes, fetchedProcesses.totalSize());
	}

	@Override
	public PagedElements<UserProcessInstanceWithPosition> queryWithPosition(final String className,
			final QueryOptions queryOptions, final Iterable<Long> cardId) {
		final PagedElements<UserProcessInstanceWithPosition> fetchedProcesses = workflowEngine
				.queryWithPosition(className, queryOptions, cardId);
		return new PagedElements<UserProcessInstanceWithPosition>( //
				from(fetchedProcesses) //
						.transform(new Function<UserProcessInstanceWithPosition, UserProcessInstanceWithPosition>() {

							@Override
							public UserProcessInstanceWithPosition apply(final UserProcessInstanceWithPosition input) {
								final UserProcessInstance resolved = from(resolve(asList(input))).get(0);
								return new UserProcessInstanceWithPositionImpl(resolved, input.getPosition());
							}

						}), //
				fetchedProcesses.totalSize());
	}

	private Iterable<UserProcessInstance> resolve(final Iterable<? extends UserProcessInstance> fetchedProcesses) {
		return ForeignReferenceResolver.<UserProcessInstance> newInstance() //
				.withEntries(fetchedProcesses) //
				.withEntryFiller(new ProcessEntryFiller()) //
				.withSerializer(new AbstractSerializer<UserProcessInstance>() {
				}) //
				.build() //
				.resolve();
	}

	@Override
	public Iterable<UserProcessClass> findAllProcessClasses() {
		return workflowEngine.findAllProcessClasses();
	}

	@Override
	public Iterable<UserProcessClass> findActiveProcessClasses() {
		final Iterable<UserProcessClass> allClasses;
		if (configuration.isEnabled()) {
			allClasses = workflowEngine.findProcessClasses();
		} else {
			allClasses = Collections.emptyList();
		}
		return allClasses;
	}

	@Override
	public Iterable<UserProcessClass> findProcessClasses(final boolean activeOnly) {
		final Iterable<UserProcessClass> processClasses;
		if (activeOnly) {
			processClasses = filter(findActiveProcessClasses(), processesWithXpdlAssociated());
		} else {
			processClasses = findAllProcessClasses();
		}
		return processClasses;
	}

	private Predicate<UserProcessClass> processesWithXpdlAssociated() {
		final Predicate<UserProcessClass> processesWithXpdlAssociated = new Predicate<UserProcessClass>() {
			@Override
			public boolean apply(final UserProcessClass input) {
				boolean apply = false;
				try {
					apply = input.getName().equals(Constants.BASE_PROCESS_CLASS_NAME) //
							|| input.isSuperclass() //
							|| input.getDefinitionVersions().length > 0;
				} catch (final CMWorkflowException e) {
				}
				return apply;
			}
		};
		return processesWithXpdlAssociated;
	}

	@Override
	public UserProcessClass findProcessClass(final Long classId) {
		final Optional<UserProcessClass> optional = from(findAllProcessClasses()) //
				.filter(new Predicate<UserProcessClass>() {
					@Override
					public boolean apply(final UserProcessClass input) {
						return input.getId().equals(classId);
					}
				}).first();
		return optional.isPresent() ? optional.get() : null;
	}

	@Override
	public UserProcessClass findProcessClass(final String className) {
		final Optional<UserProcessClass> optional = from(findAllProcessClasses()) //
				.filter(new Predicate<UserProcessClass>() {
					@Override
					public boolean apply(final UserProcessClass input) {
						return input.getName().equals(className);
					}
				}).first();
		return optional.isPresent() ? optional.get() : null;
	}

	/*
	 * Management
	 */

	/**
	 * Returns the process start activity for the current user.
	 *
	 * @param process
	 *            class name
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	@Override
	public CMActivity getStartActivity(final String processClassName) throws CMWorkflowException {
		return workflowEngine.findProcessClassByName(processClassName).getStartActivity();
	}

	@Override
	public CMActivity getStartActivityOrDie( //
			final String processClassName //
	) throws CMWorkflowException, CMDBWorkflowException {

		final UserProcessClass theProess = workflowEngine.findProcessClassByName(processClassName);
		final CMActivity theActivity = theProess.getStartActivity();
		if (theActivity == null) {
			throw WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.createException(theProess.getDescription());
		}

		return theActivity;
	}

	@Override
	public CMActivity getStartActivityOrDie( //
			final Long processClassId //
	) throws CMWorkflowException, CMDBWorkflowException {

		final UserProcessClass theProess = workflowEngine.findProcessClassById(processClassId);
		final CMActivity theActivity = theProess.getStartActivity();
		if (theActivity == null) {
			throw WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.createException(theProess.getDescription());
		}

		return theActivity;
	}

	@Override
	public UserProcessInstance getProcessInstance(final String processClassName, final Long cardId) {
		logger.debug("getting process instance for class name '{}' and card id '{}'", processClassName, cardId);
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		return workflowEngine.findProcessInstance(processClass, cardId);
	}

	@Override
	public UserProcessInstance getProcessInstance(final Long processClassId, final Long cardId) {
		logger.debug("getting process instance for class id '{}' and card id '{}'", processClassId, cardId);
		final CMProcessClass processClass = workflowEngine.findProcessClassById(processClassId);
		return workflowEngine.findProcessInstance(processClass, cardId);
	}

	@Override
	public UserActivityInstance getActivityInstance(final String processClassName, final Long processCardId,
			final String activityInstanceId) {
		logger.debug("getting activity instance '{}' for process '{}'", activityInstanceId, processClassName);
		final UserProcessInstance processInstance = getProcessInstance(processClassName, processCardId);
		return getActivityInstance(processInstance, activityInstanceId);
	}

	@Override
	public UserActivityInstance getActivityInstance(final Long processClassId, final Long processCardId,
			final String activityInstanceId) {
		logger.debug("getting activity instance '{}' for process '{}'", activityInstanceId, processClassId);
		final UserProcessInstance processInstance = getProcessInstance(processClassId, processCardId);
		return getActivityInstance(processInstance, activityInstanceId);
	}

	private UserActivityInstance getActivityInstance(final UserProcessInstance processInstance,
			final String activityInstanceId) {
		for (final UserActivityInstance activityInstance : processInstance.getActivities()) {
			if (activityInstance.getId().equals(activityInstanceId)) {
				return activityInstance;
			}
		}
		logger.error("activity instance '{}' not found", activityInstanceId);
		return NULL_ACTIVITY_INSTANCE;
	}

	/**
	 * Retrieve the processInstance and check if the given date is the same of
	 * the process begin date in this case, we assume that the process is
	 * updated
	 *
	 * @param processClassName
	 * @param processInstanceId
	 * @param givenBeginDate
	 * @return
	 */
	@Override
	public boolean isProcessUpdated( //
			final String processClassName, //
			final Long processInstanceId, //
			final DateTime givenBeginDate //
	) {

		final CMProcessInstance processInstance = getProcessInstance(processClassName, processInstanceId);
		return isProcessUpdated(processInstance, givenBeginDate);
	}

	private boolean isProcessUpdated(final CMProcessInstance processInstance, final DateTime givenBeginDate) {
		final DateTime currentBeginDate = processInstance.getBeginDate();
		return givenBeginDate.equals(currentBeginDate);
	}

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 *
	 * @param processClassName
	 *            process class name
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 *
	 * @return the created process instance
	 *
	 * @throws CMWorkflowException
	 */
	@Override
	public UserProcessInstance startProcess(final String processClassName, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		return startProcess(processClass, vars, widgetSubmission, advance);
	}

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 *
	 * @param processClassId
	 *            process class id
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 *
	 * @return the created process instance
	 *
	 * @throws CMWorkflowException
	 */
	@Override
	public UserProcessInstance startProcess(final Long processClassId, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final CMProcessClass proc = workflowEngine.findProcessClassById(processClassId);
		return startProcess(proc, vars, widgetSubmission, advance);
	}

	private UserProcessInstance startProcess(final CMProcessClass process, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final UserProcessInstance procInst = workflowEngine.startProcess(process, vars);
		final List<UserActivityInstance> activities = procInst.getActivities();
		if (activities.size() != 1) {
			throw new UnsupportedOperationException(
					format("Not just one activity to advance! (%d activities)", activities.size()));
		}
		final UserActivityInstance firstActInst = activities.get(0);
		final Map<String, Object> mergedVars = mergeVars( //
				from(procInst.getValues()) //
						.filter(new ValuesFilter(process)), //
				vars);
		workflowEngine.updateActivity(firstActInst, mergedVars, widgetSubmission);
		final UserProcessInstance output;
		if (advance) {
			output = workflowEngine.advanceActivity(firstActInst);
		} else {
			output = firstActInst.getProcessInstance();
		}
		return output;
	}

	/**
	 * Only non-null attributes are accepted with the following exceptions:
	 * date, datetime and time attributes are always set even if null (needed
	 * for initialize correctly workflow instance variables).
	 */
	@NotThreadSafe
	private static class ValuesFilter extends ForwardingAttributeTypeVisitor
			implements Predicate<Entry<String, Object>> {

		private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

		@Override
		protected CMAttributeTypeVisitor delegate() {
			return DELEGATE;
		}

		private final CMProcessClass process;
		private String name;
		private Object value;
		private boolean applies;

		public ValuesFilter(final CMProcessClass process) {
			this.process = process;
		}

		@Override
		public boolean apply(final Entry<String, Object> input) {
			name = input.getKey();
			value = input.getValue();
			applies = (value != null);
			final CMAttribute attribute = process.getAttribute(name);
			if (attribute != null) {
				attribute.getType().accept(this);
			} else {
				applies = false;
			}
			return applies;
		}

		@Override
		public void visit(final DateAttributeType attributeType) {
			applies = true;
		}

		@Override
		public void visit(final DateTimeAttributeType attributeType) {
			applies = true;
		}

		@Override
		public void visit(final LookupAttributeType attributeType) {
			if (value instanceof IdAndDescription) {
				applies = IdAndDescription.class.cast(value).getId() != null;
			}
		}

		@Override
		public void visit(final ReferenceAttributeType attributeType) {
			if (value instanceof IdAndDescription) {
				applies = IdAndDescription.class.cast(value).getId() != null;
			}
		}

		@Override
		public void visit(final TimeAttributeType attributeType) {
			applies = true;
		}

	}

	/**
	 * This awful hack is needed because SOMEONE decided that it was a good idea
	 * to specify default attributes in the database, so old clients did it and
	 * now we have to deal with it.
	 *
	 * @param databaseValues
	 *            values as they are in the newly created database row
	 * @param entrySet
	 *            values submitted in the form
	 *
	 * @return database values overridden by the submitted ones
	 */
	private Map<String, Object> mergeVars(final Iterable<Entry<String, Object>> databaseValues,
			final Map<String, ?> submittedValues) {
		final Map<String, Object> mergedValues = new HashMap<String, Object>();
		for (final Entry<String, ?> e : databaseValues) {
			mergedValues.put(e.getKey(), e.getValue());
		}
		for (final Entry<String, ?> e : submittedValues.entrySet()) {
			mergedValues.put(e.getKey(), e.getValue());
		}
		return mergedValues;
	}

	@Override
	public UserProcessInstance updateProcess(final String processClassName, final Long processCardId,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		return updateProcess( //
				processClass.getId(), //
				processCardId, //
				activityInstanceId, //
				vars, //
				widgetSubmission, //
				advance);
	}

	@Override
	public UserProcessInstance updateProcess(final Long processClassId, final Long processCardId,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		lockLogic.checkActivityLockedbyUser(processCardId, activityInstanceId);

		final CMProcessClass processClass = workflowEngine.findProcessClassById(processClassId);
		final UserProcessInstance processInstance = workflowEngine.findProcessInstance(processClass, processCardId);

		/*
		 * check if the given begin date is the same of the stored process, to
		 * be sure to deny the update of old versions
		 */
		if (vars.containsKey(BEGIN_DATE_ATTRIBUTE)) {
			final Long givenBeginDateAsLong = (Long) vars.get(BEGIN_DATE_ATTRIBUTE);
			final DateTime givenBeginDate = new DateTime(givenBeginDateAsLong);
			if (!isProcessUpdated(processInstance, givenBeginDate)) {
				throw ConsistencyExceptionType.OUT_OF_DATE_PROCESS.createException();
			}

			/*
			 * must be removed to not use it as a custom attribute
			 */
			vars.remove(BEGIN_DATE_ATTRIBUTE);
		}

		updateProcess( //
				processInstance, //
				activityInstanceId, //
				vars, //
				widgetSubmission, //
				advance);

		lockLogic.unlockActivity(processCardId, activityInstanceId);

		/*
		 * retrieve again the processInstance because the updateProcess return
		 * the old processInstance, not the updated
		 */
		return workflowEngine.findProcessInstance(processClass, processCardId);
	}

	private UserProcessInstance updateProcess(final UserProcessInstance processInstance,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final UserActivityInstance activityInstance = processInstance.getActivityInstance(activityInstanceId);
		workflowEngine.updateActivity(activityInstance, vars, widgetSubmission);
		final UserProcessInstance output;
		if (advance) {
			output = workflowEngine.advanceActivity(activityInstance);
		} else {
			output = activityInstance.getProcessInstance();
		}
		return output;
	}

	@Override
	public void suspendProcess(final String processClassName, final Long processCardId) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = workflowEngine.findProcessInstance(processClass, processCardId);
		workflowEngine.suspendProcessInstance(processInstance);
	}

	@Override
	public void resumeProcess(final String processClassName, final Long processCardId) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = workflowEngine.findProcessInstance(processClass, processCardId);
		workflowEngine.resumeProcessInstance(processInstance);
	}

	/*
	 * Administration
	 */

	@Override
	public void sync() throws CMWorkflowException {
		assure(privilegeContext.hasAdministratorPrivileges());
		workflowEngine.sync();
	}

	@Override
	public DataSource getProcessDefinitionTemplate(final Long processClassId) throws CMWorkflowException {
		return workflowEngine.findProcessClassById(processClassId).getDefinitionTemplate();
	}

	@Override
	public String[] getProcessDefinitionVersions(final Long processClassId) throws CMWorkflowException {
		return workflowEngine.findProcessClassById(processClassId).getDefinitionVersions();
	}

	@Override
	public DataSource getProcessDefinition(final Long processClassId, final String version) throws CMWorkflowException {
		return workflowEngine.findProcessClassById(processClassId).getDefinition(version);
	}

	@Override
	public void updateProcessDefinition(final Long processClassId, final DataSource xpdlFile)
			throws CMWorkflowException {
		workflowEngine.findProcessClassById(processClassId).updateDefinition(xpdlFile);
	}

	/*
	 * It's WRONG to display the latest sketch for every process
	 */

	@Override
	public void removeSketch(final Long processClassId) {
		final CMProcessClass process = workflowEngine.findProcessClassById(processClassId);
		final String filterPattern = process.getName() + ".*";
		final String[] processImages = filesStore.list(SKETCH_PATH, filterPattern);
		if (processImages.length > 0) {
			filesStore.remove(SKETCH_PATH + processImages[0]);
		}
	}

	@Override
	public void addSketch(final Long processClassId, final DataSource ds) throws IOException {
		final CMProcessClass process = workflowEngine.findProcessClassById(processClassId);
		final String relativeUploadPath = SKETCH_PATH + process.getName() + filesStore.getExtension(ds.getName());
		filesStore.save(ds.getInputStream(), relativeUploadPath);
	}

	@Override
	public void abortProcess(final String processClassName, final long processCardId) throws CMWorkflowException {
		logger.info("aborting process with id '{}' for class '{}'", processCardId, processClassName);
		if (processCardId < 0) {
			logger.error("invalid card id '{}'", processCardId);
			throw WorkflowExceptionType.WF_CANNOT_ABORT_PROCESS.createException();
		}
		final CMProcessClass process = workflowEngine.findProcessClassByName(processClassName);
		final UserProcessInstance pi = workflowEngine.findProcessInstance(process, processCardId);
		workflowEngine.abortProcessInstance(pi);

	}

	@Override
	public void abortProcess(final Long processClassId, final long processCardId) throws CMWorkflowException {
		logger.info("aborting process with id '{}' for class '{}'", processCardId, processClassId);
		lockLogic.checkNotLockedInstance(processCardId);
		final CMProcessClass processClass = workflowEngine.findProcessClassById(processClassId);
		abortProcess(processClass.getName(), processCardId);
	}

}
