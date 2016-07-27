package org.cmdbuild.logic.taskmanager.store;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.cmdbuild.data.store.task.AsynchronousEventTaskDefinition;
import org.cmdbuild.data.store.task.ConnectorTaskDefinition;
import org.cmdbuild.data.store.task.GenericTaskDefinition;
import org.cmdbuild.data.store.task.ReadEmailTaskDefinition;
import org.cmdbuild.data.store.task.StartWorkflowTaskDefinition;
import org.cmdbuild.data.store.task.SynchronousEventTaskDefinition;

public class ParameterNames {

	/**
	 * Container for all {@link AsynchronousEventTaskDefinition} parameter
	 * names.
	 */
	public static class AsynchronousEvent {

		private AsynchronousEvent() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = EMPTY;

		private static final String FILTER_PREFIX = ALL_PREFIX + "filter.";
		public static final String FILTER_CLASSNAME = FILTER_PREFIX + "classname";
		public static final String FILTER_CARDS = FILTER_PREFIX + "cards";

		private static final String ACTION_PREFIX = ALL_PREFIX + "action.";

		private static final String EMAIL_PREFIX = ACTION_PREFIX + "email.";
		public static final String EMAIL_ACTIVE = EMAIL_PREFIX + "active";
		public static final String EMAIL_ACCOUNT = EMAIL_PREFIX + "account";
		public static final String EMAIL_TEMPLATE = EMAIL_PREFIX + "template";

	}

	/**
	 * Container for all {@link ConnectorTaskDefinition} parameter names.
	 */
	public static class Connector {

		private Connector() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = EMPTY;

		private static final String DATA_SOURCE_PREFIX = ALL_PREFIX + "datasource.";
		public static final String DATA_SOURCE_TYPE = DATA_SOURCE_PREFIX + "type";
		public static final String DATA_SOURCE_CONFIGURATION = DATA_SOURCE_PREFIX + "configuration";

		private static final String SQL_PREFIX = EMPTY;
		public static final String SQL_TYPE = SQL_PREFIX + "type";
		public static final String SQL_HOSTNAME = SQL_PREFIX + "hostname";
		public static final String SQL_PORT = SQL_PREFIX + "port";
		public static final String SQL_DATABASE = SQL_PREFIX + "database";
		public static final String SQL_INSTANCE = SQL_PREFIX + "instance";
		public static final String SQL_USERNAME = SQL_PREFIX + "username";
		public static final String SQL_PASSWORD = SQL_PREFIX + "password";
		public static final String SQL_FILTER = SQL_PREFIX + "filter";

		private static final String NOTIFICATION_PREFIX = ALL_PREFIX + "notification.";
		public static final String NOTIFICATION_ACTIVE = NOTIFICATION_PREFIX + "active";
		public static final String NOTIFICATION_ACCOUNT = NOTIFICATION_PREFIX + "account";
		private static final String NOTIFICATION_TEMPLATE = NOTIFICATION_PREFIX + "template.";
		public static final String NOTIFICATION_ERROR_TEMPLATE = NOTIFICATION_TEMPLATE + "error";

		private static final String MAPPING_PREFIX = ALL_PREFIX + "mapping.";
		public static final String MAPPING_TYPES = MAPPING_PREFIX + "types";
		public static final String MAPPING_ATTRIBUTES = MAPPING_PREFIX + "attributes";

		static final String MAPPING_SEPARATOR = ",";

	}

	/**
	 * Container for all {@link GenericTaskDefinition} parameter names.
	 */
	public static class Generic {

		private Generic() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = EMPTY;

		public static final String CONTEXT_PREFIX = ALL_PREFIX + "context.";

		public static String context(final String context, final String key) {
			return format("%s%s.%s", CONTEXT_PREFIX, context, key);
		}

		private static final String EMAIL_PREFIX = ALL_PREFIX + "email.";
		public static final String EMAIL_ACTIVE = EMAIL_PREFIX + "active";
		public static final String EMAIL_TEMPLATE = EMAIL_PREFIX + "template";
		public static final String EMAIL_ACCOUNT = EMAIL_PREFIX + "account";

		private static final String EMAIL_ATTACHMENT_PREFIX = EMAIL_PREFIX + "attachments.";
		private static final String EMAIL_ATTACHMENT_REPORT_PREFIX = EMAIL_ATTACHMENT_PREFIX + "report.";

		public static final String REPORT_ACTIVE = EMAIL_ATTACHMENT_REPORT_PREFIX + "active";
		public static final String REPORT_NAME = EMAIL_ATTACHMENT_REPORT_PREFIX + "name";
		public static final String REPORT_EXTENSION = EMAIL_ATTACHMENT_REPORT_PREFIX + "extension";

		public static final String REPORT_PARAMETERS_PREFIX = EMAIL_ATTACHMENT_REPORT_PREFIX + "parameters.";

	}

	/**
	 * Container for all {@link ReadEmailTaskDefinition} parameter names.
	 */
	public static class ReadEmail {

		private ReadEmail() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = EMPTY;

		public static final String ACCOUNT_NAME = ALL_PREFIX + "account.name";

		private static final String FOLDER_PREFIX = ALL_PREFIX + "folder.";
		public static final String INCOMING_FOLDER = FOLDER_PREFIX + "incoming";
		public static final String PROCESSED_FOLDER = FOLDER_PREFIX + "processed";
		public static final String REJECTED_FOLDER = FOLDER_PREFIX + "rejected";

