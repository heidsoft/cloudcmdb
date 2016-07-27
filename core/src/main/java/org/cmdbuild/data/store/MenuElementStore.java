package org.cmdbuild.data.store;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.services.store.menu.MenuConstants.GROUP_NAME_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;

import java.util.List;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.services.store.menu.MenuCardFilter;
import org.cmdbuild.services.store.menu.MenuElement;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class MenuElementStore extends ForwardingStore<MenuElement> {

	private final Store<MenuElement> delegate;
	private final CMDataView dataView;
	private final UserStore userStore;
	private final StorableConverter<View> viewConverter;
	private final Function<CMCard, MenuElement> CONVERT;
	private final CustomPagesLogic customPagesLogic;

	public MenuElementStore(final Store<MenuElement> delegate, final CMDataView dataView, final UserStore userStore,
			final StorableConverter<View> viewConverter, final StorableConverter<MenuElement> converter,
			final CustomPagesLogic customPagesLogic) {
		this.delegate = delegate;
		this.dataView = dataView;
		this.userStore = userStore;
		this.viewConverter = viewConverter;
		this.CONVERT = new Function<CMCard, MenuElement>() {

			@Override
			public MenuElement apply(final CMCard input) {
				return converter.convert(input);
			}
		};
		this.customPagesLogic = customPagesLogic;
	}

	@Override
	protected Store<MenuElement> delegate() {
		return delegate;
	}

	public Iterable<MenuElement> readAndFilter(final String groupName, final CMGroup group) {
		final Iterable<CMCard> menuCards = fetchMenuCardsForGroup(groupName);
		final MenuCardFilter menuCardFilter = new MenuCardFilter(dataView, group, new Supplier<PrivilegeContext>() {

			@Override
			public PrivilegeContext get() {
				return userStore.getUser().getPrivilegeContext();
			}

		}, viewConverter, userStore, customPagesLogic);
		return from(menuCardFilter.filterReadableMenuCards(menuCards)) //
				.transform(CONVERT);
	}

	private Iterable<CMCard> fetchMenuCardsForGroup(final String groupName) {
		final List<CMCard> menuCards = Lists.newArrayList();
		final CMClass menuClass = dataView.findClass(MENU_CLASS_NAME);
		final CMQueryResult result = dataView.select(anyAttribute(menuClass)) //
				.from(menuClass) //
				.where(condition(attribute(menuClass, GROUP_NAME_ATTRIBUTE), eq(groupName))) //
				.run();
		for (final CMQueryRow row : result) {
			menuCards.add(row.getCard(menuClass));
		}
		return menuCards;
	}

}
