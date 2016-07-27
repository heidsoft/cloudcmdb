package org.cmdbuild.dao.query.clause;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.ForwardingClass;

public class AnyClass extends ForwardingClass {

	private static final AnyClass ANY_CLASS = new AnyClass();

	public static CMClass anyClass() {
		return ANY_CLASS;
	}

	private static final CMClass UNSUPPORTED = UnsupportedProxyFactory.of(CMClass.class).create();
	private static final String ANY_STRING = "*";

	private AnyClass() {
	}

	@Override
	protected CMClass delegate() {
		return UNSUPPORTED;
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String toString() {
		return ANY_STRING;
	}

}
