package org.cmdbuild.cmdbf.xml;

import static com.google.common.collect.Iterables.transform;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.ForwardingClass;

import com.google.common.base.Function;

public class CMClassHistory extends ForwardingClass {

	private final CMClass delegate;

	public CMClassHistory(final CMClass delegate) {
		this.delegate = delegate;
	}

	@Override
	protected CMClass delegate() {
		return delegate;
	}

	public CMClass getBaseType() {
		return delegate;
	}

	@Override
	public CMClass getParent() {
		return super.getParent() != null ? new CMClassHistory(super.getParent()) : null;
	};

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		return transform(super.getLeaves(), TO_HISTORIC);
	}

	@Override
	public Iterable<? extends CMClass> getDescendants() {
		return transform(super.getDescendants(), TO_HISTORIC);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return delegate.equals(obj);
	}

	private static final Function<CMClass, CMClass> TO_HISTORIC = new Function<CMClass, CMClass>() {

		@Override
		public CMClass apply(final CMClass input) {
			return new CMClassHistory(input);
		}

	};
}
