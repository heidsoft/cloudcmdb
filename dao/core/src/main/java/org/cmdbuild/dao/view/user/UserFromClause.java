package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.ForwardingEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.NullEntryTypeVisitor;
import org.cmdbuild.dao.query.clause.from.ClassFromClause;
import org.cmdbuild.dao.query.clause.from.ForwardingFromClause;
import org.cmdbuild.dao.query.clause.from.FromClause;

public class UserFromClause extends ForwardingFromClause {

	static FromClause newInstance(final UserDataView userDataView, final FromClause delegate) {
		return new UserFromClause(userDataView, delegate);
	}

	private final UserDataView userDataView;
	private final FromClause delegate;

	private UserFromClause(final UserDataView userDataView, final FromClause delegate) {
		this.userDataView = userDataView;
		this.delegate = delegate;
	}

	@Override
	protected FromClause delegate() {
		return delegate;
	}

	@Override
	public CMEntryType getType() {
		return userDataView.proxy(super.getType());
	}

	@Override
	public EntryTypeStatus getStatus(final CMEntryType entryType) {
		return new ForwardingEntryTypeVisitor() {

			private final CMEntryTypeVisitor _delegate = NullEntryTypeVisitor.getInstance();

			private EntryTypeStatus status;

			@Override
			protected CMEntryTypeVisitor delegate() {
				return _delegate;
			}

			public EntryTypeStatus getStatus(final CMEntryType entryType) {
				status = UserFromClause.super.getStatus(entryType);
				entryType.accept(this);
				return status;
			}

			@Override
			public void visit(final CMClass type) {
				final ClassFromClause classFromClause = new ClassFromClause(userDataView, delegate.getType(),
						delegate.getAlias());
				status = classFromClause.getStatus(entryType);
			}

		}.getStatus(entryType);
	}

}
