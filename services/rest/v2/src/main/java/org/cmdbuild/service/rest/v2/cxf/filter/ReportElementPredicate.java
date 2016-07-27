package org.cmdbuild.service.rest.v2.cxf.filter;

import org.cmdbuild.logic.data.access.filter.model.Attribute;
import org.cmdbuild.logic.data.access.filter.model.Element;
import org.cmdbuild.logic.report.ReportLogic.Report;

import com.google.common.base.Predicate;

public class ReportElementPredicate extends ElementPredicate<Report> {

	public ReportElementPredicate(final Element element) {
		super(element);
	}

	@Override
	protected Predicate<Report> predicateOf(final Attribute element) {
		return new ReportAttributePredicate(element);
	}

	@Override
	protected Predicate<Report> predicateOf(final Element element) {
		return new ReportElementPredicate(element);
	}

}