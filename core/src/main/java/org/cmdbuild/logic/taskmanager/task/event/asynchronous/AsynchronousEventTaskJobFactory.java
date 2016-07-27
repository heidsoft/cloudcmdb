package org.cmdbuild.logic.taskmanager.task.event.asynchronous;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.ofInstance;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.common.template.engine.Engines.emptyStringOnNull;
import static org.cmdbuild.common.template.engine.Engines.nullOnError;
import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.dao.guava.Functions.toCard;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.ClassHistory.history;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.eq;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.lt;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.and;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.condition;
import static org.cmdbuild.scheduler.command.Commands.conditional;
import static org.cmdbuild.services.template.engine.EngineNames.CARD_PREFIX;

import java.util.Comparator;

import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.data.store.task.TaskStore;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.QuerySpecsBuilderFiller;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.mapping.json.JsonFilterHelper;
import org.cmdbuild.logic.taskmanager.commons.SchedulerCommandWrapper;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.logic.taskmanager.store.LogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.util.CardIdFilterElementGetter;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.template.engine.CardEngine;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Ordering;

public class AsynchronousEventTaskJobFactory extends AbstractJobFactory<AsynchronousEventTask> {

	private static final Comparator<CMCard> BEGIN_DATE_DESCENDING = new Comparator<CMCard>() {

		@Override
		public int compare(final CMCard o1, final CMCard o2) {
			return o2.getBeginDate().compareTo(o1.getBeginDate());
		}

	};

	private final CMDataView dataView;
	private final EmailAccountFacade emailAccountFacade;
	private final EmailTemplateLogic emailTemplateLogic;
	private final TaskStore taskStore;
	private final LogicAndStoreConverter logicAndStoreConverter;
	private final EmailTemplateSenderFactory emailTemplateSenderFactory;

	public AsynchronousEventTaskJobFactory(final CMDataView dataView, final EmailAccountFacade emailAccountFacade,
			final EmailTemplateLogic emailTemplateLogic, final TaskStore taskStore,
			final LogicAndStoreConverter logicAndStoreConverter,
			final EmailTemplateSenderFactory emailTemplateSenderFactory) {
		this.dataView = dataView;
		this.emailAccountFacade = emailAccountFacade;
		this.emailTemplateLogic = emailTemplateLogic;
		this.taskStore = taskStore;
		this.logicAndStoreConverter = logicAndStoreConverter;
		this.emailTemplateSenderFactory = emailTemplateSenderFactory;
	}

	@Override
	protected Class<AsynchronousEventTask> getType() {
		return AsynchronousEventTask.class;
	}

