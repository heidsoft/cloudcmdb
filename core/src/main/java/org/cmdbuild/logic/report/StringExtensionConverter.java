package org.cmdbuild.logic.report;

import static org.cmdbuild.logic.report.ReportLogic.Extensions.csv;
import static org.cmdbuild.logic.report.ReportLogic.Extensions.odt;
import static org.cmdbuild.logic.report.ReportLogic.Extensions.pdf;
import static org.cmdbuild.logic.report.ReportLogic.Extensions.rtf;
import static org.cmdbuild.logic.report.ReportLogic.Extensions.zip;

import org.cmdbuild.logic.report.ReportLogic.Extension;

public enum StringExtensionConverter implements ExtensionConverter {
	CSV {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Extension extension() {
			return csv();
		}

	}, //
	ODT {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Extension extension() {
			return odt();
		}

	}, //
	PDF {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Extension extension() {
			return pdf();
		}

	}, //
	RTF {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Extension extension() {
			return rtf();
		}

	}, //
	ZIP {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public Extension extension() {
			return zip();
		}

	}, //
	UNDEFINED {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public Extension extension() {
			return null;
		}

	}, //
	;

	public static StringExtensionConverter of(final String value) {
		for (final StringExtensionConverter element : values()) {
			if (element.name().equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}