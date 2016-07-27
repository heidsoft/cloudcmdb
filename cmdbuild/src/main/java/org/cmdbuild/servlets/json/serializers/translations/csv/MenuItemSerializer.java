package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class MenuItemSerializer extends DefaultElementSerializer {

	private final MenuItem entry;
	private final String groupName;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final TranslatableElement element = TranslatableElement.MENU_ITEM;
		return serializeFields(groupName, entry.getUniqueIdentifier(), element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<MenuItemSerializer> {

		private DataAccessLogic dataLogic;
		private Iterable<String> selectedLanguages;
		private TranslationLogic translationLogic;
		private MenuItem entry;
		private MenuLogic menuLogic;
		private String groupName;

		@Override
		public MenuItemSerializer build() {
			return new MenuItemSerializer(this);
		}

		public Builder withDataAccessLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public Builder withSelectedLanguages(final Iterable<String> selectedLanguages) {
			this.selectedLanguages = selectedLanguages;
			return this;
		}

		public Builder withGroupName(final String groupName) {
			this.groupName = groupName;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public Builder withMenuItem(final MenuItem entry) {
			this.entry = entry;
			return this;
		}

		public Builder withMenuLogic(final MenuLogic menuLogic) {
			this.menuLogic = menuLogic;
			return this;
		}

	}

	private MenuItemSerializer(final Builder builder) {
		super.dataLogic = builder.dataLogic;
		super.selectedLanguages = builder.selectedLanguages;
		super.translationLogic = builder.translationLogic;
		super.menuLogic = builder.menuLogic;
		this.entry = builder.entry;
		this.groupName = builder.groupName;
	}

}
