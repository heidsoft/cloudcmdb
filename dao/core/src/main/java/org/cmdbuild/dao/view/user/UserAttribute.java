package org.cmdbuild.dao.view.user;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Map;

import org.cmdbuild.dao.entry.ForwardingAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;

public class UserAttribute extends ForwardingAttribute {

	static UserAttribute newInstance(final UserDataView view, final CMAttribute inner,
			final RowAndColumnPrivilegeFetcher rowAndColumnPrivilegeFetcher) {
		if (inner == null) {
			return null;
		}

		/*
		 * For non administrator user, remove the attribute with mode "hidden"
		 */
		final boolean isAdmin = view.getPrivilegeContext().hasAdministratorPrivileges();
		final Mode mode = new ForwardingAttributeTypeVisitor() {

			private final CMAttributeTypeVisitor delegate = NullAttributeTypeVisitor.getInstance();

			private Mode output;

			public Mode mode() {
				final Map<String, String> attributesPrivileges = rowAndColumnPrivilegeFetcher
						.fetchAttributesPrivilegesFor(inner.getOwner());
				output = Mode.of(attributesPrivileges.get(inner.getName()));
				inner.getType().accept(this);
				return output;
			}

			@Override
			protected CMAttributeTypeVisitor delegate() {
				return delegate;
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				if (view.findDomain(attributeType.getDomainName()) == null) {
					if (output == Mode.WRITE) {
						output = Mode.READ;
					}
				}
			}

		}.mode();
		if (isAdmin || (mode != null)) {
			return new UserAttribute(view, inner, mode);
		}

		return null;
	}

	private final UserDataView view;
	private final CMAttribute delegate;
	private final Mode mode;

	private UserAttribute( //
			final UserDataView view, //
			final CMAttribute delegate, //
			final Mode mode //
	) {
		this.view = view;
		this.delegate = delegate;
		this.mode = mode;
	}

	@Override
	protected CMAttribute delegate() {
		return delegate;
	}

	@Override
	public CMEntryType getOwner() {
		return view.proxy(super.getOwner());
	}

	@Override
	public Mode getMode() {
		return defaultIfNull(mode, super.getMode());
	}

}
