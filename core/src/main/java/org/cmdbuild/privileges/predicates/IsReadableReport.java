package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;

import com.google.common.base.Predicate;

public class IsReadableReport implements Predicate<CMCard> {

	private final ReportStore reportStore;
	private final Predicate<Report> predicate;

	public IsReadableReport(final ReportStore reportStore, final Predicate<Report> predicate) {
		this.reportStore = reportStore;
		this.predicate = predicate;
	}

	@Override
	public boolean apply(final CMCard menuCard) {
		final Integer reportId = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE, Integer.class);
		if (reportId == null) {
			return false;
		}

		final Report fetchedReport = reportStore.findReportById(reportId);
		if (fetchedReport == null) {
			return false;
		}

		return predicate.apply(fetchedReport);
	}

}
