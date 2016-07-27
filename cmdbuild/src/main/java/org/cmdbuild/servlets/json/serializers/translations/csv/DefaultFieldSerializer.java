package org.cmdbuild.servlets.json.serializers.translations.csv;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;
import static org.cmdbuild.services.store.menu.MenuConstants.DEFAULT_MENU_GROUP_NAME;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.DEFAULT;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.DESCRIPTION;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.IDENTIFIER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.KEY_SEPARATOR;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.matchFilterByName;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.matchReportByCode;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.matchViewByName;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.nullableIterable;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.filter.FilterLogic;
import org.cmdbuild.logic.filter.FilterLogic.Filter;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.logic.translation.converter.MenuItemConverter;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.model.view.View.ViewType;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultFieldSerializer implements FieldSerializer {

	private final String identifier;
	private final String owner;
	private final String fieldName;
	private final TranslatableElement element;
	private final TranslationLogic translationLogic;
	private final Iterable<String> selectedLanguages;
	private final DataAccessLogic dataLogic;
	private final FilterLogic filterLogic;
	private final LookupStore lookupStore;
	private final MenuLogic menuLogic;
	private final ViewLogic viewLogic;
	private final ReportStore reportStore;

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<FieldSerializer> {

		private String identifier;
		private String owner;
		private String fieldName;
		private TranslatableElement element;
		private TranslationLogic translationLogic;
		private Iterable<String> selectedLanguages;
		private DataAccessLogic dataLogic;
		private FilterLogic filterLogic;
		private LookupStore lookupStore;
		private MenuLogic menuLogic;
		private ViewLogic viewLogic;
		private ReportStore reportStore;

		@Override
		public FieldSerializer build() {
			return new DefaultFieldSerializer(this);
		}

		public Builder withDataLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public Builder withElement(final TranslatableElement element) {
			this.element = element;
			return this;
		}

		public Builder withSelectedLanguages(final Iterable<String> selectedLanguages) {
			this.selectedLanguages = selectedLanguages;
			return this;
		}

		public Builder withFieldName(final String fieldName) {
			this.fieldName = fieldName;
			return this;
		}

		public Builder withFilterLogic(final FilterLogic filterLogic) {
			this.filterLogic = filterLogic;
			return this;
		}

		public Builder withMenuLogic(final MenuLogic menuLogic) {
			this.menuLogic = menuLogic;
			return this;
		}

		public Builder withIdentifier(final String identifier) {
			this.identifier = identifier;
			return this;
		}

		public Builder withOwner(final String owner) {
			this.owner = owner;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public Builder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

		public Builder withViewLogic(final ViewLogic viewLogic) {
			this.viewLogic = viewLogic;
			return this;
		}

		public Builder withReportStore(final ReportStore reportStore) {
			this.reportStore = reportStore;
			return this;
		}

	}

	private DefaultFieldSerializer(final Builder builder) {
		this.element = builder.element;
		this.selectedLanguages = builder.selectedLanguages;
		this.fieldName = builder.fieldName;
		this.identifier = builder.identifier;
		this.owner = builder.owner;
		this.translationLogic = builder.translationLogic;
		this.dataLogic = builder.dataLogic;
		this.filterLogic = builder.filterLogic;
		this.menuLogic = builder.menuLogic;
		this.lookupStore = builder.lookupStore;
		this.viewLogic = builder.viewLogic;
		this.reportStore = builder.reportStore;
	}

	@Override
	public Optional<CsvTranslationRecord> serialize() {
		final CsvTranslationRecord record;
		final String key = buildKey();
		final String description = buildDescription();
		final Map<String, String> translations = readTranslations();
		final String defaultValue = fetchDefault();
		record = (isNotBlank(defaultValue)) ? writeRow(key, description, defaultValue, translations) : null;
		final Optional<CsvTranslationRecord> _record = (record != null) ? Optional.of(record) : Optional.absent();
		return _record;
	}

	// FIXME: do it better
	private String fetchDefault() {
		String defaultValue = EMPTY;
		if (element.equals(TranslatableElement.CLASS) && fieldName.equals(ClassConverter.description())) {
			defaultValue = dataLogic.findClass(identifier).getDescription();
		} else if (element.equals(TranslatableElement.ATTRIBUTECLASS)
				&& fieldName.equals(AttributeConverter.description())) {
			final CMClass ownerClass = dataLogic.findClass(owner);
			defaultValue = ownerClass.getAttribute(identifier).getDescription();
		} else if (element.equals(TranslatableElement.ATTRIBUTECLASS) && fieldName.equals(AttributeConverter.group())) {
			final CMClass ownerClass = dataLogic.findClass(owner);
			defaultValue = ownerClass.getAttribute(identifier).getGroup();
		} else if (element.equals(TranslatableElement.DOMAIN)) {
			if (fieldName.equals(DomainConverter.description())) {
				defaultValue = dataLogic.findDomain(identifier).getDescription();
			} else if (fieldName.equals(DomainConverter.directDescription())) {
				defaultValue = dataLogic.findDomain(identifier).getDescription1();
			} else if (fieldName.equals(DomainConverter.inverseDescription())) {
				defaultValue = dataLogic.findDomain(identifier).getDescription2();
			} else if (fieldName.equals(DomainConverter.masterDetail())) {
				defaultValue = dataLogic.findDomain(identifier).getMasterDetailDescription();
			}
		} else if (element.equals(TranslatableElement.ATTRIBUTEDOMAIN)) {
			if (fieldName.equals(AttributeConverter.description())) {
				final CMDomain ownerDomain = dataLogic.findDomain(owner);
				defaultValue = ownerDomain.getAttribute(identifier).getDescription();
			}
		} else if (element.equals(TranslatableElement.FILTER)) {
			if (fieldName.equals(FilterConverter.description())) {
				final Filter matchingFilter = getOnlyElement((readByName(identifier, filterLogic)));
				defaultValue = matchingFilter.getDescription();
			}
		} else if (element.equals(TranslatableElement.LOOKUP_VALUE)) {
			if (fieldName.equals(LookupConverter.description())) {
				final Lookup matchingLookup = getOnlyElement(lookupStore.readFromUuid(identifier));
				defaultValue = matchingLookup.getDescription();
			}
		} else if (element.equals(TranslatableElement.MENU_ITEM)) {
			if (fieldName.equals(MenuItemConverter.description())) {
				defaultValue = loadMenuDescription(identifier, owner);
			}
		} else if (element.equals(TranslatableElement.REPORT)) {
			if (fieldName.equals(ReportConverter.description())) {
				defaultValue = loadReportDescription(identifier);
			}
		} else if (element.equals(TranslatableElement.VIEW)) {
			if (fieldName.equals(ViewConverter.description())) {
				defaultValue = loadViewDescription(identifier);
			}
		}
		return defaultIfBlank(defaultValue, EMPTY);
	}

	private Iterable<Filter> readByName(final String identifier, final FilterLogic filterLogic) {
		return filter(filterLogic.readShared(null, 0, 0), matchFilterByName(identifier));
	}

	private String loadViewDescription(final String name) {
		final List<View> filterViews = viewLogic.read(ViewType.FILTER);
		Iterable<View> matchingViews = filter(filterViews, matchViewByName(name));
		if (isEmpty(matchingViews)) {
			final List<View> sqlViews = viewLogic.read(ViewType.SQL);
			matchingViews = filter(sqlViews, matchViewByName(name));
		}
		return getOnlyElement(matchingViews).getDescription();
	}

	private String loadReportDescription(final String code) {
		Iterable<Report> matchingReports = Lists.newArrayList();
		for (final ReportType type : ReportType.values()) {
			final Iterable<Report> reportsOfType = reportStore.findReportsByType(type);
			matchingReports = filter(reportsOfType, matchReportByCode(code));
			if (!Iterables.isEmpty(matchingReports)) {
				break;
			}
		}
		return getOnlyElement(matchingReports).getDescription();
	}

	private String loadMenuDescription(final String uuid, final String group) {
		final MenuItem rootEntry = menuLogic.read(group);
		return fetchMenuItemByUuid(rootEntry, uuid).getDescription();
	}

	private static MenuItem fetchMenuItemByUuid(final MenuItem root, final String uuid) {
		MenuItem matchingItem = null;
		if (uuid.equals(root.getUniqueIdentifier())) {
			matchingItem = root;
		}
		if (matchingItem == null) {
			for (final MenuItem child : nullableIterable(root.getChildren())) {
				matchingItem = fetchMenuItemByUuid(child, uuid);
				if (matchingItem != null) {
					break;
				}
			}
		}
		return matchingItem;
	}

	private Map<String, String> readTranslations() {
		final Converter converter = element.createConverter(fieldName);
		final TranslationObject translationObject = converter //
				.withIdentifier(identifier) //
				.withOwner(owner) //
				.create();
		final Map<String, String> translations = translationLogic.readAll(translationObject);
		return translations;
	}

	private String buildKey() {
		String key = EMPTY;
		final String FORMAT_NO_OWNER = "%s%c%s%c%s";
		final String FORMAT_WITH_OWNER = "%s%c%s%c%s%c%s";
		if (isBlank(owner)) {
			key = String.format(FORMAT_NO_OWNER, element.getType(), KEY_SEPARATOR, identifier, KEY_SEPARATOR,
					fieldName);
		} else {
			key = String.format(FORMAT_WITH_OWNER, element.getType(), KEY_SEPARATOR, owner, KEY_SEPARATOR, identifier,
					KEY_SEPARATOR, fieldName);
		}
		return key;
	}

	private String buildDescription() {
		final String FORMAT = element.extendedDescriptionFormat();
		String description = EMPTY;
		if (isBlank(owner)) {
			description = String.format(FORMAT, lowerCase(join(splitByCharacterTypeCamelCase(fieldName), " ")),
					identifier);
		} else {
			description = String.format(FORMAT, lowerCase(join(splitByCharacterTypeCamelCase(fieldName), " ")),
					identifier, FOR_MENU_REPLACE_DEFAULT.apply(owner));
		}
		return description;
	}

	private final Function<String, String> FOR_MENU_REPLACE_DEFAULT = new Function<String, String>() {

		@Override
		public String apply(final String input) {
			String convertedGroupName = input;
			if (element == TranslatableElement.MENU_ITEM && input.equals(DEFAULT_MENU_GROUP_NAME)) {
				convertedGroupName = DEFAULT;
			}
			return convertedGroupName;
		}

	};

	CsvTranslationRecord writeRow(final String key, final String description, final String defaultValue,
			final Map<String, String> translations) {
		final Map<String, Object> map = Maps.newHashMap();
		map.put(IDENTIFIER, key);
		map.put(DESCRIPTION, description);
		map.put(DEFAULT, defaultValue);
		for (final String language : selectedLanguages) {
			final String value = defaultIfNull(translations.get(language), EMPTY);
			map.put(language, value);
		}
		return new CsvTranslationRecord(map);
	}

}
