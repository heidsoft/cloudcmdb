package org.cmdbuild.service.rest.v2.cxf.filter;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.service.rest.v2.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.v2.constants.Serialization.TITLE;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.EqualTo;
import org.cmdbuild.logic.data.access.filter.model.ForwardingPredicateVisitor;
import org.cmdbuild.logic.data.access.filter.model.In;
import org.cmdbuild.logic.data.access.filter.model.PredicateVisitor;
import org.cmdbuild.logic.report.ReportLogic.Report;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class ReportAttributePredicate extends ForwardingPredicateVisitor implements Predicate<Report>, LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(ReportAttributePredicate.class.getName());

	private static final PredicateVisitor NOT_SUPPORTED = NotSupportedPredicateVisitor.getInstance();

	private final Attribute attribute;
	private Report input;
	private boolean output;

	public ReportAttributePredicate(final Attribute attribute) {
		this.attribute = attribute;
	}

	@Override
	protected PredicateVisitor delegate() {
		return NOT_SUPPORTED;
	}

	@Override
	public boolean apply(final Report input) {
		this.input = input;
		this.output = false;
		this.attribute.getPredicate().accept(this);
		return output;
	}

	@Override
	public void visit(final EqualTo predicate) {
		final boolean _output;
		final Object expected = predicate.getValue();
		if (TITLE.equals(attribute.getName())) {
			_output = input.getTitle().equals(expected);
		} else if (DESCRIPTION.equals(attribute.getName())) {
			_output = input.getDescription().equals(expected);
		} else {
			logger.warn(marker, format("attribute '%s' not supported", attribute.getName()));
			_output = true;
		}
		output = _output;
	}

	@Override
	public void visit(final In predicate) {
		final boolean _output;
		final FluentIterable<String> expected = from(predicate.getValues()) //
				.transform(toStringFunction());
		if (TITLE.equals(attribute.getName())) {
			_output = expected.contains(input.getTitle());
		} else if (DESCRIPTION.equals(attribute.getName())) {
			_output = expected.contains(input.getDescription());
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
