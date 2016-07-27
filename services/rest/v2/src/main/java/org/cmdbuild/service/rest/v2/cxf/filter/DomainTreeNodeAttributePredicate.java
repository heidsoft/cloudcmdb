package org.cmdbuild.service.rest.v2.cxf.filter;

import static java.lang.String.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TARGET_CLASS;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.Contains;
import org.cmdbuild.logic.data.access.filter.model.EqualTo;
import org.cmdbuild.logic.data.access.filter.model.ForwardingPredicateVisitor;
import org.cmdbuild.logic.data.access.filter.model.PredicateVisitor;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class DomainTreeNodeAttributePredicate extends ForwardingPredicateVisitor
		implements Predicate<DomainTreeNode>, LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(DomainTreeNodeAttributePredicate.class.getName());

	private static final PredicateVisitor NOT_SUPPORTED = NotSupportedPredicateVisitor.getInstance();

	private final Attribute attribute;
	private DomainTreeNode input;
	private boolean output;

	public DomainTreeNodeAttributePredicate(final Attribute attribute) {
		this.attribute = attribute;
	}

	@Override
	protected PredicateVisitor delegate() {
		return NOT_SUPPORTED;
	}

	@Override
	public boolean apply(final DomainTreeNode input) {
		this.input = input;
		this.output = false;
		this.attribute.getPredicate().accept(this);
		return output;
	}

	@Override
	public void visit(final Contains predicate) {
		final boolean _output;
		final Object expected = predicate.getValue();
		if (TARGET_CLASS.equals(attribute.getName())) {
			_output = (input.getIdParent() == null) ? false : input.getTargetClassName().equals(expected);
		} else {
			logger.warn(marker, format("attribute '%s' not supported", attribute.getName()));
			_output = true;
		}
		output = _output;
	}

	@Override
	public void visit(final EqualTo predicate) {
		final boolean _output;
		final Object expected = predicate.getValue();
		if (TARGET_CLASS.equals(attribute.getName())) {
			_output = (input.getIdParent() == null) ? input.getTargetClassName().equals(expected) : false;
		} else {
			logger.warn(marker, format("attribute '%s' not supported", attribute.getName()));
			_output = true;
		}
		output = _output;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE) //
				.append("attribute", attribute.getName()) //
				.append("predicate", attribute.getPredicate()) //
				.build();
	}

}
