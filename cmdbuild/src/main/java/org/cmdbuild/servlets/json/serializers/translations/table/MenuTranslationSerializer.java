package org.cmdbuild.servlets.json.serializers.translations.table;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.MenuItemConverter;
import org.cmdbuild.services.store.menu.MenuConstants;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.servlets.json.serializers.translations.commons.MenuItemSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.MenuSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.MenuEntry;
import org.cmdbuild.servlets.json.translationtable.objects.ParentEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class MenuTranslationSerializer implements TranslationSectionSerializer {

	private static final String DEFAULT_MENU_GROUP_DESCRIPTION = "*Default*";
	private final AuthenticationLogic authLogic;
	private final MenuLogic menuLogic;
	private final TranslationLogic translationLogic;
	private final Ordering<MenuItem> menuNodesOrdering = MenuItemSorter.DEFAULT.getOrientedOrdering();
	private final Ordering<CMGroup> menusOrdering = MenuSorter.DEFAULT.getOrientedOrdering();

	private final Predicate<CMGroup> HAS_MENU = new Predicate<CMGroup>() {

		@Override
		public boolean apply(final CMGroup input) {
			final String groupName = input.getName();
			final MenuItem menuForGroup = menuLogic.read(groupName);
			return menuForGroup != null && menuForGroup.getChildren().size() > 0;
		}
	};

	public MenuTranslationSerializer(final AuthenticationLogic authLogic, final MenuLogic menuLogic,
			final TranslationLogic translationLogic, final JSONArray sorters, final String separator,
			final SetupFacade setupFacade) {
		this.authLogic = authLogic;
		this.menuLogic = menuLogic;
		this.translationLogic = translationLogic;
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<CMGroup> groups = authLogic.getAllGroups();
		final Iterable<CMGroup> groupsWithMenu = Iterables.filter(groups, HAS_MENU);
		final Collection<CMGroup> allGroupsPlusDefault = Lists.newArrayList(groupsWithMenu);
		allGroupsPlusDefault.add(fakeGroupForDefaultMenu);
		final Collection<CMGroup> sortedGroups = menusOrdering.sortedCopy(allGroupsPlusDefault);
		final Collection<TranslationSerialization> jsonMenus = Lists.newArrayList();
		for (final CMGroup group : sortedGroups) {
			final MenuItem rootNode = menuLogic.read(group.getName());
			final MenuEntry rootElement = new MenuEntry();
			rootElement.setName(group.getDescription());
			rootElement.setType(rootNode.getType().getValue());
			final Collection<TableEntry> childrenElement = Lists.newArrayList();
			final Iterable<MenuItem> _children = rootNode.getChildren();
			final Iterable<MenuItem> sortedChildren = menuNodesOrdering.sortedCopy(_children);
			for (final MenuItem child : sortedChildren) {
				final MenuEntry childElement = new MenuEntry();
				childElement.setName(child.getUniqueIdentifier());
				childElement.setType(child.getType().getValue());
				final Collection<EntryField> childFields = readFields(child);
				childElement.setFields(childFields);
				final List<MenuItem> nephews = child.getChildren();
				serialize(childElement, nephews);
				childrenElement.add(childElement);
			}
			rootElement.setChildren(childrenElement);
			jsonMenus.add(rootElement);
		}
		return jsonMenus;
	}

	private void serialize(final ParentEntry rootElement, final List<MenuItem> children) {
		final Collection<TableEntry> jsonChildren = Lists.newArrayList();
		for (final MenuItem child : children) {
			final MenuEntry childElement = new MenuEntry();
			childElement.setName(child.getUniqueIdentifier());
			childElement.setType(child.getType().getValue());
			final Collection<EntryField> fields = readFields(child);
			childElement.setFields(fields);
			jsonChildren.add(childElement);
			serialize(childElement, child.getChildren());
		}
		rootElement.setChildren(jsonChildren);

	}

	private Collection<EntryField> readFields(final MenuItem child) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = MenuItemConverter.DESCRIPTION //
				.withIdentifier(child.getUniqueIdentifier()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final EntryField field = new EntryField();
		field.setName(MenuItemConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(child.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	private static final CMGroup fakeGroupForDefaultMenu = new CMGroup() {

		@Override
		public boolean isRestrictedAdmin() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isAdmin() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isActive() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Long getStartingClassId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return MenuConstants.DEFAULT_MENU_GROUP_NAME;
		}

		@Override
		public Long getId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getEmail() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<String> getDisabledModules() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getDescription() {
			return DEFAULT_MENU_GROUP_DESCRIPTION;
		}

		@Override
		public List<PrivilegePair> getAllPrivileges() {
			throw new UnsupportedOperationException();
		}
	};

}