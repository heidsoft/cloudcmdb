package org.cmdbuild.logic.taskmanager.task.connector;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.ofInstance;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.scheduler.command.Commands.composeOnExeption;
import static org.cmdbuild.scheduler.command.Commands.conditional;
import static org.cmdbuild.scheduler.command.Commands.nullCommand;

import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.taskmanager.commons.SchedulerCommandWrapper;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.sync.store.internal.AttributeValueAdapter;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	private final CMDataView dataView;
	private final DataSourceHelper jdbcService;
	private final AttributeValueAdapter attributeValueAdapter;
	private final EmailAccountFacade emailAccountFacade;
	private final EmailTemplateLogic emailTemplateLogic;
	private final EmailTemplateSenderFactory emailTemplateSenderFactory;

	public ConnectorTaskJobFactory(final CMDataView dataView, final DataSourceHelper jdbcService,
			final AttributeValueAdapter attributeValueAdapter, final EmailAccountFacade emailAccountFacade,
			final EmailTemplateLogic emailTemplateLogic, final EmailTemplateSenderFactory emailTemplateSenderFactory) {
		this.dataView = dataView;
		this.jdbcService = jdbcService;
		this.attributeValueAdapter = attributeValueAdapter;
		this.emailAccountFacade = emailAccountFacade;
		this.emailTemplateLogic = emailTemplateLogic;
		this.emailTemplateSenderFactory = emailTemplateSenderFactory;
	}

	@Override
	protected Class<ConnectorTask> getType() {
		return ConnectorTask.class;
	}

	@Override
	protected Command command(final ConnectorTask task) {
		return composeOnExeption(connector(task), sendEmail(task));

	}

	private ConnectorTaskCommandWrapper connector(final ConnectorTask task) {
		return new ConnectorTaskCommandWrapper(dataView, jdbcService, attributeValueAdapter, task);
	}

	private Command sendEmail(final ConnectorTask task) {
		final Command command;
		// TODO do it in a better way
		if (task.isNotificationActive()) {
			final Supplier<Template> emailTemplateSupplier = memoize(new Supplier<Template>() {

				@Override
				public Template get() {
					final String name = defaultString(task.getNotificationErrorTemplate());
					return emailTemplateLogic.read(name);
				}

			});
			final Optional<EmailAccount> account = emailAccountFacade.firstOf(asList(emailTemplateSupplier.get()
					.getAccount(), task.getNotificationAccount()));
			final Supplier<EmailAccount> emailAccountSupplier = account.isPresent() ? ofInstance(account.get()) : null;
			command = SchedulerCommandWrapper.of(a(emailTemplateSenderFactory.queued() //
					.withAccount(emailAccountSupplier) //
					.withTemplate(emailTemplateSupplier) //
					.withReference(task.getId()) //
					));
		} else {
			command = nullCommand();
		}
		return conditional(command, new NotificationEnabled(task));
	}

}
