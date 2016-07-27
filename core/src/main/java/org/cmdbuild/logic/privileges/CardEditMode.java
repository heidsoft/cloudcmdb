package org.cmdbuild.logic.privileges;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GRANT_CLASS_NAME;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.UI_CARD_EDIT_MODE_ATTRIBUTE;

import com.google.common.base.Function;

public class CardEditMode {

	private static final String KEY_VALUE_SEPARATOR = "=";
	private static final String ENTRY_SEPARATOR = ",";
	private static final String CARD_EDIT_MODE_PERSISTENCE_FORMAT = "create=%b,modify=%b,clone=%b,remove=%b";
	private final boolean allowCreate;
	private final boolean allowUpdate;
	private final boolean allowClone;
	private final boolean allowRemove;

	public static final CardEditMode ALLOW_ALL = CardEditMode.newInstance().build();

	public CardEditMode(final Builder builder) {
		this.allowClone = builder.allowClone;
		this.allowCreate = builder.allowCreate;
		this.allowRemove = builder.allowRemove;
		this.allowUpdate = builder.allowUpdate;
	}

	public boolean isAllowAll() {
		return allowClone && allowCreate && allowRemove && allowUpdate;
	}

	public boolean isAllowCreate() {
		return allowCreate;
	}

	public boolean isAllowUpdate() {
		return allowUpdate;
	}

	public boolean isAllowClone() {
		return allowClone;
	}

	public boolean isAllowRemove() {
		return allowRemove;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.cmdbuild.common.Builder<CardEditMode> {

		private boolean allowCreate = true;
		private boolean allowUpdate = true;
		private boolean allowClone = true;
		private boolean allowRemove = true;

		@Override
		public CardEditMode build() {
			return new CardEditMode(this);
		}

		public Builder isCreateAllowed(final boolean allowCreate) {
			this.allowCreate = allowCreate;
			return this;
		}

		public Builder isUpdateAllowed(final boolean allowUpdate) {
			this.allowUpdate = allowUpdate;
			return this;
		}

		public Builder isCloneAllowed(final boolean allowClone) {
			this.allowClone = allowClone;
			return this;
		}

		public Builder isDeleteAllowed(final boolean allowRemove) {
			this.allowRemove = allowRemove;
			return this;
		}
	}

	public static Function<CardEditMode, String> LOGIC_TO_PERSISTENCE = new Function<CardEditMode, String>() {

		@Override
		public String apply(final CardEditMode input) {
			String persistenceString = null;
			final CardEditMode notNullInput = (CardEditMode) defaultIfNull(input, ALLOW_ALL);
			persistenceString = String.format(CARD_EDIT_MODE_PERSISTENCE_FORMAT, //
					notNullInput.isAllowCreate(), //
					notNullInput.isAllowUpdate(), //
					notNullInput.isAllowClone(), //
					notNullInput.isAllowRemove());
			return persistenceString;
		}
	};

	public static Function<String, CardEditMode> PERSISTENCE_TO_LOGIC = new Function<String, CardEditMode>() {
		@Override
		public CardEditMode apply(final String input) {
			CardEditMode cardEditMode = CardEditMode.ALLOW_ALL;
			if (!isBlank(input)) {
				final String[] modes = input.split(ENTRY_SEPARATOR);
				try {
					cardEditMode = CardEditMode.newInstance() //
							.isCreateAllowed(Boolean.parseBoolean(modes[0].split(KEY_VALUE_SEPARATOR)[1])) //
							.isUpdateAllowed(Boolean.parseBoolean(modes[1].split(KEY_VALUE_SEPARATOR)[1])) //
							.isCloneAllowed(Boolean.parseBoolean(modes[2].split(KEY_VALUE_SEPARATOR)[1])) //
							.isDeleteAllowed(Boolean.parseBoolean(modes[3].split(KEY_VALUE_SEPARATOR)[1])) //
							.build();
				} catch (final Exception e) {
					final String format = "Format '%s' not supported for '%s' attribute of '%s' table";
					String.format(format, input, UI_CARD_EDIT_MODE_ATTRIBUTE, GRANT_CLASS_NAME);
					throw new UnsupportedOperationException(String.format(format, //
							input, //
							UI_CARD_EDIT_MODE_ATTRIBUTE, //
							GRANT_CLASS_NAME), e);
				}
			}
			return cardEditMode;
		}
	};

}
