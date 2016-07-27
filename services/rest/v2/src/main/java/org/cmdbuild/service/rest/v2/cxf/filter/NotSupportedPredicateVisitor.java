package org.cmdbuild.service.rest.v2.cxf.filter;

import static java.lang.String.format;

import org.cmdbuild.logic.data.access.filter.model.And;
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
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class NotSupportedPredicateVisitor implements PredicateVisitor, LoggingSupport {

	private static final Marker marker = MarkerFactory.getMarker(NotSupportedPredicateVisitor.class.getName());

	private static final NotSupportedPredicateVisitor INSTANCE = new NotSupportedPredicateVisitor();

	public static NotSupportedPredicateVisitor getInstance() {
		return INSTANCE;
	}

	private NotSupportedPredicateVisitor() {
		// use factory method
	}

	@Override
	public void visit(final And predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final Contains predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final EndsWith predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final EqualTo predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final GreaterThan predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
	}

	@Override
	public void visit(final In predicate) {
		logger.warn(marker, format("predicate '%s' not supported", predicate));
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

}
