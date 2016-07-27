package org.cmdbuild.logic.taskmanager.task.event.synchronous;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.logic.taskmanager.TaskVisitor;

import com.google.common.collect.Maps;

public class SynchronousEventTask implements Task {

	public static enum Phase {
		AFTER_CREATE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.afterCreate();
			}
		}, //
		BEFORE_UPDATE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.beforeUpdate();
			}
		}, //
		AFTER_UPDATE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.afterUpdate();
			}
		}, //
		BEFORE_DELETE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.beforeDelete();
			}
		}, //
		;

		/**
		 * Simulates in some way the use of the visitor pattern.
		 */
		public abstract void identify(PhaseIdentifier identifier);

	}

	public static interface PhaseIdentifier {

		void afterCreate();

		void beforeDelete();

		void afterUpdate();

		void beforeUpdate();

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<SynchronousEventTask> {

		private static final Iterable<String> EMPTY_GROUPS = Collections.emptyList();
		private static final Map<String, String> EMPTY_ATTRIBUTES = Collections.emptyMap();

		private Long id;
		private String description;
		private Boolean active;
		private Phase phase;
		private Iterable<String> groups;
		private String classname;
		private String filter;
		private Boolean emailEnabled;
		private String emailAccount;
		private String emailTemplate;
		private Boolean workflowEnabled;
		private String workflowClassName;
		private final Map<String, String> workflowAttributes = Maps.newHashMap();
		private Boolean workflowAdvanceable;
		private Boolean scriptingEnabled;
		private String scriptingEngine;
		private String scriptingScript;
		private Boolean scriptingSafe;

		private Builder() {
			// use factory method
		}

		@Override
		public SynchronousEventTask build() {
			validate();
			return new SynchronousEventTask(this);
		}

		private void validate() {
			active = defaultIfNull(active, FALSE);

			groups = defaultIfNull(groups, EMPTY_GROUPS);

			emailEnabled = defaultIfNull(emailEnabled, FALSE);

			workflowEnabled = defaultIfNull(workflowEnabled, FALSE);
			workflowAdvanceable = defaultIfNull(workflowAdvanceable, FALSE);

			scriptingEnabled = defaultIfNull(scriptingEnabled, FALSE);
			scriptingSafe = defaultIfNull(scriptingSafe, FALSE);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withPhase(final Phase phase) {
			this.phase = phase;
			return this;
		}

		public Builder withGroups(final Iterable<String> groups) {
			this.groups = groups;
			return this;
		}

		public Builder withTargetClass(final String classname) {
			this.classname = classname;
			return this;
		}

		public Builder withFilter(final String filter) {
			this.filter = filter;
			return this;
		}

		public Builder withEmailEnabled(final boolean enabled) {
			this.emailEnabled = enabled;
			return this;
		}

		public Builder withEmailAccount(final String account) {
			this.emailAccount = account;
			return this;
		}

		public Builder withEmailTemplate(final String template) {
			this.emailTemplate = template;
			return this;
		}

		public Builder withWorkflowEnabled(final boolean enabled) {
			this.workflowEnabled = enabled;
			return this;
		}

		public Builder withWorkflowClassName(final String className) {
			this.workflowClassName = className;
			return this;
		}

		public Builder withWorkflowAttributes(final Map<String, String> attributes) {
			this.workflowAttributes.putAll(defaultIfNull(attributes, EMPTY_ATTRIBUTES));
			return this;
		}

		public Builder withWorkflowAdvanceable(final boolean advanceable) {
			this.workflowAdvanceable = advanceable;
			return this;
		}

		public Builder withScriptingEnableStatus(final boolean scriptingEnabled) {
			this.scriptingEnabled = scriptingEnabled;
			return this;
		}

		public Builder withScriptingEngine(final String scriptingEngine) {
			this.scriptingEngine = scriptingEngine;
			return this;
		}

		public Builder withScript(final String scriptingScript) {
			this.scriptingScript = scriptingScript;
			return this;
		}

		public Builder withScriptingSafeStatus(final boolean scriptingSafe) {
			this.scriptingSafe = scriptingSafe;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final Phase phase;
	private final Iterable<String> groups;
	private final String classname;
	private final String filter;
	private final boolean emailEnabled;
	private final String emailAccount;
	private final String emailTemplate;
	private final boolean workflowEnabled;
	private final String workflowClassName;
	private final Map<String, String> workflowAttributes;
	private final boolean workflowAdvanceable;
	private final boolean scriptingEnabled;
	private final String scriptingEngine;
	private final String scriptingScript;
	private final boolean scriptingSafe;

	private SynchronousEventTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.phase = builder.phase;
		this.groups = builder.groups;
		this.classname = builder.classname;
		this.filter = builder.filter;
		this.emailEnabled = builder.emailEnabled;
		this.emailAccount = builder.emailAccount;
		this.emailTemplate = builder.emailTemplate;
		this.workflowEnabled = builder.workflowEnabled;
		this.workflowClassName = builder.workflowClassName;
		this.workflowAttributes = builder.workflowAttributes;
		this.workflowAdvanceable = builder.workflowAdvanceable;
		this.scriptingEnabled = builder.scriptingEnabled;
		this.scriptingEngine = builder.scriptingEngine;
		this.scriptingScript = builder.scriptingScript;
		this.scriptingSafe = builder.scriptingSafe;
	}

	@Override
	public void accept(final TaskVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isExecutable() {
		return false;
	}

	public Phase getPhase() {
		return phase;
	}

	public Iterable<String> getGroups() {
		return groups;
	}

	public String getTargetClassname() {
		return classname;
	}

	public String getFilter() {
		return filter;
	}

	public boolean isEmailEnabled() {
		return emailEnabled;
	}

	public String getEmailAccount() {
		return emailAccount;
	}

	public String getEmailTemplate() {
		return emailTemplate;
	}

	public boolean isWorkflowEnabled() {
		return workflowEnabled;
	}

	public String getWorkflowClassName() {
		return workflowClassName;
	}

	public Map<String, String> getWorkflowAttributes() {
		return workflowAttributes;
	}

	public boolean isWorkflowAdvanceable() {
		return workflowAdvanceable;
	}

	public boolean isScriptingEnabled() {
		return scriptingEnabled;
	}

	public String getScriptingEngine() {
		return scriptingEngine;
	}

	public String getScriptingScript() {
		return scriptingScript;
	}

	public boolean isScriptingSafe() {
		return scriptingSafe;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
