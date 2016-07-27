package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.ForwardingQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class UserQueryRow extends ForwardingQueryRow {

	private final UserDataView view;
	private final CMQueryRow delegate;

	static UserQueryRow newInstance(final UserDataView view, final CMQueryRow delegate) {
		return new UserQueryRow(view, delegate);
	}

	private UserQueryRow(final UserDataView view, final CMQueryRow delegate) {
		this.view = view;
		this.delegate = delegate;
	}

	@Override
	protected CMQueryRow delegate() {
		return delegate;
	}

	@Override
	public CMCard getCard(final Alias alias) {
		return proxy(super.getCard(alias));
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return proxy(super.getCard(type));
	}

	private CMCard proxy(final CMCard card) {
		return UserCard.newInstance(view, card);
	}

}
