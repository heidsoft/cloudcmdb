package org.cmdbuild.logic.translation;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreFactory;
import org.cmdbuild.data.store.translation.Element;
import org.cmdbuild.data.store.translation.Translation;
import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.cmdbuild.logic.translation.object.ClassDescription;
import org.cmdbuild.logic.translation.object.DomainAttributeDescription;
import org.cmdbuild.logic.translation.object.DomainDescription;
import org.cmdbuild.logic.translation.object.DomainDirectDescription;
import org.cmdbuild.logic.translation.object.DomainInverseDescription;
import org.cmdbuild.logic.translation.object.DomainMasterDetailLabel;
import org.cmdbuild.logic.translation.object.FilterDescription;
import org.cmdbuild.logic.translation.object.InstanceName;
import org.cmdbuild.logic.translation.object.LookupDescription;
import org.cmdbuild.logic.translation.object.MenuItemDescription;
import org.cmdbuild.logic.translation.object.ReportDescription;
import org.cmdbuild.logic.translation.object.ViewDescription;
import org.cmdbuild.logic.translation.object.WidgetLabel;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class DefaultTranslationLogic implements TranslationLogic {

	private static class ElementCreator implements TranslationObjectVisitor {

		private static final String DESCRIPTION = "description";
		private static final String DIRECT_DESCRIPTION = "directdescription";
		private static final String GROUP = "group";
		private static final String INSTANCENAME = "instancename";
		private static final String INVERSE_DESCRIPTION = "inversedescription";
		private static final String MASTERDETAIL_LABEL = "masterdetaillabel";

		private static ElementCreator of(final TranslationObject translationObject) {
			return new ElementCreator(translationObject);
		}

		private final TranslationObject translationObject;
		private String value;

		private ElementCreator(final TranslationObject translationObject) {
			this.translationObject = translationObject;
		}

		public Element create() {
			translationObject.accept(this);
			Validate.notNull(value, "conversion error");
			return Element.of(value);
		}

		@Override
		public void visit(final ClassDescription translationObject) {
			value = format("class.%s.%s", //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final ClassAttributeDescription translationObject) {
			value = format("attributeclass.%s.%s.%s", //
					translationObject.getClassName(), //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final ClassAttributeGroup translationObject) {
			value = format("attributeclass.%s.%s.%s", //
					translationObject.getClassName(), //
					translationObject.getName(), //
					GROUP);
		}

		@Override
		public void visit(final DomainDescription translationObject) {
			value = format("domain.%s.%s", //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final DomainDirectDescription translationObject) {
			value = format("domain.%s.%s", //
					translationObject.getName(), //
					DIRECT_DESCRIPTION);
		}

		@Override
		public void visit(final DomainInverseDescription translationObject) {
			value = format("domain.%s.%s", //
					translationObject.getName(), //
					INVERSE_DESCRIPTION);
		}

		@Override
		public void visit(final DomainMasterDetailLabel translationObject) {
			value = format("domain.%s.%s", //
					translationObject.getName(), //
					MASTERDETAIL_LABEL);
		}

		@Override
		public void visit(final DomainAttributeDescription translationObject) {
			value = format("attributedomain.%s.%s.%s", //
					translationObject.getDomainName(), //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final FilterDescription translationObject) {
			value = format("filter.%s.%s", //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final InstanceName translationObject) {
			value = format(INSTANCENAME);
		}

		@Override
		public void visit(final LookupDescription translationObject) {
			value = format("lookup.%s.%s", //
					translationObject.getName(), DESCRIPTION);
		}

		@Override
		public void visit(final MenuItemDescription translationObject) {
			value = format("menuitem.%s.%s", //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final NullTranslationObject translationObject) {
			value = EMPTY;
		}

		@Override
		public void visit(final ReportDescription translationObject) {
			value = format("report.%s.%s", //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final ViewDescription translationObject) {
			value = format("view.%s.%s", //
					translationObject.getName(), //
					DESCRIPTION);
		}

		@Override
		public void visit(final WidgetLabel translationObject) {
			value = format("widget.%s.%s", //
					translationObject.getName(), //
					DESCRIPTION);
		}

	}

	private static final class MustBeCreated implements Predicate<Translation> {

		private final Iterable<Translation> oldTranslations;

		private static Predicate<Translation> withOldTranslations(final Iterable<Translation> oldTranslations) {
			return new MustBeCreated(oldTranslations);
		}

		private MustBeCreated(final Iterable<Translation> oldTranslations) {
			this.oldTranslations = oldTranslations;
		}

		@Override
		public boolean apply(final Translation input) {
			final boolean toCreate = !isBlank(input.getValue())
					&& !ContainedInTraslations.containedIn(oldTranslations).apply(input);
			return toCreate;
		}
	}

	private static final class MustBeUpdated implements Predicate<Translation> {

		private final Iterable<Translation> newTranslations;

		private static Predicate<Translation> withNewTranslations(final Iterable<Translation> newTranslations) {
			return new MustBeUpdated(newTranslations);
		}

		private MustBeUpdated(final Iterable<Translation> newTranslations) {
			this.newTranslations = newTranslations;
		}

		@Override
		public boolean apply(final Translation input) {
			boolean toUpdate = false;
			for (final Translation translation : newTranslations) {
				if (translation.getLang().equals(input.getLang()) && !isBlank(translation.getValue())) {
					toUpdate = true;
					break;
				}
			}
			return toUpdate;
		}
	}

	private static final class MustBeDeleted implements Predicate<Translation> {

		private final Iterable<Translation> newTranslations;

		private static Predicate<Translation> withNewTranslations(final Iterable<Translation> newTranslations) {
			return new MustBeDeleted(newTranslations);
		}

		private MustBeDeleted(final Iterable<Translation> newTranslations) {
			this.newTranslations = newTranslations;
		}

		@Override
		public boolean apply(final Translation oldTranslation) {
			boolean toDelete = false;
			for (final Translation newTranslation : newTranslations) {
				if (newTranslation.getLang().equals(oldTranslation.getLang()) && isBlank(newTranslation.getValue())) {
					toDelete = true;
					break;
				}
			}
			return toDelete;
		}
	}

	private static final class ContainedInTraslations implements Predicate<Translation> {

		public static final ContainedInTraslations containedIn(final Iterable<? extends Translation> translations) {
			return new ContainedInTraslations(translations);
		}

		private final Iterable<? extends Translation> translations;

		private ContainedInTraslations(final Iterable<? extends Translation> translations) {
			this.translations = translations;
		}

		@Override
		public boolean apply(final Translation input) {
			for (final Translation translation : translations) {
				if (translation.getLang().equals(input.getLang())) {
					return true;
				}
			}
			return false;
		}

	}

	private static Function<Translation, String> TRANSLATION_TO_LANG = new Function<Translation, String>() {

		@Override
		public String apply(final Translation input) {
			return input.getLang();
		}

	};

	private static Ordering<Translation> ORDER_BY_LANG = new Ordering<Translation>() {

		@Override
		public int compare(final Translation left, final Translation right) {
			return left.getLang().compareTo(right.getLang());
		}
	};

	private final StoreFactory<Translation> storeFactory;
	private final SetupFacade setupFacade;

	public DefaultTranslationLogic(final StoreFactory<Translation> storeFactory, final SetupFacade setupFacade) {
		this.storeFactory = storeFactory;
		this.setupFacade = setupFacade;
	}

	@Override
	public void create(final TranslationObject translationObject) {
		// TODO element, language and value must not be null
		final Element element = ElementCreator.of(translationObject).create();
		final Collection<Translation> translations = extractTranslations(translationObject, element);
		final Store<Translation> store = storeFactory.create(element);
		for (final Translation translation : translations) {
			store.create(translation);
		}
	}
	
	@Override
	public Map<String, String> readAll(final TranslationObject translationObject) {
		// TODO element, language and value must not be null
		final Element element = ElementCreator.of(translationObject).create();
		final Store<Translation> store = storeFactory.create(element);
		final Map<String, String> map = newLinkedHashMap();
		final Iterable<String> enabledLanguages = setupFacade.getEnabledLanguages();
		final Collection<Translation> storedTranslations = store.readAll();
		final Collection<Translation> sortedTranslations = ORDER_BY_LANG.sortedCopy(storedTranslations);
		for (final Translation translation : sortedTranslations) {
			final String lang = translation.getLang();
			if (Iterables.contains(enabledLanguages, lang)) {
				map.put(lang, translation.getValue());
			}
		}
		return map;
	}

	@Override
	public String read(final TranslationObject translationObject, final String lang) {
		return readAll(translationObject).get(lang);
	}

	@Override
	public void update(final TranslationObject translationObject) {
		// TODO element, language and value must not be null
		final Element element = ElementCreator.of(translationObject).create();

		final Collection<Translation> newTranslations = extractTranslations(translationObject, element);
		final Map<String, Translation> translationsByLang = uniqueIndex(newTranslations, TRANSLATION_TO_LANG);
		final Store<Translation> store = storeFactory.create(element);
		final Collection<Translation> oldTranslations = store.readAll();

		final Iterable<Translation> toCreate = from(newTranslations).filter(
				MustBeCreated.withOldTranslations(oldTranslations));
		final Iterable<Translation> toDelete = from(oldTranslations).filter(
				MustBeDeleted.withNewTranslations(newTranslations));
		final Iterable<Translation> toUpdate = from(oldTranslations).filter(
				MustBeUpdated.withNewTranslations(newTranslations));

		for (final Translation translation : toCreate) {
			store.create(translation);
		}

		for (final Translation translationOnStore : toUpdate) {
			final String lang = translationOnStore.getLang();
			final Translation translationFromClient = translationsByLang.get(lang);
			translationOnStore.setValue(translationFromClient.getValue());
			store.update(translationOnStore);
		}

		for (final Translation translation : toDelete) {
			store.delete(translation);
		}
	}

	@Override
	public void delete(final TranslationObject translationObject) {
		final Element element = ElementCreator.of(translationObject).create();
		final Collection<Translation> translations = extractTranslations(translationObject, element);
		final Store<Translation> store = storeFactory.create(element);
		final Iterable<Translation> deleteable = from(store.readAll()) //
				.filter(ContainedInTraslations.containedIn(translations));
		for (final Translation translation : deleteable) {
			store.delete(translation);
		}
	}

	private Collection<Translation> extractTranslations(final TranslationObject translationObject, final Element element) {
		final Collection<Translation> translations = Lists.newArrayList();
		for (final Entry<String, String> entry : translationObject.getTranslations().entrySet()) {
			final Translation translation = new Translation();
			translation.setElement(String.class.cast(element.getGroupAttributeValue()));
			translation.setLang(entry.getKey());
			translation.setValue(entry.getValue());
			translations.add(translation);
		}
		return translations;
	}

}
