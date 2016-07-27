package org.cmdbuild.workflow.widget;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.Maps.filterEntries;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.email.EmailAttachmentsLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.model.widget.ManageEmail;
import org.cmdbuild.model.widget.ManageEmail.EmailTemplate;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps.EntryTransformer;

public class ManageEmailWidgetFactory extends ValuePairWidgetFactory {

	// TODO change logger
	private static final Logger logger = Log.WORKFLOW;
	private static final Marker marker = MarkerFactory.getMarker(ManageEmailWidgetFactory.class.getName());

	private static final String IMPLICIT_TEMPLATE_NAME = "implicitTemplateName";
	private final static String FROM_ADDRESS = "FromAddress";
	private final static String TO_ADDRESSES = "ToAddresses";
	private final static String CC_ADDRESSES = "CCAddresses";
	private final static String BCC_ADDRESSES = "BCCAddresses";
	private final static String SUBJECT = "Subject";
	private final static String CONTENT = "Content";
	private final static String CONDITION = "Condition";
	private final static String READ_ONLY = "ReadOnly";
	private final static String NOTIFY_TEMPLATE_NAME = "NotifyWith";
	private final static String TEMPLATE = "Template";
	private final static String NO_SUBJECT_PREFIX = "NoSubjectPrefix";
	private final static String GLOBAL_NO_SUBJECT_PREFIX = "GlobalNoSubjectPrefix";
	private final static String KEEP_SYNCHRONIZATION = "KeepSynchronization";
	private final static String PROMPT_SYNCHRONIZATION = "PromptSynchronization";
	private final static String DELAY_IN_SECONDS = "Delay";

	private final static String WIDGET_NAME = "manageEmail";

	private final EmailLogic emailLogic;
	private final EmailAttachmentsLogic emailAttachmentsLogic;
	private final EmailTemplateLogic emailTemplateLogic;

