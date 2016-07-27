package org.cmdbuild.dao.query.clause;

import static com.google.common.reflect.Reflection.newProxy;
import static org.cmdbuild.common.utils.Reflection.unsupported;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingDomain;

public class DomainHistory extends ForwardingDomain implements HistoricEntryType<CMDomain> {

	public static CMDomain history(final CMDomain current) {
		return of(current);
	}

	public static CMDomain of(final CMDomain current) {
		return new DomainHistory(current);
	}

	private static final CMDomain UNSUPPORTED = newProxy(CMDomain.class, unsupported("method not supported"));

	private final CMDomain delegate;

	private DomainHistory(final CMDomain current) {
		this.delegate = current;
	}

	@Override
	protected CMDomain delegate() {
		return UNSUPPORTED;
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public CMDomain getType() {
		return delegate;
	}

	@Override
	public CMIdentifier getIdentifier() {
		return delegate.getIdentifier();
	}

	@Override
	public Long getId() {
		return delegate.getId();
	}

	@Override
	public String getName() {
		return delegate.getName() + " HISTORY";
	}

	@Override
	public CMClass getClass1() {
		return delegate.getClass1();
	}

	@Override
	public CMClass getClass2() {
		return delegate.getClass2();
	}

	public CMDomain getCurrent() {
		return delegate;
	}

}
