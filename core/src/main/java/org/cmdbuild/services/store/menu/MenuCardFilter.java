package org.cmdbuild.services.store.menu;

import java.util.List;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.custompages.CustomPagesLogic;
import org.cmdbuild.model.view.View;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class MenuCardFilter {

	private final CMDataView dataView;
	private final CMGroup group;
	private final Supplier<PrivilegeContext> privilegeContext;
	private final StorableConverter<View> viewConverter;
	private final UserStore userStore;
	private final CustomPagesLogic customPagesLogic;

	public MenuCardFilter( //
			final CMDataView dataView, //
			final CMGroup group, //
			final Supplier<PrivilegeContext> privilegeContext, //
			final StorableConverter<View> viewConverter, //
			final UserStore userStore, //
			final CustomPagesLogic customPagesLogic //
	) {
		this.dataView = dataView;
		this.group = group;
		this.privilegeContext = privilegeContext;
		this.viewConverter = viewConverter;
		this.userStore = userStore;
		this.customPagesLogic = customPagesLogic;
	}

	public Iterable<CMCard> filterReadableMenuCards(final Iterable<CMCard> notFilteredMenuCards) {
		final List<CMCard> readableCards = Lists.newArrayList();
		final MenuCardPredicateFactory predicateFactory = new MenuCardPredicateFactory( //
				dataView, //
				group, //
				privilegeContext, //
				viewConverter, //
				userStore, //
				customPagesLogic);

		for (final CMCard menuCard : notFilteredMenuCards) {
			final Predicate<CMCard> predicate = predicateFactory.getPredicate(menuCard);
			if (predicate.apply(menuCard)) {
				readableCards.add(menuCard);
			}
		}

		return readableCards;
	}

}
