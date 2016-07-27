package org.cmdbuild.service.rest.v1.cxf.filter;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.dao.entrytype.Functions.name;
import static org.cmdbuild.service.rest.v1.constants.Serialization.DESTINATION;
import static org.cmdbuild.service.rest.v1.constants.Serialization.SOURCE;

import java.util.Collection;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.data.access.filter.model.And;
import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.Contains;
import org.cmdbuild.logic.data.access.filter.model.EndsWith;
import org.cmdbuild.logic.data.access.filter.model.EqualTo;
import org.cmdbuild.logic.data.access.filter.model.GreaterThan;
import org.cmdbuild.logic.data.access.filter.model.In;
import org.cmdbuild.logic.data.access.filter.model.IsNull;
import org.cmdbuild.logic.data.access.filter.model.LessThan;
import org.cmdbuild.logic.data.access.filter.model.Like;
import org.cmdbuild.logic.data.access.filter.model.Not;
import org.cmdbuild.logic.data.access.filter.model.Or;
import org.cmdbuild.logic.data.access.filter.model.PredicateVisitor;
import org.cmdbuild.logic.data.access.filter.model.StartsWith;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class DomainAttributePredicate implements Predicate<CMDomain>, PredicateVisitor, LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(DomainAttributePredicate.class.getName());

	private final Attribute attribute;
	private CMDomain input;
	private boolean output;

	public DomainAttributePredicate(final Attribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public boolean apply(final CMDomain input) {
		this.input = input;
		this.output = false;
		this.attribute.getPredicate().accept(this);
		return output;
	}

	@Override
	public void visit(final And predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Contains predicate) {
		final boolean _output;
		final Object expected = predicate.getValue();
		if (SOURCE.equals(attribute.getName())) {
			_output = contains(input.getClass1(), expected);
		} else if (DESTINATION.equals(attribute.getName())) {
			_output = contains(input.getClass2(), expected);
		} else {
			logger.warn(marker, format("attribute '%s' not supported", attribute.getName()));
			_output = true;
		}
		output = _output;

		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	private boolean contains(final CMClass target, final Object expected) {
		final Collection<CMClass> classes = newHashSet(target.getDescendants());
		classes.add(target);
		return from(classes) //
				.transform(name()) //
				.contains(expected);
	}

	@Override
	public void visit(final EndsWith predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final EqualTo predicate) {
		final boolean _output;
		final Object expected = predicate.getValue();
		if (SOURCE.equals(attribute.getName())) {
			_output = input.getClass1().getName().equals(expected);
		} else if (DESTINATION.equals(attribute.getName())) {
			_output = input.getClass2().getName().equals(expected);
		} else {
			logger.warn(marker, format("attribute '%s' not supported", attribute.getName()));
			_output = true;
		}
		output = _output;
	}

	@Override
	public void visit(final GreaterThan predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final In predicate) {
		final boolean _output;
		final FluentIterable<String> expected = from(predicate.getValues()) //
				.transform(toStringFunction());
		if (SOURCE.equals(attribute.getName())) {
			_output = expected.contains(input.getClass1().getName());
		} else if (DESTINATION.equals(attribute.getName())) {
			_output = expected.contains(input.getClass2().getName());
		} else {
			logger.warn(marker, format("attribute '%s' not supported", attribute.getName()));
			_output = true;
		}
		output = _output;
	}

	@Override
	public void visit(final IsNull predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final LessThan predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Like predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Not predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Or predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final StartsWith predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE) //
				.append("attribute", attribute.getName()) //
				.append("predicate", attribute.getPredicate()) //
				.build();
	}

}
