package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.nullableIterable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.services.store.menu.MenuConstants;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.servlets.json.serializers.translations.commons.MenuItemSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.MenuSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class MenuSectionSerializer implements TranslationSectionSerializer {

	private static final String DEFAULT_MENU_GROUP_DESCRIPTION = "*Default*";

	private final AuthenticationLogic authLogic;
	private final MenuLogic menuLogic;
	private final Iterable<String> selectedLanguages;
	private final TranslationLogic translationLogic;
	private final Ordering<MenuItem> menuNodesOrdering = MenuItemSorter.DEFAULT.getOrientedOrdering();
	private final Ordering<CMGroup> menusOrdering = MenuSorter.DEFAULT.getOrientedOrdering();

	private final Collection<TranslationSerialization> records = Lists.newArrayList();

	private final Predicate<CMGroup> HAS_MENU = new Predicate<CMGroup>() {

		@Override
		public boolean apply(final CMGroup input) {
			final String groupName = input.getName();
			final MenuItem menuForGroup = menuLogic.read(groupName);
			return menuForGroup != null && menuForGroup.getChildren().size() > 0;
		}
	};

	public MenuSectionSerializer(final AuthenticationLogic authLogic, final MenuLogic menuLogic,
			final TranslationLogic translationLogic, final JSONArray sorters,
			final Iterable<String> selectedLanguages) {
		this.authLogic = authLogic;
		this.menuLogic = menuLogic;
		this.translationLogic = translationLogic;
		this.selectedLanguages = selectedLanguages;
		// TODO: manage ordering configuration
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<CMGroup> groups = authLogic.getAllGroups();
		final Iterable<CMGroup> groupsWithMenu = Iterables.filter(groups, HAS_MENU);
		final Collection<CMGroup> allGroupsPlusDefault = Lists.newArrayList(groupsWithMenu);
		allGroupsPlusDefault.add(fakeGroupForDefaultMenu);
		final Collection<CMGroup> sortedGroups = menusOrdering.sortedCopy(allGroupsPlusDefault);
		for (final CMGroup group : sortedGroups) {
			final MenuItem rootNode = menuLogic.read(group.getName());

			recursivelySerialize(rootNode);

		}
		return records;
	}

	private void recursivelySerialize(final MenuItem rootElement) {

		if (hasValidUuid(rootElement)) {

			records.addAll(MenuItemSerializer.newInstance() //
					.withSelectedLanguages(selectedLanguages) //
					.withTranslationLogic(translationLogic) //
					.withMenuLogic(menuLogic) //
					.withMenuItem(rootElement) //
					.withGroupName(rootElement.getGroupName()) //
					.build() //
					.serialize());
		}

		final Iterable<MenuItem> sortedChildren = nullableIterable(
				menuNodesOrdering.sortedCopy(rootElement.getChildren()));

		for (final MenuItem child : sortedChildren) {
			recursivelySerialize(child);
		}
	}

	private static boolean hasValidUuid(final MenuItem node) {
		return !isBlank(node.getUniqueIdentifier());
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