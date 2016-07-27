package org.cmdbuild.logic.taskmanager.task.generic;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;
import static org.cmdbuild.common.template.engine.Engines.emptyStringOnNull;
import static org.cmdbuild.common.template.engine.Engines.map;
import static org.cmdbuild.common.template.engine.Engines.nullOnError;
import static org.cmdbuild.logic.report.StringExtensionConverter.of;
import static org.cmdbuild.scheduler.command.Commands.conditional;
import static org.cmdbuild.services.template.engine.EngineNames.CQL_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.DB_TEMPLATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.logic.report.ReportLogic.Report;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.template.engine.CqlEngine;
import org.cmdbuild.services.template.engine.DatabaseEngine;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

public class GenericTaskJobFactory extends AbstractJobFactory<GenericTask> {

	private final EmailAccountFacade emailAccountFacade;
	private final EmailTemplateLogic emailTemplateLogic;
	private final ReportLogic reportLogic;
	private final CMDataView dataView;
	private final DatabaseEngine databaseEngine;
	private final EmailTemplateSenderFactory emailTemplateSenderFactory;

	public GenericTaskJobFactory(final EmailAccountFacade emailAccountFacade,
			final EmailTemplateLogic emailTemplateLogic, final ReportLogic reportLogic, final CMDataView dataView,
			final DatabaseEngine databaseEngine, final EmailTemplateSenderFactory emailTemplateSenderFactory) {
		this.emailAccountFacade = emailAccountFacade;
		this.emailTemplateLogic = emailTemplateLogic;
		this.reportLogic = reportLogic;
		this.dataView = dataView;
		this.databaseEngine = databaseEngine;
		this.emailTemplateSenderFactory = emailTemplateSenderFactory;
	}

	@Override
	protected Class<GenericTask> getType() {
		return GenericTask.class;
	}

	@Override
	protected Command command(final GenericTask task) {
		return conditional(sendEmail(task), emailActive(task));
	}

	private Command sendEmail(final GenericTask task) {
		return new Command() {

			@Override
			public void execute() {
				final EngineBasedTemplateResolver templateResolver = templateResolverOf(task);

				final Supplier<Template> template = memoize(new Supplier<Template>() {

					@Override
					public Template get() {
						logger.debug(marker, "getting email template for '{}'", task);
						final String value = task.getEmailTemplate();
						logger.debug(marker, "template name is '{}'", value);
						return emailTemplateLogic.read(value);
					}

				});
				final Supplier<EmailAccount> account = new Supplier<EmailAccount>() {

					@Override
					public EmailAccount get() {
						logger.debug(marker, "getting email account for '{}'", task);
						final Iterable<String> eligibleAccounts = from(
								asList(template.get().getAccount(), task.getEmailAccount())).filter(String.class);
						logger.debug(marker, "eligible accounts are '{}'", eligibleAccounts);
						return emailAccountFacade.firstOfOrDefault(eligibleAccounts).get();
					}

				};
				final Collection<Supplier<? extends DataHandler>> attachments = new ArrayList<>();
				if (task.isReportActive()) {
					attachments.add(new Supplier<DataHandler>() {

						@Override
						public DataHandler get() {
							final Report report = stream(reportLogic.readAll().spliterator(), false) //
									.filter(input -> input.getTitle().equals(task.getReportName())) //
									.findFirst() //
									.get();
							return reportLogic.download(report.getId(), of(task.getReportExtension()).extension(),
									resolve(task.getReportParameters()));
						}

						private Map<String, ? extends Object> resolve(final Map<String, String> input) {
							return Maps.transformValues(input, new Function<String, String>() {

								@Override
								public String apply(final String input) {
									return templateResolver.resolve(input);
								}

							});
						}

					});
				}

				emailTemplateSenderFactory.queued() //
						.withAccount(account) //
						.withTemplate(template) //
						.withAttachments(attachments) //
						.withReference(task.getId()) //
						.withTemplateResolver(templateResolver) //
						.build() //
						.execute();
			}

			private EngineBasedTemplateResolver templateResolverOf(final GenericTask task) {
				final EngineBasedTemplateResolver.Builder templateResolverBuilder = EngineBasedTemplateResolver
						.newInstance() //
						.withEngine(
								emptyStringOnNull(nullOnError( //
										CqlEngine.newInstance() //
												.withDataView(dataView) //
												.build())), //
								CQL_PREFIX) //
						.withEngine(
								emptyStringOnNull(nullOnError( //
										databaseEngine)), //
								DB_TEMPLATE);
				for (final Entry<String, Map<String, String>> element : task.getContext().entrySet()) {
					templateResolverBuilder.withEngine(emptyStringOnNull(nullOnError(map(element.getValue()))), //
							element.getKey());
				}
				final EngineBasedTemplateResolver templateResolver = templateResolverBuilder //
						.build();
				return templateResolver;
			}

		};
	}

	private Predicate<Void> emailActive(final GenericTask task) {
		return new Predicate<Void>() {

			@Override
			public boolean apply(final Void input) {
				return task.isEmailActive();
			}

		};
	}

}
