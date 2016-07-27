package org.cmdbuild.workflow.api;

import static com.google.common.reflect.Reflection.newProxy;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.cmdbuild.workflow.Constants.CURRENT_GROUP_NAME_VARIABLE;
import static org.cmdbuild.workflow.Constants.CURRENT_USER_USERNAME_VARIABLE;
import static org.cmdbuild.workflow.Constants.PROCESS_CARD_ID_VARIABLE;

import org.cmdbuild.api.fluent.ExecutorBasedFluentApi;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.common.api.mail.Configuration;
import org.cmdbuild.common.api.mail.MailApi;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.CusSoapProxyBuilder;
import org.cmdbuild.workflow.api.WorkflowApiImpl.Context;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

import com.google.common.base.Optional;

public class SoapSharkWorkflowApiFactory implements SharkWorkflowApiFactory {

	private static MailApi NULL_MAIL_API = UnsupportedProxyFactory.of(MailApi.class).create();

	private static class ProcessData {

		public final WMSessionHandle shandle;
		public final String procInstId;

		public ProcessData(final WMSessionHandle shandle, final String procInstId) {
			this.shandle = shandle;
			this.procInstId = procInstId;
		}

	}

	private static class DelegatingWorkflowApi extends ForwardingWorkflowApi {

		private static final WorkflowApi UNSUPPORTED = newProxy(WorkflowApi.class, unsupported("delegate not setted"));

		private WorkflowApi delegate = UNSUPPORTED;

		@Override
		protected WorkflowApi delegate() {
			synchronized (this) {
				return delegate;
			}
		}

		private void setDelegate(final WorkflowApi delegate) {
			synchronized (this) {
				this.delegate = delegate;
			}
		}

	}

	private CallbackUtilities cus;
	private ProcessData processData;

	@Override
	public void setup(final CallbackUtilities cus) {
		setup(cus, null);
	}

	@Override
	public void setup(final CallbackUtilities cus, final WMSessionHandle shandle, final String procInstId) {
		setup(cus, new ProcessData(shandle, procInstId));
	}

	private void setup(final CallbackUtilities cus, final ProcessData processData) {
		this.cus = cus;
		this.processData = processData;
	}

	@Override
	public WorkflowApi createWorkflowApi() {
		return new WorkflowApiImpl(context(proxy()));
	}

	private Context context(final Private proxy) {
		return new Context() {

			// FIXME needed for cut-off circular dependency
			private final DelegatingWorkflowApi delegatingWorkflowApi = new DelegatingWorkflowApi();
			private final SchemaApi schemaApi = new CachedWsSchemaApi(proxy);
			private final MailApi mailApi = SoapSharkWorkflowApiFactory.this.mailApi();
			private final FluentApiExecutor wsFluentApiExecutor = new WsFluentApiExecutor(proxy,
					new SharkWsEntryTypeConverter(delegatingWorkflowApi),
					new SharkWsRawTypeConverter(delegatingWorkflowApi));
			private final SharkFluentApiExecutor executor = new SharkFluentApiExecutor(wsFluentApiExecutor,
					currentProcessId(), new MonostateSelfSuspensionRequestHolder());
			private final FluentApi fluentApi = new ExecutorBasedFluentApi(executor);

			@Override
			public FluentApi fluentApi() {
				return fluentApi;
			}

			@Override
			public Private proxy() {
				return proxy;
			}

			@Override
			public SchemaApi schemaApi() {
				return schemaApi;
			}

			@Override
			public MailApi mailApi() {
				return mailApi;
			}

			@Override
			public void callback(final WorkflowApiImpl object) {
				delegatingWorkflowApi.setDelegate(object);
			}

			@Override
			public Context impersonate(final String username, final String group) {
				return context(SoapSharkWorkflowApiFactory.this.proxy(username, group));
			}

		};
	}

	private Private proxy() {
		return proxy(currentUserOrEmptyOnError(), currentGroupOrEmptyOnError());
	}

	private Private proxy(final String username, final String group) {
		return new CusSoapProxyBuilder(cus) //
				.withUsername(defaultString(username, currentUserOrEmptyOnError())) //
				.withGroup(defaultString(group, currentGroupOrEmptyOnError())) //
				.build();
	}

	private String currentUserOrEmptyOnError() {
		if (processData == null) {
			return EMPTY;
		}

		try {
			final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(processData.shandle,
					processData.procInstId, CURRENT_USER_USERNAME_VARIABLE);
			final Object value = attribute.getValue();
			return String.class.cast(value);
		} catch (final Throwable e) {
			return EMPTY;
		}
	}

	private String currentGroupOrEmptyOnError() {
		if (processData == null) {
			return EMPTY;
		}

		try {
			final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(processData.shandle,
					processData.procInstId, CURRENT_GROUP_NAME_VARIABLE);
			final Object value = attribute.getValue();
			return String.class.cast(value);
		} catch (final Throwable e) {
			return EMPTY;
		}
	}

	private Optional<Long> currentProcessId() {
		if (processData == null) {
			return Optional.absent();
		}

		try {
			final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(processData.shandle,
					processData.procInstId, PROCESS_CARD_ID_VARIABLE);
			final Object value = attribute.getValue();
			return Optional.of(Number.class.cast(value).longValue());
		} catch (final Throwable e) {
			return Optional.absent();
		}
	}

	private WAPI wapi() throws Exception {
		return Shark.getInstance().getWAPIConnection();
	}

	private MailApi mailApi() {
		try {
			final ConfigurationHelper helper = new ConfigurationHelper(cus);
			final Configuration.All mailApiConfiguration = helper.getMailApiConfiguration();
			final MailApiFactory mailApiFactory = helper.getMailApiFactory();
			return mailApiFactory.create(mailApiConfiguration);
		} catch (final Exception e) {
			return NULL_MAIL_API;
		}
	}

}
