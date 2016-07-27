package org.cmdbuild.logic.taskmanager.task.email;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.ofInstance;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.common.template.engine.Engines.emptyStringOnNull;
import static org.cmdbuild.common.template.engine.Engines.map;
import static org.cmdbuild.common.template.engine.Engines.nullOnError;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.Clauses.call;
import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;
import static org.cmdbuild.logic.taskmanager.task.email.Actions.safe;
import static org.cmdbuild.services.email.EmailUtils.addLineBreakForHtml;
import static org.cmdbuild.services.template.engine.EngineNames.CARD_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.CQL_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.DB_TEMPLATE;
import static org.cmdbuild.services.template.engine.EngineNames.EMAIL_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.GROUP_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.GROUP_USERS_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.MAPPER_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.USER_PREFIX;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.Aliases;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountFacade;
import org.cmdbuild.data.store.email.EmailConstants;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.dms.StoreDocument;
import org.cmdbuild.logic.dms.StoreDocument.Document;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.EmailTemplateSenderFactory;
import org.cmdbuild.logic.email.SubjectHandler;
import org.cmdbuild.logic.email.SubjectHandler.ParsedSubject;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.logic.taskmanager.task.email.mapper.EngineBasedMapper;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.StartProcess.Hook;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.Attachment;
import org.cmdbuild.services.email.Email;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.ForwardingEmail;
import org.cmdbuild.services.template.engine.CardEngine;
import org.cmdbuild.services.template.engine.CqlEngine;
import org.cmdbuild.services.template.engine.DatabaseEngine;
import org.cmdbuild.services.template.engine.EmailEngine;
import org.cmdbuild.services.template.engine.GroupEmailEngine;
import org.cmdbuild.services.template.engine.GroupUsersEmailEngine;
import org.cmdbuild.services.template.engine.UserEmailEngine;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

public class ReadEmailTaskJobFactory extends AbstractJobFactory<ReadEmailTask> {

	private static final String //
			CONTENT = "content", //
			CC_ADDRESSES = "ccAddresses", //
			FROM_ADDRESS = "fromAddress", //
			SEPARATOR = ",", //
			SUBJECT = "subject", //
			TO_ADDRESSES = "toAddresses";

	private static final String //
			FUNCTION = "function", //
			NONE = "none", //
			REGEX = "regex";

	private static class ConditionalAction implements Action {

		private final Predicate<Email> predicate;
		private final Action delegate;

		public ConditionalAction(final Predicate<Email> predicate, final Action delegate) {
			this.predicate = predicate;
			this.delegate = delegate;
		}

		@Override
		public void execute(final Email email, final Storable storable) {
			if (predicate.apply(email)) {
				delegate.execute(email, storable);
			}
		}

	}

	private final EmailAccountFacade emailAccountFacade;
	private final EmailServiceFactory emailServiceFactory;
	private final SubjectHandler subjectHandler;
	private final Store<org.cmdbuild.data.store.email.Email> emailStore;
	private final WorkflowLogic workflowLogic;
	private final DmsLogic dmsLogic;
	private final CMDataView dataView;
	private final EmailTemplateLogic emailTemplateLogic;
	private final DatabaseEngine databaseEngine;
	private final EmailTemplateSenderFactory emailTemplateSenderFactory;

	public ReadEmailTaskJobFactory(final EmailAccountFacade emailAccountFacade,
			final EmailServiceFactory emailServiceFactory, final SubjectHandler subjectHandler,
			final Store<org.cmdbuild.data.store.email.Email> emailStore, final WorkflowLogic workflowLogic,
			final DmsLogic dmsLogic, final CMDataView dataView, final EmailTemplateLogic emailTemplateLogic,
			final DatabaseEngine databaseEngine, final EmailTemplateSenderFactory emailTemplateSenderFactory) {
		this.emailAccountFacade = emailAccountFacade;
		this.emailServiceFactory = emailServiceFactory;
		this.subjectHandler = subjectHandler;
		this.emailStore = emailStore;
		this.workflowLogic = workflowLogic;
		this.dmsLogic = dmsLogic;
		this.dataView = dataView;
		this.emailTemplateLogic = emailTemplateLogic;
		this.databaseEngine = databaseEngine;
		this.emailTemplateSenderFactory = emailTemplateSenderFactory;
	}

	@Override
	protected Class<ReadEmailTask> getType() {
		return ReadEmailTask.class;
	}

