package org.cmdbuild.service.rest.v2.cxf.filter;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import org.cmdbuild.logic.data.access.filter.model.All;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.CompositeElement;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.data.access.filter.model.ElementVisitor;
import org.cmdbuild.logic.data.access.filter.model.OneOf;

import com.google.common.base.Predicate;

public abstract class ElementPredicate<T> extends ForwardingPredicate<T> implements ElementVisitor {

	private Predicate<T> predicate;

	public ElementPredicate(final Element element) {
		element.accept(this);
	}

	@Override
	protected Predicate<T> delegate() {
		return predicate;
	}

	@Override
	public void visit(final All element) {
		predicate = and(subPredicatesOf(element));
	}

	@Override
	public void visit(final Attribute element) {
		predicate = predicateOf(element);
	}

	@Override
	public void visit(final OneOf element) {
		predicate = or(subPredicatesOf(element));
	}

	private Collection<Predicate<? super T>> subPredicatesOf(final CompositeElement composite) {
		final Collection<Predicate<? super T>> elements = newArrayList();
		for (final Element element : composite.getElements()) {
			elements.add(predicateOf(element));
		}
		return elements;
	}

	protected abstract Predicate<T> predicateOf(Attribute element);

	protected abstract Predicate<T> predicateOf(Element element);

}