		private static final String FILTER_PREFIX = ALL_PREFIX + "filter.";
		public static final String FILTER_TYPE = FILTER_PREFIX + "type";
		private static final String FILTER_REGEX_PREFIX = FILTER_PREFIX + "regex.";
		public static final String FILTER_FROM_REGEX = FILTER_REGEX_PREFIX + "from";
		public static final String FILTER_SUBJECT_REGEX = FILTER_REGEX_PREFIX + "subject";
		private static final String FILTER_FUNCTION_PREFIX = FILTER_PREFIX + "function.";
		public static final String FILTER_FUNCTION_NAME = FILTER_FUNCTION_PREFIX + "name";
		public static final String FILTER_REJECT = FILTER_PREFIX + "reject";

		private static final String ACTION_PREFIX = ALL_PREFIX + "action.";

		private static final String ATTACHMENTS_PREFIX = ACTION_PREFIX + "attachments.";
		public static final String ATTACHMENTS_ACTIVE = ATTACHMENTS_PREFIX + "active";
		public static final String ATTACHMENTS_CATEGORY = ATTACHMENTS_PREFIX + "category";

		private static final String NOTIFICATION_PREFIX = ACTION_PREFIX + "notification.";
		public static final String NOTIFICATION_ACTIVE = NOTIFICATION_PREFIX + "active";
		public static final String NOTIFICATION_TEMPLATE = NOTIFICATION_PREFIX + "template";

		private static final String WORKFLOW_PREFIX = ACTION_PREFIX + "workflow.";
		public static final String WORKFLOW_ACTIVE = WORKFLOW_PREFIX + "active";
		public static final String WORKFLOW_ADVANCE = WORKFLOW_PREFIX + "advance";
		public static final String WORKFLOW_CLASS_NAME = WORKFLOW_PREFIX + "class.name";
		public static final String WORKFLOW_FIELDS_MAPPING = WORKFLOW_PREFIX + "fields.mapping";
		private static final String WORKFLOW_ATTACHMENTS_PREFIX = WORKFLOW_PREFIX + "attachments";
		public static final String WORKFLOW_ATTACHMENTS_SAVE = WORKFLOW_ATTACHMENTS_PREFIX + "save";
		public static final String WORKFLOW_ATTACHMENTS_CATEGORY = WORKFLOW_ATTACHMENTS_PREFIX + "category";

		/**
		 * Container for all mapper parameter names.
		 */
		abstract static class MapperEngine {

			protected static final String ALL_PREFIX = "mapper.";

			public static final String TYPE = ALL_PREFIX + "type";

		}

		/**
		 * Container for all {@link _KeyValueMapperEngine} parameter names.
		 */
		public static class KeyValueMapperEngine extends MapperEngine {

			private KeyValueMapperEngine() {
				// prevents instantiation
			}

			static final String TYPE_VALUE = "keyvalue";

			private static final String KEY_PREFIX = ALL_PREFIX + "key.";
			public static final String KEY_INIT = KEY_PREFIX + "init";
			public static final String KEY_END = KEY_PREFIX + "end";

			private static final String VALUE_PREFIX = ALL_PREFIX + "value.";
			public static final String VALUE_INIT = VALUE_PREFIX + "init";
			public static final String VALUE_END = VALUE_PREFIX + "end";

		}

	}

	/**
	 * Container for all {@link StartWorkflowTaskDefinition} parameter names.
	 */
	public static class StartWorkflow {

		private StartWorkflow() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = EMPTY;

		public static final String CLASSNAME = ALL_PREFIX + "classname";
		public static final String ATTRIBUTES = ALL_PREFIX + "attributes";

	}

	/**
	 * Container for all {@link SynchronousEventTaskDefinition} parameter names.
	 */
	public static class SynchronousEvent {

		private SynchronousEvent() {
			// prevents instantiation
		}

		public static final String PHASE = "phase";

		public static final String PHASE_AFTER_CREATE = "after_create";
		public static final String PHASE_BEFORE_UPDATE = "before_update";
		public static final String PHASE_AFTER_UPDATE = "after_update";
		public static final String PHASE_BEFORE_DELETE = "before_delete";

		private static final String FILTER = "filter.";
		public static final String FILTER_GROUPS = FILTER + "groups";
		public static final String FILTER_CLASSNAME = FILTER + "classname";
		public static final String FILTER_CARDS = FILTER + "cards";

		private static final String ACTION_PREFIX = "action.";

		private static final String EMAIL_PREFIX = ACTION_PREFIX + "email.";
		public static final String EMAIL_ACTIVE = EMAIL_PREFIX + "active";
		public static final String EMAIL_ACCOUNT = EMAIL_PREFIX + "account";
		public static final String EMAIL_TEMPLATE = EMAIL_PREFIX + "template";

		private static final String WORKFLOW_PREFIX = ACTION_PREFIX + "workflow.";
		public static final String WORKFLOW_ACTIVE = WORKFLOW_PREFIX + "active";
		public static final String WORKFLOW_CLASS_NAME = WORKFLOW_PREFIX + "classname";
		public static final String WORKFLOW_ATTRIBUTES = WORKFLOW_PREFIX + "attributes";
		public static final String WORKFLOW_ADVANCE = WORKFLOW_PREFIX + "advance";

		private static final String ACTION_SCRIPT_PREFIX = ACTION_PREFIX + "scripting.";
		public static final String ACTION_SCRIPT_ACTIVE = ACTION_SCRIPT_PREFIX + "active";
		public static final String ACTION_SCRIPT_ENGINE = ACTION_SCRIPT_PREFIX + "engine";
		public static final String ACTION_SCRIPT_SCRIPT = ACTION_SCRIPT_PREFIX + "script";
		public static final String ACTION_SCRIPT_SAFE = ACTION_SCRIPT_PREFIX + "safe";

	}

	private ParameterNames() {
		// prevents instantiation
	}

}