	@Override
	protected Command command(final AsynchronousEventTask task) {
		return new Command() {

			@Override
			public void execute() {
				final String classname = task.getTargetClassname();
				final String filter = task.getFilter();
				final DateTime lastExecution = taskStore.read(logicAndStoreConverter.from(task).toStore())
						.getLastExecution();

				logger.debug(marker, "checking class '{}' with filter '{}'", classname, filter);

				final JSONObject jsonFilter = (filter == null) ? new JSONObject() : toJsonObject(filter);

				final Iterable<CMCard> cards = currentCardsMatchingFilter(classname, jsonFilter);
				for (final CMCard card : cards) {
					final CMClass cardType = card.getType();
					if (createdAfterLastExecution(card, lastExecution)) {
						logger.debug(marker, "history card not found");
						sendEmail(task, card);
					} else {
						final Optional<CMCard> lastHistoryCardWithNoFilter = historyCardBeforeLastExecutionWithNoFilter(
								cardType, card.getId(), lastExecution);
						if (lastHistoryCardWithNoFilter.isPresent()) {
							final CMCard historyCardWithNoFilter = lastHistoryCardWithNoFilter.get();
							logger.debug(marker, "found history card with id '{}'", historyCardWithNoFilter.getId());
							final Optional<CMCard> historyCardWithFilter = historyCardBeforeLastExecutionWithFilter(
									cardType, historyCardWithNoFilter, jsonFilter);
							if (!historyCardWithFilter.isPresent()) {
								logger.debug(marker, "filtered history card not found");
								sendEmail(task, card);
							}
						}
					}
				}
			}

			private boolean createdAfterLastExecution(final CMCard card, final DateTime lastExecution) {
				logger.debug(marker, "checking if card has been created after last execution");
				return (lastExecution == null) ? true : card.getBeginDate().compareTo(lastExecution) > 0;
			}

			private JSONObject toJsonObject(final String filter) {
				try {
					return new JSONObject(filter);
				} catch (final JSONException e) {
					throw new IllegalArgumentException(e);
				}
			}

			private Iterable<CMCard> currentCardsMatchingFilter(final String classname, final JSONObject jsonFilter) {
				logger.debug(marker, "getting current cards matching filter");
				final CMClass sourceClass = dataView.findClass(classname);
				final QueryOptions queryOptions = QueryOptions.newQueryOption() //
						.filter(jsonFilter) //
						.build();
				final CMQueryResult result = new QuerySpecsBuilderFiller(dataView, queryOptions, sourceClass) //
						.create() //
						.run();
				return from(result) //
						.transform(toCard(sourceClass));
			}

			private Optional<CMCard> historyCardBeforeLastExecutionWithNoFilter(final CMClass type, final Long id,
					final DateTime lastExecution) {
				logger.debug(marker, "getting last history for card of type '{}' and with id '{}'", type.getName(), id);
				final CMClass sourceClass = history(type);
				final CMQueryResult result = dataView.select(anyAttribute(sourceClass)) //
						.from(sourceClass) //
						.where(and( //
								condition(attribute(sourceClass, "CurrentId"), //
										eq(id)), //
								condition(attribute(sourceClass, "BeginDate"), //
										lt(lastExecution.toDate())) //
						)) //
						.run();
				final Iterable<CMCard> cards = from(result) //
						.transform(toCard(sourceClass));
				final Iterable<CMCard> sortedCards = Ordering.from(BEGIN_DATE_DESCENDING) //
						.sortedCopy(cards);
				return from(sortedCards) //
						.limit(1) //
						.first();
			}

			private Optional<CMCard> historyCardBeforeLastExecutionWithFilter(final CMClass type,
					final CMCard historyCard, final JSONObject jsonFilter) {
				logger.debug(marker, "getting last history for card of type '{}' and id '{}', with filter '{}'",
						type.getName(), historyCard.getId(), jsonFilter);
				try {
					final QueryOptions queryOptions = QueryOptions.newQueryOption() //
							.filter(new JsonFilterHelper(jsonFilter) //
									.merge(CardIdFilterElementGetter.of(historyCard))) //
							.build();
					final CMClass sourceClass = history(type);
					final CMQueryResult result = new QuerySpecsBuilderFiller(dataView, queryOptions, sourceClass) //
							.create() //
							.run();
					return from(result) //
							.transform(toCard(sourceClass)) //
							.limit(1) //
							.first();
				} catch (final JSONException e) {
					logger.warn(marker, "error with json format", e);
					return Optional.absent();
				}
			}

		};
	}

	private void sendEmail(final AsynchronousEventTask task, final CMCard card) {
		final Supplier<Template> emailTemplateSupplier = memoize(new Supplier<Template>() {

			@Override
			public Template get() {
				final String name = defaultString(task.getNotificationTemplate());
				return emailTemplateLogic.read(name);
			}

		});
		final Optional<EmailAccount> account = emailAccountFacade.firstOf(asList(emailTemplateSupplier.get()
				.getAccount(), task.getNotificationAccount()));
		final Supplier<EmailAccount> emailAccountSupplier = account.isPresent() ? ofInstance(account.get()) : null;
		final EngineBasedTemplateResolver templateResolver = EngineBasedTemplateResolver.newInstance() //
				.withEngine(emptyStringOnNull(nullOnError( //
						CardEngine.newInstance() //
								.withCard(card) //
								.build())), //
						CARD_PREFIX) //
				.build();
		final Command command = SchedulerCommandWrapper.of(a(emailTemplateSenderFactory.queued() //
				.withAccount(emailAccountSupplier) //
				.withTemplate(emailTemplateSupplier) //
				.withTemplateResolver(templateResolver) //
				.withReference(task.getId()) //
				));
		conditional(command, new NotificationEnabled(task)).execute();
	}

}
