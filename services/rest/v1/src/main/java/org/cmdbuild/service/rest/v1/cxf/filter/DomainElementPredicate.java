package org.cmdbuild.service.rest.v1.cxf.filter;

import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.Element;

import com.google.common.base.Predicate;

public class DomainElementPredicate extends ElementPredicate<CMDomain> {

	public DomainElementPredicate(final Element element) {
		super(element);
	}

	@Override
	protected Predicate<CMDomain> predicateOf(final Attribute element) {
		return new DomainAttributePredicate(element);
	}

	@Override
	protected Predicate<CMDomain> predicateOf(final Element element) {
		return new DomainElementPredicate(element);
	}

}