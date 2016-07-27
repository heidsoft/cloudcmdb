package org.cmdbuild.logic.report;

import org.cmdbuild.logic.report.ReportLogic.Extension;

public interface ExtensionConverter {

	boolean isValid();

	Extension extension();

}