	@Override
	protected Command command(final ReadEmailTask task) {
		final String emailAccountName = task.getEmailAccount();
		final EmailAccount selectedEmailAccount = emailAccountFor(emailAccountName).get();
		final EmailService service = emailServiceFactory.create(ofInstance(selectedEmailAccount));
		return ReadEmailCommand.newInstance() //
				.withEmailService(service) //
				.withIncomingFolder(task.getIncomingFolder()) //
				.withProcessedFolder(task.getProcessedFolder()) //
				.withRejectedFolder(task.getRejectedFolder()) //
				.withRejectNotMatching(task.isRejectNotMatching()) //
				.withEmailStore(emailStore) //
				.withAction(safe(sendNotification(task))) //
				.withAction(safe(storeAttachments(task))) //
				.withAction(safe(startProcess(task))) //
				.build();
	}

	private Optional<EmailAccount> emailAccountFor(final String name) {
		logger.debug(marker, "getting email account for name '{}'", name);
		return emailAccountFacade.firstOf(asList(name));
	}

	private Action sendNotification(final ReadEmailTask task) {
		logger.info(marker, "adding notification action");
		final Predicate<Email> condition = and(notificationActive(task), filter(task), subjectMatches());
		return new ConditionalAction( //
				condition, //
				new Action() {

					@Override
					public void execute(final Email email, final Storable storable) {
						doAdapt(email, storable);
						doExecute(email, storable);
					}

					private void doAdapt(final Email email, final Storable storable) {
						final ParsedSubject parsedSubject = subjectHandler.parse(email.getSubject());
						Validate.isTrue(parsedSubject.hasExpectedFormat(), "invalid subject format");
						final org.cmdbuild.data.store.email.Email parent = emailStore.read(storableOf(parsedSubject
								.getEmailId()));
						final org.cmdbuild.data.store.email.Email stored = emailStore.read(storable);
						stored.setSubject(parsedSubject.getRealSubject());
						stored.setReference(parent.getReference());
						stored.setNotifyWith(parent.getNotifyWith());
						emailStore.update(stored);
					}

					private void doExecute(final Email email, final Storable storable) {
						final org.cmdbuild.data.store.email.Email stored = emailStore.read(storable);
						final Supplier<Template> emailTemplateSupplier = memoize(new Supplier<Template>() {

							@Override
							public Template get() {
								final String name = defaultString(defaultIfBlank(task.getNotificationTemplate(),
										stored.getNotifyWith()));
								return emailTemplateLogic.read(name);
							}

						});
						final Optional<EmailAccount> account = emailAccountFacade.firstOf(asList(emailTemplateSupplier
								.get().getAccount(), task.getEmailAccount()));
						final Supplier<EmailAccount> emailAccountSupplier = account.isPresent() ? ofInstance(account
								.get()) : null;
						final CMCard genericProcessCard = workflowLogic.getProcessInstance(dataView.getActivityClass()
								.getName(), stored.getReference());
						final CMCard processCard = workflowLogic.getProcessInstance(genericProcessCard.getType()
								.getName(), stored.getReference());
						final Email htmlEmail = new ForwardingEmail() {

							@Override
							protected Email delegate() {
								return email;
							}
							
							@Override
							public String getContent() {
								return addLineBreakForHtml(super.getContent());
							}

						};
						final EngineBasedTemplateResolver templateResolver = EngineBasedTemplateResolver.newInstance() //
								.withEngine(emptyStringOnNull(nullOnError( //
										UserEmailEngine.newInstance() //
												.withDataView(dataView) //
												.build())), //
										USER_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										GroupEmailEngine.newInstance() //
												.withDataView(dataView) //
												.build())), //
										GROUP_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										GroupUsersEmailEngine.newInstance() //
												.withDataView(dataView) //
												.withSeparator(EmailConstants.ADDRESSES_SEPARATOR) //
												.build() //
										)), //
										GROUP_USERS_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										EmailEngine.newInstance() //
												.withEmail(htmlEmail) //
												.build())), //
										EMAIL_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError(map( //
										EngineBasedMapper.newInstance() //
												.withText(email.getContent()) //
												.withEngine(task.getMapperEngine()) //
												.build() //
												.map() //
										))), //
										MAPPER_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										CardEngine.newInstance() //
												.withCard(processCard) //
												.build() //
										)), //
										CARD_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										CqlEngine.newInstance() //
												.withDataView(dataView) //
												.build() //
										)), //
										CQL_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										databaseEngine //
										)), //
										DB_TEMPLATE) //
								.build();
						emailTemplateSenderFactory.queued() //
								.withAccount(emailAccountSupplier) //
								.withTemplate(emailTemplateSupplier) //
								.withTemplateResolver(templateResolver) //
								.withReference(task.getId()) //
								.build() //
								.execute();
					}

				});
	}

	private Predicate<Email> notificationActive(final ReadEmailTask task) {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email input) {
				return task.isNotificationActive();
			}

		};
	}

	private Predicate<Email> subjectMatches() {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				final ParsedSubject parsedSubject = subjectHandler.parse(email.getSubject());
				if (!parsedSubject.hasExpectedFormat()) {
					return false;
				}

				try {
					emailStore.read(storableOf(parsedSubject.getEmailId()));
				} catch (final Exception e) {
					return false;
				}

				return true;
			}

		};
	}

	private Action storeAttachments(final ReadEmailTask task) {
		logger.info(marker, "adding attachments action");
		final Predicate<Email> condition = and(attachmentsActive(task), filter(task), hasAttachments());
		return new ConditionalAction( //
				condition, //
				new Action() {

					@Override
					public void execute(final Email email, final Storable storable) {
						final org.cmdbuild.data.store.email.Email stored = emailStore.read(storable);
						StoreDocument.newInstance() //
								.withDmsLogic(dmsLogic) //
								.withClassName(EMAIL_CLASS_NAME) //
								.withCardId(stored.getId()) //
								.withCategory(task.getAttachmentsCategory()) //
								.withDocuments(documentsFrom(email.getAttachments())) //
								.build() //
								.execute();
					}

				});
	}

	private Predicate<Email> attachmentsActive(final ReadEmailTask task) {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email input) {
				return task.isAttachmentsActive();
			}

		};

	}

	private Predicate<Email> hasAttachments() {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				return !isEmpty(email.getAttachments());
			}

		};
	}

	private Action startProcess(final ReadEmailTask task) {
		logger.info(marker, "adding start process action");
		final Predicate<Email> condition = and(workflowActive(task), filter(task));
		return new ConditionalAction( //
				condition, //
				new Action() {

					@Override
					public void execute(final Email email, final Storable storable) {
						final org.cmdbuild.data.store.email.Email stored = emailStore.read(storable);
						final TemplateResolver templateResolver = EngineBasedTemplateResolver.newInstance() //
								.withEngine(emptyStringOnNull(nullOnError( //
										EmailEngine.newInstance() //
												.withEmail(email) //
												.build())), //
										EMAIL_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError(map( //
										EngineBasedMapper.newInstance() //
												.withText(email.getContent()) //
												.withEngine(task.getMapperEngine()) //
												.build() //
												.map() //
										))), //
										MAPPER_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										CqlEngine.newInstance() //
												.withDataView(dataView) //
												.build() //
										)), //
										CQL_PREFIX) //
								.withEngine(emptyStringOnNull(nullOnError( //
										databaseEngine //
										)), //
										DB_TEMPLATE) //
								.build();
						StartProcess.newInstance() //
								.withWorkflowLogic(workflowLogic) //
								.withHook(new Hook() {

									@Override
									public void created(final UserProcessInstance userProcessInstance) {
										stored.setReference(userProcessInstance.getCardId());
										emailStore.update(stored);

										if (task.isWorkflowAttachments()) {
											StoreDocument.newInstance() //
													.withDmsLogic(dmsLogic) //
													.withClassName(task.getWorkflowClassName()) //
													.withCardId(userProcessInstance.getCardId()) //
													.withCategory(task.getWorkflowAttachmentsCategory()) //
													.withDocuments(documentsFrom(email.getAttachments())) //
													.build() //
													.execute();
										}
									}

									@Override
									public void advanced(final UserProcessInstance userProcessInstance) {
										// nothing to do
									}

								}) //
								.withTemplateResolver(templateResolver) //
								.withClassName(task.getWorkflowClassName()) //
								.withAttributes(task.getWorkflowAttributes()) //
								.withAdvanceStatus(task.isWorkflowAdvanceable()) //
								.build() //
								.execute();
					}

				});
	}

	private Predicate<Email> workflowActive(final ReadEmailTask task) {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email input) {
				return task.isWorkflowActive();
			}

		};
	}

	private Predicate<Email> filter(final ReadEmailTask task) {
		final Predicate<Email> output;
		final String value = task.getFilterType();
		if (REGEX.equalsIgnoreCase(value)) {
			output = addressAndSubjectRespectFilter(task);
		} else if (FUNCTION.equalsIgnoreCase(value)) {
			output = functionFilter(task);
		} else if (NONE.equalsIgnoreCase(value)) {
			output = alwaysTrue();
		} else {
			logger.warn(marker, "filter type '{}' is not expected, ignoring it", value);
			output = alwaysTrue();
		}
		return output;
	}

	private Predicate<Email> addressAndSubjectRespectFilter(final ReadEmailTask task) {
		logger.debug(marker, "creating main filter for email");
		return and(fromAddressRespectsFilter(task), subjectRespectsFilter(task));
	}

	private Predicate<Email> fromAddressRespectsFilter(final ReadEmailTask task) {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				logger.debug(marker, "checking from address");
				if (isEmpty(task.getRegexFromFilter())) {
					logger.debug(marker, "no from address filters");
					return true;
				}
				for (final String regex : task.getRegexFromFilter()) {
					final Pattern fromPattern = Pattern.compile(regex);
					final Matcher fromMatcher = fromPattern.matcher(email.getFromAddress());
					if (fromMatcher.matches()) {
						logger.debug(marker, "from address matches regex '{}'", regex);
						return true;
					}
				}
				logger.debug(marker, "from address not matching");
				return false;
			}

		};
	}

	private Predicate<Email> subjectRespectsFilter(final ReadEmailTask task) {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				logger.debug(marker, "checking subject");
				if (isEmpty(task.getRegexSubjectFilter())) {
					logger.debug(marker, "no subject filters");
					return true;
				}
				for (final String regex : task.getRegexSubjectFilter()) {
					final Pattern subjectPattern = Pattern.compile(regex);
					final Matcher subjectMatcher = subjectPattern.matcher(email.getSubject());
					if (subjectMatcher.matches()) {
						logger.debug(marker, "subject matches regex '{}'", regex);
						return true;
					}
				}
				logger.debug(marker, "subject not matching");
				return false;
			}

		};
	}

	private Predicate<Email> functionFilter(final ReadEmailTask task) {
		final String name = task.getFilterFunction();
		logger.debug(marker, "creating filter for function '{}'", name);
		final CMFunction function = checkNotNull(dataView.findFunctionByName(name), "missing function '%s'", name);
		final Map<String, CMFunctionParameter> map = Maps.uniqueIndex(function.getInputParameters(),
				new Function<CMFunctionParameter, String>() {

					@Override
					public String apply(final CMFunctionParameter input) {
						return input.getName();
					}

				});
		for (final String element : asList(FROM_ADDRESS, TO_ADDRESSES, CC_ADDRESSES, SUBJECT, CONTENT)) {
			checkArgument(map.containsKey(element), "missing input parameter '%s'", element);
			final CMAttributeType<?> type = map.get(element).getType();
			checkArgument(type instanceof TextAttributeType, "invalid type '%s' for input parameter '%s' ", type,
					element);
		}
		final Iterable<CMFunctionParameter> outputParameters = function.getOutputParameters();
		checkArgument(size(outputParameters) == 1, "output parameter must be one");
		final CMFunctionParameter outputParameter = get(outputParameters, 0);
		checkArgument(outputParameter.getType() instanceof BooleanAttributeType, "output parameter must be boolean");
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				try {
					final Map<String, Object> parameters = newHashMap();
					parameters.put(FROM_ADDRESS, email.getFromAddress());
					parameters.put(TO_ADDRESSES, serializeAddresses(email.getToAddresses()));
					parameters.put(CC_ADDRESSES, serializeAddresses(email.getCcAddresses()));
					parameters.put(SUBJECT, email.getSubject());
					parameters.put(CONTENT, email.getContent());

					final Alias f = Aliases.name("f");
					final CMQueryResult queryResult = dataView.select(anyAttribute(function, f)) //
							.from(call(function, parameters), f) //
							.run();
					boolean output;
					if (queryResult.isEmpty()) {
						logger.warn(marker, "no row returned from function '{}'", name);
						output = false;
					} else {
						output = queryResult.iterator().next() //
								.getValueSet(f) //
								.get(outputParameter.getName(), Boolean.class);
					}
					return output;
				} catch (final Exception e) {
					logger.error(marker, "error calling function", e);
					return false;
				}
			}

			public String serializeAddresses(final Iterable<String> values) {
				return on(SEPARATOR) //
						.skipNulls() //
						.join(values);
			}

		};
	}

	private Iterable<Document> documentsFrom(final Iterable<Attachment> attachments) {
		return from(attachments) //
				.transform(new Function<Attachment, Document>() {

					@Override
					public Document apply(final Attachment input) {
						return new Document() {

							@Override
							public String getName() {
								return input.getName();
							}

							@Override
							public DataHandler getDataHandler() {
								return input.getDataHandler();
							}
						};
					}

				});
	}

}
