package org.cmdbuild.service.rest.v2.cxf.filter;

import static java.lang.String.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.NAME;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.EqualTo;
import org.cmdbuild.logic.data.access.filter.model.ForwardingPredicateVisitor;
import org.cmdbuild.logic.data.access.filter.model.PredicateVisitor;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class FunctionAttributePredicate extends ForwardingPredicateVisitor implements Predicate<CMFunction>,
		LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(FunctionAttributePredicate.class.getName());

	private static final PredicateVisitor NOT_SUPPORTED = NotSupportedPredicateVisitor.getInstance();

	private final Attribute attribute;
	private CMFunction input;
	private boolean output;

	public FunctionAttributePredicate(final Attribute attribute) {
		this.attribute = attribute;
	}

	@Override
	protected PredicateVisitor delegate() {
		return NOT_SUPPORTED;
	}

	@Override
	public boolean apply(final CMFunction input) {
		this.input = input;
		this.output = false;
		this.attribute.getPredicate().accept(this);
		return output;
	}

	@Override
	public void visit(final EqualTo predicate) {
		final boolean _output;
		final Object expected = predicate.getValue();
		if (NAME.equals(attribute.getName())) {
			_output = input.getName().equals(expected);
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
