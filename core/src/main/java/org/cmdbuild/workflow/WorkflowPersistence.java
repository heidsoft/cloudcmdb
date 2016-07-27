package org.cmdbuild.workflow;

import java.util.Map;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserProcessInstanceWithPosition;

import com.google.common.collect.ForwardingObject;

public interface WorkflowPersistence {

	interface ProcessData {

		WSProcessInstanceState NO_STATE = null;
		WSProcessInstInfo NO_PROCESS_INSTANCE_INFO = null;
		Map<String, ?> NO_VALUES = null;
		WSActivityInstInfo[] NO_ACTIVITIES = null;

		WSProcessInstanceState state();

		WSProcessInstInfo processInstanceInfo();

		Map<String, ?> values();

		WSActivityInstInfo[] addActivities();

		WSActivityInstInfo[] activities();

	}

	class NoProcessData implements ProcessData {

		private static final NoProcessData INSTANCE = new NoProcessData();

		public static NoProcessData getInstance() {
			return INSTANCE;
		}

		private NoProcessData() {
			// use factory method
		}

		@Override
		public WSProcessInstanceState state() {
			return NO_STATE;
		}

		@Override
		public WSProcessInstInfo processInstanceInfo() {
			return NO_PROCESS_INSTANCE_INFO;
		}

		@Override
		public Map<String, ?> values() {
			return NO_VALUES;
		}

		@Override
		public WSActivityInstInfo[] addActivities() {
			return NO_ACTIVITIES;
		}

		@Override
		public WSActivityInstInfo[] activities() {
			return NO_ACTIVITIES;
		}

	}

	abstract class ForwardingProcessData extends ForwardingObject implements ProcessData {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingProcessData() {
		}

		@Override
		protected abstract ProcessData delegate();

		@Override
		public WSProcessInstanceState state() {
			return delegate().state();
		}

		@Override
		public WSProcessInstInfo processInstanceInfo() {
			return delegate().processInstanceInfo();
		}

		@Override
		public Map<String, ?> values() {
			return delegate().values();
		}

		@Override
		public WSActivityInstInfo[] addActivities() {
			return delegate().addActivities();
		}

		@Override
		public WSActivityInstInfo[] activities() {
			return delegate().activities();
		}

	}

	Iterable<UserProcessClass> getAllProcessClasses();

	UserProcessClass findProcessClass(Long id);

	UserProcessClass findProcessClass(String name);

	UserProcessInstance createProcessInstance(WSProcessInstInfo processInstInfo, ProcessData processData)
			throws CMWorkflowException;

	UserProcessInstance createProcessInstance(CMProcessClass processClass, WSProcessInstInfo processInstInfo,
			ProcessData processData) throws CMWorkflowException;

	UserProcessInstance updateProcessInstance(CMProcessInstance processInstance, ProcessData processData)
			throws CMWorkflowException;

	UserProcessInstance findProcessInstance(WSProcessInstInfo processInstInfo) throws CMWorkflowException;

	UserProcessInstance findProcessInstance(CMProcessInstance processInstance);

	UserProcessInstance findProcessInstance(CMProcessClass processClass, Long cardId);

	Iterable<? extends UserProcessInstance> queryOpenAndSuspended(UserProcessClass processClass);

	PagedElements<UserProcessInstance> query(String className, QueryOptions queryOptions);

	PagedElements<UserProcessInstanceWithPosition> queryWithPosition(String className, QueryOptions queryOptions,
			Iterable<Long> cardId);

}
