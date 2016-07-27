package org.cmdbuild.dao.entrytype;

public abstract class ForwardingDomain extends ForwardingEntryType implements CMDomain {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingDomain() {
	}

	@Override
	protected abstract CMDomain delegate();

	@Override
	public CMClass getClass1() {
		return delegate().getClass1();
	}

	@Override
	public CMClass getClass2() {
		return delegate().getClass2();
	}

	@Override
	public String getDescription1() {
		return delegate().getDescription1();
	}

	@Override
	public String getDescription2() {
		return delegate().getDescription2();
	}

	@Override
	public String getCardinality() {
		return delegate().getCardinality();
	}

	@Override
	public boolean isMasterDetail() {
		return delegate().isMasterDetail();
	}

	@Override
	public String getMasterDetailDescription() {
		return delegate().getMasterDetailDescription();
	}

	@Override
	public Iterable<String> getDisabled1() {
		return delegate().getDisabled1();
	}

	@Override
	public Iterable<String> getDisabled2() {
		return delegate().getDisabled2();
	}

}
