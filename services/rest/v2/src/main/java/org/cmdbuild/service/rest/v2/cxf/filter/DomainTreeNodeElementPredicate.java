package org.cmdbuild.service.rest.v2.cxf.filter;

import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.model.domainTree.DomainTreeNode;

import com.google.common.base.Predicate;

public class DomainTreeNodeElementPredicate extends ElementPredicate<DomainTreeNode> {

	public DomainTreeNodeElementPredicate(final Element element) {
		super(element);
	}

	@Override
	protected Predicate<DomainTreeNode> predicateOf(final Attribute element) {
		return new DomainTreeNodeAttributePredicate(element);
	}

	@Override
	protected Predicate<DomainTreeNode> predicateOf(final Element element) {
		return new DomainTreeNodeElementPredicate(element);
	}

}