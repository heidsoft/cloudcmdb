package org.cmdbuild.data.store.email;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

public class EmailTemplateStorableConverter extends BaseStorableConverter<EmailTemplate> {

	public static final String TABLE_NAME = "_EmailTemplate";

	public static final String NAME = Constants.CODE_ATTRIBUTE;
	public static final String DESCRIPTION = Constants.DESCRIPTION_ATTRIBUTE;
	public static final String FROM = "From";
	public static final String TO = "To";
	public static final String CC = "CC";
	public static final String BCC = "BCC";
	public static final String SUBJECT = "Subject";
	public static final String BODY = "Body";
	public static final String ACCOUNT = "Account";
	public static final String KEEP_SYNCHRONIZATION = "KeepSynchronization";
	public static final String PROMPT_SYNCHRONIZATION = "PromptSynchronization";
	public static final String DELAY = "Delay";

	private static final IdAndDescription NULL_ACCOUNT = new IdAndDescription(null, null);

	@Override
	public String getClassName() {
		return TABLE_NAME;
	}

	@Override
	public String getIdentifierAttributeName() {
		return NAME;
	}

	@Override
	public EmailTemplate convert(final CMCard card) {
		return DefaultEmailTemplate.newInstance() //
				.withId(card.getId()) //
				.withName(card.get(NAME, String.class)) //
				.withDescription(card.get(DESCRIPTION, String.class)) //
				.withFrom(card.get(FROM, String.class)) //
				.withTo(card.get(TO, String.class)) //
				.withCc(card.get(CC, String.class)) //
				.withBcc(card.get(BCC, String.class)) //
				.withSubject(card.get(SUBJECT, String.class)) //
				.withBody(card.get(BODY, String.class)) //
				.withAccount(defaultIfNull(card.get(ACCOUNT, IdAndDescription.class), NULL_ACCOUNT).getId()) //
				.withKeepSynchronization(defaultIfNull(card.get(KEEP_SYNCHRONIZATION, Boolean.class), true)) //
				.withPromptSynchronization(defaultIfNull(card.get(PROMPT_SYNCHRONIZATION, Boolean.class), false)) //
				.withDelay(defaultIfNull(card.get(DELAY, Integer.class), 0).longValue()) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final EmailTemplate emailTemplate) {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put(NAME, emailTemplate.getName());
		values.put(DESCRIPTION, emailTemplate.getDescription());
		values.put(FROM, emailTemplate.getFrom());
		values.put(TO, emailTemplate.getTo());
		values.put(CC, emailTemplate.getCc());
		values.put(BCC, emailTemplate.getBcc());
		values.put(SUBJECT, emailTemplate.getSubject());
		values.put(BODY, emailTemplate.getBody());
		values.put(ACCOUNT, emailTemplate.getAccount());
		values.put(KEEP_SYNCHRONIZATION, emailTemplate.isKeepSynchronization());
		values.put(PROMPT_SYNCHRONIZATION, emailTemplate.isPromptSynchronization());
		values.put(DELAY, emailTemplate.getDelay());
		return values;
	}

}