	public ManageEmailWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier,
			final EmailLogic emailLogic, final EmailAttachmentsLogic emailAttachmentsLogic,
			final EmailTemplateLogic emailTemplateLogic) {
		super(templateRespository, notifier);
		this.emailLogic = emailLogic;
		this.emailAttachmentsLogic = emailAttachmentsLogic;
		this.emailTemplateLogic = emailTemplateLogic;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	/*
	 * naive but fast to write solution ...first do it works...
	 */
	@Override
	protected Widget createWidget(final WidgetDefinition definition) {
		// I want to preserve the order
		final Map<String, EmailTemplate> emailTemplatesByName = newLinkedHashMap();
		final Collection<String> managedParameters = newHashSet();
		managedParameters.add(READ_ONLY);
		managedParameters.add(BUTTON_LABEL);
		managedParameters.add(GLOBAL_NO_SUBJECT_PREFIX);

		final Map<String, String> templates = filterKeysStartingWith(definition, TEMPLATE);
		for (final String key : templates.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, TEMPLATE);
			final String name = readString(definition.get(key));
			if (isNotBlank(name)) {
				try {
					final EmailTemplateLogic.Template _template = emailTemplateLogic.read(name);
					template.setFromAddress(_template.getFrom());
					template.setToAddresses(_template.getTo());
					template.setCcAddresses(_template.getCc());
					template.setBccAddresses(_template.getBcc());
					template.setSubject(_template.getSubject());
					template.setContent(_template.getBody());
					template.setVariables(_template.getVariables());
					template.setAccount(_template.getAccount());
					template.setKeepSynchronization(_template.isKeepSynchronization());
					template.setPromptSynchronization(_template.isPromptSynchronization());
					template.setDelay(_template.getDelay());
				} catch (final Exception e) {
					logger.warn(marker, "error getting template, skipping", e);
				}
			}
		}
		managedParameters.addAll(templates.keySet());

		final Map<String, String> fromAddresses = filterKeysStartingWith(definition, FROM_ADDRESS);
		for (final String key : fromAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, FROM_ADDRESS);
			template.setFromAddress(readString(definition.get(key)));
		}
		managedParameters.addAll(fromAddresses.keySet());

		final Map<String, String> toAddresses = filterKeysStartingWith(definition, TO_ADDRESSES);
		for (final String key : toAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, TO_ADDRESSES);
			template.setToAddresses(readString(definition.get(key)));
		}
		managedParameters.addAll(toAddresses.keySet());

		final Map<String, String> ccAddresses = filterKeysStartingWith(definition, CC_ADDRESSES);
		for (final String key : ccAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, CC_ADDRESSES);
			template.setCcAddresses(readString(definition.get(key)));
		}
		managedParameters.addAll(ccAddresses.keySet());

		final Map<String, String> bccAddresses = filterKeysStartingWith(definition, BCC_ADDRESSES);
		for (final String key : bccAddresses.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, BCC_ADDRESSES);
			template.setBccAddresses(readString(definition.get(key)));
		}
		managedParameters.addAll(bccAddresses.keySet());

		final Map<String, String> subjects = filterKeysStartingWith(definition, SUBJECT);
		for (final String key : subjects.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, SUBJECT);
			template.setSubject(readString(definition.get(key)));
		}
		managedParameters.addAll(subjects.keySet());

		final Map<String, String> notifyWithThemplate = filterKeysStartingWith(definition, NOTIFY_TEMPLATE_NAME);
		for (final String key : notifyWithThemplate.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, NOTIFY_TEMPLATE_NAME);
			template.setNotifyWith(readString(definition.get(key)));
		}
		managedParameters.addAll(notifyWithThemplate.keySet());

		final Map<String, String> contents = filterKeysStartingWith(definition, CONTENT);
		for (final String key : contents.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, CONTENT);
			template.setContent(readString(definition.get(key)));
		}
		managedParameters.addAll(contents.keySet());

		final Map<String, String> conditions = filterKeysStartingWith(definition, CONDITION);
		for (final String key : conditions.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, CONDITION);
			template.setCondition(readString(definition.get(key)));
		}
		managedParameters.addAll(conditions.keySet());

		final Map<String, String> noSubjectPrexifes = filterKeysStartingWith(definition, NO_SUBJECT_PREFIX);
		for (final String key : noSubjectPrexifes.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, NO_SUBJECT_PREFIX);
			template.setNoSubjectPrefix(readBooleanFalseIfMissing(definition.get(key)));
		}
		managedParameters.addAll(noSubjectPrexifes.keySet());

		final Map<String, String> keepSynchronizations = filterKeysStartingWith(definition, KEEP_SYNCHRONIZATION);
		for (final String key : keepSynchronizations.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, KEEP_SYNCHRONIZATION);
			template.setKeepSynchronization(readBoolean(definition.get(key), true));
		}
		managedParameters.addAll(keepSynchronizations.keySet());

		final Map<String, String> promptSynchronizations = filterKeysStartingWith(definition, PROMPT_SYNCHRONIZATION);
		for (final String key : promptSynchronizations.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, PROMPT_SYNCHRONIZATION);
			template.setPromptSynchronization(readBooleanFalseIfMissing(definition.get(key)));
		}
		managedParameters.addAll(promptSynchronizations.keySet());

		final Map<String, String> delays = filterKeysStartingWith(definition, DELAY_IN_SECONDS);
		for (final String key : delays.keySet()) {
			final EmailTemplate template = getTemplateForKey(emailTemplatesByName, key, DELAY_IN_SECONDS);
			template.setDelay(defaultIfNull(readInteger(definition.get(key)), 0) * 1000);
		}
		managedParameters.addAll(delays.keySet());

		final ManageEmail widget = new ManageEmail(emailLogic, emailAttachmentsLogic);
		widget.setTemplates(transformEntries(emailTemplatesByName,
				new EntryTransformer<String, EmailTemplate, EmailTemplate>() {

					final Map<String, String> unmanaged = extractUnmanagedStringParameters(definition,
							managedParameters);

					@Override
					public EmailTemplate transformEntry(final String key, final EmailTemplate value) {
						value.setKey(key);

						final Map<String, String> variables = value.getVariables();
						final Map<String, String> newVariables = newHashMap(unmanaged);
						newVariables.putAll(variables);
						value.setVariables(newVariables);

						return value;
					}

				}).values());
		widget.setReadOnly(definition.containsKey(READ_ONLY));
		widget.setNoSubjectPrefix(readBooleanFalseIfMissing(definition.get(GLOBAL_NO_SUBJECT_PREFIX)));

		return widget;
	}

	private Map<String, String> filterKeysStartingWith(final Map<String, Object> valueMap, final String prefix) {
		return transformValues(filterEntries(valueMap, new Predicate<Entry<String, Object>>() {

			@Override
			public boolean apply(final Entry<String, Object> input) {
				return input.getKey().startsWith(prefix);
			};

		}), toStringFunction());
	}

	private EmailTemplate getTemplateForKey(final Map<String, EmailTemplate> templates, final String key,
			final String prefix) {
		final String id = defaultIfEmpty(key.replaceFirst(prefix, EMPTY), IMPLICIT_TEMPLATE_NAME);
		if (templates.containsKey(id)) {
			return templates.get(id);
		} else {
			final EmailTemplate template = new EmailTemplate();
			templates.put(id, template);
			return template;
		}
	}
}
