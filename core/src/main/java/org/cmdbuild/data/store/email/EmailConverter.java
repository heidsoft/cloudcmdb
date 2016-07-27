package org.cmdbuild.data.store.email;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.data.store.email.EmailConstants.ACCOUNT_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.BCC_ADDRESSES_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.CARD_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.CC_ADDRESSES_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.CONTENT_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.DELAY_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_STATUS_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.FROM_ADDRESS_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.KEEP_SYNCHRONIZATION_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.NOTIFY_WITH_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.NO_SUBJECT_PREFIX_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.PROMPT_SYNCHRONIZATION_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.SUBJECT_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.TEMPLATE_ATTRIBUTE;
import static org.cmdbuild.data.store.email.EmailConstants.TO_ADDRESSES_ATTRIBUTE;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.data.store.dao.BaseStorableConverter;

import com.google.common.collect.Maps;

public class EmailConverter extends BaseStorableConverter<Email> {

	private final EmailStatusConverter converter;

	public EmailConverter(final EmailStatusConverter converter) {
		this.converter = converter;
	}

	@Override
	public String getClassName() {
		return EMAIL_CLASS_NAME;
	}

	@Override
	public Email convert(final CMCard card) {
		final Email email = new Email(card.getId());
		email.setFromAddress(defaultIfBlank(card.get(FROM_ADDRESS_ATTRIBUTE, String.class), null));
		email.setToAddresses(defaultIfBlank(card.get(TO_ADDRESSES_ATTRIBUTE, String.class), null));
		email.setCcAddresses(defaultIfBlank(card.get(CC_ADDRESSES_ATTRIBUTE, String.class), null));
		email.setBccAddresses(defaultIfBlank(card.get(BCC_ADDRESSES_ATTRIBUTE, String.class), null));
		email.setSubject(defaultIfBlank(card.get(SUBJECT_ATTRIBUTE, String.class), null));
		email.setContent(defaultIfBlank(card.get(CONTENT_ATTRIBUTE, String.class), null));
		email.setNotifyWith(defaultIfBlank(card.get(NOTIFY_WITH_ATTRIBUTE, String.class), null));
		email.setNoSubjectPrefix(defaultIfNull(card.get(NO_SUBJECT_PREFIX_ATTRIBUTE, Boolean.class), false));
		email.setAccount(defaultIfBlank(card.get(ACCOUNT_ATTRIBUTE, String.class), null));
		email.setTemplate(defaultIfBlank(card.get(TEMPLATE_ATTRIBUTE, String.class), null));
		email.setKeepSynchronization(defaultIfNull(card.get(KEEP_SYNCHRONIZATION_ATTRIBUTE, Boolean.class), true));
		email.setPromptSynchronization(defaultIfNull(card.get(PROMPT_SYNCHRONIZATION_ATTRIBUTE, Boolean.class), false));
		email.setDelay(defaultIfNull(card.get(DELAY_ATTRIBUTE, Integer.class), 0).longValue());
		email.setDate((card.getBeginDate()));
		email.setStatus(converter.fromId(card.get(EMAIL_STATUS_ATTRIBUTE, IdAndDescription.class).getId()));
		email.setReference((card.get(CARD_ATTRIBUTE) != null) ? card.get(CARD_ATTRIBUTE, IdAndDescription.class)
				.getId() : null);
		return email;
	}

	@Override
	public Map<String, Object> getValues(final Email email) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(FROM_ADDRESS_ATTRIBUTE, email.getFromAddress());
		values.put(TO_ADDRESSES_ATTRIBUTE, email.getToAddresses());
		values.put(CC_ADDRESSES_ATTRIBUTE, email.getCcAddresses());
		values.put(BCC_ADDRESSES_ATTRIBUTE, email.getBccAddresses());
		values.put(SUBJECT_ATTRIBUTE, email.getSubject());
		values.put(CONTENT_ATTRIBUTE, email.getContent());
		values.put(CARD_ATTRIBUTE, email.getReference());
		values.put(NOTIFY_WITH_ATTRIBUTE, email.getNotifyWith());
		values.put(NO_SUBJECT_PREFIX_ATTRIBUTE, email.isNoSubjectPrefix());
		values.put(ACCOUNT_ATTRIBUTE, email.getAccount());
		values.put(TEMPLATE_ATTRIBUTE, email.getTemplate());
		values.put(KEEP_SYNCHRONIZATION_ATTRIBUTE, email.isKeepSynchronization());
		values.put(PROMPT_SYNCHRONIZATION_ATTRIBUTE, email.isPromptSynchronization());
		values.put(DELAY_ATTRIBUTE, email.getDelay());
		values.put(EMAIL_STATUS_ATTRIBUTE, converter.toId(email.getStatus()));
		return values;
	}

}
