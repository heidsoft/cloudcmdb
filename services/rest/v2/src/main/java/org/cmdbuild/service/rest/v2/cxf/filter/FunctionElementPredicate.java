package org.cmdbuild.service.rest.v2.cxf.filter;

import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.Element;

import com.google.common.base.Predicate;

public class FunctionElementPredicate extends ElementPredicate<CMFunction> {

	public FunctionElementPredicate(final Element element) {
		super(element);
	}

	@Override
	protected Predicate<CMFunction> predicateOf(final Attribute element) {
		return new FunctionAttributePredicate(element);
	}

	@Override
	protected Predicate<CMFunction> predicateOf(final Element element) {
		return new FunctionElementPredicate(element);
	}

}